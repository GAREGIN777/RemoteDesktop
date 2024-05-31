package com.example.remotedesktop


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.remotedesktop.Constants.PusherClient
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Helpers.VideoRecorder
import com.example.remotedesktop.Services.PusherService
import com.example.remotedesktop.Services.VideoRecordingService
import com.example.remotedesktop.databinding.ActivityUserBinding
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange


class UserActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserBinding
    private lateinit var userData : User;
    private lateinit var serviceIntent: Intent;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userData = intent.getSerializableExtra("firestoreUser") as User


        ActivityCompat.requestPermissions(
            this,
            PusherService.REQUIRED_PERMISSIONS,
            100
        )



        /*serviceIntent = Intent(this, VideoRecordingService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)*/
        serviceIntent = Intent(this,PusherService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)


        val homeFragment = UserHomeFragment();
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id, homeFragment)
            .commit();


    }

    fun stopBgService(){
        if(::serviceIntent.isInitialized) {
            stopService(serviceIntent);
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        stopBgService()
    }
    fun getUserData() : User{
        return userData;
    }

    fun fragmentTransaction(
        fragment: Fragment,
        tag: String
    ) {
        supportFragmentManager.beginTransaction().replace(binding.mainFragmentContainer.id,fragment).addToBackStack(tag).commit();
    }
}