pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "OmniBans-Fabric"

includeBuild("..") {
    dependencySubstitution {
        substitute(module("com.hihelloy.work.omnibans:common")).using(project(":common"))
        substitute(module("com.hihelloy.work.omnibans:api")).using(project(":api"))
    }
}
