# jetpack and database
-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**
# dependencies

-keep class com.intuit.sdp.** { *; }
-keep class com.intuit.ssp.** { *; }
-keep class com.github.bumptech.glide.** { *; }
-keep class com.koushikdutta.ion.** { *; }
-keep class com.hanks.** { *; }
-keep class com.github.danielnilsson9.** { *; }
-keep class com.github.armcha.** { *; }
-keep class com.yarolegovich.** { *; }
# android support and IME
-keep class com.android.inputmethod.** { *; }
-keep class com.android.inputmethodcommon.** { *; }
# billing
-keep class com.android.vending.billing.** { *; }
# facebook
-dontwarn com.facebook.ads.internal.**
-keeppackagenames com.facebook.*
-keep public class com.facebook.ads.** {
   public protected *;
}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}
# giphy
-keep public class com.giphy.sdk.core.** { *; }
-dontwarn com.giphy.sdk.core.**
# fabric and firebase
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}
-keep class com.google.android.gms.measurement.AppMeasurement$OnEventListener {*;}
-keep class com.google.android.gms.measurement.AppMeasurement {
   static com.google.android.gms.measurement.AppMeasurement getInstance(android.content.Context, java.lang.String, java.lang.String);
   void registerOnMeasurementEventListener(com.google.android.gms.measurement.AppMeasurement$OnEventListener);
}
-keep class com.google.firebase.FirebaseApp {
   static com.google.firebase.FirebaseApp getInstance();
   boolean isDataCollectionDefaultEnabled();
}
-keep class com.crashlytics.android.ndk.** { *; }
-dontwarn com.crashlytics.android.ndk.**

-keepattributes *Annotation*
   -keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
    }
   -keep enum org.greenrobot.eventbus.ThreadMode { *; }

# default
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keep class android.support.v8.renderscript.** { *; }
-keep class androidx.renderscript.** { *; }

#Các class được bảo mật
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

 # Add this global rule
    -keepattributes Signature


    -keepclassmembers class com.tapbi.spark.objects.** {
      *;
    }
-keep class com.google.android.gms.internal.ads.** { *; }
-dontwarn kotlinx.parcelize.Parcelize