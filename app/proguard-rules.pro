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

# Keep JavaMail classes used by EmailRepository
-keep class javax.mail.Session { *; }
-keep class javax.mail.Authenticator { *; }
-keep class javax.mail.PasswordAuthentication { <init>(...); }
-keep class javax.mail.Message { *; }
-keep class javax.mail.Message$RecipientType { *; }
-keep class javax.mail.Transport { public static void send(javax.mail.Message); }
-keep class javax.mail.internet.MimeMessage { <init>(...); *; }
-keep class javax.mail.internet.InternetAddress { <init>(...); public static javax.mail.internet.InternetAddress[] parse(java.lang.String); }
-keep class javax.mail.MessagingException { <init>(...); }

# Keep SMTP provider loaded via reflection by JavaMail
-keep class com.sun.mail.smtp.SMTPProvider { *; }
-keep class com.sun.mail.smtp.SMTPTransport { *; }

-dontwarn javax.mail.**
-dontwarn javax.activation.**
-dontwarn com.sun.mail.**
