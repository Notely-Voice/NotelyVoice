package audio.converter

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import audio.utils.IMPORTING_PREFIX
import audio.utils.generateWavFile
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class AndroidAudioConverter(
    private val context: Context
) : AudioConverter {
    // Target format constants
    private val targetSampleRate = 16000
    private val targetChannels = 1
    private val targetBitDepth = 16

    override suspend fun convertAudioToWav(
        path: String,
        onProgress: (Float) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(path)

            // Locate the audio track
            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex == -1) return@withContext null

            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)

            // Prepare output WAV file
            val outputFile = context.generateWavFile(prefix = IMPORTING_PREFIX)

            // Convert in chunks and report progress
            val success = processAudioInChunks(
                extractor = extractor,
                format = format,
                outputFile = outputFile,
                onProgress = onProgress
            )

            if (success) outputFile.absolutePath else null
        } catch (e: Exception) {
            Napier.e("Audio conversion failed: ${e.message}", e)
            null
        } finally {
            extractor.release()
        }
    }

    private fun processAudioInChunks(
        extractor: MediaExtractor,
        format: MediaFormat,
        outputFile: File,
        onProgress: (Float) -> Unit
    ): Boolean {
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return false
        val originalSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val originalChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val durationUs = format.getLong(MediaFormat.KEY_DURATION)

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val info = MediaCodec.BufferInfo()
        var sawInputEOS = false
        var sawOutputEOS = false
        var totalBytesWritten = 0L
        var lastProgress = 0f

        return try {
            // Write placeholder header
            val header = createWavHeader(0)
            RandomAccessFile(outputFile, "rw").use { raf ->
                raf.write(header)

                while (!sawOutputEOS) {
                    if (!sawInputEOS) {
                        val inIndex = codec.dequeueInputBuffer(10000)
                        if (inIndex >= 0) {
                            val inBuf = codec.getInputBuffer(inIndex) ?: continue
                            val sampleSize = extractor.readSampleData(inBuf, 0)

                            if (sampleSize < 0) {
                                sawInputEOS = true
                                codec.queueInputBuffer(
                                    inIndex,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                            } else {
                                codec.queueInputBuffer(
                                    inIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    0
                                )
                                extractor.advance()
                            }
                        }
                    }

                    val outIndex = codec.dequeueOutputBuffer(info, 10000)
                    when {
                        outIndex >= 0 -> {
                            codec.getOutputBuffer(outIndex)?.let { buf ->
                                val chunk = ByteArray(info.size)
                                buf.position(info.offset)
                                buf.limit(info.offset + info.size)
                                buf.get(chunk)

                                val processed =
                                    processPcmChunk(chunk, originalSampleRate, originalChannels)
                                raf.write(processed)
                                totalBytesWritten += processed.size

                                // Report progress every ~1%
                                if (durationUs > 0) {
                                    val progress = (info.presentationTimeUs.toDouble() / durationUs)
                                        .coerceIn(0.0, 1.0)
                                        .toFloat()
                                    if (progress - lastProgress >= 0.01f) {
                                        lastProgress = progress
                                        onProgress(progress)
                                    }
                                }
                            }
                            codec.releaseOutputBuffer(outIndex, false)

                            if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                sawOutputEOS = true
                            }
                        }

                        outIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {}
                        outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
                    }
                }

                // Rewrite header with actual data length
                raf.seek(0)
                raf.write(createWavHeader(totalBytesWritten.toInt()))
            }
            true
        } catch (e: Exception) {
            Napier.e("Error processing audio chunks: ${e.message}", e)
            false
        } finally {
            codec.stop()
            codec.release()
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val mime = extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) return i
        }
        return -1
    }

    private fun processPcmChunk(
        chunk: ByteArray,
        originalSampleRate: Int,
        originalChannels: Int
    ): ByteArray {
        val shortBuf = ByteBuffer.wrap(chunk)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
        val shorts = ShortArray(shortBuf.remaining()).also { shortBuf.get(it) }

        val mono = if (originalChannels == 2) convertStereoToMono(shorts) else shorts
        val resampled = if (originalSampleRate != targetSampleRate) resampleAudio(
            mono,
            originalSampleRate
        ) else mono

        return ByteBuffer.allocate(resampled.size * 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { resampled.forEach(::putShort) }
            .array()
    }

    private fun convertStereoToMono(stereo: ShortArray): ShortArray {
        return ShortArray(stereo.size / 2) { i ->
            ((stereo[i * 2].toInt() + stereo[i * 2 + 1].toInt()) / 2).toShort()
        }
    }

    private fun resampleAudio(
        input: ShortArray,
        inputSampleRate: Int
    ): ShortArray {
        if (inputSampleRate == targetSampleRate) return input
        val ratio = inputSampleRate.toDouble() / targetSampleRate
        return ShortArray((input.size / ratio).toInt()) { i ->
            val idx = (i * ratio).toInt()
            if (idx < input.size) input[idx] else 0
        }
    }

    private fun createWavHeader(
        pcmDataLength: Int,
        sampleRate: Int = targetSampleRate,
        channels: Int = targetChannels
    ): ByteArray {
        val bitsPerSample = targetBitDepth
        val byteRate = sampleRate * channels * (bitsPerSample / 8)
        val blockAlign = channels * (bitsPerSample / 8)
        val totalDataLen = pcmDataLength + 36

        return ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(totalDataLen)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1.toShort())
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort(blockAlign.toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(pcmDataLength)
        }.array()
    }

    override suspend fun extractAudioFromVideoToWav(
        videoPath: String,
        onProgress: (Float) -> Unit
    ): String? = withContext(Dispatchers.IO) {
        if (!isValidVideoFile(videoPath)) {
            Napier.w("Unsupported video format: $videoPath")
            return@withContext null
        }

        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(videoPath)

            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex == -1) {
                Napier.w("No audio track found in video")
                return@withContext null
            }

            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)

            // Prepare output WAV file
            val outputFile = context.generateWavFile(prefix = IMPORTING_PREFIX)

            // Convert in chunks and report progress
            val success = processAudioInChunks(
                extractor = extractor,
                format = format,
                outputFile = outputFile,
                onProgress = onProgress
            )

            if (success) outputFile.absolutePath else null

        } catch (e: Exception) {
            Napier.e("Video to audio conversion failed: ${e.message}", e)
            null
        } finally {
            extractor.release()
        }
    }

    private fun isValidVideoFile(path: String): Boolean {
        val videoExtensions = listOf("mp4", "mov", "avi", "mkv", "3gp", "webm")
        return videoExtensions.any { path.lowercase().endsWith(".$it") }
    }
}
