package com.nkds.hosikoouma.nouma.features.conversation

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onRecordingStarted: () -> Unit,
    private val onRecordingFinished: (File) -> Unit,
    private val onError: (Throwable) -> Unit
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    fun startPreview(surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("MissingPermission")
    fun startRecording(outputFile: File) {
        val localVideoCapture = videoCapture ?: return

        onRecordingStarted()

        val outputOptions = FileOutputOptions.Builder(outputFile).build()
        activeRecording = localVideoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            onRecordingFinished(outputFile)
                        } else {
                            activeRecording?.close()
                            onError(recordEvent.cause ?: RuntimeException("Unknown recording error"))
                        }
                    }
                }
            }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun release() {
        cameraProvider?.unbindAll()
    }
}
