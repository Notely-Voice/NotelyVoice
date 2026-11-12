package com.module.notelycompose.notes.domain.mapper

import com.module.notelycompose.notes.data.model.NoteDataModel
import com.module.notelycompose.notes.data.model.PhotoDataModel
import com.module.notelycompose.notes.data.model.RecordingDataModel
import com.module.notelycompose.notes.data.model.TextAlignDataModel
import com.module.notelycompose.notes.domain.model.NoteDomainModel
import com.module.notelycompose.notes.domain.model.PhotoDomainModel
import com.module.notelycompose.notes.domain.model.RecordingDomainModel
import com.module.notelycompose.notes.domain.model.TextAlignDomainModel

class NoteDomainMapper(
    private val textFormatMapper: TextFormatMapper
) {
    fun mapToDomainModel(dataModel: NoteDataModel): NoteDomainModel {
        return NoteDomainModel(
            id = dataModel.id,
            title = dataModel.title,
            content = dataModel.content,
            starred = dataModel.starred,
            formatting = dataModel.formatting.map { textFormatMapper.mapToDomainModel(it) },
            textAlign = mapTextAlignToDomainModel(dataModel.textAlign),
            recordingPath = dataModel.recordingPath,
            recordings = dataModel.recordings.map { mapRecordingToDomainModel(it) },
            photos = dataModel.photos.map { mapPhotoToDomainModel(it) },
            createdAt = dataModel.createdAt
        )
    }

    fun mapToDataModel(domainModel: NoteDomainModel): NoteDataModel {
        return NoteDataModel(
            id = domainModel.id,
            title = domainModel.title,
            content = domainModel.content,
            starred = domainModel.starred,
            formatting = domainModel.formatting.map { textFormatMapper.mapToDataModel(it) },
            textAlign = mapTextAlignToDataModel(domainModel.textAlign),
            recordingPath = domainModel.recordingPath,
            recordings = domainModel.recordings.map { mapRecordingToDataModel(it) },
            photos = domainModel.photos.map { mapPhotoToDataModel(it) },
            createdAt = domainModel.createdAt
        )
    }

    fun mapRecordingToDomainModel(dataModel: RecordingDataModel): RecordingDomainModel {
        return RecordingDomainModel(
            id = dataModel.id,
            noteId = dataModel.noteId,
            filePath = dataModel.filePath,
            transcription = dataModel.transcription,
            durationMs = dataModel.durationMs,
            position = dataModel.position,
            createdAt = dataModel.createdAt
        )
    }

    fun mapRecordingToDataModel(domainModel: RecordingDomainModel): RecordingDataModel {
        return RecordingDataModel(
            id = domainModel.id,
            noteId = domainModel.noteId,
            filePath = domainModel.filePath,
            transcription = domainModel.transcription,
            durationMs = domainModel.durationMs,
            position = domainModel.position,
            createdAt = domainModel.createdAt
        )
    }

    fun mapPhotoToDomainModel(dataModel: PhotoDataModel): PhotoDomainModel {
        return PhotoDomainModel(
            id = dataModel.id,
            noteId = dataModel.noteId,
            filePath = dataModel.filePath,
            position = dataModel.position,
            createdAt = dataModel.createdAt
        )
    }

    fun mapPhotoToDataModel(domainModel: PhotoDomainModel): PhotoDataModel {
        return PhotoDataModel(
            id = domainModel.id,
            noteId = domainModel.noteId,
            filePath = domainModel.filePath,
            position = domainModel.position,
            createdAt = domainModel.createdAt
        )
    }

    fun mapTextAlignToDomainModel(textAlignDataModel: TextAlignDataModel): TextAlignDomainModel {
        return when (textAlignDataModel) {
            TextAlignDataModel.Left -> TextAlignDomainModel.Left
            TextAlignDataModel.Right -> TextAlignDomainModel.Right
            TextAlignDataModel.Center -> TextAlignDomainModel.Center
            TextAlignDataModel.Justify -> TextAlignDomainModel.Justify
            TextAlignDataModel.Start -> TextAlignDomainModel.Start
            TextAlignDataModel.End -> TextAlignDomainModel.End
        }
    }

    fun mapTextAlignToDataModel(textAlignDomainModel: TextAlignDomainModel): TextAlignDataModel {
        return when (textAlignDomainModel) {
            TextAlignDomainModel.Left -> TextAlignDataModel.Left
            TextAlignDomainModel.Right -> TextAlignDataModel.Right
            TextAlignDomainModel.Center -> TextAlignDataModel.Center
            TextAlignDomainModel.Justify -> TextAlignDataModel.Justify
            TextAlignDomainModel.Start -> TextAlignDataModel.Start
            TextAlignDomainModel.End -> TextAlignDataModel.End
        }
    }
}
