package com.example.remotedesktop.Helpers


    import android.Manifest
    import android.content.Context
    import android.content.pm.PackageManager
    import android.net.Uri
    import android.os.Handler
    import android.os.Looper
    import android.util.Log
    import androidx.camera.core.*
    import androidx.camera.lifecycle.ProcessCameraProvider
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.LifecycleOwner
    import com.google.firebase.storage.FirebaseStorage
    import java.io.File
    import java.text.SimpleDateFormat
    import java.util.*
    import java.util.concurrent.ExecutorService
    import java.util.concurrent.Executors

    class ImageRecorder(
        private val context: Context,
        private val lifecycleOwner: LifecycleOwner,
        private val imageId: String
    ) {

        private var imageCapture: ImageCapture? = null
        private lateinit var outputDirectory: File
        private lateinit var cameraExecutor: ExecutorService

        init {
            outputDirectory = context.getExternalFilesDir("images")!!
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        fun startCapturing() {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Log.e(TAG, "Permissions not granted.")
            }
        }

        private fun startCamera() {
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()

            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
                    captureImage()
                }, ContextCompat.getMainExecutor(context))

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }

        private fun captureImage() {
            val imageFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            imageCapture?.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri ?: Uri.fromFile(imageFile)
                        Log.d(TAG, "Image file saved at: $savedUri")
                        uploadImageToFirebase(imageFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Image capture failed: ${exception.message}", exception)
                    }
                }
            )
        }

        private fun uploadImageToFirebase(imageFile: File) { // pusher
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${imageId}.jpg")

            storageRef.putBytes(imageFile.readBytes())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "Image uploaded to Firebase Storage successfully.")
                    } else {
                        val e = it.exception
                        if (e != null) {
                            Log.e(TAG, "Error uploading image to Firebase Storage: ${e.message}", e)
                        }
                    }
                    imageFile.delete()
                    captureImage()  // Capture next image
                }
        }

        private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        companion object {
            private const val TAG = "ImageRecorder"
            private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
            val REQUIRED_PERMISSIONS = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }
