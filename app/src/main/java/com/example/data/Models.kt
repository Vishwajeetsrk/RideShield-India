package com.example.data

import androidx.room.*
import androidx.annotation.Keep

@Keep
@Entity(tableName = "users")
data class UserProfile(
    @PrimaryKey val phone: String,
    val name: String,
    val aadhaar: String,
    val drivingLicense: String,
    val isKycVerified: Boolean = false,
    val selfieUrl: String? = null
)

@Keep
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "car" or "bike"
    val registrationNumber: String,
    val pricePerHr: Double,
    val batteryPct: Int,
    val rangeKm: Int,
    val lat: Double,
    val lng: Double,
    val status: String, // "Available", "Booked", "Active"
    val imageType: String, // "sedan", "suv", "sports_car", "scooter", "sports_bike"
    val ownerPhone: String = "",
    val isLocked: Boolean = true
)

@Keep
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val userPhone: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String, // "PENDING", "ACTIVE", "COMPLETED"
    val startKm: Double = 0.0,
    val endKm: Double? = null,
    val baseFare: Double = 0.0,
    val totalFare: Double? = null,
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val damageReport: String? = null
)

@Keep
@Entity(tableName = "security_alerts")
data class SecurityAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "SMOKE", "ACCIDENT", "TAMPER", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Keep
data class SupportMessage(
    val sender: String, // "user" or "ai" or "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
