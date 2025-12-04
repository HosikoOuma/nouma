package com.nkds.hosikoouma.nouma.features.conversation

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraManager: CameraManager
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = { context ->
            PreviewView(context).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                cameraManager.startPreview(this.surfaceProvider)
            }
        }
    )
}