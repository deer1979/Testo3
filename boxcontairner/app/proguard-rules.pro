# Add project specific ProGuard rules here.

# Preservar línea para mejor debug en producción
-keepattributes SourceFile,LineNumberTable

# ── FIRESTORE: data classes serializadas
# Sin esto R8 puede renombrar los campos y Firestore no puede deserializar
-keepclassmembers class com.boxcontairner.domain.model.** {
    *;
}
-keepclassmembers class com.boxcontairner.data.local.entities.** {
    *;
}

# ── KOTLIN: metadata para reflection de data classes
-keep class kotlin.Metadata { *; }

# ── HILT generated
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.** { *; }

# ── ROOM: el compilador KSP genera implementación
# Las reglas vienen de la librería automáticamente, no hace falta agregar acá.

# ── FIREBASE: keep para reflection en deserialización
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── ML KIT: keep para text recognition
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ── TIMBER
-dontwarn org.jetbrains.annotations.**

# ── COROUTINES
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
