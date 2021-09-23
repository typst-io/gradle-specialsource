package io.typecraft.gradlesource

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URL
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.assertEquals

class RemapTaskTest {
    /**
     * This requires BuildTools to run with `--rev 1.17.1 --remapped` option.
     */
    @Test
    fun remap(@TempDir dir: File) {
        val pluginVersion = System.getenv("GRADLE_SPECIALSOURCE_VERSION")
        val homeDir = System.getProperty("user.home").replace("\\", "/")
        val mojang2ObfMappingPath =
            "${homeDir}/.m2/repository/org/spigotmc/minecraft-server/1.17.1-R0.1-SNAPSHOT/minecraft-server-1.17.1-R0.1-SNAPSHOT-maps-mojang.txt".replace(
                "\\",
                "/"
            )
        val obf2SpigotMappingPath =
            "${homeDir}/.m2/repository/org/spigotmc/minecraft-server/1.17.1-R0.1-SNAPSHOT/minecraft-server-1.17.1-R0.1-SNAPSHOT-maps-spigot.csrg".replace(
                "\\",
                "/"
            )
        val mojang2ObfJarPathGradle = dir.resolve("my-plugin-obf-gradle.jar").absolutePath.replace("\\", "/")
        val obf2SpigotJarPathGradle = dir.resolve("my-plugin-spigot-gradle.jar").absolutePath.replace("\\", "/")
        val sources = mapOf(
            "build.gradle" to """
                import io.typecraft.gradlesource.RemapTask
                
                plugins {
                    id 'java'
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
                    maven { url 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/' }
                    mavenLocal()
                }
                
                dependencies {
                    compileOnly('org.spigotmc:spigot:1.17.1-R0.1-SNAPSHOT:remapped-mojang')
                }
                
                def mojangToObf = tasks.register('mojangToObf', RemapTask) {
                    inJarFile = tasks.jar.archiveFile
                    outJarFile = new File('${mojang2ObfJarPathGradle}')
                    mappingFile = new File('${mojang2ObfMappingPath}')
                    reverse = true
                }
                
                def obfToSpigot = tasks.register('obfToSpigot', RemapTask) {
                    inJarFile = mojangToObf.get().outJarFile
                    outJarFile = new File('${obf2SpigotJarPathGradle}')
                    mappingFile = new File('${obf2SpigotMappingPath}')
                }
                
                assemble.dependsOn(mojangToObf)
                assemble.dependsOn(obfToSpigot)
            """.trimIndent(),
            "settings.gradle" to """
                rootProject.name = 'my-plugin'
            """.trimIndent(),
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
            """.trimIndent(),
            "plugin.yml" to """
                main: mypkg.MyPlugin
                name: MyPlugin
                version: 1.0.0
            """.trimIndent()
        )
        for ((path, code) in sources) {
            val file = dir.resolve(path)
            file.parentFile.mkdirs()
            file.writeText(code)
        }
        val runner = GradleRunner.create()
            .withProjectDir(dir)
            .withPluginClasspath()
            .withArguments("assemble")
            .withGradleVersion("7.1.1")

        // 1. build success
        val result = runner.build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":assemble")?.outcome)

        // 2. remap equality with original SpecialSource
        val inJarFile = dir.resolve("build/libs/my-plugin-1.0.0.jar")
        val mojang2ObfJarFile = dir.resolve("my-plugin-obf.jar")
        val obf2SpigotJarFile = dir.resolve("my-plugin-spigot.jar")
        val ssJar = dir.resolve("special-source.jar")
        ssJar.writeBytes(URL("https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.10.0/SpecialSource-1.10.0-shaded.jar").readBytes())
        val ssPath = ssJar.absolutePath.replace("\\", "/")
        val cpSep = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            ";"
        } else ":"
        val cmds = listOf(
            "java -cp \"${ssPath}${cpSep}${homeDir}/.m2/repository/org/spigotmc/spigot/1.17.1-R0.1-SNAPSHOT/spigot-1.17.1-R0.1-SNAPSHOT-remapped-mojang.jar\" net.md_5.specialsource.SpecialSource --live -i ${
                inJarFile.absolutePath.replace(
                    "\\",
                    "/"
                )
            } -o ${mojang2ObfJarFile.name} -m $mojang2ObfMappingPath --reverse",
            "java -cp \"${ssPath}${cpSep}${homeDir}/.m2/repository/org/spigotmc/spigot/1.17.1-R0.1-SNAPSHOT/spigot-1.17.1-R0.1-SNAPSHOT-remapped-obf.jar\" net.md_5.specialsource.SpecialSource --live -i ${mojang2ObfJarFile.name} -o ${obf2SpigotJarFile.name} -m $obf2SpigotMappingPath"
        )
        for (cmd in cmds) {
            val proc = Runtime.getRuntime().exec(cmd, null, dir)
            val exitCode = proc.waitFor()
            proc.destroyForcibly()
            assertEquals(
                0, exitCode, """
                exit-code: $exitCode
                output:
                    ${proc.inputStream.bufferedReader().readText()}
                error: 
                    ${proc.errorStream.bufferedReader().readText()}
                cmd: $cmd
            """.trimIndent()
            )
        }
        val jarPairs = listOf(
            mojang2ObfJarFile to File(mojang2ObfJarPathGradle),
            obf2SpigotJarFile to File(obf2SpigotJarPathGradle)
        )
        for ((fileA, fileB) in jarPairs) {
            val jarA = JarFile(fileA)
            val jarB = JarFile(fileB)
            for (entry in jarA.entries()) {
                if (!entry.isDirectory) {
                    println("Comparing ${entry.name}...")
                    val inA = jarA.getInputStream(entry)
                    val inB = jarB.getInputStream(entry)
                    Assertions.assertArrayEquals(inA.readBytes(), inB.readBytes(), "mismatch from ${entry.name}")
                    inA.close()
                    inB.close()
                }
            }
            jarA.close()
            jarB.close()
        }
    }
}
