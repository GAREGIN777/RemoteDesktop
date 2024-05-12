package com.example.remotedesktop

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.remotedesktop.Adapters.ConnectDeviceItemAdapter
import com.example.remotedesktop.Firebase.Collections
import com.example.remotedesktop.Firebase.ConnectedDevice
import com.example.remotedesktop.Firebase.User
import com.example.remotedesktop.databinding.FragmentAdminSettingsBinding
import com.example.remotedesktop.databinding.FragmentConnectedDevicesBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [ConnectedDevicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConnectedDevicesFragment : Fragment() {
    private var _binding: FragmentConnectedDevicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var activity : ServerActivity
    private lateinit var adapter: ConnectDeviceItemAdapter;
    private lateinit var sharedUserData: User;
    private lateinit var firestore: FirebaseFirestore;

    private  var connectedUsers : ArrayList<User> = ArrayList<User>();

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        _binding = FragmentConnectedDevicesBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        activity = requireActivity() as ServerActivity;
        sharedUserData = activity.getUserData();

        firestore.collection(Collections.USERS_COLL).document(sharedUserData.uid).collection(Collections.CONNECTED_DEVICES_SUB_COLL).get().addOnSuccessListener {
           val gotUsersConnected = it.toObjects<ConnectedDevice>();
            val connectedDeviceTasks = mutableListOf<Task<DocumentSnapshot>>()

            for(gotUserConnected in gotUsersConnected){
            val connectedUserRef = firestore.collection(Collections.USERS_COLL).document(gotUserConnected.connectedId)

                val task = connectedUserRef.get()
                connectedDeviceTasks.add(task)
            }

            Tasks.whenAllComplete(connectedDeviceTasks)
                .addOnSuccessListener { taskResults ->
                    // Process the results
                    for (taskResult in taskResults) {
                        if (taskResult.isSuccessful) {
                            val documentSnapshot = taskResult.result as DocumentSnapshot
                            val connectedUserDevice = documentSnapshot.toObject(User::class.java);
                            if(connectedUserDevice != null){
                            connectedUsers.add(connectedUserDevice);
                            }
                        }
                    }
                    adapter = ConnectDeviceItemAdapter(requireContext(),connectedUsers);
                    binding.connectedDevicesList.adapter = adapter;
                }

            //connectedUsers = gotUsersConnected as ArrayList<User>;

            // Notify the adapter that the data set has changed
        }
        binding.connectedDevicesList.setOnItemClickListener { parent, view, position, id ->
            // Get the clicked item
            val selectedItem = adapter.getItem(position)

            // Do something with the clicked item
            if (selectedItem != null) {
                // Example: Show a toast with the clicked item's text
         Toast.makeText(context,selectedItem.uid,Toast.LENGTH_LONG).show();
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
         * @return A new instance of fragment ConnectedDevicesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConnectedDevicesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}