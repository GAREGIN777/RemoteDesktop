package com.example.remotedesktop.Services
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import com.example.remotedesktop.Helpers.VideoRecorder
import com.example.remotedesktop.R
import com.example.remotedesktop.UserActivity
import com.google.firebase.auth.FirebaseAuth

class VideoRecordingService : LifecycleService() {
    private lateinit var videoRecorder: VideoRecorder
    private val CHANNEL_ID = "VideoRecordingServiceChannel"
    private val NOTIFICATION_ID = 123
    private lateinit var storeId : String;


    override fun onCreate() {
        super.onCreate()
        val auth = FirebaseAuth.getInstance();
        val uid = auth.currentUser?.uid;
        if(uid != null){
            storeId = uid;
        }
        createNotificationChannel()

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startForegroundService() {

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }


    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, UserActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Video Recording Service")
            .setContentText("Recording videos in the background")
            .setSmallIcon(R.drawable.ic_custom_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Video Recording Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //return super.onStartCommand(intent, flags, startId)
        videoRecorder = VideoRecorder(applicationContext, this ,storeId)
        //startForegroundService()
        videoRecorder.startRecording()
        startForegroundService()
        return START_NOT_STICKY;
    }








}