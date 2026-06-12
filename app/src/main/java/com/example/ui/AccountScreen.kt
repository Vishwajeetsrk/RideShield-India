package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Booking
import com.example.ui.theme.*

@Composable
fun AccountScreen(
    viewModel: RideShieldViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val firebaseAuthStatus by viewModel.firebaseAuthStatus.collectAsState()
    val isFirebaseAuthSync by viewModel.isFirebaseAuthSync.collectAsState()

    val completedBookings = remember(allBookings) {
        allBookings.filter { it.status == "COMPLETED" }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 80.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Header Title
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = RideNeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SECURE PROFILE CENTER",
                        color = TextPrimaryGlow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manage your biometric identities and cloud verification data safely.",
                    color = TextMutedGlow,
                    fontSize = 12.sp
                )
            }

            // High-Performance Profile Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                        .testTag("user_profile_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Glowing Avatar Node
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(RideNeonCyan, RideNeonBlue)
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                Text(
                                    text = (profile?.name?.take(2) ?: "RS").uppercase(),
                                    color = DeepSlateBackground,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = profile?.name ?: "Vishwajeet Kumar",
                                    color = TextPrimaryGlow,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = RideNeonCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+91 ${profile?.phone ?: "9876543210"}",
                                        color = TextMutedGlow,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Divider(color = BorderGlass)
                        Spacer(modifier = Modifier.height(18.dp))

                        // Aadhaar & DL verification metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "AADHAAR ID", color = TextMutedGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = profile?.aadhaar?.ifEmpty { "9988-1245-8812" } ?: "9988-1245-8812",
                                    color = TextPrimaryGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "VERIFIED", color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "DRIVING LICENSE", color = TextMutedGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = profile?.drivingLicense?.ifEmpty { "DL-2026-N2021" } ?: "DL-2026-N2021",
                                    color = TextPrimaryGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "VERIFIED", color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // Firebase authentication Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x333B82F6), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud connection status",
                            tint = if (isFirebaseAuthSync) AccentTeal else RideNeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FIREBASE STORAGE INTEGRATION",
                                color = TextPrimaryGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = firebaseAuthStatus,
                                color = TextMutedGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isFirebaseAuthSync) AccentTeal else RideNeonCyan)
                        )
                    }
                }
            }

            // ACTIVE & UPCOMING RENTAL DASHBOARD SECTION
            item {
                val vehicles by viewModel.vehicles.collectAsState()
                val activeBookings = remember(allBookings) {
                    allBookings.filter { it.status == "ACTIVE" }
                }
                val upcomingBookings = remember(allBookings) {
                    allBookings.filter { it.status == "PENDING" }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROTECTED FLEET DASHBOARD",
                        color = RideNeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    if (activeBookings.isEmpty() && upcomingBookings.isEmpty()) {
                        // Reassuring standby card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(RideNeonCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield standby",
                                        tint = RideNeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "All Rental Systems Standby",
                                        color = TextPrimaryGlow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "No active rentals on current key node. Rent a vehicle to activate telemetry.",
                                        color = TextMutedGlow,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Display active bookings
                    activeBookings.forEach { booking ->
                        val vehicle = vehicles.find { it.id == booking.vehicleId }
                        val vName = vehicle?.name ?: "Premium Secured Vehicle"
                        val vPlate = vehicle?.registrationNumber ?: "DL-3C-AL-9981"
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x1A14B8A6)), // soft emerald background
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RideNeonCyan, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                                .testTag("active_rental_dashboard_card")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = "Live active telemetry",
                                            tint = RideNeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LIVE ACTIVE RENTAL",
                                            color = RideNeonCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(RideNeonCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "AI-SECURE LINKED",
                                            color = RideNeonCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = vName, color = TextPrimaryGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Plate: $vPlate", color = TextMutedGlow, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = BorderGlass)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "INSURANCE PROTECTION", color = TextMutedGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (booking.hasPremiumInsurance) "Gold Shield Zero-Depreciation" else "Basic Liability Coverage",
                                            color = if (booking.hasPremiumInsurance) RideNeonCyan else TextPrimaryGlow,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.currentScreen.value = "active_ride" },
                                        colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                                        modifier = Modifier.height(36.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = "MONITOR", color = DeepSlateBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Display upcoming (pending) countdown bookings
                    upcomingBookings.forEach { booking ->
                        val vehicle = vehicles.find { it.id == booking.vehicleId }
                        val vName = vehicle?.name ?: "Premium Secured Vehicle"
                        val vPlate = vehicle?.registrationNumber ?: "DL-3C-AL-9981"
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x156366F1)), // soft blue background
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, RideNeonBlue, RoundedCornerShape(20.dp))
                                .padding(16.dp)
                                .testTag("upcoming_rental_dashboard_card")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Upcoming rental",
                                            tint = RideNeonBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "RESERVED UPCOMING RIDE",
                                            color = RideNeonBlue,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(RideNeonBlue.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "READY TO ACTIVATE",
                                            color = RideNeonBlue,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = vName, color = TextPrimaryGlow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Plate: $vPlate", color = TextMutedGlow, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // UPCOMING TRIP COUNTDOWN COMPONENT
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = "Countdown",
                                                tint = AccentOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(text = "TRIP COUNTDOWN", color = TextMutedGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = "T-Minus 02:40:15",
                                                    color = AccentOrange,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = { viewModel.unlockAndStartRide(booking.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                                            modifier = Modifier.height(34.dp).testTag("dashboard_unlock_button"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text(text = "UNLOCK NOW", color = DeepSlateBackground, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PAST COMPLETED RENTAL HISTORY TRACKER BLOCK
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPLETED RENTALS HISTORIES",
                        color = RideNeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1A00E5FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${completedBookings.size} RIDES",
                            color = RideNeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (completedBookings.isEmpty()) {
                item {
                    // Prepopulated Mock History Card for better first look experience
                    CompletedRentalHistoryCard(
                        booking = Booking(
                            id = "b_mock_1",
                            vehicleId = "v1",
                            userPhone = profile?.phone ?: "9876543210",
                            startTime = System.currentTimeMillis() - 86400000 * 2,
                            endTime = System.currentTimeMillis() - 86400000 * 2 + 7200000,
                            status = "COMPLETED",
                            baseFare = 200.0,
                            totalFare = 400.0,
                            damageReport = "AI DAMAGE RESOLVED - Checked clear"
                        ),
                        vehicleName = "Mahindra Thar 4X4"
                    )
                }
            } else {
                items(completedBookings) { booking ->
                    CompletedRentalHistoryCard(
                        booking = booking,
                        vehicleName = "Premium Connected Vehicle"
                    )
                }
            }

            // Sign Out Option
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("account_logout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRedSOS.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRedSOS)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = AccentRedSOS,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIGN OUT OF PORT",
                            color = AccentRedSOS,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CompletedRentalHistoryCard(booking: Booking, vehicleName: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .testTag("past_booking_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vehicleName,
                        color = TextPrimaryGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "₹${booking.totalFare ?: 320.0}",
                    color = RideNeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMutedGlow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    val date = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(booking.startTime))
                    Text(text = "Rented: $date", color = TextMutedGlow, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TextMutedGlow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Booking Reference: ID ${booking.id}", color = TextMutedGlow, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.damageReport ?: "AI Scanner Cleared: No new damages matched.",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
