package com.example.serveu.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serveu.databinding.ItemEmergencyRequestBinding
import com.example.serveu.model.EmergencyRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyAdapter(
    private val emergencyRequests: MutableList<EmergencyRequest>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EmergencyAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemEmergencyRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(request: EmergencyRequest) {

            // 🚨 Emergency Type
            binding.tvEmergencyType.text = request.emergencyType

            // Accent color by type
            binding.typeAccent.setBackgroundColor(
                when (request.emergencyType) {
                    "Accident" -> Color.parseColor("#DC2626")
                    "Medical Emergency" -> Color.parseColor("#F59E0B")
                    "Vehicle Breakdown" -> Color.parseColor("#2563EB")
                    else -> Color.parseColor("#64748B")
                }
            )

            // 📱 User phone
            binding.tvUserPhoneNumber.text =
                request.userPhoneNumber.ifEmpty { "Not available" }

            // 🚨 Emergency contact
            binding.tvEmergencyContact.text =
                request.emergencyContact

            // 📍 Location
            binding.tvLocation.text =
                "${request.latitude}, ${request.longitude}"

            // 🕒 Timestamp
            binding.tvTimestamp.text = formatTime(request.timestamp)

            // 🗑 Resolve / Delete
            binding.btnDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    animateDelete(pos, request.id)
                }
            }
        }

        private fun animateDelete(position: Int, id: String) {
            binding.root.animate()
                .translationX(binding.root.width.toFloat())
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    emergencyRequests.removeAt(position)
                    notifyItemRemoved(position)
                    onDeleteClick(id)
                }
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmergencyRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(emergencyRequests[position])
    }

    override fun getItemCount(): Int = emergencyRequests.size

    // 🕒 timestamp → readable time
    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
