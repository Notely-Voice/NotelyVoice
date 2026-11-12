package audio.converter

import audio.utils.IMPORTING_PREFIX
import audio.utils.generateNewAudioFile
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import platform.AVFoundation.*
import platform.AVFAudio.*
import platform.CoreAudioTypes.*
import platform.CoreMedia.*
import platform.Foundation.NSURL
import platform.darwin.dispatch_queue_create
import kotlin.coroutines.*

@OptIn(ExperimentalForeignApi::class)
class IOSAudioConverter : AudioConverter {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun convertAudioToWav(
        path: String,
        onProgress: (Float) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Prepare asset and I/O
            val inputUrl = NSURL.fileURLWithPath(path)
            val asset = AVURLAsset.URLAssetWithURL(inputUrl, null)

            val outputUrl = generateNewAudioFile(IMPORTING_PREFIX) ?: return@withContext null
            val outputPath = outputUrl.path ?: return@withContext null

            // Reader and writer
            val reader = AVAssetReader(asset, null)
            val writer = AVAssetWriter(outputUrl, AVFileTypeWAVE, null)

            // Select audio track
            val audioTrack =
                asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as? AVAssetTrack
                    ?: run {
                        Napier.e { "No audio track found in asset" }
                        return@withContext null
                    }

            // Reader output settings (PCM)
            val readerOutputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )
            val readerOutput = AVAssetReaderTrackOutput(audioTrack, readerOutputSettings)
            if (!reader.canAddOutput(readerOutput)) {
                Napier.e { "Reader cannot add output" }
                return@withContext null
            }
            reader.addOutput(readerOutput)

