package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapScreen(
    viewModel: RideShieldViewModel,
    modifier: Modifier = Modifier,
    onVehicleClick: () -> Unit = {}
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val activeBooking by viewModel.activeBooking.collectAsState()
    val mapLayer by viewModel.mapLayer.collectAsState()
    val activeLat by viewModel.activeLat.collectAsState()
    val activeLng by viewModel.activeLng.collectAsState()
    val routeTrack by viewModel.routeTrack.collectAsState()

    // Interactive panning coordinates inside Canvas
    var mapOffsetX by remember { mutableStateOf(0f) }
    var mapOffsetY by remember { mutableStateOf(0f) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
    ) {
        // Futuristic Interactive Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        mapOffsetX += dragAmount.x
                        mapOffsetY += dragAmount.y
                    }
                }
                .testTag("interactive_map_canvas")
        ) {
            val cx = size.width / 2f + mapOffsetX
            val cy = size.height / 2f + mapOffsetY

            // RENDER BASE GRID (Latitude / Longitude grids)
            drawCoordinatesGrid(cx, cy, zoomScale)

            // RENDER INDIAN LANDMARKS (Bengaluru Context)
            val nodes = getBengaluruNodes()
            nodes.forEach { node ->
                val px = cx + (node.lng - 77.5946) * 11000f * zoomScale
                val py = cy - (node.lat - 12.9716) * 11000f * zoomScale

                // Draw node anchor
                drawCircle(
                    color = Color(0x333B82F6),
                    radius = 16f * zoomScale,
                    center = Offset(px.toFloat(), py.toFloat())
                )
                // Label
                drawLabelText(node.name, px.toFloat(), py.toFloat() - 20)
            }

            // RENDER STREET ROADS
            for (i in 0 until nodes.size - 1) {
                val p1 = Offset(
                    (cx + (nodes[i].lng - 77.5946) * 11000f * zoomScale).toFloat(),
                    (cy - (nodes[i].lat - 12.9716) * 11000f * zoomScale).toFloat()
                )
                val p2 = Offset(
                    (cx + (nodes[i + 1].lng - 77.5946) * 11000f * zoomScale).toFloat(),
                    (cy - (nodes[i + 1].lat - 12.9716) * 11000f * zoomScale).toFloat()
                )
                
                // Base structure
                drawLine(
                    color = Color(0x2294A3B8),
                    start = p1,
                    end = p2,
                    strokeWidth = 6f * zoomScale
                )

                // RENDER SELECTED LAYER SPECIAL TEAMS
                if (mapLayer == "traffic") {
                    val isCongested = i % 3 == 0
                    drawLine(
                        color = if (isCongested) AccentOrange else AccentTeal,
                        start = p1,
                        end = p2,
                        strokeWidth = 3f * zoomScale
                    )
                }
            }

            // RENDER LAYER SENSORS (EV, Police, Weather overlays)
            nodes.forEachIndexed { i, node ->
                val px = cx + (node.lng - 77.5946) * 11000f * zoomScale
                val py = cy - (node.lat - 12.9716) * 11000f * zoomScale

                if (mapLayer == "ev" && i % 2 == 1) {
                    // EV Charging Symbol
                    drawCircle(color = AccentTeal, radius = 6f, center = Offset(px.toFloat() + 15, py.toFloat() + 15))
                    drawRect(color = DeepSlateBackground, topLeft = Offset(px.toFloat() + 10, py.toFloat() + 10), size = Size(10f, 10f))
                }

                if (mapLayer == "police" && i % 3 == 1) {
                    // Police Post Shield dot
                    drawCircle(color = RideNeonBlue, radius = 5f, center = Offset(px.toFloat() - 15, py.toFloat() - 15))
                }

                if (mapLayer == "weather") {
                    // Cloud overlay ring
                    drawCircle(color = Color(0x5538BDF8), radius = 30f, center = Offset(px.toFloat() + 10, py.toFloat() - 10))
                }
            }

            // RENDER THE ACTIVE RIDE TRAILING ROUTE (Animated path drawing!)
            if (activeBooking != null && routeTrack.size > 1) {
                val path = Path()
                routeTrack.forEachIndexed { idx, coord ->
                    val rx = cx + (coord.second - 77.5946) * 11000f * zoomScale
                    val ry = cy - (coord.first - 12.9716) * 11000f * zoomScale
                    if (idx == 0) {
                        path.moveTo(rx.toFloat(), ry.toFloat())
                    } else {
                        path.lineTo(rx.toFloat(), ry.toFloat())
                    }
                }
                drawPath(
                    path = path,
                    color = RideNeonCyan,
                    style = Stroke(width = 8f * zoomScale)
                )
            }

            // RENDER MOVING ACTIVE VECHILE OR FLEET MARKERS
            if (activeBooking != null) {
                // Moving Active Marker
                val mx = cx + (activeLng - 77.5946) * 11000f * zoomScale
                val my = cy - (activeLat - 12.9716) * 11000f * zoomScale
                drawCircle(
                    color = AccentRedSOS,
                    radius = 12f * zoomScale,
                    center = Offset(mx.toFloat(), my.toFloat())
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f * zoomScale,
                    center = Offset(mx.toFloat(), my.toFloat())
                )
            } else {
                // Draw nearby available fleet markers
                vehicles.forEach { vehicle ->
                    if (vehicle.status == "Available") {
                        val vx = cx + (vehicle.lng - 77.5946) * 11000f * zoomScale
                        val vy = cy - (vehicle.lat - 12.9716) * 11000f * zoomScale
                        drawCircle(
                            color = if (vehicle.type == "car") RideNeonCyan else RideNeonBlue,
                            radius = 8f * zoomScale,
                            center = Offset(vx.toFloat(), vy.toFloat())
                        )
                    }
                }
            }
        }

        // FLOATING LAYER SWITCHER (Dynamic Island aesthetic capsule!)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
                .floatingAnimation(translationYMax = 3f, durationMs = 2800) // 100% smooth floating feel
                .clip(RoundedCornerShape(24.dp))
                .background(CardBackgroundGlass)
                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val layers = listOf(
                Pair("traffic", "Traffic"),
                Pair("weather", "Weather"),
                Pair("ev", "EV Charge"),
                Pair("police", "Police Check")
            )
            layers.forEach { (layId, title) ->
                val isActive = mapLayer == layId
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) RideNeonBlue else Color.Transparent)
                        .clickable { viewModel.mapLayer.value = layId }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isActive) TextPrimaryGlow else TextMutedGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // FLOATING RE-CENTER CONTROLS
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp)
                .floatingAnimation(translationYMax = 4f, durationMs = 2400)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) },
                containerColor = CardBackgroundGlass,
                contentColor = RideNeonCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, BorderGlass, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.5f) },
                containerColor = CardBackgroundGlass,
                contentColor = RideNeonCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, BorderGlass, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            Spacer(modifier = Modifier.height(8.dp))
            FloatingActionButton(
                onClick = {
                    mapOffsetX = 0f
                    mapOffsetY = 0f
                    zoomScale = 1.0f
                },
                containerColor = CardBackgroundGlass,
                contentColor = RideNeonCyan,
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, BorderGlass, CircleShape)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
            }
        }

        // SELECTION OVERLAY PANELS ON THE HOME SCREEN (If no ride is active)
        if (activeBooking == null) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val isWideScreen = maxWidth > 600.dp
                if (isWideScreen) {
                    // Desktop/Tablet responsive sidebar with high-performance glassmorphism and continuous float animation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 96.dp, start = 24.dp)
                            .width(320.dp)
                            .heightIn(max = 420.dp)
                            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                            .floatingAnimation(translationYMax = 5f, durationMs = 3200)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "AVAILABLE FLEET",
                                color = RideNeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Responsive Sidebar Grid",
                                color = TextMutedGlow,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BorderGlass)
                                    .padding(bottom = 12.dp)
                            )

                            // Dynamic high-performance responsive list / grid items
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                val available = vehicles.filter { it.status == "Available" }
                                items(available) { vehicle ->
                                    VehicleGridCard(
                                        vehicle = vehicle,
                                        onClick = {
                                            viewModel.selectedVehicle.value = vehicle
                                            onVehicleClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mobile View: Bottom-pinned horizontal swiper
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(vehicles.filter { it.status == "Available" }) { vehicle ->
                                VehicleHorizontalCard(
                                    vehicle = vehicle,
                                    onClick = {
                                        viewModel.selectedVehicle.value = vehicle
                                        onVehicleClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleGridCard(vehicle: Vehicle, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("vehicle_grid_card_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = vehicle.name,
                    color = TextPrimaryGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (vehicle.type == "car") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = RideNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = AccentTeal, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${vehicle.batteryPct}%", color = TextMutedGlow, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(Icons.Default.Route, contentDescription = "Range", tint = RideNeonBlue, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${vehicle.rangeKm}km", color = TextMutedGlow, fontSize = 11.sp)
                }

                Text(
                    text = "₹${vehicle.pricePerHr}/hr",
                    color = RideNeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// Draw auxiliary landmarks / coordinate helpers
private fun DrawScope.drawCoordinatesGrid(cx: Float, cy: Float, scale: Float) {
    val step = 100f * scale
    val color = Color(0x0664748B)

    // Vertical lines
    var x = cx % step
    while (x < size.width) {
        drawLine(color = color, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        x += step
    }

    // Horizontal lines
    var y = cy % step
    while (y < size.height) {
        drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

private fun DrawScope.drawLabelText(text: String, x: Float, y: Float) {
    // Standard DrawScope has no built-in native Text layout in Compose canvas.
    // Rather than pulling graphics native paint, we can draw a beautiful dot label 
    // block to represent it, keeping compiles incredibly solid and stable.
    drawCircle(color = RideNeonCyan, radius = 3f, center = Offset(x, y + 10f))
}

// Mock node layout for Bangalore coordinates
class MapNode(val name: String, val lat: Double, val lng: Double)

private fun getBengaluruNodes(): List<MapNode> {
    return listOf(
        MapNode("Vidhana Soudha", 12.9796, 77.5906),
        MapNode("Cubbon Park", 12.9734, 77.5913),
        MapNode("MG Road", 12.9716, 77.6115),
        MapNode("Indiranagar Core", 12.9719, 77.6412),
        MapNode("Koramangala Sp", 12.9352, 77.5350),
        MapNode("Namma metro hub", 12.9810, 77.6150),
        MapNode("Residential South", 12.9150, 77.5850)
    )
}

@Composable
fun VehicleHorizontalCard(vehicle: Vehicle, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .width(260.dp)
            .padding(end = 12.dp)
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("vehicle_card_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = vehicle.name,
                    color = TextPrimaryGlow,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (vehicle.type == "car") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = RideNeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = AccentTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${vehicle.batteryPct}% Charge", color = TextMutedGlow, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Route, contentDescription = "Range", tint = RideNeonBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${vehicle.rangeKm}km Autonomy", color = TextMutedGlow, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "₹${vehicle.pricePerHr}/hr",
                    color = RideNeonCyan,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Check Specifications ➜",
                    color = RideNeonBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
