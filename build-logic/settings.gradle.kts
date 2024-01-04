dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

rootProject.name = "build-logic"

include(":commons")
include(":java-library")
include(":java-application")
include(":spring-boot")
include(":report-aggregation")