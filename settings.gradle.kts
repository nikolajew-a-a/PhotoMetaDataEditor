rootProject.name = "PhotoMetaDataEditor"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Автоматически скачивает нужный JDK для сборки, если его нет на машине
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":domain")
include(":data")
include(":composeApp")
