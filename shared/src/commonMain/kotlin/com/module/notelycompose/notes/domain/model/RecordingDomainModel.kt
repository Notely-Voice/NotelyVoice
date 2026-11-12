package com.module.notelycompose.notes.domain.model

import kotlinx.datetime.LocalDateTime

data class RecordingDomainModel(
    val id: Long,
    val noteId: Long,
    val filePath: String,
    val transcription: String,
    val durationMs: Long,
    val position: Long,
    val createdAt: LocalDateTime
)
