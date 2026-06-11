package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CompletionScreen(
    viewModel: RideShieldViewModel,
    onPaymentClose: () -> Unit
) {
    val activeBooking by viewModel.activeBooking.collectAsState()
    val isProcessing by viewModel.damageProcessing.collectAsState()
    val damageReport by viewModel.damageReportResult.collectAsState()

    val beforeBmp by viewModel.beforeBitmapState.collectAsState()
    val afterBmp by viewModel.afterBitmapState.collectAsState()

    var billingStatus by remember { mutableStateOf("pending") } // pending, pay_processing, completed

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
                Text(
                    text = "VEHICLE RETURN CHECKOUT",
                    color = TextPrimaryGlow,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Icon(
                    imageVector = Icons.Default.AddHome,
                    contentDescription = null,
                    tint = RideNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AI Smart Damage Inspector",
                color = TextPrimaryGlow,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "We compare pre-ride diagnostic photos with post-ride photos instantly using Gemini Pro Vision.",
                color = TextMutedGlow,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // FOTO PANELS: Before vs After
            Row(modifier = Modifier.fillMaxWidth()) {
                // Pre-ride Foto Box
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "PRE-RIDE PHOTO", color = TextMutedGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x0EFFFFFF))
                            .border(1.dp, BorderGlass, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (beforeBmp != null) {
                            Image(
                                bitmap = beforeBmp!!.asImageBitmap(),
                                contentDescription = "Before Photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Populate default mock base pre-ride image
                            LaunchedEffect(Unit) {
                                val mockBmp = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(mockBmp)
                                val paint = Paint().apply { color = android.graphics.Color.DKGRAY }
                                canvas.drawRect(RectF(10f, 10f, 140f, 140f), paint)
                                viewModel.beforeBitmapState.value = mockBmp
                            }
                            CircularProgressIndicator(color = RideNeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Post-ride Foto Box
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "POST-RIDE PHOTO", color = TextMutedGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x0EFFFFFF))
                            .border(
                                1.dp,
                                if (afterBmp != null) AccentTeal else BorderGlass,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (afterBmp != null) {
                            Image(
                                bitmap = afterBmp!!.asImageBitmap(),
                                contentDescription = "After Photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Prompt to Take post return photo
                            Button(
                                onClick = {
                                    // Generate mock return photo (simulate a tiny scratch scratch line!)
                                    val mockBmp = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(mockBmp)
                                    val paint = Paint().apply { color = android.graphics.Color.DKGRAY }
                                    canvas.drawRect(RectF(10f, 10f, 140f, 140f), paint)
                                    // Slight scratch line
                                    paint.color = android.graphics.Color.RED
                                    canvas.drawLine(40f, 40f, 95f, 95f, paint)
                                    viewModel.afterBitmapState.value = mockBmp
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardBackgroundGlass),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .testTag("upload_post_photo_button")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = RideNeonCyan)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Snap return", fontSize = 9.sp, color = TextPrimaryGlow)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI PROCESS TRIGGER
            if (damageReport == null) {
                Button(
                    onClick = {
                        viewModel.submitAfterPhotoAndProcess(beforeBmp, afterBmp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                    shape = RoundedCornerShape(16.dp),
                    enabled = afterBmp != null && !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("run_ai_damage_detection")
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = DeepSlateBackground, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activating Gemini claims scan...", color = DeepSlateBackground)
                    } else {
                        Icon(Icons.Default.QueryStats, contentDescription = null, tint = DeepSlateBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trigger AI Damage Comparison", color = DeepSlateBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DAMAGE ANALYSIS DETAILS DRAW
            if (damageReport != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x3B10B981)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentTeal, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FactCheck, contentDescription = null, tint = AccentTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "GEMINI SMART CLAIMS REPORT",
                                color = TextPrimaryGlow,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = damageReport!!,
                            color = TextPrimaryGlow,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AUTO BILLING RECEIPT CARD
                Text(
                    text = "AUTO BILLING DETAILS",
                    color = TextMutedGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0EFFFFFF)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGlass, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val fareRate = activeBooking?.baseFare ?: 120.0
                        val fareCalc = fareRate * 1.5

                        BillingRow("Base Telemetry Ride Fee", "₹${"%.2f".format(fareRate)}")
                        BillingRow("Live Telemetry GPS/CCTV", "₹0.00 (SHIELD INCLUDED)")
                        BillingRow("GST & Indian Road Cess (18%)", "₹${"%.2f".format(fareRate * 0.18)}")
                        Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 12.dp))
                        BillingRow("Damage claim liability", "₹0.00 (No severe scratches)", AccentTeal)
                        Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 12.dp))
                        BillingRow("Total Debited (Autopay)", "₹${"%.2f".format(fareCalc + (fareRate * 0.18))}", RideNeonCyan, true)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // COMPLETE PAYMENT & FINALIZE RIDE
                Button(
                    onClick = {
                        viewModel.finalPayAndCloseTrip()
                        onPaymentClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("complete_checkout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RideNeonCyan),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Approve & Finalize Return ➜",
                        color = DeepSlateBackground,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BillingRow(label: String, value: String, color: Color = TextPrimaryGlow, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMutedGlow, fontSize = 12.sp)
        Text(
            text = value,
            color = color,
            fontSize = if (isBold) 16.sp else 12.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold
        )
    }
}
