pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "OmniBans-Forge"

includeBuild("..") {
    dependencySubstitution {
        substitute(module("com.hihelloy.work.omnibans:common")).using(project(":common"))
        substitute(module("com.hihelloy.work.omnibans:api")).using(project(":api"))
    }
}
