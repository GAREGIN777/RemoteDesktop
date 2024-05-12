package com.example.remotedesktop.Firebase


import android.content.Context
import com.example.remotedesktop.Helpers.isNetworkConnected
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.Serializable


enum class UserRole(s : String) {
    ADMIN("ADMIN"),
    USER("USER"),
}


interface ParamChangeListener{
    fun onResult(statusMessage : String,status : Boolean)
}

public data class User(
    var uid : String,
    var name: String,
    var photoUrl: String?,
    var role: UserRole,
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
            "userRole" to role,
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

    companion object{
        const val SUCCESS_MESSAGE = "Success";
        const val UNKNOWN_ERROR_MESSAGE = "Unknown Error";
    }

}

