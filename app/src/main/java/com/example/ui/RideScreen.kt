package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Booking
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RideScreen(
    viewModel: RideShieldViewModel,
    booking: Booking,
    onCompleteRide: () -> Unit
) {
    val speed by viewModel.vehicleSpeed.collectAsState()
    val smoke by viewModel.smokeLevel.collectAsState()
    val isCctvRecording by viewModel.isCctvRecording.collectAsState()
    val activeLat by viewModel.activeLat.collectAsState()
    val activeLng by viewModel.activeLng.collectAsState()

    // Blinking animation state for CCTV red recorder dot
    var isRecDotVisible by remember { mutableStateOf(true) }
    LaunchedEffect(key1 = isCctvRecording) {
        if (isCctvRecording) {
            while (true) {
                isRecDotVisible = !isRecDotVisible
                kotlinx.coroutines.delay(800)
            }
        }
    }

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
            // Screen header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE TRIP CORE",
                        color = TextPrimaryGlow,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "AI-SECURE ON", color = AccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DOUBLE CORE GAUGE: SPEEDOMETER & CABIN SMOKE DETECTOR
            Row(modifier = Modifier.fillMaxWidth()) {
                // 1. Futuristic Speedometer
                Card(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(180.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "SPEEDOMETER",
                            color = TextMutedGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(modifier = Modifier.size(90.dp)) {
                            // Custom canvas needle dial
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0x1F60A5FA),
                                    startAngle = 150f,
                                    sweepAngle = 240f,
                                    useCenter = false,
                                    style = Stroke(width = 8f),
                                    topLeft = Offset(5f, 5f),
                                    size = Size(size.width - 10f, size.height - 10f)
                                )

                                // Current Speed sweep
                                val sweep = (speed / 120f) * 240f
                                drawArc(
                                    color = if (speed > 90f) AccentOrange else RideNeonCyan,
                                    startAngle = 150f,
                                    sweepAngle = sweep.coerceAtMost(240f),
                                    useCenter = false,
                                    style = Stroke(width = 8f),
                                    topLeft = Offset(5f, 5f),
                                    size = Size(size.width - 10f, size.height - 10f)
                                )

                                // Center Pin
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                drawCircle(color = TextPrimaryGlow, radius = 6f, center = Offset(cx, cy))

                                // Needle angle calculation (start angle is 150 deg)
                                val theta = Math.toRadians((150f + sweep).toDouble())
                                val needleLength = size.width / 2.2f
                                val nx = cx + (needleLength * cos(theta)).toFloat()
                                val ny = cy + (needleLength * sin(theta)).toFloat()
                                drawLine(
                                    color = TextPrimaryGlow,
                                    start = Offset(cx, cy),
                                    end = Offset(nx, ny),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${speed.toInt()} KM/H",
                            color = if (speed > 90f) AccentOrange else TextPrimaryGlow,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Active Cabin Smoke Dial
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CABIN SMOKE",
                            color = TextMutedGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Icon(
                            imageVector = Icons.Default.SmokingRooms,
                            contentDescription = "Smoke sensor",
                            tint = if (smoke > 50f) AccentOrange else RideNeonCyan,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${smoke.toInt()} PPM",
                            color = if (smoke > 50f) AccentOrange else TextPrimaryGlow,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (smoke > 50f) "CIGARETTE DETECT" else "AIR QUALITY OK",
                            color = if (smoke > 50f) AccentOrange else AccentTeal,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CCTV LIVE INTEGRATION STREAM VIEW (Futuristic mock camera preview!)
            Text(
                text = "IN-CABIN TELEMETRY CCTV",
                color = TextMutedGlow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Interactive wide-angle cabin monitoring system.",
                color = TextMutedGlow,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                    .testTag("cctv_viewport")
            ) {
                // Interactive Vector Camera Scan Filter Inside Canvas!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw outer border and security grid (3x3 grid)
                    val w = size.width
                    val h = size.height
                    
                    val gridColor = Color(0x1F00E5FF)
                    drawLine(color = gridColor, start = Offset(w / 3f, 0f), end = Offset(w / 3f, h), strokeWidth = 1f)
                    drawLine(color = gridColor, start = Offset(w * 2f / 3f, 0f), end = Offset(w * 2f / 3f, h), strokeWidth = 1f)
                    drawLine(color = gridColor, start = Offset(0f, h / 3f), end = Offset(w, h / 3f), strokeWidth = 1f)
                    drawLine(color = gridColor, start = Offset(0f, h * 2f / 3f), end = Offset(w, h * 2f / 3f), strokeWidth = 1f)

                    // Draw abstract cabin contours (steering wheel sketch, pilot avatar outlines)
                    val avatarPath = Path().apply {
                        moveTo(w / 2f - 40f, h - 30f)
                        lineTo(w / 2f + 40f, h - 30f)
                        quadraticTo(w / 2f + 25f, h - 110f, w / 2f, h - 110f)
                        quadraticTo(w / 2f - 25f, h - 110f, w / 2f - 40f, h - 30f)
                    }
                    drawPath(path = avatarPath, color = Color(0x110284C7)) // driver silhouette
                    drawCircle(color = Color(0x110284C7), radius = 25f, center = Offset(w / 2f, h - 140f))

                    // Draw steering wheel outline
                    drawCircle(
                        color = Color(0x1B60A5FA),
                        radius = 40f,
                        center = Offset(w / 2.1f, h - 50f),
                        style = Stroke(width = 6f)
                    )

                    // Overlay chromatic scan filter line
                    val scanLineY = (System.currentTimeMillis() % 4000) / 4000f * h
                    drawLine(
                        color = Color(0x4000E5FF).copy(alpha = 0.25f),
                        start = Offset(0f, scanLineY),
                        end = Offset(w, scanLineY),
                        strokeWidth = 3f
                    )
                }

                // Overlay Recorder Controls inside CCTV
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x7F000000))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCctvRecording && isRecDotVisible) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentRedSOS)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isCctvRecording) "REC 1080P" else "CCTV OFF",
                        color = if (isCctvRecording) Color.White else TextMutedGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // GPS Coordinate status overlaid watermark
                Text(
                    text = "GPS: ${"%.4f".format(activeLat)}, ${"%.4f".format(activeLng)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI EMERGENCY ACCIDENT TRIGGER & SOS DOCKED CONTROL
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x2B1E1B4B)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, AccentRedSOS.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "SOS beacons",
                        tint = AccentRedSOS,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "EMERGENCY EMERGENCY SOS",
                        color = TextPrimaryGlow,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Press below to broadcast accident coords & dispatch police.",
                        color = TextMutedGlow,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Button(
                        onClick = { viewModel.triggerSOS() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRedSOS),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("emergency_sos_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CellTower, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "TRIGGER POLICE ALARM DISPATCH",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RIDE COMPLETE TRIGGER BUTTON
            Button(
                onClick = onCompleteRide,
                colors = ButtonDefaults.buttonColors(containerColor = RideNeonBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .testTag("end_trip_button")
            ) {
                Text(
                    text = "Return Vehicle & Submit Photos ➜",
                    color = TextPrimaryGlow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
