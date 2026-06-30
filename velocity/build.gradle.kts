plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation(project(":common"))
}

tasks.shadowJar {
    archiveFileName.set("OmniBans-Velocity.jar")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
