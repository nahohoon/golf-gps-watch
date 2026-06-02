# Release 빌드용 (현재 minify 비활성화 — Play 배포 후 필요 시 활성화)
-keep class com.golfgps.watch.MainActivity { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
