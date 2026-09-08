buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
    }
}

apply(plugin = "com.android.library")
apply(plugin = "kotlin-android")
apply(plugin = "com.lagradost.cloudstream3.gradle")

fun Project.cloudstream(configuration: com.lagradost.cloudstream3.gradle.CloudstreamExtension.() -> Unit) =
    extensions.getByName<com.lagradost.cloudstream3.gradle.CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: com.android.build.gradle.BaseExtension.() -> Unit) =
    extensions.getByName<com.android.build.gradle.BaseExtension>("android").configuration()

version = 2

cloudstream {
    setRepo("https://github.com/hmaehm/lk-kid-dev")
    description = "LK21 movie and series extension with configurable domain settings"
    authors = listOf("hmaehm")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    language = "id"
}

android {
    namespace = "com.layarkacakid.dev"
    compileSdkVersion(34)

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xskip-metadata-version-check"
            )
        }
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    "compileOnly"("com.github.recloudstream.cloudstream:library:-SNAPSHOT")
    "compileOnly"("org.jsoup:jsoup:1.17.2")
    "compileOnly"("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    "compileOnly"("com.github.Blatzar:NiceHttp:0.4.11")
}
