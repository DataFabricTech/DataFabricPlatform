// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal() // if pluginManagement.repositories looks like this, it can be omitted as this is the default
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("../share")
includeBuild("../libs")
includeBuild("../services")

// == Define the inner structure of this component ==
rootProject.name = "aggregation"
include("test-coverage")

