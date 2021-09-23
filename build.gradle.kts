plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    `java-gradle-plugin`
    `kotlin-dsl`
}

group = "io.typecraft"
version = "1.0.0"

repositories {
    mavenCentral()
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
            implementationClass = "io.typecraft.gradlesource.spigot.SpigotRemapPlugin"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
