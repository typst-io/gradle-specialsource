package io.typecraft.gradlesource.spigot

import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.AbstractArchiveTask

/**
 * Configuration for remap mojang to spigot for distribution.
 *
 * @since 1.0
 * @see [SpigotRemapPlugin][io.typecraft.gradlesource.spigot.SpigotRemapPlugin]
 */
abstract class SpigotRemapExtension {
    /**
     * Inputs a jar task [AbstractArchiveTask]. Mandatory.
     *
     * Example:
     *
     * ```kotlin
     * sourceJarTask.set(tasks.jar)
     * ```
     *
     * As this just a simple and convenient input, you can configure the `RemapTask` in detail.
     * See [SpigotRemapPlugin] to check what tasks are.
     */
    abstract val sourceJarTask: Property<AbstractArchiveTask>

    /**
     * Inputs a spigot version. Mandatory.
     *
     * Example:
     *
     * ```kotlin
     * spigotVersion.set("1.16.5") // 1.16.5-R0.1-SNAPSHOT
     * ```
     *
     * This used for get the mapping files in maven local repository.
     */
    abstract val spigotVersion: Property<String>

    val spigotVersionExact: Provider<String> = spigotVersion.map { ver ->
        val pieces = ver.split("-")
        val r = pieces.getOrNull(1) ?: "R0.1"
        val tag = pieces.getOrNull(2) ?: "SNAPSHOT"
        "1.16.5-${r}-${tag}"
    }
}
