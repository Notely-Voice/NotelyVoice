package com.module.notelycompose.notes.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class NoteDataModel(
    val id: Long,
    val title: String,
    val content: String,
    val starred: Boolean,
    val formatting: List<TextFormatDataModel>,
    val textAlign: TextAlignDataModel,
    val recordingPath: String,
    val recordings: List<RecordingDataModel> = emptyList(),
    val photos: List<PhotoDataModel> = emptyList(),
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
)
