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
