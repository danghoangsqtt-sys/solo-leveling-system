# ── kotlinx.serialization ─────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable data classes in core (AI/sync DTOs)
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static ** INSTANCE;
    static ** Companion;
    ** serialVersionUID;
    private static final ** $$serialDesc;
    ** $$delegatedSerializer;
    ** $childSerializers;
    public static ** serializer();
}

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ── Ktor ──────────────────────────────────────────────────────────────────────
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-dontwarn kotlinx.coroutines.**

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ── Security Crypto ───────────────────────────────────────────────────────────
-keep class androidx.security.crypto.** { *; }

# ── OkHttp / Okio (Ktor Android engine) ──────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Reflection (Kotlin) ───────────────────────────────────────────────────────
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**
