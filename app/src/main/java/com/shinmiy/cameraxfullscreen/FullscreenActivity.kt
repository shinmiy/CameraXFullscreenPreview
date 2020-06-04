package com.shinmiy.cameraxfullscreen

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.lifecycle.lifecycleScope
import com.shinmiy.cameraxfullscreen.databinding.ActivityFullscreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class FullscreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenBinding

    private val cameraClass: CameraClass by inject()

    private val backCamera
        get() = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Wakelock
        // https://developer.android.com/training/scheduling/wakelock.html
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Dim screen
        val attributes = window.attributes.also {
            it.screenBrightness = Float.MIN_VALUE
        }
        window.attributes = attributes

        startCameraWithPermissionCheck()
    }

    override fun onResume() {
        super.onResume()
        hide()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun hide() {
        supportActionBar?.hide()
        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        binding.fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun startCamera() {
        lifecycleScope.launch(Dispatchers.Main) {
            val cameraProvider = cameraClass.getCameraProvider()

            // Preview
            val preview = Preview.Builder().build()

            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(this@FullscreenActivity, backCamera, preview)

            val surfaceProvider = binding.fullscreenContent.createSurfaceProvider()
            preview.setSurfaceProvider(surfaceProvider)
        }
    }
}
