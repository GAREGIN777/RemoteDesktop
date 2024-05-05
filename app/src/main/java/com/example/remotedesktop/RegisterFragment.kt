package com.example.remotedesktop

import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Firebase.UserRole
import com.example.remotedesktop.Tags.FragmentTags
import com.example.remotedesktop.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestote: FirebaseFirestore


    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!


    //private val auth = Firebase.auth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //auth.useAppLanguage();
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestote = FirebaseFirestore.getInstance()
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        // Trigger Google Sign In
        val signInIntent = mGoogleSignInClient.signInIntent


        binding.emailSign.setOnClickListener{
            (activity as MainActivity).fragmentTransaction(RegisterEmailFragment(),FragmentTags.REGISTER_EMAIL_FRAGMENT_TAG)
        }

        binding.googleSign.setOnClickListener {
            binding.googleSign.isEnabled = false
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {

                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show();

                    // Google Sign In failed
                    // Handle error
            }
        }
    }

        fun firebaseAuthWithGoogle(idToken: String) {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success
                        val user = auth.currentUser
                        if (user != null) {
                            firestote.collection(Collections.USERS_COLL).document(user.uid)
                                .set(User(user.displayName,user.photoUrl.toString(),UserRole.ADMIN).toMap())
                            (activity as MainActivity).restartActivity();
                        };
                        // Update UI accordingly
                    } else {
                        binding.googleSign.isEnabled = true
                        Toast.makeText(context, task.exception?.localizedMessage,Toast.LENGTH_SHORT).show();
                        // Sign in failed
                        // Handle error
                    }
                }
        }



        companion object {
            private const val RC_SIGN_IN = 9001

            /**
             * Use this factory method to create a new instance of
             * this fragment using the provided parameters.
             *
             * @param param1 Parameter 1.
             * @param param2 Parameter 2.
             * @return A new instance of fragment RegisterFragment.
             */
            // TODO: Rename and change types and number of parameters
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                RegisterFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }
    }
