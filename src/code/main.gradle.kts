import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {

    project.apply { from("gradle/configuration.gradle.kts") }
    val appConfig: AppConfiguration by extra
    val kotlinVersion: String by extra

    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath(appConfig.android.gradlePlugin)
        classpath(kotlin("gradle-plugin", kotlinVersion))

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}


subprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
