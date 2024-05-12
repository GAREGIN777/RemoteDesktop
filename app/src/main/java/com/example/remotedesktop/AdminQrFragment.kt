package com.example.remotedesktop

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.DeviceQrCode
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.Helpers.generateQRCodeImage
import com.example.remotedesktop.databinding.FragmentAdminQrBinding
import com.example.remotedesktop.databinding.FragmentHomeBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AdminQrFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminQrFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentAdminQrBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedUserData : User
    private lateinit var countDownTimer: CountDownTimer


    fun timePastInMillis(timestamp: Timestamp) : Long{
        val currentTime = Timestamp.now()
        val diffInMillis = currentTime.seconds - timestamp.seconds
        return diffInMillis;
    }
    fun isMoreThan30MinutesAgo(timestamp: Timestamp): Boolean {
        val diffInMillis = timePastInMillis(timestamp)
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis * 1000)
        return diffInMinutes > 30
    }

    private fun startTimer(millisInFuture : Long) {
        //val millisInFuture: Long = 30 * 60 * 1000 // 30 minutes
            val countDownInterval: Long = 1000 // 1 second

        countDownTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) % 60
                val minutes = (millisUntilFinished / (1000 * 60)) % 60
                val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24

                val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                binding.qrTimer.text = timeString
            }

            override fun onFinish() {
                binding.qrTimer.text = "00:00:00"
                recreateQrForm()
                // You can perform any action here when the timer finishes
            }
        }.start()
    }

    private fun recreateQr(){
        firestore.collection(Collections.USERS_ADMIN_QR_COLL).add(DeviceQrCode(sharedUserData.uid, Timestamp.now()).toMap()).addOnSuccessListener { documentReference ->
            sharedUserData.updateQr(documentReference.id,requireContext(),firestore)
            displayQr(documentReference.id)
            deleteQrForm()
            countDownTimer.cancel()
            startTimer(MILLIS_START_TIME)

            // Here you can use documentReference.id to get the ID of the newly created document
        }
            .addOnFailureListener { e ->
                binding.recreateQr.isEnabled = true

               Toast.makeText(context,e.localizedMessage,Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteQrForm() {
        binding.recreateQr.isEnabled = true
        binding.recreateQrForm.visibility = View.GONE
    }

    private fun recreateQrForm() {
       binding.recreateQrForm.visibility = View.VISIBLE
        binding.recreateQr.setOnClickListener{
            it.isEnabled = false
            recreateQr()
        }
    }

    private fun displayQr(qr : String){
        val qrBitmap: Bitmap = generateQRCodeImage(qr, 512, 512)
        binding.qrCode.setImageBitmap(qrBitmap)
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
        _binding = FragmentAdminQrBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

          val activity = requireActivity() as ServerActivity
       sharedUserData = activity.getUserData();
        val qr = sharedUserData.qr;
        if(qr != null){
            val qrInfo = firestore.collection(Collections.USERS_ADMIN_QR_COLL).document(qr)
            qrInfo.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val deviceQrCode = documentSnapshot.toObject(DeviceQrCode::class.java)
                        if (deviceQrCode != null) {
                            if(isMoreThan30MinutesAgo(deviceQrCode.createdAt)){
                                Toast.makeText(context,getString(R.string.qr_was_generated),Toast.LENGTH_LONG).show()
                                binding.recreateQrForm.visibility = View.VISIBLE;
                                recreateQrForm()
                            }
                            else{
                                val millisInFuture = MILLIS_START_TIME-timePastInMillis(deviceQrCode.createdAt)*1000;
                                startTimer(millisInFuture)
                            //seconds
                            }

                            // Now you can use the deviceQrCode object as needed
                        }
                    } else {
                        recreateQrForm()
                        // Handle non-existing document
                    }
                    displayQr(qr)
                }
                .addOnFailureListener { e ->
                    // Handle errors
                    Toast.makeText(context,e.localizedMessage,Toast.LENGTH_LONG).show()
                }
        }
        else{
           recreateQr()
        }



    }

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminQrFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminQrFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private  const val MILLIS_START_TIME : Long = 30*60*1000;
    }
}