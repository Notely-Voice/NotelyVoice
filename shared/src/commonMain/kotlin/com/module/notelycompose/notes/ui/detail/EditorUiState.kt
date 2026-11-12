package com.module.notelycompose.notes.ui.detail

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import com.module.notelycompose.notes.extension.TEXT_SIZE_BODY
import com.module.notelycompose.notes.extension.TEXT_SIZE_HEADING
import com.module.notelycompose.notes.extension.TEXT_SIZE_SUBHEADING
import com.module.notelycompose.notes.extension.TEXT_SIZE_TITLE

data class EditorUiState(
    val content: TextFieldValue = TextFieldValue(""),
    val formats: List<TextUiFormat> = emptyList(),
    val textAlign: TextAlign = TextAlign.Left,
    val selectionSize: TextFormatUiOption = TextUiFormats.Body,
    val recording: RecordingPathUiModel,
    val recordings: List<RecordingUiModel> = emptyList(),
    val photos: List<PhotoUiModel> = emptyList(),
    val isStarred: Boolean,
    val createdAt: String,
    val bodyTextSize: Float
)

data class TextUiFormat(
    val range: IntRange,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textSize: Float? = null
)

data class TextFormatUiOption(
    val size: Float
)

object TextUiFormats {
    val Title = TextFormatUiOption(TEXT_SIZE_TITLE)
    val Heading = TextFormatUiOption(TEXT_SIZE_HEADING)
    val SubHeading = TextFormatUiOption(TEXT_SIZE_SUBHEADING)
    val Body = TextFormatUiOption(TEXT_SIZE_BODY)
    val NoSelection = TextFormatUiOption(0f)
}

data class RecordingPathUiModel(
    val recordingPath: String,
    val isRecordingExist: Boolean
)

data class RecordingUiModel(
    val id: Long,
    val filePath: String,
    val transcription: String,
    val durationMs: Long
)

data class PhotoUiModel(
    val id: Long,
    val filePath: String
)
