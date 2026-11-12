package com.module.notelycompose.dailyexport

import com.module.notelycompose.core.asFlow
import com.module.notelycompose.notes.data.model.NoteDataModel
import com.module.notelycompose.notes.domain.NoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val UNTITLED = "untitled"
private const val MAX_TITLE_LENGTH = 40

class DailyExportRunner(
    private val noteDataSource: NoteDataSource,
    private val repository: DailyExportRepository,
    private val fileWriter: DailyExportFileWriter
) {

    /** Exports the notes selected by the saved scope. Returns how many notes were written. */
    suspend fun run(): Result<Int> {
        val settings = repository.currentSettings()
        val folderUri = settings.folderUri
            ?: return finish(Result.failure(IllegalStateException("No export folder selected")))

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val notes = noteDataSource.getNotes().asFlow().first()
        val previousHashes = repository.getExportedHashes()

        val selected = notes.filter { note ->
            when (settings.scope) {
                DailyExportScope.ALL -> true
                DailyExportScope.CREATED_TODAY -> note.createdAt.date == today
                DailyExportScope.CHANGED_SINCE_LAST_EXPORT -> previousHashes[note.id] != note.contentHash()
            }
        }

        val newHashes = previousHashes.toMutableMap()
        for (note in selected) {
            val written = fileWriter.write(
                folderUri = folderUri,
                directories = listOf(note.createdAt.date.year.toString(), note.createdAt.date.monthTwoDigits()),
                fileName = note.fileName(),
                content = note.content,
                overwrite = !settings.keepVersions
            )
            if (written.isFailure) {
                repository.setExportedHashes(newHashes)
                return finish(Result.failure(written.exceptionOrNull() ?: IllegalStateException("Export failed")))
            }
            newHashes[note.id] = note.contentHash()
        }
        // Drop hashes of deleted notes so the map does not grow forever.
        newHashes.keys.retainAll(notes.map { it.id }.toSet())
        repository.setExportedHashes(newHashes)
        return finish(Result.success(selected.size))
    }

    private suspend fun finish(result: Result<Int>): Result<Int> {
        val message = result.fold(
            onSuccess = { count -> "Exported $count note(s)" },
            onFailure = { error -> "Export failed: ${error.message ?: "unknown error"}" }
        )
        repository.setLastRun(Clock.System.now().toEpochMilliseconds(), message)
        return result
    }

    private fun NoteDataModel.contentHash(): Int = 31 * title.hashCode() + content.hashCode()

    private fun NoteDataModel.fileName(): String {
        val date = createdAt.date
        val safeTitle = title.trim()
            .ifBlank { UNTITLED }
            .replace(Regex("[^\\p{L}\\p{N} _-]"), "")
            .trim()
            .ifBlank { UNTITLED }
            .take(MAX_TITLE_LENGTH)
        return "${date}_${safeTitle}_$id.txt"
    }

    private fun LocalDate.monthTwoDigits(): String = monthNumber.toString().padStart(2, '0')
}
