# Velora release shrinking rules.

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.velora.vault.**$$serializer { *; }
-keepclassmembers class com.velora.vault.** {
    *** Companion;
}
-keepclasseswithmembers class com.velora.vault.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt / Dagger
-dontwarn com.google.errorprone.annotations.**

# Retain crypto model classes used via reflection-free serialization only.
-keepattributes Signature, RuntimeVisibleAnnotations
