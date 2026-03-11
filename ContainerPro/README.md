# ContainerPro — Field Service Tool

Clon limpio de ContainerLINK sin marcas, reconstruido con:
- **Jetpack Compose** + Material Design 3
- **Paleta Emerald Green + Amber** (tendencia 2025, sin azul)
- **WiFi Direct P2P** para conexión a unidades ML5
- **Anyline SDK OCR** para escaneo de número ISO 6346
- **CameraX** para fotografías de servicio
- **100% local** — sin base de datos externa

---

## Pantallas implementadas

| Pantalla | Descripción |
|----------|-------------|
| `WelcomeScreen` | Login técnico con ID + PIN |
| `HomeScreen` | Dashboard con acceso a todas las funciones |
| `ScanScreen` | Escaneo OCR del número del contenedor |
| `WifiConnectScreen` | Conexión WiFi Direct P2P a unidad ML5 |
| `ModelSelectScreen` | Selección de modelo ML3/ML5 |
| `DRRScreen` | Diagnóstico remoto del reefer (temperatura, alarmas, estado) |
| `ServiceCheckScreen` | Checklist de 16 ítems por categoría |
| `PhotosScreen` | Captura y gestión de fotos |
| `ReportsScreen` | Historial de reportes locales |
| `SettingsScreen` | Información de sesión y ajustes |

---

## Setup

### Requisitos
- Android Studio Hedgehog o superior
- Kotlin 2.0+
- minSdk 26 (Android 8.0)
- targetSdk 35

### Dependencias clave
```
Compose BOM: 2024.09.03
Navigation Compose: 2.8.2
CameraX: 1.3.4
Anyline SDK: 48.1.0
Accompanist Permissions: 0.34.0
```

### Fuentes requeridas
Agregar en `app/src/main/res/font/`:
- `dm_sans_regular.ttf`
- `dm_sans_medium.ttf`
- `dm_sans_semibold.ttf`
- `dm_sans_bold.ttf`
- `dm_sans_extrabold.ttf`
- `jetbrainsmono_regular.ttf`
- `jetbrainsmono_bold.ttf`

Descargables desde:
- [DM Sans — Google Fonts](https://fonts.google.com/specimen/DM+Sans)
- [JetBrains Mono](https://www.jetbrains.com/legalnotices/mono/)

### Anyline SDK
Requiere licencia de `io.anyline:anyline-sdk:48.1.0`.
La lógica de escaneo OCR está preparada en `ScanScreen.kt`
— integrar el `ScanActivity` de Anyline con el `scanConfig.json`
del APK original (ya extraído y sin datos de marca).

### WiFi Direct
La conexión real funciona en dispositivo físico.
`WifiDirectService.kt` implementa el flujo completo:
- `discoverPeers()` → busca unidades ML5 en rango
- `connect(device)` → conecta P2P
- `disconnect()` → libera conexión

El TCP socket para leer parámetros DRR se conecta al
`groupOwnerIp` obtenido post-conexión P2P.

---

## Iconos utilizados (Material Symbols — Apache 2.0)

| Función | Icono |
|---------|-------|
| Escanear contenedor | `Icons.Rounded.QrCodeScanner` |
| WiFi Direct | `Icons.Rounded.Wifi / WifiTethering` |
| DRR / Diagnóstico | `Icons.Rounded.Analytics` |
| Modelos | `Icons.Rounded.ViewInAr` |
| Checklist | `Icons.Rounded.FactCheck` |
| Fotos | `Icons.Rounded.CameraAlt` |
| Reportes | `Icons.Rounded.Description` |
| Temperatura | `Icons.Rounded.Thermostat / AcUnit` |
| Alarmas | `Icons.Rounded.Warning` |
| Compresor | `Icons.Rounded.ElectricBolt` |

Todos los iconos son de **Material Icons Extended** (licencia Apache 2.0).
**Ningún recurso gráfico pertenece a Carrier, Transicold o CareMAX.**

---

## Paleta de colores

```
Primary (Emerald):     #4EC48C — #2E9E6D — #1B7A52
Secondary (Amber):     #F59C1A — #AD6A00 — #8B5300
Background:            #0D1511 — #131C17 — #1A241E
Error (Coral):         #EF5350
Success:               #4CAF50
Warning:               #FFB300
```

---

*Desarrollado como herramienta de servicio técnico independiente.*
*No afiliado con Carrier Corporation, Transicold ni CareMAX.*
