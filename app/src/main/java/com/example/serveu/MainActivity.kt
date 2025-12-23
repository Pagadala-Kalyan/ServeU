package com.example.serveu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serveu.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.nio.charset.Charset
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val serviceId = "com.serveu.LOCATION_SHARE"
    private val strategy = Strategy.P2P_STAR
    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var connectedEndpointId: String? = null
    private val requestCodePermissions = 1
    private var isAdvertising = false
    private var isDiscovering = false

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            binding.status.text = "ServeU connecting to ${connectionInfo.endpointName}"
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                binding.status.text = "ServeU Connected! Live GPS sharing."
                binding.stopBtn.visibility = View.VISIBLE
                binding.startHelpBtn.visibility = View.GONE
            } else {
                binding.status.text = "Connection failed. Tap Start again."
            }
        }

        override fun onDisconnected(endpointId: String) { resetUI() }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection("ServeU-HELP", endpointId, connectionLifecycleCallback)
        }
        override fun onEndpointLost(endpointId: String) {}
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val locationData = String(it, Charset.defaultCharset())
                binding.status.text = "RECEIVED SOS: $locationData"
            }
        }
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectionsClient = Nearby.getConnectionsClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.startHelpBtn.setOnClickListener { startServeU() }
        binding.stopBtn.setOnClickListener { stopServeU() }
        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), requestCodePermissions)
        } else {
            startLocationUpdates()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodePermissions && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "GPS + Bluetooth permissions required!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).apply {
            setMinUpdateIntervalMillis(1000L)
        }.build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val lat = String.format("%.6f", location.latitude)
                val lng = String.format("%.6f", location.longitude)
                binding.locationText.text = "My GPS: $lat, $lng"
                connectedEndpointId?.let { sendLocation("$lat,$lng") }
            }
        }
    }

    private fun sendLocation(locationData: String) {
        val payload = Payload.fromBytes(locationData.toByteArray(Charset.defaultCharset()))
        connectedEndpointId?.let { connectionsClient.sendPayload(it, payload) }
    }

    private fun startServeU() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        connectionsClient.startAdvertising("ServeU-HELP-${Random.nextInt(9999)}", serviceId,
            connectionLifecycleCallback, advertisingOptions)

        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        connectionsClient.startDiscovery(serviceId, endpointDiscoveryCallback, discoveryOptions)

        binding.status.text = "🔍 ServeU scanning 100m range..."
        isAdvertising = true
        isDiscovering = true
    }

    private fun stopServeU() {
        connectionsClient.stopAllEndpoints()
        if (isAdvertising) connectionsClient.stopAdvertising()
        if (isDiscovering) connectionsClient.stopDiscovery()
        resetUI()
    }

    private fun resetUI() {
        binding.status.text = "🚗 ServeU: Offline SOS Ready"
        binding.locationText.text = "📍 GPS: Acquiring signal..."
        binding.startHelpBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        connectedEndpointId = null
        isAdvertising = false
        isDiscovering = false
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopServeU()
    }
}
