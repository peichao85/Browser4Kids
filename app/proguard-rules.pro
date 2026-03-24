# Browser4Kids ProGuard Rules

# Keep Room entities
-keep class com.browser4kids.data.model.** { *; }

# Keep enum classes
-keepclassmembers enum * { *; }
