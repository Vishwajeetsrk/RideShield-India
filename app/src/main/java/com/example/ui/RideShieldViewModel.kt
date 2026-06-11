package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RideShieldViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val profileDao = db.userProfileDao()
    private val vehicleDao = db.vehicleDao()
    private val bookingDao = db.bookingDao()
    private val alertDao = db.securityAlertDao()

    // Observable states
    val profile = profileDao.getProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val vehicles = vehicleDao.getAllVehiclesFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeBooking = bookingDao.getActiveBookingFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val securityAlerts = alertDao.getAllAlertsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat assistance log
    private val _chatMessages = MutableStateFlow<List<SupportMessage>>(
        listOf(SupportMessage("system", "Welcome to RideShield. Secure, AI-Protected Rentals."))
    )
    val chatMessages: StateFlow<List<SupportMessage>> = _chatMessages.asStateFlow()

    // Selection & Navigation
    val selectedVehicle = MutableStateFlow<Vehicle?>(null)
    val ownerVehiclesFlow = profile.flatMapLatest { prof ->
        if (prof != null) {
            vehicleDao.getVehiclesByOwner(prof.phone)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Temporary values for forms
    val authLoading = MutableStateFlow(false)
    val isOtpSent = MutableStateFlow(false)
    val isAadhaarSent = MutableStateFlow(false)
    val isDlSent = MutableStateFlow(false)
    val isSelfieCaptured = MutableStateFlow(false)

    // Current screen navigation state
    val currentScreen = MutableStateFlow("auth") // auth, marketplace, details, active_ride, completion, owner, help

    // Active Ride Telemetry variables
    val vehicleSpeed = MutableStateFlow(0f)
    val smokeLevel = MutableStateFlow(12f) // ppm
    val isCctvRecording = MutableStateFlow(false)
    val activeLat = MutableStateFlow(12.9716) // Default Bengaluru (Namma Metro Area)
    val activeLng = MutableStateFlow(77.5946)
    val mapLayer = MutableStateFlow("traffic") // traffic, weather, ev, police

    // Dynamic Island notification active alert
    val activePopupAlert = MutableStateFlow<SecurityAlert?>(null)

    // Damage processing states
    val damageProcessing = MutableStateFlow(false)
    val beforeBitmapState = MutableStateFlow<Bitmap?>(null)
    val afterBitmapState = MutableStateFlow<Bitmap?>(null)
    val damageReportResult = MutableStateFlow<String?>(null)

    // Route tracking coordinate queue (for animated lines)
    private val _routeTrack = MutableStateFlow<List<Pair<Double, Double>>>(emptyList())
    val routeTrack: StateFlow<List<Pair<Double, Double>>> = _routeTrack.asStateFlow()

    init {
        // Hydrate initial mock fleet of premium vehicles
        viewModelScope.launch {
            val count = db.vehicleDao().getAllVehiclesFlow().first().size
            if (count == 0) {
                val mockFleet = listOf(
                    Vehicle("v1", "Mahindra Thar 4X4", "car", "KA-51-MD-9910", 250.0, 95, 380, 12.9786, 77.5906, "Available", "suv"),
                    Vehicle("v2", "Tesla Model 3 Tech", "car", "KA-03-EV-2026", 450.0, 88, 410, 12.9698, 77.5936, "Available", "sedan"),
                    Vehicle("v3", "Ather 450X Gen 4", "bike", "KA-01-ES-1245", 80.0, 100, 110, 12.9734, 77.6010, "Available", "scooter"),
                    Vehicle("v4", "Royal Enfield Himalayan", "bike", "KA-05-RE-8822", 120.0, 72, 350, 12.9644, 77.5876, "Available", "sports_bike"),
                    Vehicle("v5", "Tata Nexon EV Max", "car", "KA-04-EV-8820", 200.0, 68, 280, 12.9810, 77.6150, "Available", "suv")
                )
                db.vehicleDao().insertVehicles(mockFleet)
            }
        }

        // Loop for simulated live telemetry updates when ride is active
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                val booking = bookingDao.getActiveBookingFlow().firstOrNull()
                if (booking != null) {
                    // Update speed randomly
                    var newSpeed = (vehicleSpeed.value + (-5..5).random()).coerceIn(0f, 130f)
                    // Speed Limit Check Alert (e.g. 100 km/h)
                    if (newSpeed > 95f) {
                        newSpeed = 98f // Cap or log warning
                    }
                    vehicleSpeed.value = newSpeed

                    // Update GPS coordinates slightly towards a target
                    val deltaLat = (0..100).random() / 1000000.0 * (if ((0..1).random() == 0) 1 else -1)
                    val deltaLng = (0..100).random() / 1000000.0 * (if ((0..1).random() == 0) 1 else -1)
                    val newLat = activeLat.value + deltaLat
                    val newLng = activeLng.value + deltaLng
                    activeLat.value = newLat
                    activeLng.value = newLng

                    _routeTrack.value = _routeTrack.value + Pair(newLat, newLng)

                    // Jitter smoke levels
                    val smoked = (smokeLevel.value + (-1..1).random()).coerceIn(8f, 140f)
                    smokeLevel.value = smoked

                    // Smoke hazard triggering detection
                    if (smoked > 70f) {
                        triggerAlert(
                            "Smoke / Cigarette Alert",
                            "Cigarette smoke detected inside vehicle cabin! Owner and administration notified.",
                            "SMOKE"
                        )
                    }

                    // Simulated random security alert (e.g. extreme tilt or tamper)
                    if ((1..25).random() == 15) {
                        triggerAlert(
                            "Tampering Warning",
                            "High accelerometer tilt detected. Checking vehicle side cameras.",
                            "TAMPER"
                        )
                    }
                }
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    // OTP / Login Simulation
    fun sendOtp(phone: String) {
        if (phone.isEmpty()) return
        viewModelScope.launch {
            authLoading.value = true
            kotlinx.coroutines.delay(1000)
            isOtpSent.value = true
            authLoading.value = false
        }
    }

    fun verifyOtpAndLogin(phone: String, otp: String, name: String) {
        viewModelScope.launch {
            authLoading.value = true
            kotlinx.coroutines.delay(1000)
            val existing = profileDao.getProfileByPhone(phone)
            val profile = existing ?: UserProfile(
                phone = phone,
                name = name.ifEmpty { "Vishwajeet Kumar" },
                aadhaar = "",
                drivingLicense = "",
                isKycVerified = false
            )
            profileDao.saveProfile(profile)
            authLoading.value = false
            if (profile.isKycVerified) {
                currentScreen.value = "marketplace"
            } else {
                currentScreen.value = "kyc"
            }
        }
    }

    // KYC Upload Simulation
    fun submitAadhaar(aadhaar: String) {
        viewModelScope.launch {
            isAadhaarSent.value = true
            checkKycCompletion()
        }
    }

    fun submitDl(dl: String) {
        viewModelScope.launch {
            isDlSent.value = true
            checkKycCompletion()
        }
    }

    fun submitSelfie(bitmap: Bitmap?) {
        viewModelScope.launch {
            isSelfieCaptured.value = true
            checkKycCompletion()
        }
    }

    private suspend fun checkKycCompletion() {
        if (isAadhaarSent.value && isDlSent.value && isSelfieCaptured.value) {
            val prof = profile.value
            if (prof != null) {
                val updatedProf = prof.copy(
                    aadhaar = "9988-1245-8812",
                    drivingLicense = "DL-2026-N2021",
                    isKycVerified = true
                )
                profileDao.saveProfile(updatedProf)
                kotlinx.coroutines.delay(1500)
                currentScreen.value = "marketplace"
            }
        }
    }

    // Vehicle bookings
    fun startBooking(vehicle: Vehicle) {
        viewModelScope.launch {
            val prof = profile.value ?: return@launch
            val bookingId = "b_" + System.currentTimeMillis()
            val beforeUri = "android.resource://com.example/drawable/ic_launcher_background" // mock uri

            // Change vehicle state
            val updatedVehicle = vehicle.copy(status = "Booked")
            vehicleDao.updateVehicle(updatedVehicle)

            val newBooking = Booking(
                id = bookingId,
                vehicleId = vehicle.id,
                userPhone = prof.phone,
                startTime = System.currentTimeMillis(),
                status = "PENDING",
                beforePhotoUri = beforeUri,
                baseFare = vehicle.pricePerHr
            )
            bookingDao.insertBooking(newBooking)
            selectedVehicle.value = updatedVehicle
        }
    }

    fun unlockAndStartRide(bookingId: String) {
        viewModelScope.launch {
            val booking = bookingDao.getBookingById(bookingId) ?: return@launch
            val updatedBooking = booking.copy(status = "ACTIVE")
            bookingDao.insertBooking(updatedBooking)

            val veh = vehicleDao.getVehicleById(booking.vehicleId) ?: return@launch
            val updatedVeh = veh.copy(status = "Active", isLocked = false)
            vehicleDao.updateVehicle(updatedVeh)

            activeLat.value = veh.lat
            activeLng.value = veh.lng
            _routeTrack.value = listOf(Pair(veh.lat, veh.lng))
            isCctvRecording.value = true

            selectedVehicle.value = updatedVeh
            currentScreen.value = "active_ride"

            // Log warning siren off
            triggerAlert("Engine Unlocked", "AI-Shield activated. Smart safety streams started.", "SYSTEM")
        }
    }

    // Active trip alarms
    fun triggerSOS() {
        viewModelScope.launch {
            triggerAlert(
                "SOS METRICS BROADCAST",
                "Emergency dispatch requested! Sharing live CCTV telemetry & coordinates with Namma Metro Control Room & Police.",
                "ACCIDENT"
            )
            // Sim speed braking
            vehicleSpeed.value = 0f
        }
    }

    fun triggerAlert(title: String, message: String, type: String) {
        viewModelScope.launch {
            val alert = SecurityAlert(
                title = title,
                message = message,
                type = type
            )
            alertDao.insertAlert(alert)
            activePopupAlert.value = alert
            // Auto dismiss after 4 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (activePopupAlert.value?.title == title) {
                    activePopupAlert.value = null
                }
            }, 4500)
        }
    }

    fun clearPopup() {
        activePopupAlert.value = null
    }

    // Completion / Damage Check Claim Flow
    fun stopRideAndPreparePhotos(booking: Booking) {
        viewModelScope.launch {
            // Update booking items
            damageReportResult.value = null
            damageProcessing.value = false
            currentScreen.value = "completion"
        }
    }

    fun submitAfterPhotoAndProcess(beforeBitmap: Bitmap?, afterBitmap: Bitmap?) {
        viewModelScope.launch {
            damageProcessing.value = true
            damageReportResult.value = null
            
            // Invoke Gemini visual comparison (using Pro with thinking config HIGH, or smart local claims engine)
            val result = withContext(Dispatchers.IO) {
                GeminiService.analyzeDamage(beforeBitmap, afterBitmap)
            }
            
            damageReportResult.value = result
            damageProcessing.value = false
        }
    }

    fun finalPayAndCloseTrip() {
        viewModelScope.launch {
            val booking = activeBooking.value ?: return@launch
            val updatedBooking = booking.copy(
                status = "COMPLETED",
                endTime = System.currentTimeMillis(),
                endKm = booking.startKm + 12.8,
                totalFare = booking.baseFare * 1.5
            )
            bookingDao.insertBooking(updatedBooking)

            val veh = vehicleDao.getVehicleById(booking.vehicleId) ?: return@launch
            val updatedVeh = veh.copy(status = "Available", isLocked = true)
            vehicleDao.updateVehicle(updatedVeh)

            selectedVehicle.value = null
            currentScreen.value = "marketplace"
            triggerAlert("Ride Finalized", "₹${"%.2f".format(booking.baseFare * 1.5)} auto-debited securely via UPI.", "SYSTEM")
        }
    }

    // Chat AI assistance
    fun sendMessage(text: String) {
        if (text.isEmpty()) return
        val userMsg = SupportMessage("user", text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            val answer = GeminiService.getAssistantResponse(text, _chatMessages.value)
            _chatMessages.value = _chatMessages.value + SupportMessage("ai", answer)
        }
    }

    // Owner specific
    fun addOwnerVehicle(name: String, type: String, reg: String, price: Double) {
        viewModelScope.launch {
            val prof = profile.value ?: return@launch
            val vId = "v_owner_" + System.currentTimeMillis()
            val newV = Vehicle(
                id = vId,
                name = name,
                type = type,
                registrationNumber = reg,
                pricePerHr = price,
                batteryPct = 100,
                rangeKm = if (type == "car") 350 else 115,
                lat = 12.9716,
                lng = 77.5946,
                status = "Available",
                imageType = if (type == "car") "sedan" else "scooter",
                ownerPhone = prof.phone
            )
            vehicleDao.insertVehicle(newV)
            triggerAlert("Vehicle Registered", "Owner validation completed. Live cellular GPS streams active.", "SYSTEM")
        }
    }

    fun ownerImmobilizeVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            val updated = vehicle.copy(isLocked = !vehicle.isLocked)
            vehicleDao.updateVehicle(updated)
            triggerAlert(
                if (updated.isLocked) "Engine Locked" else "Engine Armed",
                "Remote security command transmitted successfully to cellular dashboard.",
                "SYSTEM"
            )
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            alertDao.clearAlerts()
        }
    }

    fun logout() {
        viewModelScope.launch {
            profileDao.clearUsers()
            alertDao.clearAlerts()
            currentScreen.value = "auth"
            isOtpSent.value = false
            isAadhaarSent.value = false
            isDlSent.value = false
            isSelfieCaptured.value = false
        }
    }
}
