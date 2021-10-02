plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    `java-gradle-plugin`
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "io.typecraft"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.md-5:SpecialSource:1.10.0")
    compileOnly(fileTree("libs") {
        include("*.jar")
    })
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

gradlePlugin {
    plugins {
        create("spigot") {
            id = "io.typecraft.gradlesource.spigot"
            displayName = "Gradle SpecialSource"
            description = "SpecialSource for Gradle to remap name definitions."
            implementationClass = "io.typecraft.gradlesource.spigot.SpigotRemapPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/typecraft-io/gradle-specialsource"
    vcsUrl = "https://github.com/typecraft-io/gradle-specialsource.git"
    tags = listOf("spigot", "bukkit", "specialsource")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
