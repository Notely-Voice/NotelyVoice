package com.module.notelycompose.platform

import com.module.notelycompose.core.debugPrintln
import com.module.notelycompose.whisper.WhisperCallback
import com.module.notelycompose.whisper.WhisperContext
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithContentsOfURL
import kotlin.math.max
import kotlin.math.min



actual class Transcriber{
    private var canTranscribe: Boolean = false
    private var isTranscribing = false
    private var isModelLoaded = false
    private var whisperContext: WhisperContext? = null


    actual fun hasRecordingPermission(): Boolean {
        return true
    }


    actual suspend fun requestRecordingPermission(): Boolean {
        return true
    }


    actual suspend fun initialize(modelFileName: String) {
        debugPrintln{"speech: initialize model"}
        if(!isModelLoaded)
            loadBaseModel()
    }

    private fun loadBaseModel(modelFileName: String){
        try {
            whisperContext = null
            debugPrintln{"Loading model: $modelFileName"}
            val modelPath = getModelPath(modelFileName)
            whisperContext = WhisperContext.createContext(modelPath)
            debugPrintln{"Loaded model ${modelPath.substringAfterLast("/")}"}
            isModelLoaded = true
            canTranscribe = true

        } catch (e: Throwable) {
            debugPrintln{"========================== ${e.message}"}
            e.printStackTrace()
        }
    }

    actual fun doesModelExists(modelFileName: String) : Boolean{
        return NSFileManager.defaultManager.fileExistsAtPath(getModelPath())
    }

    actual fun isValidModel(modelFileName: String) : Boolean{
        try {
            if(!isModelLoaded)
                loadBaseModel(modelFileName)
        }catch (e:Exception){
            return false
        }
        return true
    }

    actual suspend fun moveModelToProtectedStorage(modelFileName: String): Result<String> {
        return Result.failure(Exception("Not supported on iOS"))
    }

    actual fun doesModelExistInExternalStorage(modelFileName: String): Boolean {
        return false
    }

    actual fun doesModelExistInInternalStorage(modelFileName: String): Boolean {
        return false
    }

    actual suspend fun stop() {
        isTranscribing = false
        whisperContext?.stopTranscribing()
    }

    actual suspend fun finish() {
        whisperContext?.release()
    }

    actual suspend fun start(
        filePath: String, language: String,
        onProgress : (Int) -> Unit,
        onNewSegment : (Long, Long,String) -> Unit,
        onComplete : () -> Unit,
        onError : () -> Unit
    ) {
        if (!canTranscribe) {
            debugPrintln{"Model not loaded yet"}
            return
        }

        canTranscribe = false

        try {
            debugPrintln{"Reading wave samples... "}
            val data = decodeWaveFile(filePath)
            debugPrintln{"${data.size / (16000 / 1000)} ms\n"}
            debugPrintln{"Transcribing data...\n"}
            whisperContext?.fullTranscribe(data, language, object : WhisperCallback{
                override fun onProgress(progress: Int) {
                    onProgress(progress)
                }

                override fun onNewSegment(l1: Long, l2: Long, text: String) {
                    onNewSegment(l1,l2,text)
                }

                override fun onComplete() {
                    onComplete()
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            debugPrintln{"${e.message}\n"}
        }

        canTranscribe = true

    }

    @OptIn(ExperimentalForeignApi::class)
    fun decodeWaveFile(path: String): FloatArray {
        val url = NSURL.fileURLWithPath(path)
        val data = NSData.dataWithContentsOfURL(url) ?: throw Exception("Failed to read file")

        val length = data.length.toInt()
        val bytes = data.bytes?.reinterpret<ByteVar>() ?: throw Exception("Invalid WAV file")

        // Skip 44-byte WAV header
        val start = 44
        val sampleCount = (length - start) / 2
        val floatArray = FloatArray(sampleCount)

        var i = 0
        while (i < sampleCount) {
            val byteIndex = start + i * 2
            val low = bytes[byteIndex].toInt() and 0xFF
            val high = bytes[byteIndex + 1].toInt()
            val shortVal = (high shl 8) or low
            floatArray[i] = max(-1.0f, min(shortVal / 32767.0f, 1.0f))
            i++
        }

        return floatArray
    }

    private fun getModelPath(modelFileName: String):String{
        val documentsDirectory = NSFileManager.defaultManager.URLsForDirectory(
            NSDocumentDirectory,
            NSUserDomainMask
        ).first() as NSURL

        return documentsDirectory.URLByAppendingPathComponent(modelFileName)?.path?:""
    }

}