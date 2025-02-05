// == Define locations for build logic ==
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
    includeBuild("../build-logic")
}

// == Define locations for components ==
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("../platforms")
includeBuild("../share")
includeBuild("../libs")

// == Define the inner structure of this component ==
rootProject.name = "services" // the component name

// sub module
//include("core")
//include("dataLayer")
//include("extraction")
//include("jpaSample")
//include("dataRelationshipTrigger")
include("new-spring-boot-sample")


