// SettingsActivity.kt
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.notelyvoice.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Start SenseVoice service
        val intent = Intent(this, SenseVoiceService::class.java)
        startService(intent)
    }
}