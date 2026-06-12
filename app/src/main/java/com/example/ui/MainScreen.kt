package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.SecurityAlert
import com.example.ui.theme.*

@Composable
fun MainScreen(viewModel: RideShieldViewModel) {
    val currentTab by viewModel.currentScreen.collectAsState()
    val activeAlarm by viewModel.activePopupAlert.collectAsState()
    val activeBooking by viewModel.activeBooking.collectAsState()

    var showActiveNotificationsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
    ) {
        // CONTENT ROUTING ACCORDING TO VIEWMODEL TABS
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP MAIN TITLE PANEL (Unless we are in auth screens)
            if (currentTab != "auth" && currentTab != "kyc") {
                TopPremiumNavBar(
                    viewModel = viewModel,
                    onNotificationsClick = { showActiveNotificationsDialog = true }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "auth" -> AuthScreen(viewModel)
                    "kyc" -> AuthScreen(viewModel)
                    "marketplace" -> MapScreen(
                        viewModel = viewModel,
                        onVehicleClick = { viewModel.currentScreen.value = "details" }
                    )
                    "details" -> VehicleDetailsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.currentScreen.value = "marketplace" }
                    )
                    "active_ride" -> {
                        if (activeBooking != null) {
                            RideScreen(
                                viewModel = viewModel,
                                booking = activeBooking!!,
                                onCompleteRide = { viewModel.stopRideAndPreparePhotos(activeBooking!!) }
                            )
                        } else {
                            // Fallback if pressed without booking
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No Active Booking Yet. Reserve a ride from Marketplace.", color = TextPrimaryGlow)
                            }
                        }
                    }
                    "completion" -> CompletionScreen(
                        viewModel = viewModel,
                        onPaymentClose = { viewModel.currentScreen.value = "marketplace" }
                    )
                    "owner" -> OwnerDashboardScreen(viewModel)
                    "help" -> AiAssistantSheet(viewModel)
                    "account" -> AccountScreen(viewModel)
                }
            }

            // BOTTOM PREMIUM CAPSULE BAR (Hide during auth / claim processes)
            if (currentTab != "auth" && currentTab != "kyc" && currentTab != "completion" && currentTab != "details") {
                BottomFuturisticNavbar(viewModel, currentTab)
            }
        }

        // DYNAMIC ISLAND ALERT PUSH NOTIFICATION (Slices down from top!)
        AnimatedVisibility(
            visible = activeAlarm != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp)
                .zIndex(10f)
        ) {
            if (activeAlarm != null) {
                DynamicIslandBanner(
                    alert = activeAlarm!!,
                    onDismiss = { viewModel.clearPopup() }
                )
            }
        }

        // FLEET ALARMS LOG DRAWER OVERLAY
        if (showActiveNotificationsDialog) {
            FleetIncidentAlertsDialog(
                viewModel = viewModel,
                onDismiss = { showActiveNotificationsDialog = false }
            )
        }
    }
}

// Custom helper because zIndex requires import or modifier.zIndex
val ModifierZIndexFix = Modifier

