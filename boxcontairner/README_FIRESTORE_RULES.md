# README — Reglas Firestore

## Estado actual: NO deployar todavía

Durante **desarrollo**, mantené las reglas que tenga tu proyecto Firebase (probablemente las default "test mode" que dejan todo abierto a usuarios autenticados). Las reglas de este archivo son para cuando salgas a producción.

## Cuándo deployar `firestore.rules`

Cuando estés por publicar la app y pase todo esto:

1. ✅ App Check activado en Firebase Console (consola → App Check → tu app android).
2. ✅ Huella SHA-256 del APK de release cargada en App Check + Play Console.
3. ✅ Probaste el flujo de registro/login en un device de test con App Check activo.
4. ✅ Ya está creado el primer SUPER_ADMIN (vos mismo) en Firestore.

## Cómo deployar

**Opción A — Firebase Console (manual, rápido)**

1. Abrí tu proyecto en https://console.firebase.google.com
2. Firestore Database → Rules
3. Copiá el contenido de `firestore.rules` y pegalo en el editor.
4. Click "Publish".

**Opción B — Firebase CLI (recomendado para tener versionado)**

```bash
npm install -g firebase-tools
firebase login
firebase init firestore     # apuntalo al archivo firestore.rules
firebase deploy --only firestore:rules
```

## Qué hacen estas reglas

**Defensa en profundidad** — incluso si alguien rompe la app cliente, el backend solo
permite operaciones legítimas.

| Recurso | Quién puede leer | Quién puede escribir |
|---|---|---|
| `_meta/bootstrap` | Cualquier autenticado | Solo el primer usuario, una vez. Inmutable después. |
| `users/{uid}` (propio) | El usuario | Solo cambiar nick (no role ni isActive) |
| `users/{uid}` (cualquiera) | ADMIN+ | SUPER_ADMIN |
| Lista de usuarios | ADMIN+ | — |
| `containers/{id}` | Usuario activo | Crear/editar: usuario activo · Borrar: ADMIN+ |
| `containers/{id}/history/{ev}` | Usuario activo | Append-only — no se puede editar ni borrar |

**App Check obligatorio en todo** — incluso un usuario autenticado válido no puede hacer requests si la app no presenta token de App Check válido. Eso bloquea bots y clientes alternativos que copien tu `google-services.json`.

## Probar las reglas antes de deployar

Firebase Console tiene un "Rules Playground" donde podés simular requests con distintos usuarios. Probá al menos:

- Usuario OPERATOR intentando borrar un container → debe ser DENIED.
- Usuario OPERATOR intentando ponerse `role = "SUPER_ADMIN"` en su propio doc → DENIED.
- Crear `_meta/bootstrap` cuando ya existe → DENIED.
- ADMIN listando users → ALLOWED.

## Bootstrap del primer SUPER_ADMIN

Estas reglas permiten que el cliente cree `/_meta/bootstrap` + `/users/{uid}` con role SUPER_ADMIN solo si `_meta/bootstrap` no existe todavía. La transacción es atómica — si dos clientes corren simultáneamente, solo uno gana.

Esto preserva la UX que tenías pensada (primer usuario = IT, se autopromueve) pero sin las race conditions ni huecos de seguridad del v1.
