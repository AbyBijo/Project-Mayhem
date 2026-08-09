# JavascriptInterface methods must retain their annotations and names.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
