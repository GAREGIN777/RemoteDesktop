package com.example.remotedesktop.Services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.remotedesktop.Constants.PusherClient
import com.example.remotedesktop.Constants.RetrofitAPI
import com.example.remotedesktop.DataClasses.PusherPoint
import com.example.remotedesktop.R
import com.example.remotedesktop.Retrofit.LocationAPI
import com.example.remotedesktop.Retrofit.LocationAPIRequest
import com.example.remotedesktop.Retrofit.LocationResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class PusherService : Service() {

    private lateinit var locationAPI: LocationAPI
    private lateinit var pusher : Pusher;
    private lateinit var auth : FirebaseAuth;

    private val pointsList = mutableListOf<PusherPoint>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance();

        // Initialize Retrofit for Pusher API

        // Start the foreground service with a notification
        startForegroundService()
    }

    private fun startForegroundService() {
        val channelId =
            createNotificationChannel("pusher_service", "Pusher Service")

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Pusher Service")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.ic_custom_camera)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Trigger an event to Pusher
        //triggerPusherEvent("my-channel", "my-event", mapOf("message" to "Hello from service"))
        val userId = auth.uid;
        if(userId != null) {
            setupLocationUpdates(userId)
        }
        return START_STICKY
    }

    private fun addPusherPoint(location: Location) {
        val point = PusherPoint(
            lat = location.latitude,
            lng = location.longitude,
            speed = location.speed.toInt(),
            bearing = location.bearing,
            timestamp = location.time
        )
        pointsList.add(point)
    }

    //main launcher
    private fun setupLocationUpdates(userId: String) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 100 // 10 seconds
            fastestInterval = 200 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    addPusherPoint(location)
                }
            }
        }
        startLocationUpdates()
        initApi(userId)
    }



    private fun sendDelayedUpdates(userId: String){
        Handler(Looper.getMainLooper()).postDelayed({
            sendLocationUpdates(userId)
        }, 1000)
    }

    private fun sendLocationUpdates(userId: String) {

        if (pointsList.isEmpty()) {
            sendDelayedUpdates(userId)
            return;
        }

        val channelId = "your_channel_id"
        val pointsToSend = pointsList.toList() // Create a copy to avoid concurrent modification
        pointsList.clear() // Clear the list after copying

        locationAPI.sendLocationUpdates(userId, LocationAPIRequest(pointsToSend)).enqueue(object : Callback<LocationResponse> {
            override fun onResponse(
                call: Call<LocationResponse>,
                response: Response<LocationResponse>
            ) {
                if (response.isSuccessful) {
                    showToast("Points sent successfully")
                } else {
                    showToast("Failed to send points: ${response.code()}")
                }
                sendDelayedUpdates(userId)
            }

            override fun onFailure(call: Call<LocationResponse>, t: Throwable) {
                t.localizedMessage?.let { showToast(it) };
                sendDelayedUpdates(userId)
            }

        })
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun initApi(userId : String) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(RetrofitAPI.RETROFIT_BASE_SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        locationAPI = retrofit.create(LocationAPI::class.java)
        sendDelayedUpdates(userId)


    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }



    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object{

     val REQUIRED_PERMISSIONS =
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )



    }
}