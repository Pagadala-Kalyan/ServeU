package com.example.serveu.model

data class EmergencyRequest(
    var id: String = "",
    var userPhoneNumber: String = "",
    var emergencyContact: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timestamp: Long = 0L,
    var emergencyType: String = ""   // 🔥 ADD THIS if missing
)
