package io.typst.gradlesource

data class ArchiveName(
    val baseName: String,
    val appendix: String,
    val version: String,
    val classifier: String,
    val extension: String
) {
    fun toFileName(): String =
        listOf(
            baseName,
            appendix,
            version,
            classifier,
            extension
        ).filter {
            it.isNotEmpty()
        }.joinToString("-") + if (extension.isNotEmpty()) {
            ".${extension}"
        } else ""

    companion object {
        @JvmStatic
        fun jar(name: String, version: String, classifier: String): ArchiveName =
            ArchiveName(name, "", version, classifier, "jar")

        @JvmStatic
        fun simpleJar(name: String, version: String): ArchiveName =
            jar(name, version, "")
    }
}
