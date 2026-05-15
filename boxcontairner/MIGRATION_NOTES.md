# BoxContainer v2 — Notas de migración

**Generado:** 14 mayo 2026
**Base:** v1 (ZIP entregado por Douglas)
**Alcance:** refactor completo de seguridad, sincronización, arquitectura y limpieza.

---

## ⚠️ Acción manual requerida ANTES de abrir el proyecto

1. **Copiá tu `google-services.json`** de la consola Firebase a `app/google-services.json`. El v2 NO incluye tu key (mejor higiene).
2. **Sincronizá Gradle** (Android Studio: *File → Sync Project with Gradle Files*).
3. **Clean & Rebuild** (*Build → Clean Project*, después *Build → Rebuild Project*).
4. La DB local se va a recrear desde cero (version 6 + destructive migration). Si tenías
   datos de prueba en el device, se pierden. Los datos en Firestore quedan intactos.

## ⚠️ Cuando estés por salir a producción (NO ahora)

5. Activá App Check en consola Firebase + cargá la SHA-256 del APK release.
6. Deployá `firestore.rules` (ver `README_FIRESTORE_RULES.md`).
7. Verificá que el bootstrap del primer SUPER_ADMIN funcionó (vos mismo).

---

## Cambios por categoría

### 🔴 CRÍTICOS — bugs y seguridad

#### ✅ C1. Bug Room `archived` / `isArchived` resuelto
- **Antes:** entity tenía `val archived: Boolean` pero el DAO consultaba `WHERE isArchived = 0` — inconsistencia bloqueante.
- **Ahora:** todo usa `archived` (entity + DAO + entity index).
- **Archivos tocados:** `ContainerEntity.kt`, `ContainerDao.kt`.
- **Migración DB:** version 5 → 6, destructive (la caché local se borra; Firestore es source-of-truth).

#### ✅ C2. Escalado de privilegios en bootstrap resuelto
- **Antes:** si `register` fallaba a medias, en el siguiente login el usuario se auto-promovía a SUPER_ADMIN. Sin transacción.
- **Ahora:** bootstrap via transacción Firestore atómica que crea `_meta/bootstrap` + `users/{uid}` juntos. Si dos clientes corren simultáneamente, solo uno gana. El `login` ya no auto-promueve — si una cuenta llega sin perfil, se cierra sesión con error explícito.
- **Archivos tocados:** `AuthRepository.kt`.
- **UX preservada:** la idea original (primer usuario = IT = SUPER_ADMIN automático) sigue funcionando. Solo se blindó la implementación.

#### ✅ C3. Catches silentes de Firestore reemplazados con logging
- **Antes:** 7 instancias de `try { ... } catch (_: Exception) {}` en `ContainerRepository`.
- **Ahora:** cada catch loguea con `Timber.w(e, "...")`. En release esos warnings van a Crashlytics. En debug van a logcat.
- **Archivos tocados:** `ContainerRepository.kt`.
- **Beneficio:** ya no hay sync silenciosa fallida. Cualquier error de red, reglas o cuota queda registrado.

#### ✅ C4. Persistencia offline de Firestore activada
- **Antes:** sin configurar.
- **Ahora:** `persistentCacheSettings { setSizeBytes(100 MB) }` en `FirebaseModule.provideFirestore`. El SDK encola writes pendientes y los manda cuando hay red.
- **Archivos tocados:** `FirebaseModule.kt`.

#### ✅ C5. `firebase-vertexai` + `gemini-1.5-flash` eliminados
- **Antes:** dependencia y `@Provides` muerto en Hilt. Vertex AI removido del BoM, gemini-1.5 retornaba 404.
- **Ahora:** no figura en `app/build.gradle.kts` ni en `FirebaseModule`. Decisión tuya: no había caso de uso, era scaffold del template.
- **Si en el futuro querés agregar IA:** se hace con `firebase-ai` + `Firebase.ai(GenerativeBackend.googleAI()).generativeModel("gemini-2.5-flash")`. No agregués `firebase-vertexai`.

