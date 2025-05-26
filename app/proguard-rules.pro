# Default Proguard rules are in proguard-android-optimize.txt

# Hilt
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
-keepclassmembers class * {
    @dagger.hilt.android.internal.lifecycle.HiltViewModel <init>(...);
}
-keep class * {
    @dagger.hilt.android.AndroidEntryPoint *;
}
-keep class * {
    @dagger.hilt.InstallIn *;
}
-keep class * {
    @dagger.Module *;
}
-keep class * {
    @dagger.Provides *;
}
# If you use @AssistedInject
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}
-keep class * {
    @dagger.assisted.AssistedFactory *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep Kotlin metadata (Important for reflection, serialization, etc.)
-keep,allowobfuscation,allowshrinking class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata <fields>;
}
# Keep annotation for Kotlin's @Parcelize
-keep class kotlinx.parcelize.Parcelize {} # For Kotlin 1.3.60+
-keep interface kotlinx.parcelize.Parceler { *; } # For Kotlin 1.3.60+

# Retrofit & OkHttp (if ProGuard causes issues with them)
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature # Needed for GSON with Generics
-keepattributes InnerClasses # Needed for GSON
-keepattributes Annotation # Needed for Retrofit annotations

# GSON (if you use it for serialization with Retrofit)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep your data model classes that are serialized/deserialized by Gson
# Replace com.example.pdfvan.data.model.** with your actual package for data models
-keep class com.example.pdfvan.data.model.** { *; } # Keep all members of your data classes
# Keep Bluetooth classes if they are being reflectively accessed or if issues persist
-keep class android.bluetooth.** { *; }
-keep interface android.bluetooth.** { *; }
-keep class com.dantsu.escposprinter.** { *; }
-keep interface com.dantsu.escposprinter.** { *; }

# For ESCPOS-ThermalPrinter-Android library (if it uses reflection or specific class names internally)
# You might need to check its documentation or test if ProGuard breaks it.
# Example (you'd need to find out the actual classes if needed):
# -keep class com.dantsu.escposprinter.** { *; }

# Keep any other classes that are accessed via reflection or that ProGuard might incorrectly remove.
# For example, custom views used in XML layouts, etc.
# -keep public class com.example.pdfvan.MyCustomView { ... }