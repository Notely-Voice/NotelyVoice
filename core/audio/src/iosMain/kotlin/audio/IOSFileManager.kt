package audio

import audio.converter.AudioConverter
import audio.launcher.IOSAudioPickerLauncher
import audio.launcher.IOSVideoPickerLauncher
import audio.utils.deleteFile
import audio.utils.savePickedAudioToAppStorage
import audio.utils.savePickedVideoToAppStorage
import platform.Foundation.NSURL

internal class IOSFileManager(
    private val launcher: IOSAudioPickerLauncher,
    private val videoLauncher: IOSVideoPickerLauncher,
    private val audioConverter: AudioConverter
) : FileManager {

    private var pickedAudioPath: String? = null
    private var pickedVideoPath: String? = null

    override fun launchAudioPicker(onResult: () -> Unit) {
        pickedAudioPath = null
        launcher.launch { selectedPath ->
            pickedAudioPath = selectedPath
            selectedPath?.let { onResult() }
        }
    }

    override fun launchVideoPicker(onResult: () -> Unit) {
        pickedVideoPath = null
        videoLauncher.launch { selectedPath ->
            pickedVideoPath = selectedPath
            selectedPath?.let { onResult() }
        }
    }

    override fun launchPhotosPicker(onResult: () -> Unit) {
        // TODO: iOS implementation (PHPickerViewController). Android-only feature for now.
    }

    override suspend fun processPickedAudioToWav(onProgress: (Float) -> Unit): String? {
        val inputPath = copyToAppStorage() ?: return null
        val outputPath = audioConverter.convertAudioToWav(inputPath, onProgress)
        deleteFile(inputPath)
        return outputPath
    }

    override suspend fun processPickedVideoToWav(onProgress: (Float) -> Unit): String? {
        val inputPath = copyVideoToAppStorage() ?: return null
        val outputPath = audioConverter.extractAudioFromVideoToWav(inputPath, onProgress)
        deleteFile(inputPath)
        return outputPath
    }

    override suspend fun processPickedPhotos(): List<String> {
        // TODO: iOS implementation. Android-only feature for now.
        return emptyList()
    }

    private fun copyToAppStorage(): String? {
        return pickedAudioPath?.let { path ->
            NSURL.fileURLWithPath(path).savePickedAudioToAppStorage()?.path
        }.also {
            pickedAudioPath = null
        }
    }

    private fun copyVideoToAppStorage(): String? {
        return pickedVideoPath?.let { path ->
            NSURL.fileURLWithPath(path).savePickedVideoToAppStorage()?.path
        }.also {
            pickedVideoPath = null
        }
    }
}
