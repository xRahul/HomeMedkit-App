# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, Exceptions

# Keep serialization models
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.Serializable <methods>;
    @kotlinx.serialization.Serializable <fields>;
}

-keepnames @kotlinx.serialization.Serializable class *

# Keep Room entities and DAOs
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class *

# Keep application DTOs and network models
-keep class in.rahulja.medicinekit.data.dto.** { *; }
-keep class in.rahulja.medicinekit.data.model.** { *; }
-keep class in.rahulja.medicinekit.network.models.** { *; }

# Ktor & GenerativeAI version mismatch fixes
-dontwarn io.ktor.**
-dontwarn com.google.ai.client.generativeai.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.slf4j.**
-dontwarn org.apache.http.**
-dontwarn com.google.api.**
-dontwarn com.google.auth.**