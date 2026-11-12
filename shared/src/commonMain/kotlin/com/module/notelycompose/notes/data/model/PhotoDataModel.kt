package com.module.notelycompose.notes.data.model

import kotlinx.datetime.LocalDateTime

data class PhotoDataModel(
    val id: Long,
    val noteId: Long,
    val filePath: String,
    val position: Long,
    val createdAt: LocalDateTime
)
