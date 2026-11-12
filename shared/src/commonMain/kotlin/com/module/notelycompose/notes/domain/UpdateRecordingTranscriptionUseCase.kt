package com.module.notelycompose.notes.domain

class UpdateRecordingTranscriptionUseCase(
    private val noteDataSource: NoteDataSource
) {
    suspend fun execute(recordingId: Long, transcription: String) {
        noteDataSource.updateRecordingTranscription(recordingId, transcription)
    }
}
