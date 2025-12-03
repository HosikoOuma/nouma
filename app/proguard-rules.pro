# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#------------------ General Kotlin ------------------#

# Keep Kotlin metadata, which is essential for reflection and coroutines
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @kotlin.Metadata <methods>;
}
-keepclasseswithmembers,allowshrinking class * {
    @kotlin.jvm.JvmName <methods>;
}

# Keep data classes (often used for models and serialization)
-keepclassmembers class * extends kotlin.jvm.internal.markers.KMappedMarker {
    *;
}

# Keep companion objects of serializable classes
-keepclassmembers class * {
    ** Companion;
}

# Keep all Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}


#------------------ Jetpack Compose ------------------#

# Keep Composable functions and Previews
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}
-keep class androidx.compose.runtime.** { *; }


#------------------ Room ------------------#

# Keep entities, DAOs, and Databases for Room
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }


#------------------ Coil (Image Loading) ------------------#

# Coil uses OkHttp, which has its own robust ProGuard rules.
# Usually, no specific rules are needed for Coil itself if you use the latest versions.
# This rule is a safeguard.
-keepclassmembers class * {
    @coil.annotation.* <methods>;
}


#------------------ Coroutines ------------------#

# Keep coroutine-related classes
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class ** {
    kotlinx.coroutines.flow.StateFlow field;
    kotlinx.coroutines.flow.SharedFlow field;
}
-keepclassmembers class * {
    *** perform*(*, kotlin.coroutines.Continuation);
}

#------------------ Keep your app's model/data classes ------------------#

# This is a generic rule. Replace com.nkds.hosikoouma.nouma.data.** with your actual package name for models.
# It prevents obfuscation of fields in your data/model classes, which is crucial if you use them with serialization or reflection.
-keepclassmembers class com.nkds.hosikoouma.nouma.data.** { *; }
-keep class com.nkds.hosikoouma.nouma.data.** { *; }

