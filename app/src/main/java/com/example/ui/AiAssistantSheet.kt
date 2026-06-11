package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SupportMessage
import com.example.ui.theme.*

@Composable
fun AiAssistantSheet(viewModel: RideShieldViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto scroll chat to bottom when message list expands
    LaunchedEffect(key1 = messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSlateBackground)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        // Chat Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = RideNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SHIELDY CO-PILOT",
                    color = TextPrimaryGlow,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(RideNeonCyan.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = "THINKING HIGH", color = RideNeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SUGGESTION PILLS ROW
        Text(
            text = "QUICK SAFETY INQUIRIES",
            color = TextMutedGlow,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "SOS Incident Protocol",
                "Review EV Station Maps",
                "Marginal pricing check"
            )
            suggestions.forEach { label ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackgroundGlass)
                        .border(1.dp, BorderGlass, RoundedCornerShape(12.dp))
                        .clickable { viewModel.sendMessage(label) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = label, color = TextPrimaryGlow, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MESSAGE CONTENT TIMELINE
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai" || msg.sender == "system"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.sender == "system") Color(0x333B82F6)
                            else if (isAi) Color(0x1AFFFFFF)
                            else RideNeonBlue
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAi) 4.dp else 16.dp,
                            bottomEnd = if (isAi) 16.dp else 4.dp
                        ),
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(
                                1.dp,
                                if (isAi) BorderGlass else Color.Transparent,
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isAi) 4.dp else 16.dp,
                                    bottomEnd = if (isAi) 16.dp else 4.dp
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (msg.sender == "system") "SHIELD ANNOUNCEMENT" else if (isAi) "SHIELDY AI" else "YOU",
                                color = if (isAi) RideNeonCyan else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = msg.text,
                                color = if (isAi) TextPrimaryGlow else Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INPUT CONTEXT CONTROL BOX
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackgroundGlass)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputQuery,
                onValueChange = { inputQuery = it },
                placeholder = { Text("Ask Shieldy coordinates/SOS guide...", color = TextMutedGlow, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryGlow,
                    unfocusedTextColor = TextPrimaryGlow,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text")
            )

            IconButton(
                onClick = {
                    val query = inputQuery.trim()
                    if (query.isNotEmpty()) {
                        viewModel.sendMessage(query)
                        inputQuery = ""
                    }
                },
                enabled = inputQuery.isNotEmpty(),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (inputQuery.isNotEmpty()) RideNeonCyan else Color.Transparent)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputQuery.isNotEmpty()) DeepSlateBackground else TextMutedGlow,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
