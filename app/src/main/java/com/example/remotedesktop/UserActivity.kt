package com.example.remotedesktop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.databinding.ActivityServerBinding
import com.example.remotedesktop.databinding.ActivityUserBinding


class UserActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserBinding
    private lateinit var userData : User;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userData = intent.getSerializableExtra("firestoreUser") as User


        val homeFragment = UserHomeFragment();
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id,homeFragment)
            .commit();


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