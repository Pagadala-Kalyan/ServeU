package com.example.serveu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.serveu.databinding.ActivityGeminiBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class GeminiGuidanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeminiBinding

    private val apiKey = "Type API Key Here" // optional for online mode

    private var mode: String = "OFFLINE"
    private var emergencyType: String = "Emergency"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1️⃣ Inflate UI first
        binding = ActivityGeminiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2️⃣ Read intent safely
        mode = intent?.getStringExtra("MODE") ?: "OFFLINE"
        emergencyType = intent?.getStringExtra("EMERGENCY_TYPE") ?: "Emergency"

        // 3️⃣ Configure UI
        if (mode == "OFFLINE") {
            setupOfflineUI()
        } else {
            setupOnlineUI()
        }
    }

    // ---------------- OFFLINE MODE ----------------

    private fun setupOfflineUI() {
        binding.inputText.isEnabled = false
        binding.inputText.hint = "Offline guidance only"
        binding.sendBtn.isEnabled = false
        binding.sendBtn.alpha = 0.5f

        binding.responseText.text =
            "🤖 AI Safety Guidance (Offline Mode)\n\n" +
                    getOfflineGuidance(emergencyType)
    }

    private fun getOfflineGuidance(type: String): String {
        return when {
            type.contains("Accident", true) ->
                """
                1. Ensure your own safety first and move away from traffic.
                2. Do not move injured persons unless there is immediate danger.
                3. Control bleeding using clean cloth if possible.
                4. Contact emergency services immediately.
                5. Stay with the injured person until help arrives.
                """.trimIndent()

            type.contains("Medical", true) ->
                """
                1. Check breathing and consciousness.
                2. Keep the person calm and comfortable.
                3. Do not give food or water.
                4. Monitor symptoms carefully.
                5. Seek medical help immediately.
                """.trimIndent()

            else ->
                """
                1. Stay calm and avoid panic.
                2. Move to a safe and visible place.
                3. Keep your phone battery conserved.
                4. Inform someone you trust.
                5. Wait safely for assistance.
                """.trimIndent()
        }
    }

    // ---------------- ONLINE MODE ----------------

    private fun setupOnlineUI() {

        // 1️⃣ Always show safety guidelines first (even online)
        binding.responseText.text =
            "🛟 Safety Guidance\n\n" +
                    getOfflineGuidance(emergencyType)

        // 2️⃣ Then enhance with AI suggestions (if available)
        getAiGuidance(emergencyType)

        // 3️⃣ Enable user interaction
        binding.sendBtn.setOnClickListener {
            val userQuery = binding.inputText.text.toString().trim()
            if (userQuery.isNotEmpty()) {
                getAiGuidance(userQuery)
                binding.inputText.text?.clear()
            }
        }
    }


    private fun getAiGuidance(prompt: String) {
        lifecycleScope.launch {
            try {
                binding.responseText.append("\n\n🧑‍💻 You:\n$prompt")

                val client = OkHttpClient()

                val body = """
                {
                  "contents": [{
                    "parts":[{"text":"Give clear, calm safety guidance for: $prompt"}]
                  }]
                }
                """.trimIndent()

                val requestBody =
                    body.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
                    )
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                val json = JSONObject(responseBody)
                val text =
                    json.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                binding.responseText.append("\n\n🤖 Gemini:\n$text")

            } catch (e: Exception) {
                // ✅ Online fallback — NO repeated offline guidance
                binding.responseText.append(
                    "\n\n🚀 AI Assistant:\n" +
                            "Enhanced interactive guidance will be available in future versions.\n" +
                            "Please continue following the safety steps shown above."

                )
            }
        }
    }
}
