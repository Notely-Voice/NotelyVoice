package com.module.notelycompose.notes.domain

import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.notes.data.model.NoteDataModel
import com.module.notelycompose.notes.data.model.PhotoDataModel
import com.module.notelycompose.notes.data.model.RecordingDataModel
import com.module.notelycompose.notes.data.model.TextAlignDataModel
import com.module.notelycompose.notes.data.model.TextFormatDataModel
import com.module.notelycompose.notes.domain.model.Note


interface NoteDataSource {
    suspend fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    ): Long?

    suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    )

    fun getNotes(): CommonFlow<List<NoteDataModel>>
    fun getStarredNotes(): CommonFlow<List<NoteDataModel>>
    fun getVoiceNotes(): CommonFlow<List<NoteDataModel>>
    fun getNotesByKeyword(keyword: String): CommonFlow<List<NoteDataModel>>
    fun getNoteById(id: Long): NoteDataModel?
    fun getLastNote(): NoteDataModel?
    fun getLastNoteId(): Long?
    suspend fun deleteNoteById(id: Long)

    // Multiple recordings per note
    suspend fun insertRecording(
        noteId: Long,
        filePath: String,
        transcription: String,
        durationMs: Long,
        position: Long
    ): Long?

    suspend fun updateRecordingTranscription(id: Long, transcription: String)
    suspend fun updateRecordingDuration(id: Long, durationMs: Long)
    suspend fun deleteRecordingById(id: Long)
    suspend fun deleteRecordingsByNoteId(noteId: Long)
    fun getRecordingsByNoteId(noteId: Long): List<RecordingDataModel>

    // Multiple photos per note
    suspend fun insertPhoto(
        noteId: Long,
        filePath: String,
        position: Long
    ): Long?

    suspend fun deletePhotoById(id: Long)
    suspend fun deletePhotosByNoteId(noteId: Long)
    fun getPhotosByNoteId(noteId: Long): List<PhotoDataModel>
}
