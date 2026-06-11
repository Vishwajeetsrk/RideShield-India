package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.ui.theme.*

@Composable
fun OwnerDashboardScreen(viewModel: RideShieldViewModel) {
    val ownerVehicles by viewModel.ownerVehiclesFlow.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    var regName by remember { mutableStateOf("") }
    var regType by remember { mutableStateOf("car") } // car, bike
    var regNum by remember { mutableStateOf("") }
    var regPrice by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OWNER TELEMETRY HUB",
                color = TextPrimaryGlow,
                fontSize = 13.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                    .background(CardBackgroundGlass)
                    .testTag("open_add_vehicle_dialog_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add vehicle", tint = RideNeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Earning & Fleet Tracking Status",
            color = TextPrimaryGlow,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Trace earnings, view live vector locations, or immobilize vehicles remotely.",
            color = TextMutedGlow,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // EARNINGS CHART: Custom Canvas Vector Timeline Chart!
        Text(
            text = "FLEET EARNING TIMELINE (INR)",
            color = TextMutedGlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x0EFFFFFF))
                .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw horizontal line blocks
                val gridColor = Color(0x0664748B)
                drawLine(color = gridColor, start = Offset(0f, h * 0.25f), end = Offset(w, h * 0.25f), strokeWidth = 1f)
                drawLine(color = gridColor, start = Offset(0f, h * 0.5f), end = Offset(w, h * 0.5f), strokeWidth = 1f)
                drawLine(color = gridColor, start = Offset(0f, h * 0.75f), end = Offset(w, h * 0.75f), strokeWidth = 1f)

                // Render vector earning milestones: Mon, Tue, Wed, Thu, Fri, Sat, Sun
                // Coordinates mapping:
                val points = listOf(
                    Offset(0f, h * 0.8f),
                    Offset(w * 0.16f, h * 0.7f),
                    Offset(w * 0.33f, h * 0.45f),
                    Offset(w * 0.5f, h * 0.55f),
                    Offset(w * 0.66f, h * 0.3f),
                    Offset(w * 0.83f, h * 0.15f),
                    Offset(w, h * 0.1f)
                )

                val linePath = Path()
                points.forEachIndexed { idx, offset ->
                    if (idx == 0) {
                        linePath.moveTo(offset.x, offset.y)
                    } else {
                        linePath.lineTo(offset.x, offset.y)
                    }
                    // Draw dot glow
                    drawCircle(color = RideNeonCyan, radius = 5f, center = offset)
                }

                // Draw line
                drawPath(
                    path = linePath,
                    color = RideNeonCyan,
                    style = Stroke(width = 4f),
                    alpha = 0.85f
                )

                // Draw area overlay gradient under line
                val areaPath = Path().apply {
                    addPath(linePath)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(listOf(Color(0x1F00E5FF), Color.Transparent))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ADD VEHICLE INLINE DIALOG (Glassmorphic panel if toggle checked!)
        AnimatedVisibility(
            visible = showAddDialog,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .border(2.dp, BorderGlass, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGISTER NEW FLEET UNIT",
                            color = TextPrimaryGlow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        IconButton(onClick = { showAddDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AccentRedSOS)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Model Name (e.g. Ather 450X)", color = TextMutedGlow) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryGlow,
                            unfocusedTextColor = TextPrimaryGlow,
                            focusedBorderColor = RideNeonCyan,
                            unfocusedBorderColor = BorderGlass
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regNum,
                        onValueChange = { regNum = it },
                        label = { Text("Registration ID (e.g. KA-51-MD-1245)", color = TextMutedGlow) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryGlow,
                            unfocusedTextColor = TextPrimaryGlow,
                            focusedBorderColor = RideNeonCyan,
                            unfocusedBorderColor = BorderGlass
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_num_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = regPrice,
                        onValueChange = { regPrice = it },
                        label = { Text("Rental Charge Period (₹ / Hour)", color = TextMutedGlow) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryGlow,
                            unfocusedTextColor = TextPrimaryGlow,
                            focusedBorderColor = RideNeonCyan,
                            unfocusedBorderColor = BorderGlass
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("reg_price_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vehicle type choice segment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { regType = "car" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (regType == "car") RideNeonBlue else CardBackgroundGlass),
                            modifier = Modifier.testTag("reg_type_car")
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Four Wheeler")
                        }

                        Button(
                            onClick = { regType = "bike" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (regType == "bike") RideNeonBlue else CardBackgroundGlass),
                            modifier = Modifier.testTag("reg_type_bike")
                        ) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Two Wheeler")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val priceParsed = regPrice.toDoubleOrNull() ?: 120.0
                            viewModel.addOwnerVehicle(regName, regType, regNum, priceParsed)
                            showAddDialog = false
                            regName = ""
                            regNum = ""
                            regPrice = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                        modifier = Modifier.fillMaxWidth().testTag("add_vehicle_confirm_button"),
                        enabled = regName.isNotEmpty() && regNum.isNotEmpty() && regPrice.isNotEmpty()
                    ) {
                        Text("Establish Cellular Link", color = DeepSlateBackground)
                    }
                }
            }
        }

        // FLEET LISTINGS HEADING
        Text(
            text = "FLEET STATUS MONITOR",
            color = TextMutedGlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (ownerVehicles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x05FFFFFF))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TextMutedGlow, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No vehicles listed yet.",
                        color = TextMutedGlow,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Lists owner vehicles with remote immobilizer toggler
            ownerVehicles.forEach { vehicle ->
                OwnerVehicleStripCard(
                    vehicle = vehicle,
                    onImmobilize = { viewModel.ownerImmobilizeVehicle(vehicle) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun OwnerVehicleStripCard(vehicle: Vehicle, onImmobilize: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            .testTag("owner_strip_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = vehicle.name, color = TextPrimaryGlow, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${vehicle.registrationNumber} • Price: ₹${vehicle.pricePerHr}/hr", color = TextMutedGlow, fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            (if (vehicle.status == "Active") AccentOrange else AccentTeal).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = vehicle.status.uppercase(),
                        color = if (vehicle.status == "Active") AccentOrange else AccentTeal,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${vehicle.batteryPct}% Range Left", color = TextMutedGlow, fontSize = 11.sp)
                }

                // Remote Immobilizer lock trigger (Cyber actions!)
                Button(
                    onClick = onImmobilize,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (vehicle.isLocked) AccentRedSOS.copy(alpha = 0.2f) else CardBackgroundGlass
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .border(
                            1.dp,
                            if (vehicle.isLocked) AccentRedSOS else BorderGlass,
                            RoundedCornerShape(8.dp)
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = if (vehicle.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock",
                        tint = if (vehicle.isLocked) AccentRedSOS else TextPrimaryGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (vehicle.isLocked) "Engine Locked" else "Active Arm",
                        color = if (vehicle.isLocked) AccentRedSOS else TextPrimaryGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
