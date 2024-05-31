package com.example.remotedesktop

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Helpers.VideoRecorder
import com.example.remotedesktop.Services.VideoRecordingService
import com.example.remotedesktop.Tags.FragmentTags
import com.example.remotedesktop.databinding.FragmentHomeBinding
import com.example.remotedesktop.databinding.FragmentUserHomeBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserHomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentUserHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedUser : User;

    fun goToMain(){
        val intent : Intent =  Intent(requireContext(),MainActivity::class.java);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent);
    }
    fun logout(){
        (requireActivity() as UserActivity).stopBgService()
        FirebaseAuth.getInstance().signOut();
        goToMain()
    }

    fun showLogoutModal(){
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_admin_logout)

        val submitBtn: Button = dialog.findViewById(R.id.submit_logout);
        val denyBtn : Button = dialog.findViewById(R.id.deny_logout);

        submitBtn.setOnClickListener{
            it.isEnabled = false
            logout()
        }

        denyBtn.setOnClickListener{
            dialog.dismiss();
        }
        dialog.show();
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedUser = (requireActivity() as UserActivity).getUserData()


        /*videoRecorder = VideoRecorder(requireContext(),this,sharedUser.uid)

         videoRecorder.startRecording()*/


        // Inflate the layout for this fragment
        _binding = FragmentUserHomeBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as UserActivity;
        binding.connectDeviceBtn.setOnClickListener{
            activity.fragmentTransaction(UserConnectDeviceFragment(),FragmentTags.USER_CONNECT_DEVICE_FRAGMENT_TAG);
        }

        binding.logoutDeviceBtn.setOnClickListener{
            showLogoutModal()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserHomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}