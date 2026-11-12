package com.module.notelycompose.notes.domain

class DeletePhotoByIdUseCase(
    private val noteDataSource: NoteDataSource
) {
    suspend fun execute(photoId: Long) {
        noteDataSource.deletePhotoById(photoId)
    }
}