#### ✅ C6. PIN blindado con App Check + lockout local
- **Antes:** PIN 6 dígitos sin nada que lo respaldara.
- **Ahora:**
  - **App Check (Play Integrity)** instalado en `BoxContainerApplication.onCreate()`. En debug usa `DebugAppCheckProviderFactory` (te va a loguear un token; whitelistalo en consola). En release usa Play Integrity (firma de Google Play).
  - **Lockout local**: 5 intentos fallidos → bloqueo de 5 minutos. Implementado en `AuthLockoutStore` con `EncryptedSharedPreferences` (no en claro en disco). Integrado en `AuthRepository.login` y reflejado en `LoginScreen` con countdown.
- **Archivos nuevos:** `core/security/AuthLockoutStore.kt`.
- **Archivos tocados:** `BoxContainerApplication.kt`, `AuthRepository.kt`, `AuthViewModel.kt`, `LoginScreen.kt`.

#### ✅ C7. Reglas Firestore preparadas para producción
- **Archivo nuevo:** `firestore.rules` (raíz del proyecto).
- **No deployar todavía** — está documentado en `README_FIRESTORE_RULES.md`.
- En desarrollo, seguís usando tus reglas actuales (probablemente "test mode" permisivas).

---

### 🟠 SERIOS — arquitectura y mantenibilidad

#### ✅ S1. `ContainerViewModel` separado en List + Detail
- **Antes:** un solo VM manejaba lista y detalle. El `init { syncContainers() }` se disparaba al abrir el detalle también.
- **Ahora:** `ContainerListViewModel` (lista + sync + nav) y `ContainerDetailViewModel` (detalle puro). Cada uno con su propio ciclo de vida.
- **Archivos nuevos:** `ui/containers/ContainerListViewModel.kt`, `ui/containers/detail/ContainerDetailViewModel.kt`.
- **Archivo eliminado:** `ui/ContainerViewModel.kt` (el monolítico).

#### ✅ S2. Lógica de `addOrUpdateContainer` extraída a UseCase
- **Antes:** función de 50 líneas en el Repository mezclando reglas de negocio (re-ingreso vs nuevo) con persistencia.
- **Ahora:** `RegisterContainerEntryUseCase` con `handleReentry()` y `handleNewEntry()` separados. El Repository quedó como capa de persistencia pura con métodos chicos.
- **Archivos nuevos:** `domain/usecase/RegisterContainerEntryUseCase.kt`.
- **Archivos tocados:** `ContainerRepository.kt` (más simple ahora).

#### ✅ S3. Primitive Obsession resuelto con enums
- **Antes:** `status: String`, `role: String`, `action: String`, `instruccion: String` con valores mágicos por todo el código.
- **Ahora:**
  - `ContainerStatus` enum (INSP, OK, EST, DMG)
  - `Role` enum (SUPER_ADMIN, ADMIN, OPERATOR) con `canManageContainers` y `canManageUsers` ya tipadas
  - `WorkInstruction` enum (FULL_PTI, VISUAL_CHECK, NONE) con `displayLabel`
  - `HistoryAction` enum (ENTRY, STATUS_CHANGE, EDIT, ARCHIVE)
- **Compat Firestore:** los enums se siguen serializando a String (vía `.name` o `.displayLabel`) para no romper datos existentes. Hay helpers `fromStringOrDefault` para deserializar tolerantemente.
- **Archivos nuevos:** `domain/model/{ContainerStatus,Role,WorkInstruction,HistoryAction}.kt`.

#### ✅ S4. Enforcement de roles en UI
- **Antes:** OPERATOR veía el botón "Eliminar Registro" igual que ADMIN.
- **Ahora:** `AuthRepository.currentRole` es un `StateFlow<Role>` que se hidrata al startup. `ContainerDetailScreen` muestra "Eliminar" y "Editar" solo si `role.canManageContainers`. `SettingsScreen` muestra el rol del usuario.
- **Defensa en profundidad:** la UI esconde la acción, las reglas Firestore (cuando las deployes) la bloquean en el backend.

