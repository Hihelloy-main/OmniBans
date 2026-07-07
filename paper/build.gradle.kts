plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.discordsrv:discordsrv:1.26.0")
    implementation(project(":common"))
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("net.kyori:adventure-text-serializer-bungeecord:4.3.4")
    implementation("net.kyori:adventure-text-serializer-plain:4.17.0")
}

tasks.shadowJar {
    archiveFileName.set("OmniBans-Paper.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
