# 🚗 RideShield India

<div align="center">

### Smart • Secure • Connected Mobility

**AI-Powered Car & Bike Rental Platform for India**

Real-Time GPS Tracking • Smart Vehicle Security • AI Assistance • Fleet Management • Android & iOS Apps

</div>

---

## 📖 Overview

RideShield India is a next-generation vehicle rental platform designed for the Indian market.

The platform allows users to rent cars and bikes while providing vehicle owners with advanced fleet management tools, real-time tracking, security monitoring, and analytics.

RideShield combines the convenience of modern ride-sharing platforms with enhanced security technologies such as GPS tracking, geofencing, CCTV integration, emergency SOS, accident detection, and AI-powered assistance.

---

## ✨ Key Features

### 👤 User Features

- Mobile OTP Authentication
- Google Login
- Apple Login
- Driving License Verification
- Aadhaar Verification
- Search Nearby Vehicles
- Real-Time Vehicle Availability
- Vehicle Booking
- Ride Scheduling
- Secure Payments
- Ride History
- Reviews & Ratings
- Emergency SOS
- Live GPS Tracking

---

### 🚘 Vehicle Owner Features

- Vehicle Listing Management
- Fleet Dashboard
- Earnings Analytics
- Booking Management
- Vehicle Availability Control
- GPS Monitoring
- Security Alerts
- Vehicle Health Tracking
- Damage Reports
- Maintenance Scheduling

---

### 🛡️ Security Features

- Real-Time GPS Tracking
- Geofencing
- Theft Detection
- Emergency SOS
- Panic Alerts
- Remote Vehicle Lock
- Tamper Detection
- Accident Detection
- CCTV Integration
- Motion Detection
- Smoke Detection
- Fire Detection
- Route History

---

### 🤖 AI Features

- AI Customer Support
- Smart Vehicle Recommendations
- Fraud Detection
- AI Damage Detection
- Risk Analysis
- Smart Pricing Suggestions
- Predictive Maintenance

---

### 🗺️ Maps & Tracking

- Live Vehicle Tracking
- Real-Time Route Updates
- Traffic Information
- Nearby Petrol Pumps
- EV Charging Stations
- Parking Locations
- Hospitals
- Police Stations
- Service Centers

---

## 📱 Platform Support

| Platform | Status |
|-----------|---------|
| Android | ✅ |
| iOS | ✅ |
| Web Dashboard | ✅ |
| Admin Panel | ✅ |
| Owner Dashboard | ✅ |

---

# 🏗️ System Architecture

```text
Mobile App (Flutter)
        │
        ▼
API Gateway
        │
        ▼
Backend Services
        │
 ┌──────┼──────┐
 ▼      ▼      ▼
Auth  Booking Payment
Service Service Service
 │
 ▼
Supabase PostgreSQL
 │
 ▼
Realtime Engine
 │
 ▼
GPS Tracking & Notifications
```

---

# 🚀 Technology Stack

## Frontend

- Flutter
- Dart
- Material 3
- Riverpod
- GoRouter

---

## Backend

- Node.js
- TypeScript
- Hono
- REST API

---

## Database

- PostgreSQL
- Supabase

---

## Authentication

- Supabase Auth
- OTP Login
- Google OAuth
- Apple Sign-In

---

## Storage

- Supabase Storage

Used For:

- Vehicle Images
- User Documents
- CCTV Footage
- Damage Reports

---

## Maps

- Mapbox
- OpenStreetMap

Features:

- Live Tracking
- Vehicle Location
- Route History
- Geofencing

---

## Notifications

- Firebase Cloud Messaging (FCM)

---

## Monitoring

- Sentry
- PostHog

---

## AI Services

- Gemini API
- OpenRouter
- Ollama

---

# 📂 Project Structure

```text
rideshield-india/

├── apps/
│   ├── mobile/
│   ├── admin-dashboard/
│   └── owner-dashboard/
│
├── backend/
│   ├── api/
│   ├── auth/
│   ├── booking/
│   ├── payments/
│   ├── tracking/
│   └── notifications/
│
├── database/
│   ├── migrations/
│   └── schemas/
│
├── docs/
│
├── assets/
│   ├── icons/
│   ├── animations/
│   └── vehicle-models/
│
└── README.md
```

---

# 🔐 Security Best Practices

## Authentication

- JWT Authentication
- Refresh Tokens
- MFA Support
- Device Verification

---

## API Security

- Rate Limiting
- Request Validation
- Input Sanitization
- CSRF Protection
- Secure Headers

---

## Storage Security

- Signed URLs
- Encrypted Uploads
- Secure File Access

---

## Vehicle Security

- Geofencing
- GPS Tracking
- Remote Lock
- Theft Detection
- Emergency Alerts

---

# 🗄️ Database Tables

### Users

- User Information
- Authentication Data

### Vehicles

- Vehicle Details
- Availability

### Bookings

- Reservation Information

### Payments

- Transaction Records

### Reviews

- User Feedback

### GPS Events

- Tracking History

### Security Alerts

- Incident Logs

### CCTV Clips

- Recorded Footage

### Damage Reports

- Vehicle Inspection Reports

### Support Tickets

- Customer Support

---

# 🛣️ Development Roadmap

## Phase 1 – MVP

- User Authentication
- Vehicle Listing
- Vehicle Search
- Booking System
- Payments
- Reviews
- GPS Tracking

---

## Phase 2 – Growth

- Owner Dashboard
- AI Assistant
- Analytics
- Damage Detection
- Fraud Detection

---

## Phase 3 – Advanced Security

- CCTV Streaming
- Smoke Detection
- Fire Detection
- Engine Immobilizer
- Vehicle IoT Integration

---

## Phase 4 – Scale

- Multi-City Expansion
- Corporate Rentals
- EV Rentals
- Subscription Plans
- Franchise System

---

# 💰 Revenue Model

- Booking Commission
- Subscription Plans
- Premium Listings
- Insurance Partnerships
- Vehicle Protection Plans
- Corporate Rentals
- Fleet Management Services

---

# 🌎 Vision

To become India's most trusted and secure vehicle rental ecosystem by combining mobility, AI, and smart vehicle security into a single platform.

---

# 🤝 Contributing

Contributions, feature requests, and suggestions are welcome.

Please open an issue before submitting major changes.

---

# 📜 License

MIT License

---

# 👨‍💻 Author

**Vishwajeet**

BCA Graduate • Full Stack Developer • AI Enthusiast

Building innovative solutions using AI, Cloud, Web, and Mobile Technologies.

---

<div align="center">

### ⭐ Star this repository if you like the project!

Made with ❤️ in India 🇮🇳

</div>

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
