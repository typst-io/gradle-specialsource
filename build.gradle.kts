plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.3.1"
}

group = "io.typst"
version = "2.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.md-5:SpecialSource:1.11.5")
    compileOnly(fileTree("libs") {
        include("*.jar")
    })
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

gradlePlugin {
    website.set("https://github.com/typst-io/gradle-specialsource")
    vcsUrl.set("https://github.com/typst-io/gradle-specialsource.git")
    plugins {
        create("spigot") {
            id = "io.typst.gradlesource.spigot"
            displayName = "Gradle SpecialSource"
            description = "SpecialSource for Gradle to remap name definitions."
            implementationClass = "io.typst.gradlesource.spigot.SpigotRemapPlugin"
            tags.set(listOf("spigot", "bukkit", "specialsource"))
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
