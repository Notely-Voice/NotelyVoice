package audio.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import audio.launcher.AndroidAudioPickerLauncher
import audio.launcher.AndroidPhotosPickerLauncher
import audio.launcher.AndroidVideoPickerLauncher

class LauncherHolder {
    var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    var audioPickerLauncher: AndroidAudioPickerLauncher? = null
    var videoPickerLauncher: AndroidVideoPickerLauncher? = null
    var photosPickerLauncher: AndroidPhotosPickerLauncher? = null

    fun init(activity: ComponentActivity) {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
        audioPickerLauncher = AndroidAudioPickerLauncher(activity)
        videoPickerLauncher = AndroidVideoPickerLauncher(activity)
        photosPickerLauncher = AndroidPhotosPickerLauncher(activity)
    }
}
