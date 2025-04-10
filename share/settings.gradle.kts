pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
    plugins {
        id("org.jsonschema2pojo") version "1.2.2"
    }
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("../platforms")

rootProject.name = "share" // the component name

include(":annotator")
include(":schema")
