package com.module.notelycompose.dailyexport

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MIME_TEXT = "text/plain"

class AndroidDailyExportFileWriter(
    private val context: Context
) : DailyExportFileWriter {

    override suspend fun write(
        folderUri: String,
        directories: List<String>,
        fileName: String,
        content: String,
        overwrite: Boolean
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(folderUri))
            require(root != null && root.canWrite()) { "Export folder is not accessible. Choose it again." }

            val folder = directories.fold(root) { parent, name ->
                parent.findFile(name)?.takeIf { it.isDirectory }
                    ?: parent.createDirectory(name)
                    ?: error("Could not create folder $name")
            }

            val existing = folder.findFile(fileName)
            val target = when {
                existing != null && overwrite -> existing
                else -> folder.createFile(MIME_TEXT, fileName) ?: error("Could not create $fileName")
            }
            // "wt" truncates so a shorter note does not leave old text behind.
            context.contentResolver.openOutputStream(target.uri, "wt")?.use { it.write(content.toByteArray()) }
                ?: error("Could not open $fileName for writing")
        }
    }
}
