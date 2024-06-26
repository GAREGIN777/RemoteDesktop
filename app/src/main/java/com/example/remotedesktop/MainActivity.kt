package com.example.remotedesktop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Firebase.UserRole
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity(){

    private lateinit var binding : ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance();


        var defaultFragment: Fragment = RegisterFragment();
        var defaultActivity: Class<out AppCompatActivity> = MainActivity::class.java

        val currentUser = auth.currentUser

        if(currentUser != null){
            val docRef = firestore.collection(Collections.USERS_COLL).document(currentUser.uid)
        docRef.get().addOnCompleteListener{
            if(it.isSuccessful && it.getResult().exists()){

                val firestoreUser : User? = it.getResult().toObject(User::class.java)
                if(firestoreUser != null){
                    //defaultFragment = HomeFragment();

                    Toast.makeText(applicationContext,it.getResult().get("userRole").toString(),Toast.LENGTH_SHORT).show();
                    val intent = when (firestoreUser.userRole) {
                        UserRole.ADMIN -> Intent(this, ServerActivity::class.java)
                        UserRole.USER -> Intent(this, UserActivity::class.java)
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("firestoreUser",firestoreUser)
                    startActivity(intent)
                }
                else{
                    auth.signOut()
                    restartActivity()
                }
            }
            else if(!it.isSuccessful){
                Toast.makeText(applicationContext,"Check your connection and try again",Toast.LENGTH_LONG).show()
            }
            else {
                auth.signOut()
                restartActivity()
            }

        }

    }
        else{
            supportFragmentManager.beginTransaction()
                .replace(binding.mainFragmentContainer.id,defaultFragment)
                .addToBackStack(null)
                .commit()
        }

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