package com.module.notelycompose.di

import android.app.Application
import com.module.notelycompose.FileSaverHandler
import com.module.notelycompose.FileSaverLauncherHolder
import com.module.notelycompose.FolderPickerHandler
import com.module.notelycompose.FolderPickerLauncherHolder
import com.module.notelycompose.audio.domain.AudioRecorderInteractor
import com.module.notelycompose.audio.domain.AudioRecorderInteractorImpl
import com.module.notelycompose.audio.domain.SaveAudioNoteInteractor
import com.module.notelycompose.audio.domain.SaveAudioNoteInteractorImpl
import com.module.notelycompose.dailyexport.AndroidDailyExportFileWriter
import com.module.notelycompose.dailyexport.AndroidDailyExportScheduler
import com.module.notelycompose.dailyexport.DailyExportFileWriter
import com.module.notelycompose.dailyexport.DailyExportRunner
import com.module.notelycompose.dailyexport.DailyExportScheduler
import com.module.notelycompose.database.NoteDatabase
import com.module.notelycompose.export.domain.ExportSelectionInteractor
import com.module.notelycompose.export.domain.ExportSelectionInteractorImpl
import com.module.notelycompose.platform.AndroidPlatform
import com.module.notelycompose.platform.BrowserLauncher
import com.module.notelycompose.platform.Downloader
import com.module.notelycompose.platform.Platform
import com.module.notelycompose.platform.PlatformAudioPlayer
import com.module.notelycompose.platform.PlatformUtils
import com.module.notelycompose.modelDownloader.ModelDownloaderViewModel
import com.module.notelycompose.platform.Transcriber
import com.module.notelycompose.platform.dataStore
import com.module.notelycompose.platform.pdf.AndroidPdfGenerator
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule = module {
    single<String>(qualifier = named("AppVersion")) {
        val app: Application = get()
        try {
            val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    single { FileSaverLauncherHolder() }
    single { FileSaverHandler(get()) }
    single<Platform> { AndroidPlatform(get(named("AppVersion")), get()) }
    single { dataStore(get()) }
    single { PlatformUtils(get(), get(), get()) }
    single { BrowserLauncher(get()) }
    single { AndroidPdfGenerator(get()) }

    single<SqlDriver> {
        AndroidSqliteDriver(NoteDatabase.Schema, context = get(), "notes.db")
    }

    // a new player per view model, so several recordings can each have their own
    factory { PlatformAudioPlayer() }

    single { Downloader(get(), get()) }

    single { Transcriber(get(), get(), get()) }


    // domain
    single<AudioRecorderInteractor> { AudioRecorderInteractorImpl(get(), get(), get()) }
    single<SaveAudioNoteInteractor> {
        SaveAudioNoteInteractorImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    // export
    single { FolderPickerLauncherHolder() }
    single { FolderPickerHandler(get()) }
    single<ExportSelectionInteractor> {
        ExportSelectionInteractorImpl(
            context = get(),
            folderPickerHandler = get()
        )
    }

    // daily export (Android only)
    single<DailyExportFileWriter> { AndroidDailyExportFileWriter(get()) }
    single<DailyExportScheduler> { AndroidDailyExportScheduler(get(), get()) }
    single { DailyExportRunner(get(), get(), get()) }
}
