package com.module.notelycompose.notes.domain

import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.model.PhotoDomainModel

class GetPhotosByNoteId(
    private val noteDataSource: NoteDataSource,
    private val noteDomainMapper: NoteDomainMapper
) {
    fun execute(noteId: Long): List<PhotoDomainModel> {
        return noteDataSource.getPhotosByNoteId(noteId)
            .map { noteDomainMapper.mapPhotoToDomainModel(it) }
    }
}
