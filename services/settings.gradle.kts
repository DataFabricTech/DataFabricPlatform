// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("../utilities")
includeBuild("../libs")

// == Define the inner structure of this component ==
rootProject.name = "services" // the component name

include("sample")
include("core")
