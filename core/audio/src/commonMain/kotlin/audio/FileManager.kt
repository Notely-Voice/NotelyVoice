package audio

interface FileManager {
    fun launchAudioPicker(onResult: () -> Unit)
    fun launchVideoPicker(onResult: () -> Unit)
    fun launchPhotosPicker(onResult: () -> Unit)

    suspend fun processPickedAudioToWav(onProgress: (Float) -> Unit): String?

    suspend fun processPickedVideoToWav(onProgress: (Float) -> Unit): String?

    /**
     * Copies the picked photos into the app storage and returns their file paths.
     */
    suspend fun processPickedPhotos(): List<String>
}
