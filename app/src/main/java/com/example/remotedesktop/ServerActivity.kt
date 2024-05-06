package com.example.remotedesktop

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.example.remotedesktop.databinding.ActivityServerBinding
import com.google.firebase.FirebaseApp
import org.checkerframework.checker.units.qual.A
import java.io.Serializable


class ServerActivity : AppCompatActivity() {
    private lateinit var binding : ActivityServerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

         val userData = intent.getSerializableExtra("firestoreUser") as User

        userData.let {
            Toast.makeText(applicationContext,userData?.name,Toast.LENGTH_LONG).show()

        }




        val homeFragment : HomeFragment = HomeFragment();
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id,homeFragment)
            .commit();


    }

    fun fragmentTransaction(
        fragment: Fragment,
        tag: String
    ) {
        supportFragmentManager.beginTransaction().replace(binding.mainFragmentContainer.id,fragment).addToBackStack(tag).commit();

    }
    }
