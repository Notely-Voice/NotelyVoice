package com.module.notelycompose.notes.domain

class InsertRecordingUseCase(
    private val noteDataSource: NoteDataSource
) {
    suspend fun execute(
        noteId: Long,
        filePath: String,
        transcription: String = "",
        durationMs: Long = 0L,
        position: Long = 0L
    ): Long? {
        return noteDataSource.insertRecording(
            noteId = noteId,
            filePath = filePath,
            transcription = transcription,
            durationMs = durationMs,
            position = position
        )
    }
}
