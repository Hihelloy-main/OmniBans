pluginManagement {
    repositories {
        maven("https://repo.spongepowered.org/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "OmniBans-Sponge"

includeBuild("..") {
    dependencySubstitution {
        substitute(module("com.hihelloy.work.omnibans:common")).using(project(":common"))
        substitute(module("com.hihelloy.work.omnibans:api")).using(project(":api"))
    }
}
