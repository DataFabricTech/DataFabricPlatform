// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
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

// == Define the inner structure of this component ==
rootProject.name = "services" // the component name

// sub module
include("core")
//include("gateway") - golang
include("dataLayer")
