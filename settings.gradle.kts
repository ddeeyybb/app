pluginManagement {
    repositories {
        google()  // Ensure Google repo is added
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()  // Ensure Google repo is added
        mavenCentral()
    }
}

// ADD THESE LINES:
rootProject.name = "app"  // <-- Replace with your app's name
include(":app")
