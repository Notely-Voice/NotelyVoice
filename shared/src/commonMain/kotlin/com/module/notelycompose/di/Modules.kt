package com.module.notelycompose.di


import com.module.notelycompose.audio.presentation.AudioPlayerViewModel
import com.module.notelycompose.audio.presentation.AudioRecorderViewModel
import com.module.notelycompose.audio.presentation.mappers.AudioPlayerPresentationToUiMapper
import com.module.notelycompose.audio.presentation.mappers.AudioRecorderPresentationToUiMapper
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.notes.data.NoteSqlDelightDataSource
import com.module.notelycompose.notes.domain.DeleteNoteById
import com.module.notelycompose.notes.domain.DeletePhotoByIdUseCase
import com.module.notelycompose.notes.domain.DeleteRecordingByIdUseCase
import com.module.notelycompose.notes.domain.GetAllNotesUseCase
import com.module.notelycompose.notes.domain.GetLastNote
import com.module.notelycompose.notes.domain.GetNoteById
import com.module.notelycompose.notes.domain.GetPhotosByNoteId
import com.module.notelycompose.notes.domain.GetRecordingsByNoteId
import com.module.notelycompose.notes.domain.InsertNoteUseCase
import com.module.notelycompose.notes.domain.InsertPhotoUseCase
import com.module.notelycompose.notes.domain.InsertRecordingUseCase
import com.module.notelycompose.notes.domain.NoteDataSource
import com.module.notelycompose.notes.domain.SearchNotesUseCase
import com.module.notelycompose.notes.domain.UpdateNoteUseCase
import com.module.notelycompose.notes.domain.UpdateRecordingTranscriptionUseCase
import com.module.notelycompose.notes.domain.mapper.NoteDomainMapper
import com.module.notelycompose.notes.domain.mapper.TextFormatMapper
import com.module.notelycompose.audio.presentation.AudioImportViewModel
import com.module.notelycompose.export.presentation.ExportSelectionViewModel
import com.module.notelycompose.modelDownloader.ModelSelection
import com.module.notelycompose.notes.presentation.detail.NoteDetailScreenViewModel
import com.module.notelycompose.notes.presentation.detail.PhotoImportViewModel
import com.module.notelycompose.notes.presentation.detail.TextEditorViewModel
import com.module.notelycompose.notes.presentation.helpers.TextEditorHelper
import com.module.notelycompose.notes.presentation.list.NoteListViewModel
import com.module.notelycompose.notes.presentation.list.mapper.NotesFilterMapper
import com.module.notelycompose.notes.presentation.mapper.EditorPresentationToUiStateMapper
import com.module.notelycompose.notes.presentation.mapper.NotePresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextAlignPresentationMapper
import com.module.notelycompose.notes.presentation.mapper.TextFormatPresentationMapper
import com.module.notelycompose.onboarding.data.PreferencesRepository
import com.module.notelycompose.onboarding.presentation.OnboardingViewModel
import com.module.notelycompose.platform.presentation.PlatformViewModel
import com.module.notelycompose.transcription.TranscriptionViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


internal expect val platformModule: Module

val appModule = module {

    single<NoteDataSource> {
        NoteSqlDelightDataSource(
            database = NoteDatabase(get())
        )
    }

    factory { ModelSelection(get()) }

}

val mapperModule = module {
    single { EditorPresentationToUiStateMapper() }
    single { AudioPlayerPresentationToUiMapper() }
    single { AudioRecorderPresentationToUiMapper() }
    single { NoteDomainMapper(get()) }
    single { TextFormatMapper() }
    single { NotesFilterMapper() }
    single { NotePresentationMapper() }
    single { TextFormatPresentationMapper() }
    single { TextAlignPresentationMapper() }
    single { TextEditorHelper() }
}
val repositoryModule = module {
    singleOf(::PreferencesRepository)
}

val viewModelModule = module {
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::NoteListViewModel)
    viewModelOf(::PlatformViewModel)
    viewModelOf(::TranscriptionViewModel)
    viewModelOf(::TextEditorViewModel)
    viewModelOf(::NoteDetailScreenViewModel)
    viewModelOf(::ModelDownloaderViewModel)
    viewModelOf(::AudioRecorderViewModel)
    viewModelOf(::AudioPlayerViewModel)
    viewModelOf(::AudioImportViewModel)
    viewModelOf(::PhotoImportViewModel)
    viewModelOf(::ExportSelectionViewModel)
}

val useCaseModule = module {
    factory { DeleteNoteById(get()) }
    factory { GetAllNotesUseCase(get(), get()) }
    factory { GetLastNote(get(), get()) }
    factory { GetNoteById(get(), get()) }
    factory { InsertNoteUseCase(get(), get(), get()) }
    factory { SearchNotesUseCase(get(), get()) }
    factory { UpdateNoteUseCase(get(), get(), get()) }
    factory { GetRecordingsByNoteId(get(), get()) }
    factory { InsertRecordingUseCase(get()) }
    factory { DeleteRecordingByIdUseCase(get()) }
    factory { UpdateRecordingTranscriptionUseCase(get()) }
    factory { GetPhotosByNoteId(get(), get()) }
    factory { InsertPhotoUseCase(get()) }
    factory { DeletePhotoByIdUseCase(get()) }
}
