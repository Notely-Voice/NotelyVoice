package audio.launcher

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class AndroidPhotosPickerLauncher(
    private val activity: ComponentActivity
) {
    fun launch(onResult: (List<Uri>) -> Unit) {
        val launcher = activity.activityResultRegistry.register(
            "pick_photos",
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            onResult(uris)
        }

        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
