package com.example.remotedesktop

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Firebase.UserRole
import com.example.remotedesktop.databinding.FragmentRegisterBinding
import com.example.remotedesktop.databinding.FragmentRegisterEmailBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [RegisterEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterEmailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestote: FirebaseFirestore


    private var _binding: FragmentRegisterEmailBinding? = null
    private val binding get() = _binding!!


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
        // Inflate the layout for this fragment
        _binding = FragmentRegisterEmailBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        firestote = FirebaseFirestore.getInstance()

        binding.submit.setOnClickListener {
        it.isEnabled = false
            val email = binding.emailInputLayout.editText?.text.toString().trim();
            val password = binding.passwordInputLayout.editText?.text.toString().trim();

            signIn(email,password)
        }

    }

    private fun signIn(email: String, password: String) {

if(email.isNotEmpty() && password.isNotEmpty()) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                val user = auth.currentUser
                if (user != null) {
                    val userFirestore = User(Build.MODEL, null,UserRole.ADMIN).toMap();
                    firestote.collection(Collections.USERS_COLL).document(user.uid)
                        .set(userFirestore)
                    (activity as MainActivity).restartActivity();
                };
                // You can navigate to another activity or update UI here
            } else {
                task.exception?.let { exception ->
                    val errorMessage = exception.message ?: "Authentication failed."
                    if(errorMessage.contains("in use",true)){
                        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(requireActivity()){
                            if(it.isSuccessful){
                                (activity as MainActivity).restartActivity();
                            }
                            else{
                                it.exception?.let {
                                    val loginErrorMessage = it.message ?: "Authentication failed."
                                    if (loginErrorMessage.contains("email", true)) {
                                    binding.emailInputLayout.error = exception.localizedMessage
                                    hideErrorAfterDelay(binding.emailInputLayout)
                                    // Handle email-related errors
                                    }
                                    else if (loginErrorMessage.contains("password", true)) {
                                        binding.passwordInputLayout.error = exception.localizedMessage
                                        hideErrorAfterDelay(binding.passwordInputLayout)
                                        // Handle password-related errors
                                    }
                                    Toast.makeText(context, it.localizedMessage?.plus("${email} - ${password}") ?: it.localizedMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    else if (errorMessage.contains("email", true)) {
                        binding.emailInputLayout.error = exception.localizedMessage
                        hideErrorAfterDelay(binding.emailInputLayout)
                        // Handle email-related errors
                    } else if (errorMessage.contains("password", true)) {
                        binding.passwordInputLayout.error = exception.localizedMessage
                        hideErrorAfterDelay(binding.passwordInputLayout)
                        // Handle password-related errors
                    }
                    Toast.makeText(context, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    // If sign in fails, display a message to the user.
                }
                binding.submit.isEnabled = true
            }
        }
}
        else{
         binding.submit.isEnabled = true
        }


    }

    private fun hideErrorAfterDelay(textInputLayout: TextInputLayout) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // Delay for 3 seconds
            textInputLayout.error = null // Hide the error message
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterEmailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterEmailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}