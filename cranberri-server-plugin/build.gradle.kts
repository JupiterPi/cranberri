plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "jupiterpi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.9")
}

kotlin {
    jvmToolchain(17)
}

tasks.register<Copy>("installServerPlugin") {
    dependsOn("shadowJar")
    from("$buildDir/libs/cranberri-server-plugin-1.0-SNAPSHOT-all.jar")
    into("$buildDir/../../dev-server/plugins")
}