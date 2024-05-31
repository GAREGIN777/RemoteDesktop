package com.example.remotedesktop.Helpers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraX
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
@SuppressLint("RestrictedApi")

class VideoRecorder(private val context: Context, private val lifecycleOwner: LifecycleOwner,private val videoId : String) {


    private var videoCapture: VideoCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    init {
        outputDirectory = context.getExternalFilesDir("videos")!!
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startRecording() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            Log.e(TAG, "Permissions not granted.")
        }
    }

    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        videoCapture = VideoCapture.Builder().setBitRate(10000).build()

        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, videoCapture)
                startRecordingVideo()
                                             }, ContextCompat.getMainExecutor(context))


        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }



    @SuppressLint("RestrictedApi")
    private fun startRecordingVideo() {


            Handler(Looper.getMainLooper()).postDelayed({
                // This code will run after the specified delay
                // For example, you can start recording video after the delay
                videoCapture?.stopRecording();

            }, FIREBASE_DELAY)
            val videoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
            )


            val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()


            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                return
            }


            videoCapture?.startRecording(
                outputOptions,
                cameraExecutor,
                object : VideoCapture.OnVideoSavedCallback {

                    override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri ?: videoFile.toUri()
                        Log.d(TAG, "Video file saved at: $savedUri")
                        uploadVideoToFirebase(videoFile)
                    }

                    override fun onError(
                        videoCaptureError: Int,
                        message: String,
                        cause: Throwable?
                    ) {
                        Log.e(TAG, "Video capture failed: $message", cause)
                    }
                }
            )
        }




    private fun uploadVideoToFirebase(videoFile: File) {

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("videos/${videoId}.mp4")

        storageRef.putBytes(videoFile.readBytes())
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d(TAG, "Video uploaded to Firebase Storage successfully.")
                }
                else{
                    val e = it.exception;
                    if (e != null) {
                        Log.e(TAG, "Error uploading video to Firebase Storage: ${e.message}", e)
                    }
                }
                videoFile.delete()
                startRecordingVideo()
            }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val FIREBASE_DELAY = 15000L;
        private const val TAG = "VideoRecorder"
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    }
}