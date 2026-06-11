package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Futuristic Glassmorphic Dark UI Theme colors
val DeepSlateBackground = Color(0xFF050A10) // Very soft OLED near black (#050A10)
val CardBackgroundGlass = Color(0x19FFFFFF)  // Transparent white-tinted glass overlay
val BorderGlass = Color(0x26FFFFFF)          // Crystal white slim border

// Glowing primary accents
val RideNeonCyan = Color(0xFF34D399)         // Sleek Emerald Green (emerald-400)
val RideNeonBlue = Color(0xFF6366F1)         // Sleek Indigo Blue (indigo-500)
val AccentTeal = Color(0xFF10B981)           // Safe status green (emerald-500)
val AccentOrange = Color(0xFFF59E0B)         // Warning amber limits
val AccentRedSOS = Color(0xFFEF4444)         // Emergency triggers

// Grey scaling
val TextPrimaryGlow = Color(0xFFF8FAFC)
val TextMutedGlow = Color(0xFF94A3B8)
val DarkPurpleMuted = Color(0xFF1E1B4B)

/**
 * Premium Fintech CSS Dark Mode Variable System mapped to Jetpack Compose Colors.
 * Maintains deep charcoal backgrounds and vibrant neon accent colors.
 */
object CssThemeVariables {
    // Background tokens
    val `--bg-primary` = DeepSlateBackground         // Deep charcoal background
    val `--bg-card-glass` = CardBackgroundGlass      // Glassmorphism background
    val `--border-glass` = BorderGlass               // Thin crystal borders
    
    // Brand buttons and alert states
    val `--accent-neon-cyan` = RideNeonCyan          // Vibrant terminal/hud emerald accent
    val `--accent-neon-blue` = RideNeonBlue          // Premium functional links & buttons
    val `--accent-neon-warning` = AccentOrange        // Alert accent for warnings/sensors
    val `--accent-neon-sos` = AccentRedSOS           // Vital state/SOS trigger red
    
    // Typography tokens
    val `--text-primary` = TextPrimaryGlow
    val `--text-muted` = TextMutedGlow
}

/**
 * Global CSS Transition / Keyframe Simulation Helper for Jetpack Compose.
 * Creates smooth 60fps floating UI animation keyframes for responsive/floating elements.
 */
@Composable
fun Modifier.floatingAnimation(
    translationYMax: Float = 6f,
    durationMs: Int = 2200
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "css_floating_keyframes")
    val translationY by infiniteTransition.animateFloat(
        initialValue = -translationYMax,
        targetValue = translationYMax,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "css_transform_translateY"
    )
    return this.graphicsLayer {
        this.translationY = translationY
    }
}

