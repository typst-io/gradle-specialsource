package io.typecraft.gradlesource.spigot

import io.typecraft.gradlesource.RemapTask
import io.typecraft.gradlesource.archiveNameFromTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Plugin for remap mojang to spigot for distribution.
 *
 * This plugin will register:
 * - Extensions:
 *    - `spigotRemap` [SpigotRemapExtension]
 * - Tasks:
 *    - `remapMojangToObf` [RemapTask]: remap your jar(mojang mapping) to obfuscated.
 *    - `remapObfToSpigot` [RemapTask]: remap the obfuscated jar to spigot(for distribution).
 * - Plugins:
 *    - `java` - [JavaPlugin][org.gradle.api.plugins.JavaPlugin]
 *
 * @since 1.0
 */
class SpigotRemapPlugin : Plugin<Project> {
    override fun apply(p: Project) {
        p.pluginManager.apply(JavaPlugin::class)
        val ext = p.extensions.create<SpigotRemapExtension>("spigotRemap")
        val remapMojangToObf = p.tasks.register<RemapTask>("remapMojangToObf")
        val remapObfToSpigot = p.tasks.register<RemapTask>("remapObfToSpigot")
        remapMojangToObf.configure {
            inJarFile.set(ext.sourceJarTask.flatMap { jarTask ->
                jarTask.archiveFile
            })
            outJarFile.set(ext.sourceJarTask.flatMap { jarTask ->
                jarTask.destinationDirectory.map { dir ->
                    val archiveName = archiveNameFromTask(jarTask).copy(classifier = "obf")
                    dir.file(archiveName.toFileName())
                }
            })
            mappingFile.set(p.layout.file(ext.spigotVersion.map { ver ->
                val config = p.configurations.detachedConfiguration()
                p.dependencies.add(config.name, "org.spigotmc:minecraft-server:$ver:remapped-obf")
                config.singleFile
            }))
            reverse.set(true)
        }
        remapObfToSpigot.configure {
            inJarFile.set(remapMojangToObf.flatMap {
                it.outJarFile
            })
            outJarFile.set(ext.sourceJarTask.flatMap { jarTask ->
                jarTask.destinationDirectory.map { dir ->
                    val archiveName = archiveNameFromTask(jarTask).copy(classifier = "spigot")
                    dir.file(archiveName.toFileName())
                }
            })
            mappingFile.set(p.layout.file(ext.spigotVersion.map { ver ->
                val config = p.configurations.detachedConfiguration()
                p.dependencies.add(config.name, "org.spigotmc:spigot:$ver:jar:remapped-mojang")
                config.singleFile
            }))
        }
    }
}
