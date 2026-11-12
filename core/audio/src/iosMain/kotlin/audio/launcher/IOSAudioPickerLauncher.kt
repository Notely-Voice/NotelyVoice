package audio.launcher

import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject

internal class IOSAudioPickerLauncher {

    private var launcherCallback: ((String?) -> Unit)? = null

    @OptIn(ExperimentalForeignApi::class)
    private val pickerDelegate = object : NSObject(),
        UIDocumentPickerDelegateProtocol,
        UIAdaptivePresentationControllerDelegateProtocol {

        override fun documentPicker(
            controller: UIDocumentPickerViewController,
            didPickDocumentsAtURLs: List<*>
        ) {
            val selectedUrl = didPickDocumentsAtURLs.filterIsInstance<NSURL>().firstOrNull()
            val fileManager = NSFileManager.defaultManager

            val sandboxDir = fileManager
                .URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
                .first() as NSURL

            selectedUrl?.let { sourceUrl ->
                if (sourceUrl.startAccessingSecurityScopedResource()) {
                    try {
                        val fileName = sourceUrl.lastPathComponent ?: "imported_audio.mp3"
                        val destinationUrl = sandboxDir.URLByAppendingPathComponent(fileName)

                        // Remove existing file if any
                        if (fileManager.fileExistsAtPath(destinationUrl!!.path!!)) {
                            fileManager.removeItemAtURL(destinationUrl, null)
                        }

                        // Copy selected file to sandbox
                        fileManager.copyItemAtURL(sourceUrl, destinationUrl, null)

                        launcherCallback?.invoke(destinationUrl.path)
                    } catch (e: Exception) {
                        Napier.e("❌ Failed to copy file: ${e.message}")
                        launcherCallback?.invoke(null)
                    } finally {
                        sourceUrl.stopAccessingSecurityScopedResource()
                    }
                } else {
                    Napier.e("❌ Cannot access security-scoped file")
                    launcherCallback?.invoke(null)
                }
            } ?: launcherCallback?.invoke(null)
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            launcherCallback?.invoke(null)
        }

        override fun presentationControllerWillDismiss(presentationController: UIPresentationController) {
            (presentationController.presentedViewController as? UIDocumentPickerViewController)?.let {
                documentPickerWasCancelled(it)
            }
        }
    }

    private val contentTypes: List<UTType>
        get() = listOf("aac", "aiff", "caf", "flac", "m4a", "mp3", "wav")
            .mapNotNull { UTType.typeWithFilenameExtension(it) }
            .ifEmpty { listOf(UTTypeContent) }

    fun launch(onResult: (String?) -> Unit) {
        launcherCallback = onResult

        val picker = UIDocumentPickerViewController(forOpeningContentTypes = contentTypes)
        picker.delegate = pickerDelegate
        picker.allowsMultipleSelection = false

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null
        )
    }
}
