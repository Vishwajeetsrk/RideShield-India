package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Vehicle
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: RideShieldViewModel,
    modifier: Modifier = Modifier,
    onVehicleClick: () -> Unit = {}
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val filteredVehicles by viewModel.filteredVehicles.collectAsState()
    val activeBooking by viewModel.activeBooking.collectAsState()
    val mapLayer by viewModel.mapLayer.collectAsState()
    val activeLat by viewModel.activeLat.collectAsState()
    val activeLng by viewModel.activeLng.collectAsState()
    val routeTrack by viewModel.routeTrack.collectAsState()

    // Search and filter state variables
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val searchLocation by viewModel.searchLocation.collectAsState()
    val rentalStartDate by viewModel.rentalStartDate.collectAsState()
    val rentalEndDate by viewModel.rentalEndDate.collectAsState()

    // View tabs: "MAP" or "GRID"
    var viewMode by remember { mutableStateOf("MAP") } // "MAP" or "GRID"
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Interactive panning coordinates inside Canvas
    var mapOffsetX by remember { mutableStateOf(0f) }
    var mapOffsetY by remember { mutableStateOf(0f) }
    var zoomScale by remember { mutableStateOf(1.0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
    ) {
        // ----------------- ROOT SCROLLABLE CONTENT FOR FLEET DASHBOARD GRID -----------------
        if (viewMode == "GRID") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Spacer(modifier = Modifier.height(76.dp))

                // Premium Search and Filter widgets panel
                SearchAndFiltersPanel(
                    viewModel = viewModel,
                    onOpenDatePicker = { showDatePickerDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Available Fleet Title & Toggle back to Map
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AVAILABLE SHIELDED FLEET",
                            color = RideNeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${filteredVehicles.size} Premium Vehicles Available",
                            color = TextMutedGlow,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1F00E5FF))
                            .clickable { viewMode = "MAP" }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Show Map View", color = RideNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High-fidelity Multi-column Adaptive responsive Grid Display
                if (filteredVehicles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextMutedGlow, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Vehicles match your selected filter ranges.", color = TextPrimaryGlow, fontSize = 14.sp)
                        }
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        val columns = if (maxWidth > 600.dp) 3 else 2
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredVehicles) { vehicle ->
                                PremiumGridDashboardCard(
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
        } else {
            // ----------------- ROOT INTERACTIVE MAP RENDER MODE -----------------
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

                // Coordinate lines auxiliary helper
                drawCoordinatesGrid(cx, cy, zoomScale)

                // RENDER INDIAN LANDMARKS (Bengaluru Context)
                val nodes = getBengaluruNodes()
                nodes.forEach { node ->
                    val px = cx + (node.lng - 77.5946) * 11000f * zoomScale
                    val py = cy - (node.lat - 12.9716) * 11000f * zoomScale

                    drawCircle(
                        color = Color(0x333B82F6),
                        radius = 16f * zoomScale,
                        center = Offset(px.toFloat(), py.toFloat())
                    )
                    drawLabelText(node.name, px.toFloat(), py.toFloat() - 20)
                }

                // STREETS / STREET ROAD PATHWAYS
                for (i in 0 until nodes.size - 1) {
                    val p1 = Offset(
                        (cx + (nodes[i].lng - 77.5946) * 11000f * zoomScale).toFloat(),
                        (cy - (nodes[i].lat - 12.9716) * 11000f * zoomScale).toFloat()
                    )
                    val p2 = Offset(
                        (cx + (nodes[i + 1].lng - 77.5946) * 11000f * zoomScale).toFloat(),
                        (cy - (nodes[i + 1].lat - 12.9716) * 11000f * zoomScale).toFloat()
                    )

                    drawLine(
                        color = Color(0x2294A3B8),
                        start = p1,
                        end = p2,
                        strokeWidth = 6f * zoomScale
                    )

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

                // Overlay nodes
                nodes.forEachIndexed { i, node ->
                    val px = cx + (node.lng - 77.5946) * 11000f * zoomScale
                    val py = cy - (node.lat - 12.9716) * 11000f * zoomScale

                    if (mapLayer == "ev" && i % 2 == 1) {
                        drawCircle(color = AccentTeal, radius = 6f, center = Offset(px.toFloat() + 15, py.toFloat() + 15))
                    }
                    if (mapLayer == "police" && i % 3 == 1) {
                        drawCircle(color = RideNeonBlue, radius = 5f, center = Offset(px.toFloat() - 15, py.toFloat() - 15))
                    }
                    if (mapLayer == "weather") {
                        drawCircle(color = Color(0x5538BDF8), radius = 30f, center = Offset(px.toFloat() + 10, py.toFloat() - 10))
                    }
                }

                // Active trailing route mapping animation
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

                // MOVING ACTIVE DOT VEHICLES
                if (activeBooking != null) {
                    val mx = cx + (activeLng - 77.5946) * 11000f * zoomScale
                    val my = cy - (activeLat - 12.9716) * 11000f * zoomScale
                    drawCircle(color = AccentRedSOS, radius = 12f * zoomScale, center = Offset(mx.toFloat(), my.toFloat()))
                    drawCircle(color = Color.White, radius = 4f * zoomScale, center = Offset(mx.toFloat(), my.toFloat()))
                } else {
                    // Render current filtered selection
                    filteredVehicles.forEach { vehicle ->
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

            // Floated top widgets search layout over the live map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 76.dp)
            ) {
                SearchAndFiltersPanel(
                    viewModel = viewModel,
                    onOpenDatePicker = { showDatePickerDialog = true }
                )
            }

            // Layer selectors under search
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 210.dp)
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

            // Floated Re-center Zoom controllers
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp)
            ) {
                FloatingActionButton(
                    onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.5f) },
                    containerColor = CardBackgroundGlass,
                    contentColor = RideNeonCyan,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp).border(1.dp, BorderGlass, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.5f) },
                    containerColor = CardBackgroundGlass,
                    contentColor = RideNeonCyan,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp).border(1.dp, BorderGlass, CircleShape)
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
                    modifier = Modifier.size(44.dp).border(1.dp, BorderGlass, CircleShape)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }

            // Bottom horizontal swiper matching filtered options
            if (activeBooking == null) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth > 600.dp
                    if (isWideScreen) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 270.dp, end = 24.dp)
                                .width(310.dp)
                                .heightIn(max = 340.dp)
                                .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                Text(
                                    text = "SHIELD MATCHES (${filteredVehicles.size})",
                                    color = RideNeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderGlass).padding(vertical = 8.dp))
                                androidx.compose.foundation.lazy.LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(filteredVehicles.filter { it.status == "Available" }) { vehicle ->
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
                                items(filteredVehicles.filter { it.status == "Available" }) { vehicle ->
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

        // ----------- FLOATING TAB SWITCH CHANGER (Map vs Grid) -----------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(2.dp, BorderGlass, RoundedCornerShape(32.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (viewMode == "MAP") RideNeonCyan else Color.Transparent)
                    .clickable { viewMode = "MAP" }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint = if (viewMode == "MAP") DeepSlateBackground else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Map Navigator",
                        color = if (viewMode == "MAP") DeepSlateBackground else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (viewMode == "GRID") RideNeonCyan else Color.Transparent)
                    .clickable { viewMode = "GRID" }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = null,
                        tint = if (viewMode == "GRID") DeepSlateBackground else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Grid Dashboard",
                        color = if (viewMode == "GRID") DeepSlateBackground else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ---------- RENTAL DATES RANGE SETTING MODAL DIALOG ----------
        if (showDatePickerDialog) {
            AlertDialog(
                onDismissRequest = { showDatePickerDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = RideNeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SET RENTAL SCHEDULES", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryGlow)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Set precise pickup and return slots to match available telemetry. Coordinates are matching Bangalore local times.",
                            color = TextMutedGlow,
                            fontSize = 11.sp
                        )

                        var startTemp by remember { mutableStateOf(rentalStartDate) }
                        var endTemp by remember { mutableStateOf(rentalEndDate) }
                        var locationTemp by remember { mutableStateOf(searchLocation) }

                        OutlinedTextField(
                            value = locationTemp,
                            onValueChange = { locationTemp = it },
                            label = { Text("Rental Base Location", color = TextMutedGlow) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryGlow,
                                unfocusedTextColor = TextPrimaryGlow,
                                focusedBorderColor = RideNeonCyan,
                                unfocusedBorderColor = BorderGlass
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = RideNeonCyan) }
                        )

                        OutlinedTextField(
                            value = startTemp,
                            onValueChange = { startTemp = it },
                            label = { Text("Schedules Start Date", color = TextMutedGlow) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryGlow,
                                unfocusedTextColor = TextPrimaryGlow,
                                focusedBorderColor = RideNeonCyan,
                                unfocusedBorderColor = BorderGlass
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = RideNeonCyan) }
                        )

                        OutlinedTextField(
                            value = endTemp,
                            onValueChange = { endTemp = it },
                            label = { Text("Schedules End Date", color = TextMutedGlow) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimaryGlow,
                                unfocusedTextColor = TextPrimaryGlow,
                                focusedBorderColor = RideNeonCyan,
                                unfocusedBorderColor = BorderGlass
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = RideNeonCyan) }
                        )

                        // Quick tags
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            listOf("Today (2h)", "Weekend (48h)", "Weekly").forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x1F60A5FA))
                                        .clickable {
                                            if (tag.contains("Today")) {
                                                startTemp = "11 Jun 2026"
                                                endTemp = "11 Jun 2026 (2 hrs)"
                                            } else {
                                                startTemp = "11 Jun 2026"
                                                endTemp = "13 Jun 2026"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(tag, color = RideNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Save Actions
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showDatePickerDialog = false }) {
                                Text("Cancel", color = AccentRedSOS)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    viewModel.rentalStartDate.value = startTemp
                                    viewModel.rentalEndDate.value = endTemp
                                    viewModel.searchLocation.value = locationTemp
                                    showDatePickerDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan)
                            ) {
                                Text("Verify & Save Slot", color = DeepSlateBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                containerColor = Color(0xFF0F172A),
                modifier = Modifier.border(1.dp, BorderGlass, RoundedCornerShape(28.dp))
            )
        }
    }
}