            // Writer input settings (PCM)
            val writerInputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )
            val writerInput = AVAssetWriterInput(AVMediaTypeAudio, writerInputSettings)
            writerInput.expectsMediaDataInRealTime = false
            if (!writer.canAddInput(writerInput)) {
                Napier.e { "Writer cannot add input" }
                return@withContext null
            }
            writer.addInput(writerInput)

            // Start reading/writing
            if (!reader.startReading()) {
                Napier.e { "Reader start failed: ${'$'}{reader.error?.localizedDescription}" }
                return@withContext null
            }
            if (!writer.startWriting()) {
                Napier.e { "Writer start failed: ${'$'}{writer.error?.localizedDescription}" }
                return@withContext null
            }
            writer.startSessionAtSourceTime(CMTimeMake(value = 0, timescale = 1))

            // Compute total duration
            val totalDurationSec = CMTimeGetSeconds(asset.duration).takeIf { it > 0 } ?: 1.0
            var lastProgress = 0f

            // Stream samples and report progress
            return@withContext suspendCancellableCoroutine<String> { cont ->
                val queue = dispatch_queue_create("audio.writer.queue", null)
                writerInput.requestMediaDataWhenReadyOnQueue(queue) {
                    try {
                        while (writerInput.isReadyForMoreMediaData()) {
                            val sampleBuffer = readerOutput.copyNextSampleBuffer()
                            if (sampleBuffer != null) {
                                val appended = writerInput.appendSampleBuffer(sampleBuffer)
                                if (!appended) {
                                    val err =
                                        writer.error?.localizedDescription ?: "Unknown append error"
                                    Napier.e { "Append failed: $err" }
                                    cont.resumeWithException(Throwable(err))
                                    return@requestMediaDataWhenReadyOnQueue
                                }
                                // Calculate and emit progress
                                val pts = CMSampleBufferGetPresentationTimeStamp(sampleBuffer)
                                val currentSec = CMTimeGetSeconds(pts)
                                val progress =
                                    (currentSec / totalDurationSec).toFloat().coerceIn(0f, 1f)
                                if (progress - lastProgress >= 0.01f) {
                                    lastProgress = progress
                                    onProgress(progress)
                                }
                            } else {
                                // End of samples
                                writerInput.markAsFinished()
                                writer.finishWritingWithCompletionHandler {
                                    if (writer.status == AVAssetWriterStatusCompleted) {
                                        onProgress(1f)
                                        cont.resume(outputPath)
                                    } else {
                                        val err = writer.error?.localizedDescription
                                            ?: "Unknown finish error"
                                        Napier.e { "Finish failed: $err" }
                                        cont.resumeWithException(Throwable(err))
                                    }
                                }
                                break
                            }
                        }
                    } catch (e: Throwable) {
                        Napier.e("Error in writing loop: $e")
                        cont.resumeWithException(e)
                    }
                }

                cont.invokeOnCancellation {
                    Napier.e { "Conversion cancelled" }
                    reader.cancelReading()
                    writer.cancelWriting()
                }
            }
        } catch (e: Throwable) {
            Napier.e("Unexpected error: $e")
            return@withContext null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun extractAudioFromVideoToWav(
        videoPath: String,
        onProgress: (Float) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Prepare video asset and output URL
            val inputUrl = NSURL.fileURLWithPath(videoPath)
            val asset = AVURLAsset.URLAssetWithURL(inputUrl, null)

            val outputUrl = generateNewAudioFile(IMPORTING_PREFIX) ?: return@withContext null
            val outputPath = outputUrl.path ?: return@withContext null

            // Reader and writer
            val reader = AVAssetReader(asset, null)
            val writer = AVAssetWriter(outputUrl, AVFileTypeWAVE, null)

            // Select audio track from video
            val audioTrack = asset.tracksWithMediaType(AVMediaTypeAudio).firstOrNull() as? AVAssetTrack
                ?: run {
                    Napier.e { "No audio track found in video file" }
                    return@withContext null
                }

            // Reader output settings (PCM)
            val readerOutputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )
            val readerOutput = AVAssetReaderTrackOutput(audioTrack, readerOutputSettings)
            if (!reader.canAddOutput(readerOutput)) {
                Napier.e { "Reader cannot add output for video audio extraction" }
                return@withContext null
            }
            reader.addOutput(readerOutput)

            // Writer input settings (PCM)
            val writerInputSettings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 16000.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false,
                AVLinearPCMIsNonInterleavedKey to false
            )
            val writerInput = AVAssetWriterInput(AVMediaTypeAudio, writerInputSettings)
            writerInput.expectsMediaDataInRealTime = false
            if (!writer.canAddInput(writerInput)) {
                Napier.e { "Writer cannot add input for video audio extraction" }
                return@withContext null
            }
            writer.addInput(writerInput)

            // Start reading/writing
            if (!reader.startReading()) {
                Napier.e { "Reader start failed for video: ${reader.error?.localizedDescription}" }
                return@withContext null
            }
            if (!writer.startWriting()) {
                Napier.e { "Writer start failed for video: ${writer.error?.localizedDescription}" }
                return@withContext null
            }
            writer.startSessionAtSourceTime(CMTimeMake(value = 0, timescale = 1))

            // Compute total duration from video
            val totalDurationSec = CMTimeGetSeconds(asset.duration).takeIf { it > 0 } ?: 1.0
            var lastProgress = 0f

            // Stream audio samples from video and report progress
            return@withContext suspendCancellableCoroutine<String> { cont ->
                val queue = dispatch_queue_create("video.audio.writer.queue", null)
                writerInput.requestMediaDataWhenReadyOnQueue(queue) {
                    try {
                        while (writerInput.isReadyForMoreMediaData()) {
                            val sampleBuffer = readerOutput.copyNextSampleBuffer()
                            if (sampleBuffer != null) {
                                val appended = writerInput.appendSampleBuffer(sampleBuffer)
                                if (!appended) {
                                    val err = writer.error?.localizedDescription ?: "Unknown append error in video extraction"
                                    Napier.e { "Append failed during video audio extraction: $err" }
                                    cont.resumeWithException(Throwable(err))
                                    return@requestMediaDataWhenReadyOnQueue
                                }
                                // Calculate and emit progress
                                val pts = CMSampleBufferGetPresentationTimeStamp(sampleBuffer)
                                val currentSec = CMTimeGetSeconds(pts)
                                val progress = (currentSec / totalDurationSec).toFloat().coerceIn(0f, 1f)
                                if (progress - lastProgress >= 0.01f) {
                                    lastProgress = progress
                                    onProgress(progress)
                                }
                            } else {
                                // End of audio samples from video
                                writerInput.markAsFinished()
                                writer.finishWritingWithCompletionHandler {
                                    if (writer.status == AVAssetWriterStatusCompleted) {
                                        onProgress(1f)
                                        cont.resume(outputPath)
                                    } else {
                                        val err = writer.error?.localizedDescription ?: "Unknown finish error in video extraction"
                                        Napier.e { "Finish failed during video audio extraction: $err" }
                                        cont.resumeWithException(Throwable(err))
                                    }
                                }
                                break
                            }
                        }
                    } catch (e: Throwable) {
                        Napier.e("Error in video audio extraction loop: $e")
                        cont.resumeWithException(e)
                    }
                }

                cont.invokeOnCancellation {
                    Napier.e { "Video audio extraction cancelled" }
                    reader.cancelReading()
                    writer.cancelWriting()
                }
            }
        } catch (e: Throwable) {
            Napier.e("Unexpected error during video audio extraction: $e")
            return@withContext null
        }
    }
}
