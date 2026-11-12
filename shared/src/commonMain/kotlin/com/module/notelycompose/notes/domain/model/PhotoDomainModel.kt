package com.module.notelycompose.notes.domain.model

import kotlinx.datetime.LocalDateTime

data class PhotoDomainModel(
    val id: Long,
    val noteId: Long,
    val filePath: String,
    val position: Long,
    val createdAt: LocalDateTime
)
