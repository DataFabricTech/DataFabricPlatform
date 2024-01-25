dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "build-logic"

include(":commons")
include(":java-library")
include(":java-application")
include(":spring-boot")
include(":report-aggregation")