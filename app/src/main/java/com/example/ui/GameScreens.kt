package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.*
import com.example.game.Screen
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// Colors
val CyberDark = Color(0xFF0F0F13)
val CyberGray = Color(0xFF1A1A24)
val CyberGreen = Color(0xFF00FF9C)
val CyberCyan = Color(0xFF22D3EE)
val CyberPink = Color(0xFFEC4899)
val CyberYellow = Color(0xFFFACC15)
val CyberOrange = Color(0xFFF97316)
val NeonRed = Color(0xFFEF4444)

@Composable
fun BatterySurvivorApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberDark
    ) {
        when (currentScreen) {
            Screen.MENU -> GameMenuScreen(viewModel)
            Screen.GAMEPLAY -> GameplayScreen(viewModel)
            Screen.GAMEOVER -> GameOverScreen(viewModel)
            Screen.HIGHSCORES -> HighScoresScreen(viewModel)
            Screen.HELP -> HelpScreen(viewModel)
        }
    }
}

@Composable
fun GameMenuScreen(viewModel: GameViewModel) {
    val highestScore by viewModel.highScore.collectAsStateWithLifecycle()
    
    // Aesthetic animations
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberDark, Color(0xFF101224), CyberDark)
                )
            )
    ) {
        // Decorative Canvas Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            // Draw circuit-style grid background
            val gridSpacing = 120f
            for (x in 0..(width / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFF1F223D),
                    start = Offset(x * gridSpacing, 0f),
                    end = Offset(x * gridSpacing, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(height / gridSpacing).toInt()) {
                drawLine(
                    color = Color(0xFF1F223D),
                    start = Offset(0f, y * gridSpacing),
                    end = Offset(width, y * gridSpacing),
                    strokeWidth = 1f
                )
            }

            // Draw beautiful glowing digital nodes
            val dots = listOf(
                Offset(gridSpacing * 2, gridSpacing * 4),
                Offset(gridSpacing * 6, gridSpacing * 7),
                Offset(gridSpacing * 3, gridSpacing * 12),
                Offset(gridSpacing * 7, gridSpacing * 11)
            )
            for (dot in dots) {
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.12f),
                    radius = 30f,
                    center = dot
                )
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.35f),
                    radius = 10f,
                    center = dot
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Highscore banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberGray.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "🏆 RECORD SURVIVAL: ",
                        color = CyberYellow,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "$highestScore PTS",
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // Central Branding Game Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Glow Battery Icon
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val capWidth = 24f
                        val capHeight = 16f
                        val batWidth = 70f
                        val batHeight = 110f

                        val cx = size.width / 2
                        val cy = size.height / 2

                        // Draw cap
                        drawRoundRect(
                            color = CyberGreen.copy(alpha = glowAlpha),
                            topLeft = Offset(cx - capWidth / 2, cy - batHeight / 2 - capHeight + 4f),
                            size = Size(capWidth, capHeight),
                            cornerRadius = CornerRadius(5f, 5f)
                        )

                        // Draw battery outline box
                        drawRoundRect(
                            color = CyberGreen,
                            topLeft = Offset(cx - batWidth / 2, cy - batHeight / 2),
                            size = Size(batWidth, batHeight),
                            cornerRadius = CornerRadius(12f, 12f),
                            style = Stroke(width = 6f)
                        )

                        // Draw glowing charge
                        val greenStripHeight = 70f
                        drawRoundRect(
                            color = CyberGreen,
                            topLeft = Offset(cx - (batWidth - 24f) / 2, cy + batHeight / 2 - greenStripHeight - 12f),
                            size = Size(batWidth - 24f, greenStripHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }

                // Game logo
                Text(
                    text = "BATTERY\nSURVIVOR",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 36.sp,
                    color = CyberGreen,
                    letterSpacing = 2.sp,
                    lineHeight = 42.sp
                )
                
                Text(
                    text = "inside a chaotic smartphone OS",
                    color = CyberCyan.copy(alpha = glowAlpha),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Options Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = { viewModel.changeScreen(Screen.GAMEPLAY) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(2.dp, CyberCyan, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Game",
                        tint = CyberDark,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "BOOT SYSTEM (START)",
                        color = CyberDark,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { viewModel.changeScreen(Screen.HIGHSCORES) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "HISTORY LOGS (RECORD DB)",
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { viewModel.changeScreen(Screen.HELP) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, CyberPink.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "MANUAL (HOW TO SURVIVE)",
                        color = CyberPink,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                text = "OS VERSION 12.0.4 - HEATER STATE: OPTIMAL",
                color = Color.Gray.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun GameplayScreen(viewModel: GameViewModel) {
    val px by viewModel.playerX.collectAsStateWithLifecycle()
    val py by viewModel.playerY.collectAsStateWithLifecycle()
    val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()
    val durationSec by viewModel.secondsSurvived.collectAsStateWithLifecycle()
    val particlesCollected by viewModel.particlesCollected.collectAsStateWithLifecycle()
    val shieldsActive by viewModel.shieldCount.collectAsStateWithLifecycle()
    val magnetActive by viewModel.magnetActive.collectAsStateWithLifecycle()
    val batterySaverActive by viewModel.batterySaverActive.collectAsStateWithLifecycle()

    val enemies by viewModel.enemies.collectAsStateWithLifecycle()
    val particles by viewModel.particles.collectAsStateWithLifecycle()
    val powerUps by viewModel.powerUps.collectAsStateWithLifecycle()
    val visualEffects by viewModel.visualEffects.collectAsStateWithLifecycle()
    val popupData by viewModel.activePopup.collectAsStateWithLifecycle()

    val screenShakeVal by viewModel.screenShake.collectAsStateWithLifecycle()
    val glitchEffectRate by viewModel.glitchRate.collectAsStateWithLifecycle()

    val textMeasurer = rememberTextMeasurer()

    val infiniteTransition = rememberInfiniteTransition(label = "Scroll")
    val gridOffsetAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GridScroll"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
    ) {
        // Core game Canvas viewport
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        viewModel.setPlayerPosition(change.position.x, change.position.y)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            viewModel.setCanvasSize(width, height)

            // Screen shaking calculations
            var shakeX = 0f
            var shakeY = 0f
            if (screenShakeVal > 0f) {
                shakeX = (Random().nextFloat() * 2f - 1f) * screenShakeVal * 1.5f
                shakeY = (Random().nextFloat() * 2f - 1f) * screenShakeVal * 1.5f
            }

            // Background Grid Lines
            val grid = 80f
            for (gx in 0..(width / grid).toInt() + 1) {
                val startX = gx * grid + shakeX
                drawLine(
                    color = Color(0xFF131526),
                    start = Offset(startX, 0f),
                    end = Offset(startX, height),
                    strokeWidth = 2f
                )
            }
            for (gy in 0..(height / grid).toInt() + 1) {
                val startY = gy * grid + (gridOffsetAnim % grid) + shakeY
                drawLine(
                    color = Color(0xFF131526),
                    start = Offset(0f, startY),
                    end = Offset(width, startY),
                    strokeWidth = 2f
                )
            }

            // Draw branching futuristic circuit background lines
            val branchColor = Color(0xFF1B1E3B)
            drawLine(branchColor, Offset(width * 0.1f + shakeX, height * 0.8f + shakeY), Offset(width * 0.1f + shakeX, height + shakeY), 3f)
            drawLine(branchColor, Offset(width * 0.1f + shakeX, height * 0.8f + shakeY), Offset(width * 0.25f + shakeX, height * 0.7f + shakeY), 3f)
            drawLine(branchColor, Offset(width * 0.9f + shakeX, height * 0.85f + shakeY), Offset(width * 0.9f + shakeX, height + shakeY), 3f)
            drawLine(branchColor, Offset(width * 0.9f + shakeX, height * 0.85f + shakeY), Offset(width * 0.75f + shakeX, height * 0.72f + shakeY), 3f)

            // Drawing Golden/Charging Particle Nodes
            for (particle in particles) {
                if (particle.isCollected) continue
                val cx = particle.x + shakeX
                val cy = particle.y + shakeY
                
                val pBreathe = 1f + sin(System.currentTimeMillis() / 150.0).toFloat() * 0.15f
                val pScaleSize = particle.size * pBreathe

                // Glow ring
                drawCircle(
                    color = CyberGreen.copy(alpha = 0.2f),
                    radius = pScaleSize * 1.6f,
                    center = Offset(cx, cy)
                )
                // Solid core
                drawCircle(
                    color = CyberGreen,
                    radius = pScaleSize * 0.5f,
                    center = Offset(cx, cy)
                )
            }

            // Drawing Power-Up item boxes
            for (pUp in powerUps) {
                if (pUp.isCollected) continue
                val pX = pUp.x + shakeX
                val pY = pUp.y + shakeY
                val pSize = pUp.size

                val color = when (pUp.type) {
                    PowerUpType.FAST_CHARGING -> Color(0xFF7E57C2) // Violet Magnet
                    PowerUpType.BATTERY_SAVER -> Color(0xFFFFB74D) // Orange Saver
                    PowerUpType.EMP_BLAST -> Color(0xFF00E5FF)     // Cyan EMP
                    PowerUpType.RAM_CLEANER -> Color(0xFFD1C4E9)   // Silver Cleaner
                    PowerUpType.SHIELD -> Color(0xFF00FFFF)        // Teal Shield
                }

                // Rotating outline ring
                val rotAngle = (System.currentTimeMillis() / 4L) % 360f
                rotate(rotAngle, Offset(pX, pY)) {
                    drawRoundRect(
                        color = color.copy(alpha = 0.35f),
                        topLeft = Offset(pX - pSize / 2 - 6f, pY - pSize / 2 - 6f),
                        size = Size(pSize + 12f, pSize + 12f),
                        cornerRadius = CornerRadius(10f, 10f),
                        style = Stroke(width = 2f)
                    )
                }

                // Inner core box
                drawRoundRect(
                    color = color,
                    topLeft = Offset(pX - pSize / 2, pY - pSize / 2),
                    size = Size(pSize, pSize),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Letter indicator symbol inside
                val letterSymbol = when (pUp.type) {
                    PowerUpType.FAST_CHARGING -> "🧲"
                    PowerUpType.BATTERY_SAVER -> "🔋"
                    PowerUpType.EMP_BLAST -> "💥"
                    PowerUpType.RAM_CLEANER -> "🧹"
                    PowerUpType.SHIELD -> "🛡️"
                }

                drawText(
                    textMeasurer = textMeasurer,
                    text = letterSymbol,
                    topLeft = Offset(pX - 14f, pY - 18f),
                    style = TextStyle(fontSize = 11.sp)
                )
            }

            // Drawing Enemies (Cyber Threats)
            for (enemy in enemies) {
                if (enemy.isDestroyed) continue
                val eX = enemy.x + shakeX
                val eY = enemy.y + shakeY
                val eS = enemy.size

                val enemyColor = when (enemy.type) {
                    EnemyType.CHROME_TAB -> CyberPink
                    EnemyType.BACKGROUND_APP -> NeonRed
                    EnemyType.NOTIFICATION -> CyberYellow
                    EnemyType.SOFTWARE_UPDATE -> CyberOrange
                    EnemyType.RAM_MONSTER -> Color(0xFFBB86FC)
                }

                // Under glow shadow ring
                drawCircle(
                    color = enemyColor.copy(alpha = 0.15f),
                    radius = eS * 0.7f,
                    center = Offset(eX, eY)
                )

                // Geometry drawing based on threat type
                when (enemy.type) {
                    EnemyType.CHROME_TAB -> {
                        // Nested rings Chrome
                        drawCircle(color = enemyColor, radius = eS * 0.4f, center = Offset(eX, eY))
                        drawCircle(color = CyberDark, radius = eS * 0.2f, center = Offset(eX, eY))
                        drawCircle(color = CyberCyan, radius = eS * 0.08f, center = Offset(eX, eY))
                    }
                    EnemyType.BACKGROUND_APP -> {
                        // Angry triangular spikes
                        val triPath = Path().apply {
                            moveTo(eX, eY - eS * 0.4f)
                            lineTo(eX + eS * 0.4f, eY + eS * 0.3f)
                            lineTo(eX - eS * 0.4f, eY + eS * 0.3f)
                            close()
                        }
                        drawPath(path = triPath, color = enemyColor)
                    }
                    EnemyType.NOTIFICATION -> {
                        // Slim pop warning rectangle
                        drawRoundRect(
                            color = enemyColor,
                            topLeft = Offset(eX - eS * 0.7f, eY - eS * 0.25f),
                            size = Size(eS * 1.4f, eS * 0.5f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawCircle(color = Color.Red, radius = 5f, center = Offset(eX - eS * 0.4f, eY))
                    }
                    EnemyType.SOFTWARE_UPDATE -> {
                        // Double heavy loader arrows spinner
                        val rotAngle = (System.currentTimeMillis() / 6L) % 360f
                        rotate(rotAngle, Offset(eX, eY)) {
                            drawArc(
                                color = enemyColor,
                                startAngle = 0f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(eX - eS * 0.5f, eY - eS * 0.5f),
                                size = Size(eS, eS),
                                style = Stroke(width = 6f)
                            )
                        }
                    }
                    EnemyType.RAM_MONSTER -> {
                        // Breathing huge digital leaking blob
                        val blobBreathe = sin(System.currentTimeMillis() / 120.0).toFloat() * 8f
                        drawCircle(color = enemyColor, radius = (eS * 0.42f) + blobBreathe, center = Offset(eX, eY))
                        
                        // drawing small satellites circles surrounding crawls
                        repeat(4) { idx ->
                            val sAngle = (idx * 90f + System.currentTimeMillis() / 12f) % 360f
                            val sDist = (eS * 0.6f) + blobBreathe
                            val satX = eX + sin(Math.toRadians(sAngle.toDouble())).toFloat() * sDist
                            val satY = eY + Math.cos(Math.toRadians(sAngle.toDouble())).toFloat() * sDist
                            drawCircle(color = CyberCyan, radius = 5f, center = Offset(satX, satY))
                        }
                    }
                }

                // Humorous title headers
                if (eS > 80f) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = enemy.text,
                        topLeft = Offset(eX - 80f, eY - eS * 0.6f - 14f),
                        style = TextStyle(
                            color = enemyColor,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Draw glitched flickers inside intense shake moments
            if (glitchEffectRate > 0f) {
                drawRect(
                    color = CyberGreen.copy(alpha = glitchEffectRate * 0.12f),
                    topLeft = Offset(0f, Random().nextFloat() * height),
                    size = Size(width, 16f)
                )
                drawRect(
                    color = CyberPink.copy(alpha = glitchEffectRate * 0.18f),
                    topLeft = Offset(0f, Random().nextFloat() * height),
                    size = Size(width, 22f)
                )
            }

            // Drawing active Visual Fading Effects
            for (effect in visualEffects) {
                val durationRatio = effect.duration.toFloat() / effect.maxDuration.toFloat()
                val invRatio = 1.0f - durationRatio

                when (effect.type) {
                    "emp_ring" -> {
                        val maxR = width * 1.1f
                        drawCircle(
                            color = effect.color.copy(alpha = durationRatio * 0.5f),
                            radius = maxR * invRatio,
                            center = Offset(effect.x, effect.y),
                            style = Stroke(width = 5f * durationRatio)
                        )
                    }
                    "cleaner_sweep" -> {
                        val maxR = 350f * invRatio
                        drawCircle(
                            color = effect.color.copy(alpha = durationRatio * 0.5f),
                            radius = maxR,
                            center = Offset(effect.x, effect.y),
                            style = Stroke(width = 4f * durationRatio)
                        )
                    }
                    "shield_block" -> {
                        drawCircle(
                            color = effect.color.copy(alpha = durationRatio * 0.75f),
                            radius = 65f + 35f * invRatio,
                            center = Offset(effect.x, effect.y),
                            style = Stroke(width = 3f * durationRatio)
                        )
                    }
                    "explosion" -> {
                        repeat(5) { idx ->
                            val exAngle = idx * 72f
                            val explosionX = effect.x + sin(Math.toRadians(exAngle.toDouble())).toFloat() * 55f * invRatio
                            val explosionY = effect.y + Math.cos(Math.toRadians(exAngle.toDouble())).toFloat() * 55f * invRatio
                            drawCircle(
                                color = effect.color,
                                radius = 7f * durationRatio,
                                center = Offset(explosionX, explosionY)
                            )
                        }
                    }
                    "damage" -> {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = effect.text,
                            topLeft = Offset(effect.x - 70f, effect.y - 110f * invRatio),
                            style = TextStyle(
                                color = effect.color.copy(alpha = durationRatio),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                    "impact_particle" -> {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = effect.text,
                            topLeft = Offset(effect.x - 50f, effect.y - 90f * invRatio),
                            style = TextStyle(
                                color = effect.color.copy(alpha = durationRatio),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Draw Player Battery
            val plX = px + shakeX
            val plY = py + shakeY
            val cellW = 52f
            val cellH = 82f

            // Magnet Aura
            if (magnetActive) {
                val cycleRadius = 90f + sin(System.currentTimeMillis() / 100.0).toFloat() * 12f
                drawCircle(
                    color = Color(0xFF7E57C2).copy(alpha = 0.22f),
                    radius = cycleRadius,
                    center = Offset(plX, plY)
                )
                drawCircle(
                    color = Color(0xFF7E57C2).copy(alpha = 0.5f),
                    radius = cycleRadius,
                    center = Offset(plX, plY),
                    style = Stroke(width = 2f)
                )
            }

            // Orange Saver outline
            if (batterySaverActive) {
                drawCircle(
                    color = CyberOrange.copy(alpha = 0.22f),
                    radius = 80f,
                    center = Offset(plX, plY)
                )
            }

            // Cap
            val capLevelColor = if (batteryLevel <= 20f) NeonRed else CyberGreen
            drawRoundRect(
                color = capLevelColor,
                topLeft = Offset(plX - 12f, plY - cellH / 2 - 10f),
                size = Size(24f, 12f),
                cornerRadius = CornerRadius(3f, 3f)
            )

            // Heavy outline container - border-2 border-white/50
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(plX - cellW / 2, plY - cellH / 2),
                size = Size(cellW, cellH),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 4f)
            )

            // Fill battery fluid meter
            val fluidChargeHeight = (cellH - 12f) * (batteryLevel / 100f)
            val fillLoadColor = when {
                batterySaverActive -> CyberOrange
                batteryLevel <= 20f -> NeonRed
                batteryLevel <= 50f -> CyberYellow
                else -> CyberGreen
            }
            if (fluidChargeHeight > 0) {
                drawRoundRect(
                    color = fillLoadColor,
                    topLeft = Offset(plX - (cellW - 12f) / 2, plY + cellH / 2 - fluidChargeHeight - 6f),
                    size = Size(cellW - 12f, fluidChargeHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Character eyes and mouth face (from Vibrant Palette design)
            drawCircle(
                color = Color.Black,
                radius = 4f,
                center = Offset(plX - 8f, plY - 10f)
            )
            drawCircle(
                color = Color.Black,
                radius = 4f,
                center = Offset(plX + 8f, plY - 10f)
            )
            // Little cute smile
            val pathSmile = Path().apply {
                moveTo(plX - 6f, plY + 2f)
                quadraticTo(plX, plY + 7f, plX + 6f, plY + 2f)
            }
            drawPath(
                path = pathSmile,
                color = Color.Black.copy(alpha = 0.5f),
                style = Stroke(width = 2.5f)
            )

            // Light bolt icon
            val boltHov = sin(System.currentTimeMillis() / 200.0).toFloat() * 3f
            drawText(
                textMeasurer = textMeasurer,
                text = "⚡",
                topLeft = Offset(plX - 11f, plY - 32f + boltHov),
                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )

            // Shield Bubble
            if (shieldsActive > 0) {
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.16f),
                    radius = 65f,
                    center = Offset(plX, plY)
                )
                drawCircle(
                    color = CyberCyan,
                    radius = 65f + sin(System.currentTimeMillis() / 120.0).toFloat() * 3f,
                    center = Offset(plX, plY),
                    style = Stroke(width = 2.5f * shieldsActive)
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "x$shieldsActive",
                    topLeft = Offset(plX - 14f, plY - 70f),
                    style = TextStyle(color = CyberCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Overlay Panels Header Card (styled after the top HUD in the Vibrant Palette HTML)
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberGray),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left element: Dynamic Battery Outline + Text Label (HTML representation)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tiny inline battery drawing
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 22.dp)
                                .border(2.dp, CyberGreen, RoundedCornerShape(4.dp))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(batteryLevel / 100f)
                                    .background(CyberGreen)
                            )
                        }
                        Text(
                            text = "${batteryLevel.toInt()}%",
                            color = CyberGreen,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Right element: Survival duration (Uptime in HTML)
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "UPTIME SURVIVAL",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                        Text(
                            text = String.format("%02d:%02d", durationSec / 60, durationSec % 60),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Horizontal Load line representing CPU Load / Overload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
                    ) {
                        val progressFillColor = if (batteryLevel <= 20f) NeonRed else CyberGreen
                        // Draw progressive load line
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(batteryLevel / 100f)
                                .background(progressFillColor, RoundedCornerShape(3.dp))
                        )
                    }
                    Text(
                        text = "CPU LOAD",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Score Display Row from HTML
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TERMINAL THREAD SCORE",
                        color = Color.LightGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%06d PTS", score),
                        color = CyberYellow,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Floating Active Power-ups Bar & Navigation Bar (styled from "Vibrant Palette" HTML mockup)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Feature 1: Shield Power-Up display and count
                val shieldActiveNow = shieldsActive > 0
                val shieldBg = if (shieldActiveNow) CyberCyan.copy(alpha = 0.15f) else CyberGray
                val shieldBorder = if (shieldActiveNow) CyberCyan else Color.White.copy(alpha = 0.05f)
                val shieldTextCol = if (shieldActiveNow) CyberCyan else Color.LightGray.copy(alpha = 0.5f)
                Card(
                    colors = CardDefaults.cardColors(containerColor = shieldBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .border(1.dp, shieldBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x223B82F6), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SHIELD",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = shieldTextCol
                        )
                        Text(
                            text = if (shieldActiveNow) "x$shieldsActive" else "OFF",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = shieldTextCol
                        )
                    }
                }

                // Feature 2: Battery Saver state
                val saverBg = if (batterySaverActive) CyberOrange.copy(alpha = 0.15f) else CyberGray
                val saverBorder = if (batterySaverActive) CyberOrange else Color.White.copy(alpha = 0.05f)
                val saverTextCol = if (batterySaverActive) CyberOrange else Color.LightGray.copy(alpha = 0.5f)
                Card(
                    colors = CardDefaults.cardColors(containerColor = saverBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .border(1.dp, saverBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x22EAB308), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🔌", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SAVER",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = saverTextCol
                        )
                        Text(
                            text = if (batterySaverActive) "ACTIVE" else "OFF",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = saverTextCol
                        )
                    }
                }

                // Feature 3: Magnet / Fast Charging item state
                val magnetBg = if (magnetActive) Color(0xFF7E57C2).copy(alpha = 0.15f) else CyberGray
                val magnetBorder = if (magnetActive) Color(0xFF7E57C2) else Color.White.copy(alpha = 0.05f)
                val magnetTextCol = if (magnetActive) Color(0xFF7E57C2) else Color.LightGray.copy(alpha = 0.5f)
                Card(
                    colors = CardDefaults.cardColors(containerColor = magnetBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .border(1.dp, magnetBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x228B5CF6), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧲", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MAGNET",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = magnetTextCol
                        )
                        Text(
                            text = if (magnetActive) "ACTIVE" else "OFF",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = magnetTextCol
                        )
                    }
                }

                // Feature 4: Broom Active / Clean Indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clickable { /* Active cleaner indicator */ }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧹", fontSize = 17.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CLEAN",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Text(
                            text = "AUTO",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Bottom mimicked Android system navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Arrow
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                // Home Button
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                )
                // Recents
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
            }
        }

        // Low battery panic border flashing
        if (batteryLevel <= 20f) {
            val warnTransition = rememberInfiniteTransition(label = "Panic")
            val pulseAlpha by warnTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Pulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(8.dp, NeonRed.copy(alpha = pulseAlpha))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            viewModel.setPlayerPosition(change.position.x, change.position.y)
                        }
                    }
            ) {
                Text(
                    text = "⚠️ BATTERY CRITICAL! SURVIVAL THREAT HEIGHTENED! ⚠️",
                    color = NeonRed,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Floating Interactive Humorous popup Console dialogues
        popupData?.let { data ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberGray),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, CyberPink, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = data.title,
                            color = CyberPink,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = data.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        Button(
                            onClick = { viewModel.resolvePopup(leftClicked = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text(
                                text = data.btnLeft,
                                color = CyberDark,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.resolvePopup(leftClicked = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text(
                                text = data.btnRight,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(viewModel: GameViewModel) {
    val finalScore by viewModel.score.collectAsStateWithLifecycle()
    val secondsSurvived by viewModel.secondsSurvived.collectAsStateWithLifecycle()
    val particlesCollected by viewModel.particlesCollected.collectAsStateWithLifecycle()
    val highestScore by viewModel.highScore.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚡☠️",
                fontSize = 54.sp
            )

            Text(
                text = "SYSTEM CRASH!\nPOWER DRAINED",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                fontSize = 28.sp,
                color = NeonRed,
                lineHeight = 34.sp
            )

            Text(
                text = "Too many background processes overran resources. Your main threat loop has been suspended.",
                color = Color.LightGray.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CyberGray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResultRow(label = "FINAL SCORE", value = "$finalScore PTS", color = CyberYellow)
                    ResultRow(label = "TIME SURVIVED", value = String.format("%02d:%02d", secondsSurvived / 60, secondsSurvived % 60), color = CyberCyan)
                    ResultRow(label = "POWER COLLECTED", value = "$particlesCollected CORES", color = CyberGreen)
                    ResultRow(label = "HIGH SCORE RECORD", value = "$highestScore PTS", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.restartGame() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, CyberCyan, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "REBOOT SYSTEM (RETRY)",
                    color = CyberDark,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { viewModel.changeScreen(Screen.MENU) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "DISCONNECT (MAIN MENU)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun HighScoresScreen(viewModel: GameViewModel) {
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🏆 RECORD MEMORY LOGS",
                    color = CyberCyan,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Top historical survived threats written to offline SQLite store",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (topScores.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📭", fontSize = 40.sp)
                            Text(
                                text = "NO CONCURRENT LOGS YET",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(topScores) { index, record ->
                            val scoreDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                                .format(Date(record.timestamp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberGray),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (index == 0) CyberYellow.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                if (index == 0) CyberYellow else CyberCyan.copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = if (index == 0) CyberDark else CyberCyan,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${record.score} PTS",
                                            color = if (index == 0) CyberYellow else Color.White,
                                            fontSize = 16.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Black
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = String.format("Survived %02d:%02d", record.secondsSurvived / 60, record.secondsSurvived % 60),
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "🔋 x${record.particlesCollected}",
                                                color = CyberGreen,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Text(
                                        text = scoreDate,
                                        color = Color.Gray,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Highscores Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.clearAllHighScores() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "WIPE RECORD",
                        color = NeonRed,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewModel.changeScreen(Screen.MENU) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .border(1.dp, CyberCyan, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "RETURN TO SYSTEM",
                        color = CyberDark,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun HelpScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "📱 SYSTEM CORE MANUAL",
                        color = CyberPink,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Keep your battery cells running under extreme heavy usage!",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }

                item {
                    HelpSectionTitle("CONTROLS AND LOCOMOTION")
                    Text(
                        text = "Drag or slide your finger anywhere on the screen - the player (Battery Core cell) will immediately snap under your touch. Drag around precisely to steer away from cyber threads.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }

                item {
                    HelpSectionTitle("STAYING ENERGY CHARGED")
                    Text(
                        text = "Your battery life drains continuously over time as CPU consumes juice. Collect yellow/green charging particles floating down to boost your energy (+10% charge). Do not let it hit 0%!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }

                item {
                    HelpSectionTitle("CYBER THREAT HAZARDS")
                    HelpBullet(symbol = "🛑", text = "Chrome tabs: Fast rogue blocks that occasionally home-in on your terminal.")
                    HelpBullet(symbol = "🔺", text = "Background apps: Toxic miners bouncing erratically off boundaries.")
                    HelpBullet(symbol = "💬", text = "Notification spam: Fast-moving projectiles darting across the threads.")
                    HelpBullet(symbol = "🔄", text = "Software updates: Heavy rotating hazards that block dynamic spaces.")
                    HelpBullet(symbol = "👾", text = "RAM monster: Slow, massive background leaks vacuuming power options away.")
                }

                item {
                    HelpSectionTitle("SYSTEM ACCELERATORS (POWER-UPS)")
                    HelpBullet(symbol = "🧲 M", text = "Fast charging: Magnetizes all charging nodes directly to you.")
                    HelpBullet(symbol = "🔋 S", text = "Battery saver: Slows regular battery drain and slows down hazards.")
                    HelpBullet(symbol = "💥 E", text = "EMP blast: Detonates space, deleting all enemies for extra points.")
                    HelpBullet(symbol = "🧹 C", text = "RAM cleaner: Free up surrounding thread space immediately.")
                    HelpBullet(symbol = "🛡️ S", text = "Shield protection: Cyan aura ring blocks incoming hazard hits.")
                }

                item {
                    HelpSectionTitle("CHAOTIC SYSTEM POPUPS")
                    Text(
                        text = "Watch out for popup interruptions in intense moments! Press options wisely: your choice either frees RAM items or triggers major orbital storms!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Button(
                onClick = { viewModel.changeScreen(Screen.MENU) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, CyberCyan, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "ACKNOWLEDGE PARAMETERS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun HelpSectionTitle(text: String) {
    Text(
        text = text,
        color = CyberCyan,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun HelpBullet(symbol: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = symbol, fontSize = 14.sp)
        Text(
            text = text,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun ActiveBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ResultRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            color = color,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black
        )
    }
}

