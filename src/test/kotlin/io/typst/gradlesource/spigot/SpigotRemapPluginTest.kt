package io.typst.gradlesource.spigot

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test

class SpigotRemapPluginTest {
    /**
     * This requires BuildTools to run with `--rev 1.21.8 --remapped` options.
     */
    @Test
    fun configuration(@TempDir dir: File) {
        val sources = mapOf(
            "build.gradle" to """
                plugins {
                    id 'java'
                    id 'io.typst.spigradle' version '3.0.2'
                    id 'io.typst.gradlesource.spigot'
                }
                
                group 'mypkg'
                version '1.0.0'
                
                java {
                    toolchain {
                        languageVersion = JavaLanguageVersion.of(21)
                    }
                }
                
                repositories {
                    mavenCentral()
                }
                
                dependencies {
                    compileOnly('org.spigotmc:spigot:1.21.8-R0.1-SNAPSHOT:remapped-mojang')
                }
                
                spigotRemap {
                    sourceJarTask.set(tasks.jar)
                    spigotVersion.set('1.21.8')
                }
            """.trimIndent(),

            "settings.gradle" to "rootProject.name = 'my-plugin'",

            "src/main/java/mypkg/MyPlugin.java" to """
                package mypkg;
                
                import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
                import org.bukkit.plugin.java.JavaPlugin;
                
                public class MyPlugin extends JavaPlugin {
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
            .withGradleVersion("8.14.3")
        val result = runner.build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":remapMojangToObf")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":remapObfToSpigot")?.outcome)
    }
}
