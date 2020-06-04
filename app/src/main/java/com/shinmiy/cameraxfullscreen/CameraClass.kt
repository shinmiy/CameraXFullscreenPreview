package com.shinmiy.cameraxfullscreen

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CameraClass {
    suspend fun getCameraProvider(): ProcessCameraProvider
}

class CameraClassImpl(private val context: Context) : CameraClass {
    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getCameraProvider() = withContext(Dispatchers.IO) {
        ProcessCameraProvider.getInstance(context).get()
    }
}
