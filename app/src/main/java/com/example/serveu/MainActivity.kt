package com.example.serveu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.serveu.ai.AiService
import com.example.serveu.databinding.ActivityMainBinding
import com.example.serveu.firestore.FirestoreService
import com.example.serveu.model.Emergency
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val firestoreService = FirestoreService()
    private val aiService = AiService()

    private var selectedEmergency = "General SOS"
    private val emergencyNumber = "9440696941" // 🔴 change if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupEmergencyButtons()
        setupSendButton()
        checkLocationPermission()
    }

    // ---------------- UI SETUP ----------------

    private fun setupEmergencyButtons() {

        binding.btnBreakdown.setOnClickListener {
            selectedEmergency = "Vehicle Breakdown"
            binding.status.text = getString(R.string.vehicle_breakdown_selected)
        }

        binding.btnAccident.setOnClickListener {
            selectedEmergency = "Accident"
            binding.status.text = getString(R.string.accident_selected)
        }

        binding.btnMedical.setOnClickListener {
            selectedEmergency = "Medical Emergency"
            binding.status.text = getString(R.string.medical_emergency_selected)
        }

        binding.btnFuel.setOnClickListener {
            selectedEmergency = "Fuel / Battery Issue"
            binding.status.text = getString(R.string.fuel_battery_issue_selected)
        }
    }

    private fun setupSendButton() {
        binding.startHelpBtn.setOnClickListener {
            sendEmergencySms()
        }
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
            if (location != null) {
                updateLocationUI(location)
            } else {
                binding.locationText.text = getString(R.string.unable_to_fetch_gps)
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val lat = String.format(Locale.US, "%.6f", location.latitude)
        val lng = String.format(Locale.US, "%.6f", location.longitude)
        binding.locationText.text = getString(R.string.location_format, lat, lng)
    }

    // ---------------- SMS & ONLINE SERVICES ----------------

    private fun sendEmergencySms() {
        // Step 1: Immediately trigger SMS (Offline-First)
        val message = """
            SERVEU ALERT 🚨
            Type: $selectedEmergency
            Location: ${binding.locationText.text}
            Please help immediately.
        """.trimIndent()

        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = "smsto:$emergencyNumber".toUri()
        smsIntent.putExtra("sms_body", message)

        try {
            startActivity(smsIntent)
            binding.status.text = getString(R.string.sms_ready_to_send)

            // Step 2: After SMS is sent, proceed with online features if connected
            handleOnlineFeatures()

        } catch (_: Exception) {
            Toast.makeText(this, getString(R.string.no_sms_app_found), Toast.LENGTH_LONG).show()
            // Also handle online features here in case SMS fails but internet is available
            handleOnlineFeatures()
        }
    }

    private fun handleOnlineFeatures() {
        if (isInternetAvailable()) {
            binding.status.text = "Saving to cloud..."

            lifecycleScope.launch {
                try {
                    // Save to Firestore
                    val emergency = createEmergencyObject()
                    firestoreService.saveEmergency(emergency)
                    binding.status.text = "Admin Notified via Cloud."

                    // Fetch AI Suggestions
                    val suggestions = aiService.getSafetySuggestions(selectedEmergency)
                    showSafetySuggestions(suggestions)

                } catch (e: Exception) {
                    binding.status.text = "Online services failed. SMS sent."
                    showSafetySuggestions(aiService.getOfflineSafetySuggestions(selectedEmergency))
                }
            }
        } else {
            binding.status.text = "No internet. SMS sent."
            showSafetySuggestions(aiService.getOfflineSafetySuggestions(selectedEmergency))
        }
    }

    private fun createEmergencyObject(): Emergency {
        val locationText = binding.locationText.text.toString().split(",")
        val lat = locationText.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
        val lng = locationText.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0

        return Emergency(
            id = UUID.randomUUID().toString(),
            emergencyType = selectedEmergency,
            latitude = lat,
            longitude = lng
        )
    }

    private fun showSafetySuggestions(suggestions: String) {
        // Here, you would show the suggestions in a Dialog or a dedicated UI area.
        // For simplicity, we will use a Toast for now.
        Toast.makeText(this, "Safety Suggestions:\n$suggestions", Toast.LENGTH_LONG).show()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ---------------- PERMISSION RESULT ----------------

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
