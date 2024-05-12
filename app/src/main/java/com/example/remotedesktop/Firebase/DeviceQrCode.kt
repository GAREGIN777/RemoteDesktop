package com.example.remotedesktop.Firebase

import com.google.firebase.Timestamp
import java.io.Serializable
import java.util.concurrent.TimeUnit

//firebase structure, model
public data class DeviceQrCode(
    var uid : String,
    var createdAt: Timestamp,
) : Serializable {

    // Default constructor
    constructor() : this("123", Timestamp.now())

    // Method to convert User object to a map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "createdAt" to createdAt,
        )
    }

    companion object{
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
        fun isValid(gotQr : DeviceQrCode) : Boolean{
            return !isMoreThan30MinutesAgo(gotQr.createdAt);

        }
    }
}


