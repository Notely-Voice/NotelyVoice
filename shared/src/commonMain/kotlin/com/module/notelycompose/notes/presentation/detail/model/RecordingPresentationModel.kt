package com.module.notelycompose.notes.presentation.detail.model

data class RecordingPresentationModel(
    val id: Long = 0L,
    val filePath: String = "",
    val transcription: String = "",
    val durationMs: Long = 0L
)
