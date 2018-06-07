import com.android.build.gradle.internal.dsl.SigningConfig
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

apply {
    plugin("kotlin-android-extensions")
}

val appConfig: AppConfiguration by rootProject.extra
val kotlinVersion: String by rootProject.extra

android {
    compileSdkVersion(appConfig.android.targetApi)
    buildToolsVersion(appConfig.android.buildTools)

    defaultConfig {
        minSdkVersion(appConfig.android.minApi)
        targetSdkVersion(appConfig.android.targetApi)

        applicationId = "ru.endlesscode.miet.orioks"
        versionCode = 1
        versionName = "0.1.0"

        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            val isRunningOnCI = System.getenv("CI") == "true"

            if (isRunningOnCI) {
                storeFile = file("../tmp.jks")
                storePassword = System.getenv("keystore_password")
                keyAlias = System.getenv("keystore_alias")
                keyPassword = System.getenv("keystore_alias_password")
            } else {
                val secretProperties = Properties()
                secretProperties.load(FileInputStream(project.file("secret.properties")))
                keyAlias = secretProperties.getProperty("keyAlias") ?: error("'keyAlias' should be specified")
                keyPassword = secretProperties.getProperty("keyPassword") ?: error("'keyPassword' should be specified")
                storeFile = file(secretProperties.getProperty("storeFile") ?: error("'storeFile' should be specified"))
                storePassword = secretProperties.getProperty("storePassword") ?: error("'storePassword' should be specified")
            }
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false

            val files = file("./proguard").listFiles { _, name -> name.endsWith(".pro") }
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), *files)
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", kotlinVersion))

    api(project(":domain"))
    api(project(":data"))

    appConfig.lib.applyAll { implementation(it) }
    appConfig.kapt.applyAll { kapt(it) }
    appConfig.testLib.applyAll { testImplementation(it) }
}


task("printVersion").doLast {
    print(android.defaultConfig.versionName)
}
