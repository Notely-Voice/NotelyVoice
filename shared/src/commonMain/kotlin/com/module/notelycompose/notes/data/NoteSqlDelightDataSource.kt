package com.module.notelycompose.notes.data

import com.module.notelycompose.core.CommonFlow
import com.module.notelycompose.core.DateTimeUtil
import com.module.notelycompose.core.toCommonFlow
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.notes.data.model.NoteDataModel
import com.module.notelycompose.notes.data.model.PhotoDataModel
import com.module.notelycompose.notes.data.model.RecordingDataModel
import com.module.notelycompose.notes.data.model.TextAlignDataModel
import com.module.notelycompose.notes.data.model.TextFormatDataModel
import com.module.notelycompose.notes.domain.NoteDataSource
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val STARRED = 1L
private const val NOT_STARRED = 0L

class NoteSqlDelightDataSource(
    private val database: NoteDatabase
) : NoteDataSource {
    private val queries = database.noteQueries
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    override suspend fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    ): Long? {
        queries.insertNote(
            title = title,
            content = content,
            starred = starred.starredToDigit(),
            formatting = json.encodeToString(formatting),
            text_align = textAlign.toString(),
            recording_path = recordingPath,
            created_at = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
        return getLastNoteId()
    }

    override suspend fun updateNote(
        id: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextFormatDataModel>,
        textAlign: TextAlignDataModel,
        recordingPath: String
    ) {
        queries.updateNote(
            id = id,
            title = title,
            content = content,
            starred = starred.starredToDigit(),
            formatting = json.encodeToString(formatting),
            text_align = textAlign.toString(),
            recording_path = recordingPath,
            created_at = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
    }

    override fun getNotes(): CommonFlow<List<NoteDataModel>> {
        return queries
            .getAllNotes()
            .asFlow()
            .mapToList()
            .map { notes ->
                notes.map { note ->
                    note.toNoteDataModel(json)
                }
            }.toCommonFlow()
    }

    override fun getStarredNotes(): CommonFlow<List<NoteDataModel>> {
        return queries
            .getNotesByStarred(STARRED)
            .asFlow()
            .mapToList()
            .map { notes ->
                notes.map { note ->
                    note.toNoteDataModel(json)
                }
            }.toCommonFlow()
    }

    override fun getVoiceNotes(): CommonFlow<List<NoteDataModel>> {
        return queries
            .getNotesWithAnyRecording()
            .asFlow()
            .mapToList()
            .map { notes ->
                notes.map { note ->
                    note.toNoteDataModel(json)
                }
            }.toCommonFlow()
    }

    override fun getNotesByKeyword(keyword: String): CommonFlow<List<NoteDataModel>> {
        return queries
            .getNotesByContent(keyword)
            .asFlow()
            .mapToList()
            .map { notes ->
                notes.map { note ->
                    note.toNoteDataModel(json)
                }
            }.toCommonFlow()
    }

    override fun getNoteById(id: Long): NoteDataModel? {
        return queries
            .getNoteById(id)
            .executeAsOneOrNull()
            ?.toNoteDataModel(
                json,
                recordings = getRecordingsByNoteId(id),
                photos = getPhotosByNoteId(id)
            )
    }

    override fun getLastNote(): NoteDataModel? {
        return queries
            .getLastNote()
            .executeAsOneOrNull()?.toNoteDataModel(json)
    }

    override fun getLastNoteId(): Long? {
        return queries
            .getLastNoteId()
            .executeAsOneOrNull()
    }

    override suspend fun deleteNoteById(id: Long) {
        queries.deleteRecordingsByNoteId(id)
        queries.deletePhotosByNoteId(id)
        queries.deleteNoteById(id)
    }

    override suspend fun insertRecording(
        noteId: Long,
        filePath: String,
        transcription: String,
        durationMs: Long,
        position: Long
    ): Long? {
        queries.insertRecording(
            noteId = noteId,
            filePath = filePath,
            transcription = transcription,
            durationMs = durationMs,
            position = position,
            createdAt = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
        return queries.getLastRecordingId().executeAsOneOrNull()
    }

    override suspend fun updateRecordingTranscription(id: Long, transcription: String) {
        queries.updateRecordingTranscription(transcription = transcription, id = id)
    }

    override suspend fun updateRecordingDuration(id: Long, durationMs: Long) {
        queries.updateRecordingDuration(durationMs = durationMs, id = id)
    }

    override suspend fun deleteRecordingById(id: Long) {
        queries.deleteRecordingById(id)
    }

    override suspend fun deleteRecordingsByNoteId(noteId: Long) {
        queries.deleteRecordingsByNoteId(noteId)
    }

    override fun getRecordingsByNoteId(noteId: Long): List<RecordingDataModel> {
        return queries
            .getRecordingsByNoteId(noteId)
            .executeAsList()
            .map { it.toRecordingDataModel() }
    }

    override suspend fun insertPhoto(
        noteId: Long,
        filePath: String,
        position: Long
    ): Long? {
        queries.insertPhoto(
            noteId = noteId,
            filePath = filePath,
            position = position,
            createdAt = DateTimeUtil.toEpochMilli(DateTimeUtil.now())
        )
        return queries.getLastPhotoId().executeAsOneOrNull()
    }

    override suspend fun deletePhotoById(id: Long) {
        queries.deletePhotoById(id)
    }

    override suspend fun deletePhotosByNoteId(noteId: Long) {
        queries.deletePhotosByNoteId(noteId)
    }

    override fun getPhotosByNoteId(noteId: Long): List<PhotoDataModel> {
        return queries
            .getPhotosByNoteId(noteId)
            .executeAsList()
            .map { it.toPhotoDataModel() }
    }
}

fun Boolean.starredToDigit(): Long {
    return if(this) {
        STARRED
    } else {
        NOT_STARRED
    }
}
