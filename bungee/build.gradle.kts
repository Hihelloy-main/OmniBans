plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
    implementation(project(":common"))
}

tasks.shadowJar {
    archiveFileName.set("OmniBans-Bungee.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
