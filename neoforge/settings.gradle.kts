pluginManagement {
    repositories {
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

rootProject.name = "OmniBans-NeoForge"

includeBuild("..") {
    dependencySubstitution {
        substitute(module("com.hihelloy.work.omnibans:common")).using(project(":common"))
        substitute(module("com.hihelloy.work.omnibans:api")).using(project(":api"))
    }
}
