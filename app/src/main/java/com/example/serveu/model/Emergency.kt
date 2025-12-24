package com.example.serveu.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Emergency(
    val id: String = "",
    val emergencyType: String = "",
    val description: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Admin Notified",
    @ServerTimestamp val timestamp: Date? = null
)
