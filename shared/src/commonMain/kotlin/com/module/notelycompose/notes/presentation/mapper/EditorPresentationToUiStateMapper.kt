package com.module.notelycompose.notes.presentation.mapper

import com.module.notelycompose.notes.presentation.detail.model.EditorPresentationState
import com.module.notelycompose.notes.presentation.detail.model.PhotoPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.RecordingPathPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.RecordingPresentationModel
import com.module.notelycompose.notes.presentation.detail.model.TextFormatPresentationOption
import com.module.notelycompose.notes.presentation.detail.model.TextPresentationFormat
import com.module.notelycompose.notes.ui.detail.EditorUiState
import com.module.notelycompose.notes.ui.detail.PhotoUiModel
import com.module.notelycompose.notes.ui.detail.RecordingPathUiModel
import com.module.notelycompose.notes.ui.detail.RecordingUiModel
import com.module.notelycompose.notes.ui.detail.TextFormatUiOption
import com.module.notelycompose.notes.ui.detail.TextUiFormat

class EditorPresentationToUiStateMapper {
    fun mapToUiState(presentationState: EditorPresentationState): EditorUiState {
        return EditorUiState(
            content = presentationState.content,
            formats = presentationState.formats.map { mapToTextFormat(it) },
            textAlign = presentationState.textAlign,
            selectionSize = mapToTextFormatUiOption(presentationState.selectionSize),
            recording = mapToRecordingPathUi(presentationState.recording),
            recordings = presentationState.recordings.map { mapToRecordingUi(it) },
            photos = presentationState.photos.map { mapToPhotoUi(it) },
            isStarred = presentationState.starred,
            createdAt =  presentationState.createdAt,
            bodyTextSize = presentationState.bodyTextSize
        )
    }

    private fun mapToTextFormat(presentationFormat: TextPresentationFormat): TextUiFormat {
        return TextUiFormat(
            range = presentationFormat.range,
            isBold = presentationFormat.isBold,
            isItalic = presentationFormat.isItalic,
            isUnderline = presentationFormat.isUnderline,
            textSize = presentationFormat.textSize
        )
    }

    private fun mapToTextFormatUiOption(
        presentationOption: TextFormatPresentationOption
    ): TextFormatUiOption {
        return TextFormatUiOption(
            size = presentationOption.size
        )
    }

    private fun mapToRecordingPathUi(
        presentation: RecordingPathPresentationModel
    ) = RecordingPathUiModel(
        recordingPath = presentation.recordingPath,
        isRecordingExist = presentation.isRecordingExist
    )

    private fun mapToRecordingUi(
        presentation: RecordingPresentationModel
    ) = RecordingUiModel(
        id = presentation.id,
        filePath = presentation.filePath,
        transcription = presentation.transcription,
        durationMs = presentation.durationMs
    )

    private fun mapToPhotoUi(
        presentation: PhotoPresentationModel
    ) = PhotoUiModel(
        id = presentation.id,
        filePath = presentation.filePath
    )
}
