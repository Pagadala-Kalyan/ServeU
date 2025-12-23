# 🚗 ServeU – Offline Emergency Assistance App

ServeU is an offline-first Android emergency assistance application designed to help travelers and drivers quickly send location-based distress alerts when their vehicle breaks down or an emergency occurs in remote or low-network areas.
ServeU enables users to request help even without internet access by using SMS-based emergency alerts, ensuring faster and more reliable communication during emergencies.

## Offline Mode (Core Feature)

- Large, simple emergency buttons for quick access:
    - 🚗 Vehicle Breakdown
    - 🩹 Medical Emergency
    - 🚨 Accident
    - 🔋 Fuel / Battery Issue
- Automatically captures GPS location (latitude & longitude)
- Generates an emergency SMS with location details
- Sends alerts to emergency contacts, service centers, and highway patrol (configurable)
- Designed for use during stressful situations with minimal interaction

## Online Mode (Future Scope)

- Firebase logging of emergency events
- AI-powered safety guidance (Gemini)
- Live assistance tracking
- Nearby service provider discovery

## Why Offline-First?

- Works without internet connectivity
- SMS functions even in low-network conditions
- Faster emergency response
- Reliable for highways and rural regions
- Low battery and data usage

## Tech Stack

### Mobile Application
- Platform: Android
- Language: Kotlin
- UI: XML (Material Design)
- Minimum SDK: Android 7.0 (API 24)

### Device Features
- GPS / Location Services
- SMS Manager
- Runtime Permissions

### Tools & Services
- Android Studio
- Android SDK
- Google Play Services (Location)
- Git & GitHub

## Features Implemented (MVP)

- Offline emergency user interface
- Emergency button-based alert system
- GPS location capture
- SMS message generation and sending
- Permission handling for location and SMS

## Future Enhancements

- Firebase backend integration
- AI-powered safety instructions
- Live tracking and ETA updates
- Multi-language support
- Crash detection using device sensors

## Prerequisites

### System Requirements
- Windows / macOS / Linux
- Minimum 8 GB RAM recommended

### Software Requirements
- Android Studio (latest stable version)
- Java JDK 17 or above
- Android SDK (API level 24 or higher)
- Git

### Device Requirements
- Android Emulator or Physical Android Device
- Location services enabled
- SMS capability (recommended for testing)

## How to Run the Project

1. Clone the repository:
   git clone https://github.com/your-username/ServeU.git

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an emulator or physical device

5. Grant required permissions:
    - Location
    - SMS

6. Tap any emergency button to trigger the alert flow

## Testing Notes

- SMS functionality works best on a real device
- Emulator can be used for UI and logic testing
- Demo phone numbers are used for hackathon safety

## 📜 License

This project is open-source and intended for educational and hackathon purposes.
