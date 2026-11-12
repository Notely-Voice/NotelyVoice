package com.module.notelycompose.notes.data.model

import kotlinx.datetime.LocalDateTime

data class RecordingDataModel(
    val id: Long,
    val noteId: Long,
    val filePath: String,
    val transcription: String,
    val durationMs: Long,
    val position: Long,
    val createdAt: LocalDateTime
)