@Composable
fun TopPremiumNavBar(
    viewModel: RideShieldViewModel,
    onNotificationsClick: () -> Unit
) {
    val alerts by viewModel.securityAlerts.collectAsState()
    val prof by viewModel.profile.collectAsState()

    val userName = prof?.name ?: "Verified Pilot"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0x0AFFFFFF))
            .border(1.dp, Color(0x0A60A5FA))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = RideNeonCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "RIDESHIELD INDIA",
                    color = TextPrimaryGlow,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Verified: $userName",
                    color = AccentTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x0EFFFFFF))
            ) {
                val hasAlerts = alerts.any { !it.isRead }
                Box {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts Log",
                        tint = if (hasAlerts) AccentOrange else TextPrimaryGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    if (hasAlerts) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentRedSOS)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Logout/Sign out control
            IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x0EFFFFFF))
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Logout",
                    tint = AccentRedSOS,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun BottomFuturisticNavbar(viewModel: RideShieldViewModel, currentTab: String) {
    val hasActiveBooking by viewModel.activeBooking.collectAsState()

    NavigationBar(
        containerColor = DeepSlateBackground,
        tonalElevation = 8.dp,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, BorderGlass, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        // Tab 1: Marketplace
        NavigationBarItem(
            selected = currentTab == "marketplace",
            onClick = { viewModel.currentScreen.value = "marketplace" },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Rental Map", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RideNeonCyan,
                unselectedIconColor = TextMutedGlow,
                selectedTextColor = RideNeonCyan,
                unselectedTextColor = TextMutedMuted,
                indicatorColor = Color(0x1A10B981) // bg-emerald-400/10
            ),
            modifier = Modifier.testTag("tab_marketplace")
        )

        // Tab 2: During Ride Monitor (Show warning icon if no booking, otherwise telemetry)
        val isTrip = currentTab == "active_ride"
        NavigationBarItem(
            selected = isTrip,
            onClick = {
                if (hasActiveBooking != null) {
                    viewModel.currentScreen.value = "active_ride"
                } else {
                    viewModel.triggerAlert("Diagnostics locked", "Start a booking to active live CCTV telemetry.", "SYSTEM")
                }
            },
            icon = {
                Icon(
                    imageVector = if (hasActiveBooking != null) Icons.Default.Sensors else Icons.Default.LockReset,
                    contentDescription = "Ride",
                    tint = if (hasActiveBooking != null) AccentTeal else TextMutedGlow
                )
            },
            label = { Text("Active Trip", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RideNeonCyan,
                unselectedIconColor = TextMutedGlow,
                selectedTextColor = RideNeonCyan,
                unselectedTextColor = TextMutedMuted,
                indicatorColor = Color(0x1A10B981) // bg-emerald-400/10
            ),
            modifier = Modifier.testTag("tab_activeride")
        )

        // Tab 3: Owner Hub
        NavigationBarItem(
            selected = currentTab == "owner",
            onClick = { viewModel.currentScreen.value = "owner" },
            icon = { Icon(Icons.Default.SpaceDashboard, contentDescription = "Owner") },
            label = { Text("Owner Hub", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RideNeonCyan,
                unselectedIconColor = TextMutedGlow,
                selectedTextColor = RideNeonCyan,
                unselectedTextColor = TextMutedMuted,
                indicatorColor = Color(0x1A10B981) // bg-emerald-400/10
            ),
            modifier = Modifier.testTag("tab_owner")
        )

        // Tab 4: Assistant
        NavigationBarItem(
            selected = currentTab == "help",
            onClick = { viewModel.currentScreen.value = "help" },
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "Chat helper") },
            label = { Text("Shieldy Copilot", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RideNeonCyan,
                unselectedIconColor = TextMutedGlow,
                selectedTextColor = RideNeonCyan,
                unselectedTextColor = TextMutedMuted,
                indicatorColor = Color(0x1A10B981) // bg-emerald-400/10
            ),
            modifier = Modifier.testTag("tab_chat")
        )

        // Tab 5: Account Profile
        NavigationBarItem(
            selected = currentTab == "account",
            onClick = { viewModel.currentScreen.value = "account" },
            icon = { Icon(Icons.Default.AccountBox, contentDescription = "Account") },
            label = { Text("My Account", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RideNeonCyan,
                unselectedIconColor = TextMutedGlow,
                selectedTextColor = RideNeonCyan,
                unselectedTextColor = TextMutedMuted,
                indicatorColor = Color(0x1A10B981) // bg-emerald-400/10
            ),
            modifier = Modifier.testTag("tab_account")
        )
    }
}

// Dynamic Island Alert Banner
@Composable
fun DynamicIslandBanner(
    alert: SecurityAlert,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(1.dp, AccentOrange, RoundedCornerShape(24.dp))
            .clickable { onDismiss() }
            .testTag("dynamic_island_alert"),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (alert.type) {
                    "SMOKE" -> Icons.Default.SmokingRooms
                    "ACCIDENT" -> Icons.Default.Warning
                    "TAMPER" -> Icons.Default.Warning
                    else -> Icons.Default.Shield
                },
                contentDescription = null,
                tint = if (alert.type == "ACCIDENT") AccentRedSOS else AccentOrange,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = alert.message,
                    color = TextMutedGlow,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Swipe to dismiss",
                tint = TextMutedGlow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FleetIncidentAlertsDialog(
    viewModel: RideShieldViewModel,
    onDismiss: () -> Unit
) {
    val alerts by viewModel.securityAlerts.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("FLEET SECURITY ALARM JOURNAL", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        },
        text = {
            if (alerts.isEmpty()) {
                Text("No incidents logged. Your active rentals are fully shielded.", color = TextMutedGlow)
            } else {
                LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alerts) { alert ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x1AFFFFFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = alert.title, color = TextPrimaryGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = alert.type,
                                        color = if (alert.type == "ACCIDENT") AccentRedSOS else AccentOrange,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(text = alert.message, color = TextMutedGlow, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.clearAllAlerts()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRedSOS)
            ) {
                Text("Wipe Security Logs")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Journal", color = RideNeonCyan)
            }
        },
        containerColor = Color(0xFF0B0F1A),
        modifier = Modifier.border(1.dp, BorderGlass, RoundedCornerShape(28.dp))
    )
}
