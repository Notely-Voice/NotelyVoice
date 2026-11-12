// SenseVoice.kt
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.protobuf.ByteString
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.concurrent.thread

class SenseVoice private constructor(
    private val context: Context,
    private val audioFilePath: String,
    private val vadModel: String,
    private val puncModel: String
) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    fun startRecording() {
        if (isRecording) {
            return
        }
        isRecording = true
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            1024 * 10
        )
        audioRecord?.apply {
            startRecording()
            thread {
                while (isRecording) {
                    val buffer = ByteArray(1024)
                    read(buffer, 0, buffer.size)
                    val audioData = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                    val audioBytes = audioData.array()
                    val audioString = String(audioBytes.map { it.toInt() }.toTypedArray())
                    val audioBytesString = ByteString.copyFrom(audioBytes)
                    // Process audio data using SenseVoice model
                    processAudioData(audioBytesString)
                }
            }
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
    }

    private fun processAudioData(audioBytesString: ByteString) {
        // Use SenseVoice model to transcribe audio data
        val model = AutoModel(model = "iic/SenseVoiceSmall", vad_model = vadModel, punc_model = puncModel)
        val result = model.generate(input = audioBytesString)
        // Process transcription result
        processTranscriptionResult(result)
    }

    private fun processTranscriptionResult(result: String) {
        // Save transcription result to file
        val transcriptionFile = File(audioFilePath + "_transcription.txt")
        try {
            transcriptionFile.writeText(result)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        fun create(context: Context, audioFilePath: String, vadModel: String, puncModel: String): SenseVoice {
            return SenseVoice(context, audioFilePath, vadModel, puncModel)
        }
    }
}