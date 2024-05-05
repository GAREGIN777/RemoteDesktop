package com.example.remotedesktop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.example.remotedesktop.databinding.ActivityServerBinding
import com.google.firebase.FirebaseApp


class ServerActivity : AppCompatActivity() {
    private lateinit var binding : ActivityServerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        FirebaseApp.initializeApp(this)


    }

    fun fragmentTransaction(
        fragment: Fragment,
        tag: String
    ) {
        supportFragmentManager.beginTransaction().replace(binding.mainFragmentContainer.id,fragment).addToBackStack(tag).commit();

    }
    }
