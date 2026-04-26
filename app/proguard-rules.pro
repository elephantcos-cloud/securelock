-keep class com.secure.applock.service.** { *; }
-keep class com.secure.applock.util.CryptoUtil { *; }
-keepclassmembers class * {
    native <methods>;
}
