package com.example.serveu.ai

object AiApi {

    suspend fun fetchAdvice(
        type: String,
        note: String
    ): List<String> {

        // 🔴 PLACEHOLDER FOR REAL AI API CALL
        // Later you will replace this with Gemini/OpenAI

        return when (type.lowercase()) {

            "vehicle breakdown" -> listOf(
                "Turn on hazard lights immediately",
                "Move yourself to a safe distance from traffic",
                "Avoid attempting repairs on the road",
                "Stay visible and wait for assistance"
            )

            "accident" -> listOf(
                "Move to safety if possible",
                "Avoid sudden movement",
                "Signal for help",
                "Stay calm and wait"
            )

            "medical emergency" -> listOf(
                "Sit or lie down safely",
                "Breathe slowly",
                "Avoid physical strain",
                "Seek medical help immediately"
            )

            else -> listOf(
                "Stay calm",
                "Help is on the way",
                "Remain in a safe location"
            )
        }
    }
}
