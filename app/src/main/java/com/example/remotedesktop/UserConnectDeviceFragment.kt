package com.example.remotedesktop

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.DeviceQrCode
import com.example.remotedesktop.Firebase.ParamChangeListener
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.databinding.FragmentUserConnectDeviceBinding
import com.example.remotedesktop.databinding.FragmentUserHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserConnectDeviceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserConnectDeviceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var sharedUserData : User

    private var _binding: FragmentUserConnectDeviceBinding? = null
    private val binding get() = _binding!!

    private val scanLauncher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
            firestore.collection(Collections.USERS_ADMIN_QR_COLL)
                .document(result.contents)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            val gotQr: DeviceQrCode? = document.toObject(DeviceQrCode::class.java)
                            if (gotQr != null && DeviceQrCode.isValid(gotQr)) {
                                sharedUserData.connectUserToAdmin(gotQr.uid, object : ParamChangeListener {
                                    override fun onResult(statusMessage: String, status: Boolean) {
                                        if (status) {
                                            Toast.makeText(context, getString(R.string.success), Toast.LENGTH_LONG)
                                                .show()
                                        } else {
                                            val resMessage = when (statusMessage) {
                                                User.UNKNOWN_ERROR_MESSAGE -> getString(R.string.unknown_error)
                                                User.USER_TO_CONNECT_NOT_FOUND -> getString(R.string.connect_user_not_found)
                                                User.ALREADY_CONNECTED_DEVICE -> getString(R.string.alredy_connected)
                                                else -> statusMessage
                                            }
                                            Toast.makeText(context, resMessage, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                })
                            }
                            else {
                                Toast.makeText(context, getString(R.string.qr_was_generated), Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, getString(R.string.qr_was_generated), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Handle unsuccessful query
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
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
        // Inflate the layout for this fragment
        _binding = FragmentUserConnectDeviceBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedUserData = (requireActivity() as UserActivity).getUserData()
        binding.connectDeviceBtn.setOnClickListener {
            if (hasCameraPermission()) {
                startQRScanner()
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun startQRScanner() {
        //Toast.makeText(context,"Reading QR code",Toast.LENGTH_SHORT).show();
        val scanOptions = ScanOptions()
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        scanOptions.setPrompt("Reading QR code")
        scanOptions.setCameraId(0)
        scanOptions.setBarcodeImageEnabled(true)
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(false)
        scanOptions.setTorchEnabled(false)
        scanLauncher.launch(scanOptions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }




    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1;
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserConnectDeviceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserConnectDeviceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}