package com.module.notelycompose.notes.presentation.detail

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import audio.utils.deleteFile
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.DeletePhotoByIdUseCase
import com.module.notelycompose.notes.domain.DeleteRecordingByIdUseCase
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.GetPhotosByNoteId
import com.module.notelycompose.notes.domain.GetRecordingsByNoteId
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.InsertPhotoUseCase
import com.module.notelycompose.notes.domain.InsertRecordingUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.UpdateRecordingTranscriptionUseCase
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.detail.model.PhotoPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.RecordingPathPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.RecordingPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.helpers.formattedDate
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.notes.ui.detail.EditorUiState
import com.module.notelycompose.onboarding.data.PreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val ID_NOT_SET = 0L

class TextEditorViewModel(
    private val getNoteByIdUseCase: GetNoteById,
    private val insertNoteUseCase: InsertNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteById,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val getLastNoteUseCase: GetLastNote,
    private val getRecordingsByNoteIdUseCase: GetRecordingsByNoteId,
    private val insertRecordingUseCase: InsertRecordingUseCase,
    private val deleteRecordingByIdUseCase: DeleteRecordingByIdUseCase,
    private val updateRecordingTranscriptionUseCase: UpdateRecordingTranscriptionUseCase,
    private val getPhotosByNoteIdUseCase: GetPhotosByNoteId,
    private val insertPhotoUseCase: InsertPhotoUseCase,
    private val deletePhotoByIdUseCase: DeletePhotoByIdUseCase,
    private val editorPresentationToUiStateMapper: EditorPresentationToUiStateMapper,
    private val textFormatPresentationMapper: TextFormatPresentationMapper,
    private val textAlignPresentationMapper: TextAlignPresentationMapper,
    private val textEditorHelper: TextEditorHelper,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _editorPresentationState = MutableStateFlow(EditorPresentationState())
    val editorPresentationState: StateFlow<EditorPresentationState> = _editorPresentationState
    private var _currentNoteId = MutableStateFlow<Long?>(ID_NOT_SET)

    internal val currentNoteId: StateFlow<Long?> = _currentNoteId.asStateFlow()
    private val _noteIdTrigger = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            _noteIdTrigger
                .filterNotNull()
                .take(1)
                .collect { id ->
                    val note = getNoteByIdUseCase.execute(id)
                    note?.let { retrievedNote ->
                        processNote(retrievedNote)
                        _currentNoteId.value = id
                    }
                }
        }
    }

    private fun processNote(retrievedNote: NoteDomainModel) {
        viewModelScope.launch {
            var recordings = getRecordingsByNoteIdUseCase.execute(retrievedNote.id)

            // Fallback for notes that still hold only the legacy single recording path
            if (recordings.isEmpty() && retrievedNote.recordingPath.isNotEmpty()) {
                val insertedId = insertRecordingUseCase.execute(
                    noteId = retrievedNote.id,
                    filePath = retrievedNote.recordingPath,
                    position = 0L
                )
                if (insertedId != null) {
                    recordings = getRecordingsByNoteIdUseCase.execute(retrievedNote.id)
                }
            }

            val photos = getPhotosByNoteIdUseCase.execute(retrievedNote.id)

            loadNote(
                content = retrievedNote.content,
                formats = retrievedNote.formatting.map {
                    textFormatPresentationMapper.mapToPresentationModel(it)
                },
                textAlign = textAlignPresentationMapper.mapToComposeTextAlign(
                    retrievedNote.textAlign
                ),
                recordingPath = retrievedNote.recordingPath,
                recordings = recordings.map {
                    RecordingPresentationModel(
                        id = it.id,
                        filePath = it.filePath,
                        transcription = it.transcription,
                        durationMs = it.durationMs
                    )
                },
                photos = photos.map {
                    PhotoPresentationModel(
                        id = it.id,
                        filePath = it.filePath
                    )
                },
                starred = retrievedNote.starred,
                createdAt = getFormattedDate(retrievedNote.createdAt),
                bodyTextSize = preferencesRepository.getBodyTextSize().first()
            )
        }
    }

    fun onGetNoteById(id: String) {
        _noteIdTrigger.value = id.toLong()
    }

    private fun getLastNote() = getLastNoteUseCase.execute()

    fun onUpdateContent(newContent: TextFieldValue) {
        updateContent(newContent)
        createOrUpdateEvent(
            title = newContent.text,
            content = newContent.text,
            starred = _editorPresentationState.value.starred,
            formatting = _editorPresentationState.value.formats,
            textAlign = _editorPresentationState.value.textAlign,
            recordingPath = _editorPresentationState.value.recording.recordingPath,
        )
    }

    /**
     * Adds a new audio recording to the current note. Each recording keeps its own
     * transcription, so a note can contain several audio files at once.
     */
    fun onUpdateRecordingPath(recordingPath: String) {
        if (recordingPath.isEmpty()) return
        viewModelScope.launch {
            val noteId = ensureNoteExists()
            if (noteId == ID_NOT_SET) return@launch
            val position = _editorPresentationState.value.recordings.size.toLong()
            val recordingId = insertRecordingUseCase.execute(
                noteId = noteId,
                filePath = recordingPath,
                position = position
            ) ?: return@launch
            _editorPresentationState.update { state ->
                val updatedRecordings = state.recordings + RecordingPresentationModel(
                    id = recordingId,
                    filePath = recordingPath
                )
                state.copy(
                    recordings = updatedRecordings,
                    recording = recordingPath(updatedRecordings.first().filePath)
                )
            }
        }
    }

    fun onDeleteRecording(recordingId: Long) {
        val recording = _editorPresentationState.value.recordings
            .firstOrNull { it.id == recordingId } ?: return
        viewModelScope.launch {
            deleteFile(recording.filePath)
            deleteRecordingByIdUseCase.execute(recordingId)
            _editorPresentationState.update { state ->
                val updatedRecordings = state.recordings.filterNot { it.id == recordingId }
                state.copy(
                    recordings = updatedRecordings,
                    recording = recordingPath(updatedRecordings.firstOrNull()?.filePath ?: ""),
                    transcriptionRecordingId = state.transcriptionRecordingId
                        .takeIf { it != recordingId }
                )
            }
        }
    }

    fun onUpdateRecordingTranscription(recordingId: Long, transcription: String) {
        viewModelScope.launch {
            updateRecordingTranscriptionUseCase.execute(recordingId, transcription)
            _editorPresentationState.update { state ->
                state.copy(
                    recordings = state.recordings.map { recording ->
                        if (recording.id == recordingId) {
                            recording.copy(transcription = transcription)
                        } else {
                            recording
                        }
                    }
                )
            }
        }
    }

    /**
     * Marks which recording the transcription screen should work with.
     * Pass null to fall back to the legacy primary recording.
     */
    fun onSelectRecordingForTranscription(recordingId: Long?) {
        _editorPresentationState.update { it.copy(transcriptionRecordingId = recordingId) }
    }

    /**
     * File path of the recording selected for transcription, or the legacy primary
     * recording path when nothing specific was selected.
     */
    fun getRecordingPathForTranscription(): String {
        val state = _editorPresentationState.value
        val selected = state.transcriptionRecordingId?.let { selectedId ->
            state.recordings.firstOrNull { it.id == selectedId }
        }
        return selected?.filePath ?: state.recording.recordingPath
    }

    /**
     * Id of the recording selected for transcription, or null when the legacy
     * primary recording flow is used.
     */
    fun getSelectedRecordingIdForTranscription(): Long? {
        return _editorPresentationState.value.transcriptionRecordingId
    }

    /**
     * Adds several photos to the current note at once.
     */
    fun onAddPhotos(photoPaths: List<String>) {
        if (photoPaths.isEmpty()) return
        viewModelScope.launch {
            val noteId = ensureNoteExists()
            if (noteId == ID_NOT_SET) return@launch
            photoPaths.forEach { path ->
                val position = _editorPresentationState.value.photos.size.toLong()
                val photoId = insertPhotoUseCase.execute(
                    noteId = noteId,
                    filePath = path,
                    position = position
                ) ?: return@forEach
                _editorPresentationState.update { state ->
                    state.copy(
                        photos = state.photos + PhotoPresentationModel(
                            id = photoId,
                            filePath = path
                        )
                    )
                }
            }
        }
    }

    fun onDeletePhoto(photoId: Long) {
        val photo = _editorPresentationState.value.photos
            .firstOrNull { it.id == photoId } ?: return
        viewModelScope.launch {
            deleteFile(photo.filePath)
            deletePhotoByIdUseCase.execute(photoId)
            _editorPresentationState.update { state ->
                state.copy(
                    photos = state.photos.filterNot { it.id == photoId }
                )
            }
        }
    }

    /**
     * Legacy entry point: deletes the primary (first) recording of the note.
     */
    fun onDeleteRecord() {
        val firstRecording = _editorPresentationState.value.recordings.firstOrNull()
        if (firstRecording != null) {
            onDeleteRecording(firstRecording.id)
            return
        }
        deleteFile(_editorPresentationState.value.recording.recordingPath)
        _editorPresentationState.update {
            it.copy(
                recording = recordingPath(/*reset record path */"")
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun recordingPath(recordingPath: String) = RecordingPathPresentationModel(
        recordingPath = recordingPath,
        isRecordingExist = recordingPath.isNotEmpty()
    )

    private suspend fun ensureNoteExists(): Long {
        val existingId = _currentNoteId.value
        if (existingId != null && existingId != ID_NOT_SET) return existingId
        val content = _editorPresentationState.value.content.text
        val newId = insertNoteUseCase.execute(
            title = content,
            content = content,
            starred = _editorPresentationState.value.starred,
            formatting = _editorPresentationState.value.formats.map {
                textFormatPresentationMapper.mapToDomainModel(it)
            },
            textAlign = textAlignPresentationMapper.mapToDomainModel(
                _editorPresentationState.value.textAlign
            ),
            recordingPath = ""
        ) ?: ID_NOT_SET
        _currentNoteId.value = newId
        return newId
    }

    private fun loadNote(
        content: String,
        formats: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String,
        recordings: List<RecordingPresentationModel>,
        photos: List<PhotoPresentationModel>,
        starred: Boolean,
        createdAt: String,
        bodyTextSize: Float
    ) {
        _editorPresentationState.update {
            it.copy(
                content = TextFieldValue(content),
                formats = formats,
                textAlign = textAlign,
                recording = recordingPath(
                    recordings.firstOrNull()?.filePath ?: recordingPath
                ),
                recordings = recordings,
                photos = photos,
                starred = starred,
                createdAt = createdAt,
                bodyTextSize = bodyTextSize
            )
        }
    }

    fun onGetUiState(presentationState: EditorPresentationState): EditorUiState {
        return editorPresentationToUiStateMapper.mapToUiState(presentationState)
    }

    private fun insertNote(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch {
            _currentNoteId.value = insertNoteUseCase.execute(
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = recordingPath
            )
        }
    }

    private fun updateNote(
        noteId: Long,
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        viewModelScope.launch {
            updateNoteUseCase.execute(
                id = noteId,
                title = title,
                content = content,
                starred = starred,
                formatting = formatting.map { textFormatPresentationMapper.mapToDomainModel(it) },
                textAlign = textAlignPresentationMapper.mapToDomainModel(textAlign),
                recordingPath = recordingPath
            )
        }
    }

    fun onDeleteNote() {
        _currentNoteId.value?.let { noteId ->
            _editorPresentationState.value.recordings.forEach { recording ->
                deleteFile(recording.filePath)
            }
            _editorPresentationState.value.photos.forEach { photo ->
                deleteFile(photo.filePath)
            }
            val path = _editorPresentationState.value.recording.recordingPath
            deleteFile(filePath = path)
            deleteNote(id = noteId)
        }
    }

    private fun deleteNote(id: Long) {
        viewModelScope.launch {
            deleteNoteUseCase.execute(id)
        }
    }

    fun onToggleStar() {
        val starred = _editorPresentationState.value.starred
        _editorPresentationState.update {
            it.copy(
                starred = !starred
            )
        }
        onUpdateContent(newContent = _editorPresentationState.value.content)
    }

    private fun getFormattedDate(
        createdAt: LocalDateTime = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
    ): String {
        return createdAt.formattedDate()
    }

    private fun createOrUpdateEvent(
        title: String,
        content: String,
        starred: Boolean,
        formatting: List<TextPresentationFormat>,
        textAlign: TextAlign,
        recordingPath: String
    ) {
        val currentNoteId = _currentNoteId.value
        when {
            currentNoteId != null && currentNoteId != ID_NOT_SET -> {
                updateNote(
                    noteId = currentNoteId,
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }

            else -> {
                insertNote(
                    title = title,
                    content = content,
                    starred = starred,
                    formatting = formatting,
                    textAlign = textAlign,
                    recordingPath = recordingPath
                )
            }
        }
    }

    private fun updateContent(newContent: TextFieldValue) {
        viewModelScope.launch(Dispatchers.Default) {
            textEditorHelper.updateContent(
                newContent = newContent,
                currentState = _editorPresentationState.value,
                getFormattedDate = { getFormattedDate() },
                updateState = { newState ->
                    _editorPresentationState.update { newState }
                },
                bodyTextSize = preferencesRepository.getBodyTextSize().first()
            )
        }
    }

    fun onToggleBold() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isBold = !it.isBold) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun onToggleItalic() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isItalic = !it.isItalic) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun setTextSize(size: Float) {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(textSize = size) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    fun onToggleUnderline() {
        normaliseSelection()
        textEditorHelper.toggleFormat(
            currentState = _editorPresentationState.value,
            transform = { it.copy(isUnderline = !it.isUnderline) },
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
        refreshSelection()
    }

    private fun refreshSelection() {
        textEditorHelper.refreshSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    private fun normaliseSelection() {
        textEditorHelper.normaliseSelection(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }

    fun onSetAlignment(alignment: TextAlign) {
        _editorPresentationState.update { it.copy(textAlign = alignment) }
        val content = _editorPresentationState.value.content
        val formats = _editorPresentationState.value.formats
        val textAlign = _editorPresentationState.value.textAlign
        val starred = _editorPresentationState.value.starred
        val recordingPath = _editorPresentationState.value.recording.recordingPath
        if (content.text.isNotEmpty()) {
            createOrUpdateEvent(
                title = content.text,
                content = content.text,
                starred = starred,
                formatting = formats,
                textAlign = textAlign,
                recordingPath = recordingPath
            )
        }
    }

    fun onToggleBulletList() {
        textEditorHelper.toggleBulletList(
            currentState = _editorPresentationState.value,
            updateState = { newState ->
                _editorPresentationState.update { newState }
            }
        )
    }
}
