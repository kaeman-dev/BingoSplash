import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("net.fabricmc.fabric-loom")
    kotlin("jvm")
    kotlin("kapt")
    java
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val loaderVersion = providers.gradleProperty("loader_version").get()
val fabricVersion = providers.gradleProperty("fabric_version").get()
val fabricKotlinVersion = providers.gradleProperty("fabric_kotlin_version").get()
val owoVersion = providers.gradleProperty("owo_version").get()
val modVersion = providers.gradleProperty("mod_version").get()

group = "cc.me0wo"
version = modVersion

base {
    archivesName.set("BingoSplash")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.wispforest.io")
    maven("https://jitpack.io")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")
    implementation("io.wispforest:owo-lib:$owoVersion")
    include("io.wispforest:owo-lib:$owoVersion")
    kapt("io.wispforest:owo-lib:$owoVersion")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
}

loom {
    runs {
        named("client") {
            vmArg("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to minecraftVersion,
                "loader_version" to loaderVersion
            )
        )
    }
}

tasks.jar {
    archiveVersion.set("$modVersion-$minecraftVersion")
}
