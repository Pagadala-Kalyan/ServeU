package com.example.serveu.ai

object LocalAdvice {

    fun get(type: String): List<String> {
        return when (type.lowercase()) {

            "vehicle breakdown",
            "fuel / battery issue" -> listOf(
                "Turn on hazard lights",
                "Move away from traffic",
                "Do not stand on the road",
                "Conserve phone battery"
            )

            "accident" -> listOf(
                "Move to a safe location if possible",
                "Avoid unnecessary movement",
                "Stay calm and visible",
                "Wait for help"
            )

            "medical emergency" -> listOf(
                "Sit or lie down safely",
                "Avoid sudden movements",
                "Breathe slowly",
                "Call emergency services if condition worsens"
            )

            else -> listOf(
                "Stay calm",
                "Help has been notified",
                "Remain at a safe location"
            )
        }
    }
}
