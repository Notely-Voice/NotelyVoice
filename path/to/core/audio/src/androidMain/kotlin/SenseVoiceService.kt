// SenseVoiceService.kt
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.notelyvoice.SenseVoice

class SenseVoiceService : Service() {
    private lateinit var senseVoice: SenseVoice

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        senseVoice = SenseVoice.create(applicationContext, "/sdcard/audio.wav", "fsmn-vad", "ct-punc")
        senseVoice.startRecording()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        senseVoice.stopRecording()
    }
}