// ------------------- DETAILED PREMIUM SUBSECTION COMPONENT -------------------
@Composable
fun SearchAndFiltersPanel(
    viewModel: RideShieldViewModel,
    onOpenDatePicker: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val searchLocation by viewModel.searchLocation.collectAsState()
    val rentalStartDate by viewModel.rentalStartDate.collectAsState()
    val rentalEndDate by viewModel.rentalEndDate.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Search Input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by car/bike name, license...", color = TextMutedGlow, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryGlow,
                    unfocusedTextColor = TextPrimaryGlow,
                    focusedBorderColor = RideNeonCyan,
                    unfocusedBorderColor = BorderGlass,
                    focusedContainerColor = Color(0x330F172A),
                    unfocusedContainerColor = Color(0x330F172A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_search_input"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RideNeonCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMutedGlow,
                            modifier = Modifier.clickable { viewModel.searchQuery.value = "" }
                        )
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Inline schedule block with Date picker click
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1F94A3B8))
                    .clickable { onOpenDatePicker() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = RideNeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "RENTAL PERIOD (TAP TO SET)", color = TextMutedGlow, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$rentalStartDate - $rentalEndDate • $searchLocation",
                            color = TextPrimaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = RideNeonCyan, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Type Selector pills (All - Cars - Bikes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val types = listOf("All", "Car", "Bike")
                types.forEach { typeLabel ->
                    val isSelected = selectedType.lowercase() == typeLabel.lowercase() || (selectedType == "All" && typeLabel == "All")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) RideNeonCyan else Color(0x3300E5FF))
                            .border(1.dp, if (isSelected) Color.Transparent else BorderGlass, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectedType.value = typeLabel }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            color = if (isSelected) DeepSlateBackground else TextPrimaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumGridDashboardCard(vehicle: Vehicle, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackgroundGlass),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("vehicle_dashboard_grid_card_${vehicle.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Name / Plate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vehicle.name,
                        color = TextPrimaryGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = vehicle.registrationNumber,
                        color = RideNeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = if (vehicle.type == "car") Icons.Default.DirectionsCar else Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = RideNeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Charge stats and range sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Charge pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${vehicle.batteryPct}%", color = TextMutedGlow, fontSize = 11.sp)
                }

                // Range Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = RideNeonBlue, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${vehicle.rangeKm}km", color = TextMutedGlow, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Divider(color = BorderGlass)

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing details block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${vehicle.pricePerHr}/hr",
                    color = TextPrimaryGlow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RideNeonBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(text = "BOOK NOW", color = RideNeonBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------- AUXILIARY LANDMARKS STRUCTS -----------------
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

private fun DrawScope.drawCoordinatesGrid(cx: Float, cy: Float, scale: Float) {
    val step = 100f * scale
    val color = Color(0x0664748B)

    var x = cx % step
    while (x < size.width) {
        drawLine(color = color, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1f)
        x += step
    }

    var y = cy % step
    while (y < size.height) {
        drawLine(color = color, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

private fun DrawScope.drawLabelText(text: String, x: Float, y: Float) {
    drawCircle(color = RideNeonCyan, radius = 3f, center = Offset(x, y + 10f))
}

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