#### ✅ S5. Tests unitarios del checksum ISO 6346
- **Archivo nuevo:** `app/src/test/java/com/boxcontairner/util/ISO6346ScannerTest.kt`.
- Cobertura: validación canónica (CSQU3054383), check digit incorrecto, longitudes inválidas, tolerancia a separadores, corrección OCR.
- **Para correr:** `./gradlew test` o desde Android Studio botón verde junto a la clase.
- **Diferido:** tests de `ReeferPlateScanner` requieren mockear `Text` de ML Kit; documentado en el archivo.

#### ✅ S6. Crashlytics + Timber inicializados
- **Archivos nuevos:** `core/logging/CrashlyticsTree.kt`.
- **Archivos tocados:** `BoxContainerApplication.onCreate()`, `build.gradle.kts` (plugin Crashlytics + deps).
- **Behavior:** en debug, logs van a logcat con prefijos legibles. En release, WARN+ se forwardean a Crashlytics.

#### ⏸ S7. Navigation type-safe — DIFERIDO
- **Status:** mantenido el patrón de strings (`Screen("detail/{id}?editMode={x}")`) para no agregar más dependencias en una sola pasada.
- **Por qué:** la migración a routes type-safe (`@Serializable data class Detail(...)`) requiere el plugin `kotlinx-serialization` que en AGP 9 con built-in Kotlin tiene fricción. Mejor en una iteración separada.
- **Cuando lo hagas:** seguí el patrón de InspectionApp v4. La estructura de Screen sealed class está lista para reemplazo 1:1.

#### ✅ S8. Código muerto eliminado
- **Borrados:**
  - `util/ReeferAnalyzer.kt` (reemplazado por `ReeferPlateScanner` que ya estaba en uso).
  - `util/ContainerValidator.kt` (duplicaba el algoritmo de `ISO6346Scanner.isValidChecksum`).
- **Resultado:** algoritmo de check digit con una sola implementación en `ISO6346Scanner`.

#### ⏸ S9. Strings en `strings.xml` — PARCIAL
- **Hecho:** limpié `strings.xml` de la basura del template Gemini (`baking_title`, `image1_description`, etc.). Borré los drawables `baked_goods_*` (200 KB innecesarios).
- **Diferido:** mover todos los literales de las pantallas Compose a `strings.xml` requiere tocar cada `Screen.kt`. Si vas a publicar en países que no sean Latam, hagamoslo en una iteración separada con el script de extracción.

---

### 🟡 MENORES

#### ✅ M1. Java 17
`compileOptions` y `targetCompatibility` actualizados.

#### ⏸ M2. Theme — sin cambios
`themes.xml` mantenido para no romper. La app es 100% Compose, el theme XML solo se usa para statusBar/launcher.

#### ✅ M3. `strings.xml` limpio + drawables `baked_goods_*` eliminados
Ahorro: ~210 KB en el APK y semántica más limpia.

#### ✅ M4. Application inicializa Firebase, App Check, Crashlytics, Timber
Ver `BoxContainerApplication.kt`.

#### ⚠️ M5. `local.properties` y `google-services.json` NO incluidos
Tenés que poner tu propio `google-services.json` en `app/` antes de buildear. `local.properties` lo genera Android Studio automáticamente al abrir el proyecto.

#### ⏸ M6. Typo `boxcontairner` — sin cambios
Está cementado en `applicationId` y `namespace`. Cambiarlo rompe instalaciones existentes (los users pierden datos). Se queda así.

#### ✅ M7+M8. Screens grandes separadas en archivos
- `HistoryScreen` → `ui/history/HistoryScreen.kt`
- `SettingsScreen` → `ui/settings/SettingsScreen.kt`
- `ContainerItem` → `ui/containers/components/ContainerItem.kt`
- `ScanDialog` → `ui/containers/components/ScanDialog.kt`
- `CameraScanner` → `ui/scan/CameraScanner.kt`

#### ✅ M9. ProGuard rules para Firestore + Hilt + ML Kit
Ver `app/proguard-rules.pro`. Sin esto, el R8 del release rompe la serialización Firestore.

