package com.example.remotedesktop.Firebase


import android.content.Context
import com.example.remotedesktop.Helpers.isNetworkConnected
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import java.io.Serializable


enum class UserRole(s : String) {
    ADMIN("ADMIN"),
    USER("USER"),
}


interface ParamChangeListener{ fun onResult(statusMessage : String,status : Boolean)}

public data class User(
    var uid : String,
    var name: String,
    var photoUrl: String?,
    var userRole: UserRole,
    var qr : String? = null // only admin users have access
) : Serializable {

    // Default constructor

    constructor() : this("", "",null, UserRole.ADMIN,null)

    // Method to convert User object to a map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "photoUrl" to photoUrl,
            "userRole" to userRole,
            "qr" to qr
        )
    }

    //setter
     fun updateQr(newQr : String,ctx: Context,firestore: FirebaseFirestore){
        if(isNetworkConnected(ctx)) {
            val qr = this.qr
            if (qr != null) {
                firestore.collection(Collections.USERS_ADMIN_QR_COLL).document(qr).delete()
            }
            this.qr = newQr;
            firestore.collection(Collections.USERS_COLL).document(uid)
                .set(this.toMap(), SetOptions.merge())
        }
     }

    fun isNameValid(_name : String) : Boolean{
        val length = _name.trim().length;
        return (length in 1..19)
    }

    fun updateName(newName : String, listener: ParamChangeListener){
        val firestore : FirebaseFirestore  = FirebaseFirestore.getInstance();
        val trimmedName = newName.trim();
        if(isNameValid(trimmedName)) {
            this.name = trimmedName;
            firestore.collection(Collections.USERS_COLL).document(uid)
                .set(this.toMap(), SetOptions.merge()).addOnCompleteListener {
                    if(it.isSuccessful){
                        listener.onResult(trimmedName,true)
                    }
                    else{
                        val exception = it.exception;
                        val message = exception?.localizedMessage ?: UNKNOWN_ERROR_MESSAGE
                        listener.onResult(message,false);
                    }
                }
        }
    }
    fun connectUserToAdmin(adminId: String, listener: ParamChangeListener) {
        if (userRole != UserRole.USER) return

        val firestore = FirebaseFirestore.getInstance()
        val adminRef = firestore.collection(Collections.USERS_COLL).document(adminId)
        val userRef = firestore.collection(Collections.USERS_COLL).document(uid)

        getUserDocument(userRef) { userDoc ->
            if (!userDoc.exists()) {
                listener.onResult(USER_TO_CONNECT_NOT_FOUND, false)
                return@getUserDocument
            }

            getAdminDocument(adminRef) { adminDoc ->
                if (!adminDoc.exists()) {
                    listener.onResult(USER_TO_CONNECT_NOT_FOUND, false)
                    return@getAdminDocument
                }

                connectUserAndAdmin(adminRef, userRef, listener)
            }
        }
    }

    private fun getUserDocument(userRef: DocumentReference, callback: (DocumentSnapshot) -> Unit) {
        userRef.get()
            .addOnSuccessListener { userDoc ->
                callback(userDoc)
            }
    }

    private fun getAdminDocument(adminRef: DocumentReference, callback: (DocumentSnapshot) -> Unit) {
        adminRef.get()
            .addOnSuccessListener { adminDoc ->
                callback(adminDoc)
            }
    }

    private fun connectUserAndAdmin(adminRef: DocumentReference, userRef: DocumentReference, listener: ParamChangeListener) {
        val adminConnected = ConnectedDevice(uid, Timestamp.now())
        val userConnected = ConnectedDevice(adminRef.id, Timestamp.now())

        val adminConnectedRef = adminRef.collection(Collections.CONNECTED_DEVICES_SUB_COLL)
            .document(adminConnected.connectedId)
        val userConnectedRef = userRef.collection(Collections.CONNECTED_DEVICES_SUB_COLL)
            .document(userConnected.connectedId)

        checkIfAlreadyConnected(adminConnectedRef, userConnectedRef) { alreadyConnected ->
            if (alreadyConnected) {
                listener.onResult(ALREADY_CONNECTED_DEVICE, false)
            } else {
                val batch = FirebaseFirestore.getInstance().batch()
                batch.set(adminConnectedRef, adminConnected.toMap())
                batch.set(userConnectedRef, userConnected.toMap())

                batch.commit()
                    .addOnSuccessListener {
                        listener.onResult(SUCCESS_MESSAGE, true)
                    }
                    .addOnFailureListener { e ->
                        val message = e.localizedMessage ?: UNKNOWN_ERROR_MESSAGE
                        listener.onResult(message, false)
                    }
            }
        }
    }

    private fun checkIfAlreadyConnected(adminConnectedRef: DocumentReference, userConnectedRef: DocumentReference, callback: (Boolean) -> Unit) {
        adminConnectedRef.get()
            .addOnSuccessListener { adminConnectedDoc ->
                userConnectedRef.get()
                    .addOnSuccessListener { userConnectedDoc ->
                        val alreadyConnected = adminConnectedDoc.exists() || userConnectedDoc.exists()
                        callback(alreadyConnected)
                    }
            }
    }

    /*fun connectUserToAdmin(adminId: String, listener: ParamChangeListener) {
        if (userRole == UserRole.USER) {
            val firestore = FirebaseFirestore.getInstance()
            val adminRef = firestore.collection(Collections.USERS_COLL).document(adminId)
            val userRef = firestore.collection(Collections.USERS_COLL).document(uid)

            userRef.get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        adminRef.get()
                            .addOnSuccessListener { adminDoc ->
                                if (adminDoc.exists()) {
                                    val adminConnected = ConnectedDevice(uid, Timestamp.now())
                                    val userConnected = ConnectedDevice(adminId, Timestamp.now())

                                    val adminConnectedRef = adminRef.collection(Collections.CONNECTED_DEVICES_SUB_COLL)
                                        .document(adminConnected.connectedId)
                                    val userConnectedRef = userRef.collection(Collections.CONNECTED_DEVICES_SUB_COLL)
                                        .document(userConnected.connectedId)

                                    val batch: WriteBatch = firestore.batch()

                                    adminConnectedRef.get()
                                        .addOnSuccessListener { adminConnectedDoc ->
                                            userConnectedRef.get()
                                                .addOnSuccessListener { userConnectedDoc ->
                                                    if (adminConnectedDoc.exists() && userConnectedDoc.exists()) {
                                                        listener.onResult(ALREADY_CONNECTED_DEVICE, false)
                                                    } else {
                                                        batch.set(adminConnectedRef, adminConnected.toMap())
                                                        batch.set(userConnectedRef, userConnected.toMap())

                                                        batch.commit()
                                                            .addOnSuccessListener {
                                                                listener.onResult(SUCCESS_MESSAGE, true)
                                                            }
                                                            .addOnFailureListener { e ->
                                                                val message = e.localizedMessage ?: UNKNOWN_ERROR_MESSAGE
                                                                listener.onResult(message, false)
                                                            }
                                                    }
                                                }
                                        }
                                } else {
                                    listener.onResult(USER_TO_CONNECT_NOT_FOUND, false)
                                }
                            }
                    } else {
                        listener.onResult(USER_TO_CONNECT_NOT_FOUND, false)
                    }
                }
        }
    }*/

    fun updateRole(newRole : UserRole, listener: ParamChangeListener){
        val firestore : FirebaseFirestore  = FirebaseFirestore.getInstance();
            this.userRole = newRole;
            firestore.collection(Collections.USERS_COLL).document(uid)
                .set(this.toMap(), SetOptions.merge()).addOnCompleteListener {
                    if(it.isSuccessful){
                        listener.onResult(SUCCESS_MESSAGE,true)
                    }
                    else{
                        val exception = it.exception;
                        val message = exception?.localizedMessage ?: UNKNOWN_ERROR_MESSAGE
                        listener.onResult(message,false);
                    }
                }
    }

    companion object{
        const val SUCCESS_MESSAGE = "Success";
        const val UNKNOWN_ERROR_MESSAGE = "Unknown Error";
        const val ALREADY_CONNECTED_DEVICE = "Already Connected";
        const val USER_TO_CONNECT_NOT_FOUND = "User for connection not found"

    }

}

