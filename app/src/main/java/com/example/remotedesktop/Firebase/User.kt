package com.example.remotedesktop.Firebase

enum class UserRole(s : String) {
    ADMIN("ADMIN"),
    USER("USER"),
}

data class User// Constructor
    (// Getters and Setters
   var name: String?,var photoUrl:String?,var role:UserRole
) {

    // Constructor to create an instance of GoogleUser from a DocumentSnapshot

    // Method to convert GoogleUser object to a map for Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf("name" to name,"photoUrl" to photoUrl,"userRole" to role)
    }
}

