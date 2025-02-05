dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}
rootProject.name = "platforms"

include("product-platform")
include("test-platform")
include("plugins-platform")