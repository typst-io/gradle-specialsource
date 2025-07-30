package io.typst.gradlesource

import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Java bytecode remap task.
 *
 * This task uses and following [md-5's SpecialSource.](https://github.com/md-5/specialsource)
 *
 * Command-line arguments: [SpecialSource.java](https://github.com/md-5/SpecialSource/blob/master/src/main/java/net/md_5/specialsource/SpecialSource.java#L63)
 *
 * @since 1.0
 */
abstract class RemapTask : DefaultTask() {
    /**
     * Inputs a source jar file as `RegularFile`. Mandatory.
     *
     * Corresponding cmd-line args:
     *
     * ```-i ${inJarFile}```
     */
    @get:InputFile
    @get:SkipWhenEmpty
    abstract val inJarFile: RegularFileProperty

    /**
     * Inputs a destination dir. Mandatory.
     */
    @get:OutputFile
    abstract val outJarFile: RegularFileProperty

    /**
     * Input a mapping file as `RegularFile`. Mandatory.
     *
     * Corresponding cmd-line args:
     *
     * ```-srg-in ${mappingFile}```
     */
    @get:InputFile
    abstract val mappingFile: RegularFileProperty

    /**
     * Input whether reverse or not. Defaults to `false`.
     *
     * Corresponding cmd-line args:
     *
     * ```-reverse```
     */
    @get:Input
    @get:Optional
    abstract val reverse: Property<Boolean>

    @TaskAction
    fun remap() {
        val mapping = JarMapping()
        val rev = reverse.getOrElse(false)
        val mappingPath = mappingFile.asFile.get().absolutePath
        mapping.loadMappings(mappingPath, rev, false, null, null)
        val jarMap = JarRemapper(null, mapping, null)
        Jar.init(inJarFile.asFile.get()).use { jar ->
            jarMap.remapJar(jar, outJarFile.get().asFile)
        }
    }
}
