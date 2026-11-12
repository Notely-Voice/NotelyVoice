package com.module.notelycompose.notes.domain

class InsertPhotoUseCase(
    private val noteDataSource: NoteDataSource
) {
    suspend fun execute(
        noteId: Long,
        filePath: String,
        position: Long = 0L
    ): Long? {
        return noteDataSource.insertPhoto(
            noteId = noteId,
            filePath = filePath,
            position = position
        )
    }
}
