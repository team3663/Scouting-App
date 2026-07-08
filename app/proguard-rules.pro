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

-keep class net.sourceforge.jtds.** { *; }
-dontwarn net.sourceforge.jtds.**

# Google API/Drive client (google-api-client ships no consumer rules and maps JSON via
# reflection on @Key-annotated fields; R8 would otherwise strip/rename them and break uploads).
-keepattributes Signature,*Annotation*,EnclosingMethod
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * { @com.google.api.client.util.Key <fields>; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**
-dontwarn org.apache.http.**
-dontwarn javax.**

# Gson (used by GsonFactory to (de)serialize the Drive model classes)
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**