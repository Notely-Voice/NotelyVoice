package com.module.notelycompose.notes.domain

import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.model.RecordingDomainModel

class GetRecordingsByNoteId(
    private val noteDataSource: NoteDataSource,
    private val noteDomainMapper: NoteDomainMapper
) {
    fun execute(noteId: Long): List<RecordingDomainModel> {
        return noteDataSource.getRecordingsByNoteId(noteId)
            .map { noteDomainMapper.mapRecordingToDomainModel(it) }
    }
}
