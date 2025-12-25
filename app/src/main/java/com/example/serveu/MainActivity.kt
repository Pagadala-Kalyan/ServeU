package com.example.serveu

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedEmergency = "General SOS"
    private val emergencyNumber = "9440696941"

    // ✅ SMS RESULT HANDLER
    private val smsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            // User returned from SMS app → now open AI guidance
            val intent = Intent(this, GeminiGuidanceActivity::class.java)
            intent.putExtra("MODE", "OFFLINE")
            intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
            startActivity(intent)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()
        checkLocationPermission()
    }

    // ---------------- EMERGENCY SELECTION ----------------

    private fun setupEmergencyButtons() {
        binding.btnBreakdown.setOnClickListener {
            selectedEmergency = "Vehicle Breakdown"
            binding.status.text = "Vehicle Breakdown selected"
        }

        binding.btnAccident.setOnClickListener {
            selectedEmergency = "Accident"
            binding.status.text = "Accident selected"
        }

        binding.btnMedical.setOnClickListener {
            selectedEmergency = "Medical Emergency"
            binding.status.text = "Medical Emergency selected"
        }

        binding.btnFuel.setOnClickListener {
            selectedEmergency = "Fuel / Battery Issue"
            binding.status.text = "Fuel / Battery Issue selected"
        }
    }

    // ---------------- SEND LOGIC ----------------

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {
            if (isInternetAvailable()) {
                sendOnlineEmergency()
            } else {
                sendEmergencySms()
            }
        }
    }

    // ---------------- ONLINE MODE ----------------

    private fun sendOnlineEmergency() {
        val db = FirebaseFirestore.getInstance()

        val emergency = hashMapOf(
            "type" to selectedEmergency,
            "location" to binding.locationText.text.toString(),
            "timestamp" to System.currentTimeMillis(),
            "status" to "PENDING"
        )

        db.collection("emergencies")
            .add(emergency)
            .addOnSuccessListener {
                binding.status.text = "🌐 Emergency sent online"

                val intent = Intent(this, GeminiGuidanceActivity::class.java)
                intent.putExtra("MODE", "ONLINE")
                intent.putExtra("EMERGENCY_TYPE", selectedEmergency)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Online failed, using SMS", Toast.LENGTH_SHORT).show()
                sendEmergencySms()
            }
    }

    // ---------------- OFFLINE MODE (SMS) ----------------

    private fun sendEmergencySms() {

        val message = """
            SERVEU ALERT 🚨
            Type: $selectedEmergency
            Location: ${binding.locationText.text}
            Please help immediately.
        """.trimIndent()

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$emergencyNumber")
            putExtra("sms_body", message)
        }

        Toast.makeText(
            this,
            "Please send the SMS. Safety guidance will appear after.",
            Toast.LENGTH_LONG
        ).show()

        // ✅ IMPORTANT: launch via launcher, NOT startActivity
        smsLauncher.launch(smsIntent)
    }

    // ---------------- LOCATION ----------------

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        } else {
            fetchLocation()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) updateLocationUI(location)
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lng = String.format(Locale.US, "%.6f", location.longitude)
        binding.locationText.text = "$lat , $lng"
    }

    // ---------------- INTERNET CHECK ----------------

    private fun isInternetAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ---------------- PERMISSIONS ----------------

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        }
    }
}
