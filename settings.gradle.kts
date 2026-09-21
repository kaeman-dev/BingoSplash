pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }

    plugins {
        id("net.fabricmc.fabric-loom") version "1.18.2"
        kotlin("jvm") version "2.4.20"
        kotlin("kapt") version "2.4.20"
    }
}

rootProject.name = "BingoSplash"
