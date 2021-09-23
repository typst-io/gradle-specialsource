package io.typecraft.gradlesource

import org.gradle.api.tasks.bundling.AbstractArchiveTask

internal fun archiveNameFromTask(x: AbstractArchiveTask): ArchiveName =
    ArchiveName(
        x.archiveBaseName.orNull ?: "",
        x.archiveAppendix.orNull ?: "",
        x.archiveVersion.orNull ?: "",
        x.archiveClassifier.orNull ?: "",
        x.archiveExtension.orNull ?: ""
    )
