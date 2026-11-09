# R8 and ProGuard Optimization Rules for Google Play DEX Code Quality (>25% score)

# Preserve line numbers and attributes for debugging and stack traces
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Firebase Firestore deserialization
-keepclassmembers class com.example.data.** {
    <fields>;
    <init>(...);
}
-keepclassmembers class com.example.userapp.data.model.** {
    <fields>;
    <init>(...);
}
-keepclassmembers class com.eclassports.football.model.** {
    <fields>;
    <init>(...);
}

# Subscription Models
-keepclassmembers class com.example.billing.** {
    <fields>;
    <init>(...);
}

# RevenueCat Purchases SDK
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# Retrofit & OkHttp
-dontwarn javax.annotation.**
-dontwarn java.lang.invoke.**

