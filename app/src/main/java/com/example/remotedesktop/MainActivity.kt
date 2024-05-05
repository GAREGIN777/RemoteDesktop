package com.example.remotedesktop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(){

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val auth = FirebaseAuth.getInstance()





        var defaultFragment : Fragment = RegisterFragment();
        if(auth.currentUser != null){
            Toast.makeText(applicationContext, auth.currentUser!!.email,Toast.LENGTH_SHORT).show();
            defaultFragment = HomeFragment();
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id,defaultFragment)
            .commit()

    }

    fun restartActivity(){
        val intent = Intent(applicationContext, this.javaClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun fragmentTransaction(
        fragment: Fragment,
        tag: String
    ) {
        supportFragmentManager.beginTransaction().replace(binding.mainFragmentContainer.id,fragment).addToBackStack(tag).commit();

    }
}