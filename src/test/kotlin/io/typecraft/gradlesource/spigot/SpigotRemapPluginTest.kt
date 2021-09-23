package io.typecraft.gradlesource.spigot

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test

class SpigotRemapPluginTest {
    @Test
    fun configuration(@TempDir dir: File) {
        val sources = mapOf(
            "build.gradle" to """
                plugins {
                    id 'kr.entree.spigradle' version '2.2.4'
                    id 'io.typecraft.gradlesource.spigot'
                }
                
                group 'mypkg'
                version '1.0.0'
                
                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(16)
                        vendor = JvmVendorSpec.ADOPTOPENJDK
                    }
                }
                
                repositories {
                    mavenCentral()
                    mavenLocal()
                }
                
                dependencies {
                    compileOnly('org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT:remapped-mojang')
                }
                
                spigotRemap {
                    sourceJarTask.set(tasks.jar)
                    spigotVersion.set('1.17.1')
                }
            """.trimIndent(),

            "settings.gradle" to "rootProject.name = 'my-plugin'",

            "src/main/java/mypkg/MyPlugin.java" to """
                package mypkg;
                
                import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
                import org.bukkit.plugin.java.JavaPlugin;
                
                class MyPlugin extends JavaPlugin {
                    @Override
                    public void onEnable() {
                        getLogger().info(DefaultUncaughtExceptionHandlerWithName.class.getName());
                    }
                }
            """.trimIndent()
        )
        for ((path, contents) in sources) {
            val file = dir.resolve(path)
            file.parentFile.mkdirs()
            file.writeText(contents)
        }
        val runner = GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments("assemble")
            .withGradleVersion("7.1.1")
        val result = runner.build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":remapMojangToObf"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":remapObfToSpigot"))
        // TODO: more?
    }
}
