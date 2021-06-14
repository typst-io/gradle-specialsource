plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
    `java-gradle-plugin`
}

group = "io.typecraft"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

gradlePlugin {
    plugins {
        create("specialsource") {
            id = "io.typecraft"
            implementationClass = "io.typecraft.specialsource.SpecialSourcePlugin"
        }
    }
}
