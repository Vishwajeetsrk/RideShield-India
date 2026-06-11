package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VehicleDetailsScreen(
    viewModel: RideShieldViewModel,
    onBack: () -> Unit
) {
    val vehicle by viewModel.selectedVehicle.collectAsState()
    val activeBooking by viewModel.activeBooking.collectAsState()

    var rotationAngleX by remember { mutableStateOf(20f) }
    var rotationAngleY by remember { mutableStateOf(45f) }
    var vehicleZoom by remember { mutableStateOf(1.0f) }
    var isDoorOpen by remember { mutableStateOf(false) }

    if (vehicle == null) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .background(CardBackgroundGlass)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = RideNeonCyan)
                }

                Text(
                    text = "SPECIFICATION INSPECTOR",
                    color = TextPrimaryGlow,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Shield verified",
                    tint = AccentTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Title
            Text(
                text = vehicle!!.name,
                color = TextPrimaryGlow,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "${vehicle!!.registrationNumber} • AI-Shield Immobilizer V2",
                color = RideNeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3D MODEL VIEWPORT WITH INTERACTIVE CONTROL WRAPS
            Text(
                text = "INTERACTIVE 3D CAD MODEL",
                color = TextMutedGlow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Drag model to rotate. Pinch/Scroll below to animate actions.",
                color = TextMutedGlow,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1F3B82F6), DeepSlateBackground),
                            radius = 450f
                        )
                    )
                    .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationAngleY += dragAmount.x * 0.5f
                            rotationAngleX += dragAmount.y * 0.5f
                        }
                    }
                    .testTag("vehicle_3d_viewport")
            ) {
                // Trigonometric Projection Canvas (Buttery smooth 60fps local rotation matrix!)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Projection parameters
                    val scaleFactor = 65f * vehicleZoom * (if (vehicle!!.type == "car") 1.15f else 1.0f)
                    val radX = Math.toRadians(rotationAngleX.toDouble())
                    val radY = Math.toRadians(rotationAngleY.toDouble())

                    // Let's draw vertices representing a sleek car frame or motorcycle wireframe
                    val vertices = if (vehicle!!.type == "car") {
                        getSleekCarVertices()
                    } else {
                        getMotorcycleVertices()
                    }

                    // For doors open simulation, we translate certain vertices outwards
                    val translatedVertices = vertices.map { originalVertex ->
                        var point = originalVertex
                        if (isDoorOpen && point.label == "door_left") {
                            point = point.copy(y = point.y + 0.8f) // Offset doors outwards
                        } else if (isDoorOpen && point.label == "door_right") {
                            point = point.copy(y = point.y - 0.8f)
                        }
                        point
                    }

                    // Apply coordinate rotations
                    val rotatedPoints = translatedVertices.map { pt ->
                        // Rotate Y around Y-axis
                        val x1 = pt.x * cos(radY) - pt.z * sin(radY)
                        val z1 = pt.x * sin(radY) + pt.z * cos(radY)
                        // Rotate X around X-axis
                        val y2 = pt.y * cos(radX) - z1 * sin(radX)
                        val z2 = pt.y * sin(radX) + z1 * cos(radX)

                        // 2D orthographic projection
                        Offset((cx + x1 * scaleFactor).toFloat(), (cy - y2 * scaleFactor).toFloat())
                    }

                    // Draw wires connecting respective vertices
                    val edges = if (vehicle!!.type == "car") {
                        getSleekCarEdges()
                    } else {
                        getMotorcycleEdges()
                    }

                    edges.forEach { (start, end) ->
                        if (start < rotatedPoints.size && end < rotatedPoints.size) {
                            drawLine(
                                color = if (isDoorOpen && (vertices[start].label?.contains("door") == true)) AccentOrange else RideNeonCyan,
                                start = rotatedPoints[start],
                                end = rotatedPoints[end],
                                strokeWidth = 3f,
                                alpha = 0.85f
                            )
                        }
                    }

                    // Drawing subtle glowing centers under vehicle
                    drawCircle(
                        color = Color(0x3300E5FF),
                        radius = 45f,
                        center = Offset(cx, cy + (1.2f * scaleFactor).toFloat() )
                    )
                }

                // Inline model selectors
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundGlass)
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "R: ${"%.0f".format(rotationAngleY)}°", color = RideNeonCyan, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ZOOM & SPECIAL ACTION CONTROL COMPANION CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Doors (Interactive action!)
                Button(
                    onClick = { isDoorOpen = !isDoorOpen },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDoorOpen) AccentOrange else CardBackgroundGlass),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .testTag("door_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDoorOpen) Icons.Default.DoorSliding else Icons.Default.DoorBack,
                        contentDescription = null,
                        tint = if (isDoorOpen) DeepSlateBackground else TextPrimaryGlow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDoorOpen) "Doors Open" else "Open Doors",
                        color = if (isDoorOpen) DeepSlateBackground else TextPrimaryGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Zoom control slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundGlass)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = null, tint = RideNeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = vehicleZoom,
                        onValueChange = { vehicleZoom = it },
                        valueRange = 0.6f..1.5f,
                        modifier = Modifier.width(110.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI-SHIELD SECURITY TELEMETRY CHECKLIST
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SHIELD CONNECT REPORT",
                        color = TextPrimaryGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecurityStatusIndicator("Anti-Theft Engine Immobilizer", "ONLINE & LINKED", AccentTeal)
                    SecurityStatusIndicator("CCTV Interior Cabin Stream", "STANDBY SETUP RUNNING", RideNeonCyan)
                    SecurityStatusIndicator("Accident Detection Gyroscope", "CALIBRATED (Normal 1G)", AccentTeal)
                    SecurityStatusIndicator("Smoke & Fire Detector Sensor", "ZERO ppm CONCENTRATION", AccentTeal)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SPECS DETAILS WRAPS
            Text(
                text = "VEHICLE SPECIFICATIONS",
                color = TextMutedGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                SpecBlock("BATTERY CAPACITY", "${vehicle!!.batteryPct}% Charge", Icons.Default.Bolt, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                SpecBlock("DRIVING RANGE", "${vehicle!!.rangeKm} km Active", Icons.Default.Timeline, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SpecBlock("ENERGY FACTOR", "Dual Brushless EV", Icons.Default.FlashOn, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                SpecBlock("SECURITY SECURITY", "5★ Safety Verified", Icons.Default.SafetyCheck, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // PRICING BLOCK & BOOK NOW TRIGGER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "TOTAL RATE (BASE)", color = TextMutedGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "₹${vehicle!!.pricePerHr} / Hour", color = TextPrimaryGlow, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = {
                        viewModel.startBooking(vehicle!!)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .width(160.dp)
                        .testTag("instant_book_button")
                ) {
                    Text("Reserve Telemetry ➜", color = DeepSlateBackground, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun SecurityStatusIndicator(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMutedGlow, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = status, color = color, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun SpecBlock(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x06FFFFFF)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, Color(0x1A60A5FA), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = RideNeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = label, color = TextMutedGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = value, color = TextPrimaryGlow, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

// 3D vector vertices math descriptors
data class Vertex3D(val x: Float, val y: Float, val z: Float, val label: String? = null)

// SLEEK CAR WIREFRAME VERTICES (Chassis outlines + doors)
private fun getSleekCarVertices(): List<Vertex3D> {
    return listOf(
        // Chassis Bottom Base Frame
        Vertex3D(-2.0f, -0.6f, -1.0f),  // 0: Back-Right
        Vertex3D(-2.0f, 0.6f, -1.0f),   // 1: Back-Left
        Vertex3D(2.0f, 0.6f, -1.0f),    // 2: Front-Left
        Vertex3D(2.0f, -0.6f, -1.0f),   // 3: Front-Right

        // Hood / Trunk elevated deck
        Vertex3D(-1.8f, -0.5f, -0.3f),  // 4: Trunk-Right
        Vertex3D(-1.8f, 0.5f, -0.3f),   // 5: Trunk-Left
        Vertex3D(1.4f, 0.5f, -0.3f),    // 6: Hood-Left
        Vertex3D(1.4f, -0.5f, -0.3f),   // 7: Hood-Right

        // Cockpit Pillars (Bubble dome!)
        Vertex3D(-0.8f, -0.45f, 0.4f),  // 8: Roof-Back-Right
        Vertex3D(-0.8f, 0.45f, 0.4f),   // 9: Roof-Back-Left
        Vertex3D(0.6f, 0.45f, 0.4f),    // 10: Roof-Front-Left
        Vertex3D(0.6f, -0.45f, 0.4f),   // 11: Roof-Front-Right

        // Front bumper noses
        Vertex3D(2.3f, -0.4f, -0.8f),   // 12: Nose-Right
        Vertex3D(2.3f, 0.4f, -0.8f),    // 13: Nose-Left

        // Interactive Left / Right Door Midpoint markers
        Vertex3D(-0.2f, 0.6f, -0.2f, "door_left"),  // 14
        Vertex3D(0.4f, 0.6f, -0.2f, "door_left"),   // 15
        Vertex3D(-0.2f, -0.6f, -0.2f, "door_right"), // 16
        Vertex3D(0.4f, -0.6f, -0.2f, "door_right")   // 17
    )
}

private fun getSleekCarEdges(): List<Pair<Int, Int>> {
    return listOf(
        // Base perimeter connection
        Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 0),

        // Nose connections
        Pair(2, 13), Pair(3, 12), Pair(12, 13),

        // Deck elevations
        Pair(0, 4), Pair(1, 5), Pair(2, 6), Pair(3, 7),
        Pair(4, 5), Pair(6, 7), Pair(5, 6), Pair(4, 7),

        // Roof wrap connections
        Pair(4, 8), Pair(5, 9), Pair(6, 10), Pair(7, 11),
        Pair(8, 9), Pair(9, 10), Pair(10, 11), Pair(11, 8),

        // Door panels drawing (interactive highlights)
        Pair(14, 15), Pair(16, 17)
    )
}

// BIKE WIREFRAME VERTICES (Motorcycle skeleton)
private fun getMotorcycleVertices(): List<Vertex3D> {
    return listOf(
        // Wheels
        Vertex3D(-1.5f, 0.0f, -0.8f),  // 0: Rear Hub
        Vertex3D(1.5f, 0.0f, -0.8f),   // 1: Front Hub

        // Engine core box
        Vertex3D(-0.4f, -0.2f, -0.4f),  // 2
        Vertex3D(-0.4f, 0.2f, -0.4f),   // 3
        Vertex3D(0.4f, 0.2f, -0.4f),    // 4
        Vertex3D(0.4f, -0.2f, -0.4f),   // 5

        // Fuel Tank elevation (Hump!)
        Vertex3D(0.1f, -0.25f, 0.2f),   // 6
        Vertex3D(0.1f, 0.25f, 0.2f),    // 7

        // Seat deck
        Vertex3D(-0.9f, 0.0f, -0.1f),   // 8

        // Handlebars high pillars
        Vertex3D(1.1f, -0.4f, 0.5f),    // 9: Handle-Bar-Right
        Vertex3D(1.1f, 0.4f, 0.5f)      // 10: Handle-Bar-Left
    )
}

private fun getMotorcycleEdges(): List<Pair<Int, Int>> {
    return listOf(
        // Frame outline (Hub connections)
        Pair(0, 2), Pair(0, 3), Pair(1, 4), Pair(1, 5),

        // Hubs back/front skeletons
        Pair(2, 3), Pair(3, 4), Pair(4, 5), Pair(5, 2),

        // Tank skeleton connect
        Pair(2, 6), Pair(3, 7), Pair(6, 7), Pair(4, 7), Pair(5, 6),

        // Seat setup
        Pair(8, 2), Pair(8, 3), Pair(8, 6), Pair(8, 7),

        // Handles
        Pair(4, 10), Pair(5, 9), Pair(9, 10),

        // Ring outlines for wheels (Hub extensions)
        Pair(0, 8), Pair(1, 9)
    )
}
