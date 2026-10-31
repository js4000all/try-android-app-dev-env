package local.example.androidcamerasample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var statusView: TextView
    private lateinit var frameInfoView: TextView
    private val analysisExecutor = Executors.newSingleThreadExecutor()

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
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionLabel = TextView(this).apply {
            text = "v${packageInfo.versionName}  /  Take ${packageInfo.longVersionCode}"
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(12.dp, 7.dp, 12.dp, 7.dp)
            background = GradientDrawable().apply {
                setColor(0x99000000.toInt())
                cornerRadius = 6.dp.toFloat()
            }
        }
        frameInfoView = TextView(this).apply {
            text = "Frames: waiting..."
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(12.dp, 7.dp, 12.dp, 7.dp)
            background = GradientDrawable().apply {
                setColor(0x99000000.toInt())
                cornerRadius = 6.dp.toFloat()
            }
        }
        setContentView(FrameLayout(this).apply {
            addView(previewView, FrameLayout.LayoutParams(-1, -1))
            addView(statusView, FrameLayout.LayoutParams(-1, -1))
            addView(frameInfoView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            ).apply {
                setMargins(12.dp, 12.dp, 12.dp, 12.dp)
            })
            addView(versionLabel, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply {
                setMargins(12.dp, 12.dp, 12.dp, 12.dp)
            })
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

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                var totalFrames = 0L
                var windowFrames = 0
                var windowStart = SystemClock.elapsedRealtimeNanos()

                analysis.setAnalyzer(analysisExecutor) { image ->
                    try {
                        totalFrames++
                        windowFrames++
                        val now = SystemClock.elapsedRealtimeNanos()
                        val elapsed = now - windowStart
                        if (elapsed >= 1_000_000_000L) {
                            val fps = windowFrames * 1_000_000_000.0 / elapsed
                            val info = "Frames: $totalFrames  |  ${image.width}x${image.height}  |  %.1f FPS".format(java.util.Locale.US, fps)
                            frameInfoView.post { frameInfoView.text = info }
                            windowFrames = 0
                            windowStart = now
                        }
                    } finally {
                        image.close()
                    }
                }

                provider.unbindAll()
                provider.bindToLifecycle(this@MainActivity, selector, preview, analysis)
                statusView.visibility = View.GONE
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera", e)
                showStatus("Failed to start camera: ${e.javaClass.simpleName}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        analysisExecutor.shutdown()
    }

    private fun showStatus(message: String) {
        statusView.text = message
        statusView.visibility = android.view.View.VISIBLE
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        private const val TAG = "CameraSample"
    }
}
