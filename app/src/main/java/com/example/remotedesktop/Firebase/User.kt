package com.example.remotedesktop.Firebase


import java.io.Serializable


enum class UserRole(s : String) {
    ADMIN("ADMIN"),
    USER("USER"),
}


data class User(
    var name: String?,
    var photoUrl: String?,
    var role: UserRole
) : Serializable {

    // Default constructor
    constructor() : this(null, null, UserRole.ADMIN)

    // Method to convert User object to a map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "photoUrl" to photoUrl,
            "userRole" to role
        )
    }
}

