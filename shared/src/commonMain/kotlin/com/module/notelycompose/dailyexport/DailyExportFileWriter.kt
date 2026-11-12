package com.module.notelycompose.dailyexport

interface DailyExportFileWriter {
    /**
     * Writes [content] to [fileName] inside [directories] under the folder at [folderUri].
     * When [overwrite] is false and the file exists, a numbered copy is created instead.
     */
    suspend fun write(
        folderUri: String,
        directories: List<String>,
        fileName: String,
        content: String,
        overwrite: Boolean
    ): Result<Unit>
}
