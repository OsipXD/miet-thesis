// Versions
val kotlinVersion by extra("1.2.41")
val androidGradleVersion = "3.0.1"

val supportLibVersion = "27.1.1"
val constraintLayoutVersion = "1.0.2"

val daggerVersion = "2.15"
val moxyVersion = "1.5.3"
val ciceroneVersion = "3.0.0"

val gsonVersion = "2.8.2"
val timberVersion = "4.7.0"
val retrofitVersion = "2.4.0"
val interceptorVersion = "3.10.0"

val rxJavaVersion = "2.1.12"
val rxAndroidVersion = "2.0.2"
val roomVersion = "1.0.0"


// Packages
val supportPackage = "com.android.support"

val daggerPackage = "com.google.dagger"
val moxyPackage = "com.arello-mobile"

val gsonPackage = "com.google.code.gson"
val jakeWhartonPackage = "com.jakewharton"
val squarePackage = "com.squareup"
val retrofitPackage = "$squarePackage.retrofit2"

val rxPackage = "io.reactivex.rxjava2"
val roomPackage = "android.arch.persistence.room"


// Single dependencies
val rxJava = "rxJava" to dependency(rxPackage, "rxjava", rxJavaVersion)
val rxAndroid = "rxAndroid" to dependency(rxPackage, "rxandroid", rxAndroidVersion)
val javaxInject = "javaxInject" to dependency("javax.inject", "javax.inject", "1")
val retrofit = "retrofit" to dependency(retrofitPackage, "retrofit", retrofitVersion)
val interceptor = "interceptor" to dependency("$squarePackage.okhttp3", "logging-interceptor", interceptorVersion)
val gson = "gson" to dependency(gsonPackage, "gson", gsonVersion)
val room = "room" to dependency(roomPackage, "runtime", roomVersion)
val roomRx = "roomRx" to dependency(roomPackage, "rxjava2", roomVersion)
val timber = "timber" to dependency("$jakeWhartonPackage.timber", "timber", timberVersion)


// Configuration for App (see /presentation/build.gradle.kts and /data/build.gradle.kts)
val appConfig by extra(
        AppConfiguration(
                android = AndroidConfiguration(
                        buildTools = "27.0.3",
                        minApi = 19,
                        targetApi = 27,
                        gradlePlugin = dependency("com.android.tools.build", "gradle", androidGradleVersion)
                ),
                lib = Libraries(
                        "appCompat" to dependency(supportPackage, "appcompat-v7", supportLibVersion),
                        "design" to dependency(supportPackage, "design", supportLibVersion),
                        "constraintLayout" to dependency("$supportPackage.constraint", "constraint-layout", constraintLayoutVersion),
                        "dagger" to dependency(daggerPackage, "dagger", daggerVersion),
                        "moxy" to dependency(moxyPackage, "moxy-app-compat", moxyVersion),
                        "cicerone" to dependency("ru.terrakok.cicerone", "cicerone", ciceroneVersion),
                        "retrofitRx" to dependency(retrofitPackage, "adapter-rxjava2", retrofitVersion),
                        "retrofitGson" to dependency(retrofitPackage, "converter-gson", retrofitVersion),
                        timber,
                        retrofit,
                        interceptor,
                        gson,
                        rxJava,
                        rxAndroid,
                        room,
                        roomRx
                ),
                kapt = Libraries(
                        "daggerCompiler" to dependency(daggerPackage, "dagger-compiler", daggerVersion),
                        "moxyCompiler" to dependency(moxyPackage, "moxy-compiler", moxyVersion)
                ),
                testLib = Libraries(
                        "junit" to dependency("junit", "junit", "4.12")
                )
        )
)

// Libraries for Domain and Data modules (see /domain/build.gradle.kts and /data/build.gradle.kts)
val commonLibs: Libraries by extra(
        Libraries(javaxInject, rxJava, room)
)

// Libraries for Data module (see /data/build.gradle.kts)
val dataLibs: Libraries by extra(
        Libraries(interceptor, retrofit, gson, roomRx, timber)
)

val dataKaptLibs: Libraries by extra(
        Libraries(
                "roomCompiler" to dependency(roomPackage, "compiler", roomVersion)
        )
)


// Utility functions

fun dependency(`package`: String, name: String, version: String): String {
    return "$`package`:$name:$version"
}
