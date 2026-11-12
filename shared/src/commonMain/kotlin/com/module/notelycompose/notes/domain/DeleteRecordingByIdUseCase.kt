package com.module.notelycompose.notes.domain

class DeleteRecordingByIdUseCase(
    private val noteDataSource: NoteDataSource
) {
    suspend fun execute(recordingId: Long) {
        noteDataSource.deleteRecordingById(recordingId)
    }
}
