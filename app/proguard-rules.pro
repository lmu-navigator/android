# Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Realm
-keep class io.realm.annotations.RealmModule
-keep @io.realm.annotations.RealmModule class *
-keep class io.realm.internal.Keep
-keep @io.realm.internal.Keep class *
-dontwarn javax.**
-dontwarn io.realm.**

# Picasso
-dontwarn com.squareup.okhttp.**

# Guava
-dontwarn com.google.common.**

# Simmetrics
-dontwarn uk.ac.shef.wit.simmetrics.**

# TileView
-dontwarn com.qozix.**

# Fabric/Crashlytics
-keepattributes SourceFile,LineNumberTable
