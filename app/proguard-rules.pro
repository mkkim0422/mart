# Project ProGuard / R8 rules for release build.

# --- Kotlin metadata ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-keep class kotlin.Metadata { *; }

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# --- Compose (most kept by default but keep tooling-friendly bits) ---
-keep class androidx.compose.runtime.** { *; }

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$* { *; }
-keep,allowobfuscation @interface dagger.hilt.android.AndroidEntryPoint

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# --- Glance ---
-keep class androidx.glance.** { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidget
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver
-keep class * extends androidx.glance.appwidget.action.ActionCallback

# --- AdMob (Phase 6) ---
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- Firebase / Crashlytics (Phase 6) ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# --- WorkManager (Glance widget sessions run as a WorkManager worker) ---
# R8 full mode stripped `OverwritingInputMerger.<init>()` (WorkManager builds it
# by reflection) → "Could not create Input Merger" → the Glance SessionWorker
# never ran → every placed widget stayed on its static preview layout
# (이마트 5 / 다이소 3) forever. Found 2026-09-24; the Play 1.2.0 build had it too.
-keep class androidx.work.OverwritingInputMerger { <init>(); }
-keep class androidx.work.ArrayCreatingInputMerger { <init>(); }
-keep class * extends androidx.work.InputMerger { <init>(); }
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- Billing (Phase 6) ---
-keep class com.android.billingclient.** { *; }

# --- Domain models / data classes ---
-keep class com.rldjrgo.grocerynote.domain.model.** { *; }
-keep class com.rldjrgo.grocerynote.data.local.*Entity { *; }

# --- Crashlytics: keep stack-trace mappable ---
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Strip verbose/debug logs in release (privacy: item names/store names
#     were going to Log.d). R8 removes these calls + their string building.
#     Log.i/w/e kept for crash diagnostics. ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
