package com.example.remotedesktop.Firebase

import com.google.firebase.Timestamp
import java.io.Serializable

//firebase structure, model
    public data class ConnectedDevice(
        var connectedId : String,
        var createdAt: Timestamp,
    ) : Serializable {

        // Default constructor
        constructor() : this("123", Timestamp.now())

        // Method to convert User object to a map for Firestore
        fun toMap(): Map<String, Any?> {
            return mapOf(
                "connectedId" to connectedId,
                "createdAt" to createdAt,
            )
        }
    }

