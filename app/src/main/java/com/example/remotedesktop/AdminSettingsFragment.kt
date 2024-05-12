package com.example.remotedesktop

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.remotedesktop.Firebase.ParamChangeListener
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.databinding.FragmentAdminQrBinding
import com.example.remotedesktop.databinding.FragmentAdminSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminSettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var firestore: FirebaseFirestore;
    private var _binding: FragmentAdminSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedUserData : User



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut();
        val intent : Intent =  Intent(requireContext(),MainActivity::class.java);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent);
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
    fun showChangeNameModal(){
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_admin_change_name)
        /*val body = dialog.findViewById(R.id.change_name_btn) as TextView
        body.text = title*/


        val nameInput: EditText = dialog.findViewById(R.id.change_name_input);
        val submitNameBtn: Button = dialog.findViewById(R.id.change_name_btn);
        val closeModal: ImageButton = dialog.findViewById(R.id.close_modal_btn);




       /* val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }


        val noBtn = dialog.findViewById(R.id.noBtn) as Button
        .setOnClickListener {
            dialog.dismiss()
        }*/
        closeModal.setOnClickListener{
            dialog.dismiss();
        }
        submitNameBtn.setOnClickListener{
            it.isEnabled = false
            val userName = nameInput.text.toString();
            if(sharedUserData.isNameValid(userName)) {
                sharedUserData.updateName(userName, object : ParamChangeListener {
                    override fun onResult(statusMessage: String, status: Boolean) {
                        if(status){
                            binding.username.setText(statusMessage)
                            Toast.makeText(context,getString(R.string.success),Toast.LENGTH_LONG).show();
                        }
                        else {
                            val resMessage = if (statusMessage == User.UNKNOWN_ERROR_MESSAGE)  getString(R.string.unknown_error) else statusMessage;
                            Toast.makeText(context, statusMessage, Toast.LENGTH_LONG).show();
                        }
                        it.isEnabled = true;
                        dialog.dismiss();
                    }
                });
            }
            else {
                it.isEnabled = true;
                Toast.makeText(context,getString(R.string.invalid_username_error),Toast.LENGTH_LONG).show();
            }
        }
        dialog.show()
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAdminSettingsBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance();
        val activity = requireActivity() as ServerActivity
        sharedUserData = activity.getUserData();

        val photoImage = sharedUserData.photoUrl;
        if(photoImage != null){
            binding.profileImage.visibility = View.VISIBLE
            Glide.with(this).load(photoImage).into(binding.profileImage)
        }
        binding.username.setText(sharedUserData.name)
        binding.adminSettingsList.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.admin_settings_change_name -> {
                    // Do something when menu item 1 is clicked
                    showChangeNameModal()
                    true
                }
                R.id.admin_settings_logout -> {
                    showLogoutModal()
                    // Do something when menu item 2 is clicked
                    true
                }

                else -> false
            }
        }


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}