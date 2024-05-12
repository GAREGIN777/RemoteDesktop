package com.example.remotedesktop

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Tags.FragmentTags
import com.example.remotedesktop.databinding.ActivityMainBinding
import com.example.remotedesktop.databinding.ActivityServerBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import org.checkerframework.checker.units.qual.A
import java.io.Serializable


class ServerActivity : AppCompatActivity() {
    private lateinit var binding : ActivityServerBinding
    private lateinit var menu : BottomNavigationView
    private lateinit var userData : User;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userData = intent.getSerializableExtra("firestoreUser") as User




        menu = binding.menu;


        val homeFragment : HomeFragment = HomeFragment();

        binding.menu.setOnItemSelectedListener { item ->
            // Handle item selection based on the selected fragment
            when (item.itemId) {
                R.id.action_home -> fragmentTransaction(HomeFragment(),FragmentTags.HOME_FRAGMENT_TAG)//setFragmentAndMenu()
                R.id.action_settings -> fragmentTransaction(AdminSettingsFragment(),FragmentTags.ADMIN_SETTINGS_FRAGMENT_TAG)//setFragmentAndMenu(FragmentWithTags.FRAGMENT_TWO)
                R.id.action_connected_devices -> fragmentTransaction(ConnectedDevicesFragment(),FragmentTags.CONNECTED_DEVICES_FRAGMENT_TAG)
            // Handle other menu items if needed
            }
            true
         }



        supportFragmentManager.beginTransaction()
            .replace(binding.mainFragmentContainer.id,homeFragment)
            .commit();


    }


    fun getUserData(): User {
        return userData
    }

    fun fragmentTransaction(
        fragment: Fragment,
        tag: String
    ) {
        supportFragmentManager.beginTransaction().replace(binding.mainFragmentContainer.id,fragment).addToBackStack(tag).commit();
        setBottomMenuForFragment(tag)
    }


    private fun setBottomMenuForFragment(tag: String) {
        // Set the menu for the fragment based on the enum constant
        val menuExists = when (tag) {
            FragmentTags.HOME_FRAGMENT_TAG,FragmentTags.ADMIN_SETTINGS_FRAGMENT_TAG -> true
            // Add cases for other fragments as needed
            else -> false // Default menu
        }


    }

    }
