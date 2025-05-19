# Firebase Realtime Database model classes
-keepclassmembers class com.example.app.item {
    <fields>;
}

# Keep all Firebase database annotations
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <methods>;
}

# Keep your item model class completely
-keep class com.example.app.item { *; }

# Firebase general rules
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Optional: To make sure Firebase auth works too (if you use it)
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.tasks.** { *; }
