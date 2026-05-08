plugins {
    `jvm-toolchains`
    kotlin("jvm") version "2.3.20"
    id("com.gradleup.shadow") version "9.3.0"
    id("com.github.gmazzo.buildconfig") version "6.0.9"
    kotlin("plugin.serialization") version "2.3.20"
    application
}

group = "com.sybsuper"
version = project.findProperty("version")?.toString() ?: "unknown"

repositories {
    mavenCentral()

    maven {
        url = uri("https://mvn.everbuild.org/public")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://maven.solo-studios.ca/releases/")
    }
}

dependencies {
    implementation("net.minestom:minestom:2026.04.13-1.21.11")
    implementation("com.dfsek.terra:minestom:7.0.0-BETA+968637223")
    implementation("com.charleskorn.kaml:kaml:0.78.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("io.methvin:directory-watcher:0.19.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("com.sybsuper.terradevserver.MainKt")
}

buildConfig {
    packageName("com.sybsuper.terradevserver")
    buildConfigField("VERSION", version.toString())
}

tasks {
    test {
        useJUnitPlatform()
    }
}