pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "OmniBans"

include("common")
include("api")
include("paper")
include("velocity")
include("bungee")
