# ProGuard rules for ScreenLog

# Keep Retrofit and OkHttp classes
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations

-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}

# Keep GSON / Serialization models
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Room DB entities & DAOs
-keep class * extends androidx.room.RoomDatabase
-keep class com.screenlog.app.data.local.entity.** { *; }
-keep interface com.screenlog.app.data.local.dao.** { *; }

# Keep Dagger Hilt models
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
