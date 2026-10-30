package local.example.androidcamerasample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var statusView: TextView

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                showStatus("Camera permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previewView = PreviewView(this)
        statusView = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 18f
        }
        setContentView(FrameLayout(this).apply {
            addView(previewView, FrameLayout.LayoutParams(-1, -1))
            addView(statusView, FrameLayout.LayoutParams(-1, -1))
        })

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            showStatus("Camera permission required")
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        lifecycleScope.launch {
            showStatus("Starting camera...")
            try {
                val provider = ProcessCameraProvider.awaitInstance(this@MainActivity)
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                if (!provider.hasCamera(selector)) {
                    showStatus("Back camera not available")
                    return@launch
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                provider.unbindAll()
                provider.bindToLifecycle(this@MainActivity, selector, preview)
                statusView.visibility = View.GONE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
                showStatus("Failed to start camera: ${e.javaClass.simpleName}")
            }
        }
    }

    private fun showStatus(message: String) {
        statusView.text = message
        statusView.visibility = android.view.View.VISIBLE
    }

    companion object {
        private const val TAG = "CameraSample"
    }
}
