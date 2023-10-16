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

includeBuild("../share")

// == Define the inner structure of this component ==
rootProject.name = "libs" // the component name

include("list")
include("grpc")
include("configuration")
include("sqlgen")