#### ✅ M11. Coroutines bajo BoM no aplica
`kotlinx-coroutines-play-services` no viene del Firebase BoM (es de Kotlinx). Mantengo versión fija en libs.versions.toml.

#### ✅ Bonus. Índices en Room
Agregué índices a `containers.code`, `containers.archived`, `status_history.containerId`, `status_history.timestamp`. Esto mejora performance de queries que hoy ya se usan.

---

## Estructura de paquetes resultante

```
com.boxcontairner/
├── BoxContainerApplication.kt
├── MainActivity.kt
├── core/
│   ├── logging/CrashlyticsTree.kt
│   └── security/AuthLockoutStore.kt
├── data/
│   ├── local/
│   │   ├── BoxContainerDatabase.kt
│   │   ├── dao/{ContainerDao, StatusHistoryDao}.kt
│   │   └── entities/{ContainerEntity, StatusHistoryEntity}.kt
│   └── repository/{AuthRepository, ContainerRepository}.kt
├── di/
│   ├── DatabaseModule.kt
│   └── FirebaseModule.kt              ← renombrado de NetworkModule (más preciso)
├── domain/
│   ├── model/
│   │   ├── Container.kt
│   │   ├── ContainerStatus.kt         ← NUEVO enum
│   │   ├── HistoryAction.kt           ← NUEVO enum
│   │   ├── Role.kt                    ← NUEVO enum
│   │   ├── StatusHistory.kt
│   │   ├── TimelineEvent.kt
│   │   ├── User.kt
│   │   └── WorkInstruction.kt         ← NUEVO enum
│   └── usecase/
│       └── RegisterContainerEntryUseCase.kt  ← NUEVO
├── ui/
│   ├── Navigation.kt
│   ├── PreviewAnnotations.kt
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   └── LoginScreen.kt
│   ├── containers/
│   │   ├── ContainerListScreen.kt
│   │   ├── ContainerListViewModel.kt
│   │   ├── components/
│   │   │   ├── ContainerItem.kt
│   │   │   └── ScanDialog.kt
│   │   └── detail/
│   │       ├── ContainerDetailScreen.kt
│   │       └── ContainerDetailViewModel.kt
│   ├── history/HistoryScreen.kt
│   ├── scan/CameraScanner.kt
│   ├── settings/SettingsScreen.kt
│   └── theme/{Color, Theme, Type}.kt
└── util/
    ├── ContainerAnalyzer.kt
    ├── ISO6346Scanner.kt
    ├── ReeferPlateScanner.kt
    ├── ScanMode.kt
    └── ScanResult.kt
```

**Borrados explícitos:** `ReeferAnalyzer.kt`, `ContainerValidator.kt`, `ContainerViewModel.kt` (monolítico), `NetworkModule.kt` (renombrado a `FirebaseModule.kt`).

---

## Cómo retomar entre sesiones

Si esta conversación se corta y volvés a hablar conmigo en otra sesión:

1. Abrí este archivo.
2. Pasáme la sección "Estado actual" + cualquier sprint que estés ejecutando.
3. Yo arranco desde ahí sin pedirte que me cuentes todo de nuevo.

## Estado actual

- ✅ v2 generado y entregado.
- ⏳ Pendiente: vos sincronizás Gradle, hacés build, corrés tests.
- ⏳ Pendiente: que avises si algo no compila para iterar.

## Próximos pasos sugeridos (orden de prioridad)

1. **Hoy mismo:** abrir el proyecto en Android Studio, sincronizar Gradle, hacer Build → Clean → Rebuild. Si todo compila, correr `./gradlew test` para verificar el suite de checksum.
2. **Esta semana:** probar en device físico que el flow de login + registro funcione con el nuevo bootstrap atómico. Verificar que el primer usuario queda como SUPER_ADMIN y los siguientes como OPERATOR pendientes.
3. **Cuando tengas un rato:** activar App Check en consola Firebase. En debug vas a ver un token en logcat que tenés que whitelistar.
4. **Antes de salir a Play Store:** deployar `firestore.rules`. Probar antes en Rules Playground.
5. **Iteración futura (no urgente):** migración a Navigation type-safe (S7) + strings.xml completo (S9).
