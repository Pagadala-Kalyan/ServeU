package com.example.serveu.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serveu.adapter.EmergencyAdapter
import com.example.serveu.databinding.ActivityAdminDashboardBinding
import com.example.serveu.model.EmergencyRequest
import com.google.firebase.database.*
import android.content.Intent


class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: EmergencyAdapter

    // 🔥 MUST be mutable list (used by adapter)
    private val emergencyRequests = mutableListOf<EmergencyRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
            .getReference("emergency_requests")

        setupRecyclerView()
        listenForEmergencyRequests()
        binding.btnLogout.setOnClickListener {
            logout()
        }

    }
    private fun logout() {
        val prefs = getSharedPreferences("ServeU", MODE_PRIVATE)
        prefs.edit().clear().apply()

        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ---------------- RecyclerView ----------------

    private fun setupRecyclerView() {
        adapter = EmergencyAdapter(emergencyRequests) { requestId ->
            deleteRequest(requestId)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // ---------------- Firebase Listener ----------------

    private fun listenForEmergencyRequests() {
        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                emergencyRequests.clear()

                for (child in snapshot.children) {
                    val request = child.getValue(EmergencyRequest::class.java)
                    if (request != null) {
                        request.id = child.key ?: ""
                        emergencyRequests.add(request)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // You can show a Snackbar/Toast if needed
            }
        })
    }

    // ---------------- Delete ----------------

    private fun deleteRequest(requestId: String) {
        database.child(requestId).removeValue()
    }
}
