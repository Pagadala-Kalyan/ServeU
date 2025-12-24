package com.example.serveu.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiService {

    // This would be your actual Gemini/OpenAI client
    // For now, we will simulate the network call

    suspend fun getSafetySuggestions(emergencyType: String): String {
        return withContext(Dispatchers.IO) {
            // Simulate network latency
            Thread.sleep(1500)

            // The internal prompt sent to the AI
            val internalPrompt = """
                You are an AI assistant for the ServeU app, providing safety suggestions.
                The user is in an emergency: [$emergencyType].

                Generate 4-5 short, calm, step-by-step safety suggestions.
                - Focus on immediate safety, visibility, and staying calm.
                - DO NOT give medical diagnosis or treatment advice.
                - DO NOT give complex mechanical repair instructions.
                - DO NOT suggest anything dangerous or illegal.

                Your response should be a simple, bulleted list.
            """.trimIndent()

            // In a real app, you would send `internalPrompt` to your AI API
            // and return the response. Here, we return a hardcoded response.
            getOfflineSafetySuggestions(emergencyType)
        }
    }

    fun getOfflineSafetySuggestions(emergencyType: String): String {
        return when (emergencyType) {
            "Vehicle Breakdown" -> """
                - Safely pull over to the side of the road.
                - Turn on your hazard lights.
                - Place warning triangles if you have them.
                - Stay in your vehicle if it's unsafe to exit.
            """
            "Accident" -> """
                - Check for injuries and call 911 if needed.
                - Move to a safe location away from traffic.
                - Turn on your hazard lights.
                - Do not move your vehicle unless necessary for safety.
            """
            "Medical Emergency" -> """
                - Stay calm and assess the situation.
                - Call 911 if you haven't already.
                - Provide comfort to the person in need.
                - Do not move someone who may have a spinal injury.
            """
            else -> """
                - Stay calm and in a safe location.
                - Turn on your hazard lights to be more visible.
                - Wait for professional help to arrive.
                - Keep your phone on and ready for calls.
            """
        }.trimIndent()
    }
}
