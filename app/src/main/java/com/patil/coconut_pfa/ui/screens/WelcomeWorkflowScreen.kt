package com.patil.coconut_pfa.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.PlusJakartaSans
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ─── Design Tokens ───────────────────────────────────────────────────────────
private val Obsidian = Color(0xFF0C0D0E)
private val MintTeal = Color(0xFF7CE5C4)
private val Cream = Color(0xFFFCE2AC)
private val Coral = Color(0xFFFF9190)
private val CardSurface = Color(0xFF18181B)
private val SubtleGray = Color(0xFF8E929B)
private val White = Color(0xFFFFFFFF)

// ─── Root Composable ─────────────────────────────────────────────────────────

@Composable
fun WelcomeWorkflowScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            if (targetState > initialState) {
                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 4 }) togetherWith
                        (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -it / 4 })
            } else {
                (fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 4 }) togetherWith
                        (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { it / 4 })
            }
        },
        label = "WelcomeWorkflowStep",
        modifier = modifier.fillMaxSize()
    ) { step ->
        when (step) {
            0 -> Step1Welcome(onNext = { currentStep = 1 })
            1 -> Step2Pin(onBack = { currentStep = 0 }, onNext = { currentStep = 2 })
            2 -> Step3SalaryCycle(onBack = { currentStep = 1 }, onNext = { currentStep = 3 })
            3 -> Step4Accounts(onBack = { currentStep = 2 }, onNext = { currentStep = 4 })
            4 -> Step5Permissions(onBack = { currentStep = 3 }, onNext = { currentStep = 5 })
            5 -> Step6AllSet(onNext = { onNavigate(Screen.Home) })
        }
    }
}

// ─── Step Indicator ──────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(
    total: Int,
    current: Int,
    activeColor: Color = Cream,
    inactiveColor: Color = SubtleGray.copy(alpha = 0.4f)
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(if (index == current) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (index == current) activeColor else inactiveColor)
            )
        }
    }
}

// ─── Pill Button ─────────────────────────────────────────────────────────────

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = Cream,
    textColor: Color = Color(0xFF0E0E10),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ─── Hero Artwork ─────────────────────────────────────────────────────────────

@Composable
fun FortisHeroArtwork(modifier: Modifier = Modifier) {
    // Exact SVG paths and geometry from the Stitch project screen (320x340 viewBox)
    val wavyPath1 = remember { PathParser.createPathFromPathData("M -15,200 C 60,215 120,270 200,215 C 255,178 300,235 345,230") }
    val wavyPath2 = remember { PathParser.createPathFromPathData("M -20,220 C 65,240 115,292 195,235 C 250,195 305,255 350,250") }
    val wavyPath3 = remember { PathParser.createPathFromPathData("M -25,242 C 55,260 110,310 190,255 C 245,215 310,270 355,268") }

    val coralPath = remember { PathParser.createPathFromPathData("M 124,28 C 66,28 20,74 20,132 C 20,148 24,163 31,176 L 124,176 Z") }
    val whiteDonutPath = remember {
        PathParser.createPathFromPathData("M 230,46 C 267.55,46 298,76.45 298,114 C 298,151.55 267.55,182 230,182 C 192.45,182 162,151.55 162,114 C 162,76.45 192.45,46 230,46 Z M 230,79 C 249.33,79 265,94.67 265,114 C 265,133.33 249.33,149 230,149 C 210.67,149 195,133.33 195,114 C 195,94.67 210.67,79 230,79 Z").apply {
            fillType = android.graphics.Path.FillType.EVEN_ODD
        }
    }
    val mintDonutPath = remember {
        PathParser.createPathFromPathData("M 82,202 C 117.35,202 146,230.65 146,266 C 146,301.35 117.35,330 82,330 C 46.65,330 18,301.35 18,266 C 18,230.65 46.65,202 82,202 Z M 82,232 C 100.78,232 116,247.22 116,266 C 116,284.78 100.78,300 82,300 C 63.22,300 48,284.78 48,266 C 48,247.22 63.22,232 82,232 Z").apply {
            fillType = android.graphics.Path.FillType.EVEN_ODD
        }
    }
    val creamPillPath = remember { PathParser.createPathFromPathData("M 172,202 C 188,202 202,216 202,232 L 202,320 C 202,329 195,336 186,336 L 180,336 C 171,336 164,329 164,320 L 164,210 C 164,205.6 167.6,202 172,202 Z") }
    val mintColumnPath = remember { PathParser.createPathFromPathData("M 218,250 C 218,233.4 231.4,220 248,220 L 274,220 C 290.6,220 304,233.4 304,250 L 304,354 C 304,372 289.4,386 272,386 L 250,386 C 232.3,386 218,371.7 218,354 Z") }

    Canvas(modifier = modifier.aspectRatio(320f / 340f)) {
        val scale = size.width / 320f

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            nativeCanvas.save()
            nativeCanvas.scale(scale, scale)

            // 1. Drop shadow for shapes (stdDeviation=12, dx=0, dy=10, 45% black)
            val shadowPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.BLACK
                alpha = (0.45f * 255).toInt()
                maskFilter = android.graphics.BlurMaskFilter(12f, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            nativeCanvas.save()
            nativeCanvas.translate(0f, 10f)
            nativeCanvas.drawPath(coralPath, shadowPaint)
            nativeCanvas.drawPath(whiteDonutPath, shadowPaint)
            nativeCanvas.drawPath(mintDonutPath, shadowPaint)
            nativeCanvas.drawPath(creamPillPath, shadowPaint)
            nativeCanvas.drawPath(mintColumnPath, shadowPaint)
            nativeCanvas.restore()

            // 2. Wavy contour curves behind artwork
            val strokePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 1.2f
                color = android.graphics.Color.parseColor("#525660")
                alpha = (0.42f * 255).toInt()
            }
            nativeCanvas.drawPath(wavyPath1, strokePaint)
            nativeCanvas.drawPath(wavyPath2, strokePaint)
            nativeCanvas.drawPath(wavyPath3, strokePaint)

            // 3. Vector shapes
            val fillPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }

            // Shape 1: Coral Salmon Petal (#FF9190)
            fillPaint.color = android.graphics.Color.parseColor("#FF9190")
            nativeCanvas.drawPath(coralPath, fillPaint)

            // Shape 2: Bold White Donut (#FFFFFF)
            fillPaint.color = android.graphics.Color.WHITE
            nativeCanvas.drawPath(whiteDonutPath, fillPaint)

            // Shape 3: Mint Teal Donut (#88E5C9)
            fillPaint.color = android.graphics.Color.parseColor("#88E5C9")
            nativeCanvas.drawPath(mintDonutPath, fillPaint)

            // Shape 4: Warm Cream Pill (#FCE2AC)
            fillPaint.color = android.graphics.Color.parseColor("#FCE2AC")
            nativeCanvas.drawPath(creamPillPath, fillPaint)

            // Shape 5: Mint Teal Column (#7CE5C4)
            fillPaint.color = android.graphics.Color.parseColor("#7CE5C4")
            nativeCanvas.drawPath(mintColumnPath, fillPaint)

            nativeCanvas.restore()
        }
    }
}

// ─── Step 1: Welcome ─────────────────────────────────────────────────────────

@Composable
private fun Step1Welcome(
    onNext: () -> Unit,
    onRestoreBackup: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
            .drawBehind {
                // Ambient glows from Stitch design
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Coral.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(-size.width * 0.15f, -size.height * 0.05f),
                        radius = size.width * 0.85f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MintTeal.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 1.15f, size.height * 0.35f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Artwork container taking upper portion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                FortisHeroArtwork(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .widthIn(max = 310.dp)
                )
            }

            // Value proposition headline, body & CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Fortis.\nPrivate wealth,\nabsolute control.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1.19).sp,
                    color = White,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Encrypted offline personal finance and wealth tracking.",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = SubtleGray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(0.92f)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Primary Pill Button: Warm Cream Accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = Cream.copy(alpha = 0.22f),
                                topLeft = Offset(0f, 6.dp.toPx()),
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                            )
                        }
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Cream)
                        .clickable(onClick = onNext)
                        .padding(vertical = 17.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = (-0.2).sp,
                            color = Color(0xFF121316)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF121316),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Encrypted Restore Action
                val restoreText = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = SubtleGray,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    ) {
                        append("or ")
                    }
                    withStyle(
                        SpanStyle(
                            color = SubtleGray,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Restore from encrypted backup")
                    }
                }
                Text(
                    text = restoreText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRestoreBackup)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Step 2: PIN Setup ───────────────────────────────────────────────────────

private sealed interface PinKeypadItem {
    data class Digit(val number: Int, val letters: String) : PinKeypadItem
    data class Action(val label: String) : PinKeypadItem
    data object Backspace : PinKeypadItem
}

@Composable
private fun Step2Pin(onBack: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pinDigits = remember { mutableStateListOf<Int>() }
    var biometricEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row: Back button, Vault Setup badge, Lock icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE4E4E7), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF27272A),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0).copy(alpha = 0.6f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "VAULT SETUP",
                        fontFamily = PlusJakartaSans,
                        color = Color(0xFF065F46),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE4E4E7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator (showing step 2 of 6 in workflow)
            StepIndicator(
                total = 6,
                current = 1,
                activeColor = Color(0xFF18181B),
                inactiveColor = Color(0xFFE4E4E7)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title & Subtitle (Black & Light Grey text on White background)
            Text(
                text = "Create your Master PIN",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF18181B),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter a 4-digit master PIN for local biometric encryption.",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF71717A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val isFilled = index < pinDigits.size
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) Color(0xFF18181B) else Color.White)
                            .border(
                                width = if (isFilled) 0.dp else 2.dp,
                                color = if (isFilled) Color.Transparent else Color(0xFFD4D4D8),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Biometric Unlock Dark Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF18181B))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Biometric icon in rounded container
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF27272A))
                            .border(1.dp, Color(0xFF3F3F46).copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Biometrics",
                            tint = MintTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Enable Biometric Unlock",
                            fontFamily = PlusJakartaSans,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Fast access via Face ID / Fingerprint",
                            fontFamily = PlusJakartaSans,
                            color = Color(0xFFA1A1AA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Toggle switch (dark card toggle with mint track & dark thumb)
                val thumbOffset by animateDpAsState(
                    targetValue = if (biometricEnabled) 24.dp else 4.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "biometricThumb"
                )

                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(26.dp)
                        .clip(CircleShape)
                        .background(if (biometricEnabled) MintTeal else Color(0xFF27272A))
                        .border(
                            width = 1.dp,
                            color = if (biometricEnabled) Color.Transparent else Color(0xFF3F3F46),
                            shape = CircleShape
                        )
                        .clickable {
                            val newValue = !biometricEnabled
                            biometricEnabled = newValue
                            coroutineScope.launch {
                                try {
                                    UserSettingsDataStore(context).updateBiometricEnabled(newValue)
                                } catch (_: Exception) {}
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = thumbOffset)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (biometricEnabled) Color(0xFF18181B) else Color(0xFFA1A1AA))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Keypad Section on White Background
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val keyRows = listOf(
                listOf(
                    PinKeypadItem.Digit(1, ""),
                    PinKeypadItem.Digit(2, "ABC"),
                    PinKeypadItem.Digit(3, "DEF")
                ),
                listOf(
                    PinKeypadItem.Digit(4, "GHI"),
                    PinKeypadItem.Digit(5, "JKL"),
                    PinKeypadItem.Digit(6, "MNO")
                ),
                listOf(
                    PinKeypadItem.Digit(7, "PQRS"),
                    PinKeypadItem.Digit(8, "TUV"),
                    PinKeypadItem.Digit(9, "WXYZ")
                ),
                listOf(
                    PinKeypadItem.Action("Cancel"),
                    PinKeypadItem.Digit(0, ""),
                    PinKeypadItem.Backspace
                )
            )

            keyRows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    row.forEach { item ->
                        when (item) {
                            is PinKeypadItem.Digit -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE4E4E7), CircleShape)
                                        .clickable {
                                            if (pinDigits.size < 4) {
                                                pinDigits.add(item.number)
                                                if (pinDigits.size == 4) onNext()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = item.number.toString(),
                                            fontFamily = PlusJakartaSans,
                                            color = Color(0xFF18181B),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (item.letters.isNotEmpty()) {
                                            Text(
                                                text = item.letters,
                                                fontFamily = PlusJakartaSans,
                                                color = Color(0xFFA1A1AA),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Medium,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }
                            }
                            is PinKeypadItem.Action -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (pinDigits.isNotEmpty()) {
                                                pinDigits.clear()
                                            } else {
                                                onBack()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.label,
                                        fontFamily = PlusJakartaSans,
                                        color = Color(0xFF71717A),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            is PinKeypadItem.Backspace -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            if (pinDigits.isNotEmpty()) {
                                                pinDigits.removeAt(pinDigits.lastIndex)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                        contentDescription = "Backspace",
                                        tint = Color(0xFF52525B),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Skip for now",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF71717A),
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onNext() }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            )
        }
    }
}

// ─── Step 3: Salary Cycle ────────────────────────────────────────────────────

private enum class IncomeFrequency(val title: String) {
    MONTHLY("Monthly"),
    BI_WEEKLY("Bi-weekly"),
    FREELANCE("Freelance")
}

private fun getOrdinalSuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}

/**
 * Creative Horizontal Payday Wheel Slider:
 * - Big in the middle (~1.18x) and small at the edges (~0.74x).
 * - Tactile mechanical clock tick haptic whenever a date moves across the center box.
 * - Always keeps the middle date selected as the active payday.
 * - Central Target Frame ("Middle Box") with top and bottom reticle alignment notches.
 * - Precision calibrated tick marks moving in sync with the date reel.
 * - Snapping so days settle squarely inside the middle box.
 * - Tap any date to smoothly roll it directly into the center.
 */
@Composable
private fun PaydayWheelSlider(
    selectedDay: Int,
    onDayChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = remember { (1..31).toList() }
    val initialIndex = remember { (selectedDay - 1).coerceIn(0, days.size - 1) }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState)
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    val itemWidth = 58.dp
    val itemSpacing = 10.dp
    val density = LocalDensity.current
    val stepPx = with(density) { (itemWidth + itemSpacing).toPx() }

    // Continuous scroll offset in fractional item units
    val continuousPosition by remember {
        derivedStateOf {
            if (stepPx <= 0f) 0f
            else lazyListState.firstVisibleItemIndex + (lazyListState.firstVisibleItemScrollOffset.toFloat() / stepPx)
        }
    }

    // Centered index rounded to nearest day item
    val centerIndex by remember {
        derivedStateOf {
            continuousPosition.roundToInt().coerceIn(0, days.size - 1)
        }
    }

    // Trigger subtle tactile vibration whenever a new date crosses into the middle box
    var lastHapticIndex by remember { mutableIntStateOf(initialIndex) }
    LaunchedEffect(centerIndex) {
        if (centerIndex != lastHapticIndex) {
            lastHapticIndex = centerIndex
            try {
                view.performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Exception) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
            }
            onDayChange(days[centerIndex])
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val horizontalPadding = (containerWidth - itemWidth) / 2

        // 1. Center Target Frame ("Middle Box") - The target where the date is slotted
        Box(
            modifier = Modifier
                .width(itemWidth + 12.dp)
                .height(98.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(MintTeal.copy(alpha = 0.12f))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MintTeal.copy(alpha = 0.85f),
                            MintTeal.copy(alpha = 0.35f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
        )

        // Top reticle pointer (▼) pointing into the middle box
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
                .size(width = 12.dp, height = 6.dp)
        ) {
            val trianglePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(trianglePath, MintTeal)
        }

        // Bottom reticle pointer (▲)
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .size(width = 12.dp, height = 6.dp)
        ) {
            val trianglePath = Path().apply {
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width / 2f, 0f)
                close()
            }
            drawPath(trianglePath, MintTeal)
        }

        // 2. Horizontal Reel with Snapping & Dynamic Scaling
        LazyRow(
            state = lazyListState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            items(days.size) { index ->
                val day = days[index]
                val distance = abs(index - continuousPosition)

                // Scale: Big in the middle (1.18f), dropping to 0.74f at edges
                val scale = (1.18f - (distance * 0.16f)).coerceIn(0.74f, 1.18f)
                // Opacity: 1f in the middle, drops down to 0.30f at the outer boundaries
                val alpha = (1.0f - (distance * 0.22f)).coerceIn(0.30f, 1.0f)
                // Highlight factor: 1f when centered (distance <= 0.65f), interpolating to 0f
                val highlight = (1.0f - (distance / 0.65f)).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .width(itemWidth)
                        .height(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (highlight > 0.5f) MintTeal else Color.White)
                        .border(
                            width = if (highlight > 0.5f) 2.dp else 1.dp,
                            color = if (highlight > 0.5f) Color.White else Color(0xFFE8EAED),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (highlight > 0.5f) "PAYDAY" else "DAY",
                            fontFamily = PlusJakartaSans,
                            fontSize = 9.sp,
                            fontWeight = if (highlight > 0.5f) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (highlight > 0.5f) Color(0xFF00382B) else Color(0xFF8E95A0),
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = day.toString(),
                            fontFamily = PlusJakartaSans,
                            fontSize = if (highlight > 0.5f) 24.sp else 18.sp,
                            fontWeight = if (highlight > 0.5f) FontWeight.Black else FontWeight.Bold,
                            color = if (highlight > 0.5f) Color(0xFF00382B) else Color(0xFF131315),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }

    // 3. Precision Dial Ruler Track (synchronized with scroll reel)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .padding(top = 2.dp)
    ) {
        val centerX = size.width / 2f
        // Center anchor tick
        drawLine(
            color = MintTeal,
            start = Offset(centerX, 0f),
            end = Offset(centerX, size.height),
            strokeWidth = 2.5.dp.toPx()
        )
        // Dynamic tick marks moving with continuous scroll position
        val offsetFromNearest = (continuousPosition - continuousPosition.roundToInt()) * stepPx
        for (i in -4..4) {
            if (i == 0) continue
            val tickX = centerX + (i * stepPx) - offsetFromNearest
            if (tickX in 0f..size.width) {
                val tickHeight = if (i % 2 == 0) size.height * 0.7f else size.height * 0.45f
                drawLine(
                    color = Color(0xFFD4D4D8),
                    start = Offset(tickX, (size.height - tickHeight) / 2f),
                    end = Offset(tickX, (size.height + tickHeight) / 2f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun Step3SalaryCycle(onBack: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedDay by remember { mutableIntStateOf(28) }
    var selectedFrequency by remember { mutableStateOf(IncomeFrequency.MONTHLY) }
    var expectedIncome by remember { mutableStateOf("4,500") }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }

    // Educational Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Why Salary Cycle?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131315)
                )
            },
            text = {
                Text(
                    text = "Fortis organizes your spending cycle, category budgets, and cash flow forecasts around your actual payday rather than arbitrary calendar months.\n\nThis keeps your Safe-to-Spend numbers accurate and prevents false overspend warnings right before payday.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF666B72)
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        text = "Got it",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131315)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Expected Income Edit Dialog
    if (showIncomeDialog) {
        var tempIncome by remember { mutableStateOf(expectedIncome) }
        AlertDialog(
            onDismissRequest = { showIncomeDialog = false },
            title = {
                Text(
                    text = "Expected Monthly Income",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131315)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your estimated monthly net take-home salary:",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = Color(0xFF666B72)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempIncome,
                        onValueChange = { input ->
                            tempIncome = input.filter { it.isDigit() || it == ',' }
                        },
                        label = { Text("Amount ($ / ₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expectedIncome = tempIncome.ifEmpty { "4,500" }
                    showIncomeDialog = false
                }) {
                    Text(
                        text = "Save",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131315)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncomeDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSans,
                        color = Color(0xFF71717A)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scrollable Screen Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Round Back Button, Step 3 Badge, Round Info Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE4E4E7), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF27272A),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE8EAED), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF131315))
                    )
                    Text(
                        text = "STEP 3 OF 6",
                        fontFamily = PlusJakartaSans,
                        color = Color(0xFF131315).copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.6.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE4E4E7), CircleShape)
                        .clickable { showInfoDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Information",
                        tint = Color(0xFF71717A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Step Indicator Dots
            StepIndicator(
                total = 6,
                current = 2,
                activeColor = Color(0xFF18181B),
                inactiveColor = Color(0xFFE4E4E7)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Headline & Explainer Text
            Text(
                text = "Salary Cycle",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF131315),
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Calibrate your monthly budget & cash flow.",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF71717A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Frequency Selector: Sleek Pill Switch
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "INCOME FREQUENCY",
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7C828A),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFECEEF2))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IncomeFrequency.entries.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF131315) else Color.Transparent)
                                .clickable { selectedFrequency = freq }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq.title,
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF666B72)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Payday Selector: Horizontal Slider with Center Middle Box & Dynamic Haptics
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECURRING PAYDAY",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7C828A),
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MintTeal.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF006651))
                        )
                        Text(
                            text = "${selectedDay}${getOrdinalSuffix(selectedDay)} of every month",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF006651)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The Horizontal Wheel Slider (edge-to-edge for smooth carousel sweep)
                PaydayWheelSlider(
                    selectedDay = selectedDay,
                    onDayChange = { selectedDay = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Cycle Summary Card
                val cycleEndDay = if (selectedDay == 1) 31 else selectedDay - 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF4F5F7))
                        .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MintTeal.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF006651),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cycle runs from day $selectedDay to day $cycleEndDay",
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF131315)
                        )
                        Text(
                            text = "Budget limits reset every ${selectedDay}${getOrdinalSuffix(selectedDay)} of the month",
                            fontFamily = PlusJakartaSans,
                            fontSize = 11.sp,
                            color = Color(0xFF71717A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Expected Net Income: Prominent Light Input Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(22.dp))
                    .clickable { showIncomeDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "EXPECTED NET INCOME",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7C828A),
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF006651))
                        )
                        Text(
                            text = "Monthly take-home",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF006651)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF4F5F7))
                        .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$$expectedIncome",
                        fontFamily = PlusJakartaSans,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF131315)
                    )
                    Text(
                        text = ".00",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E95A0)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit income",
                        tint = Color(0xFF8E95A0),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom CTA Footer with Black Button (matching 1st page dimensions, typography & vertical line)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.95f),
                            Color.White
                        )
                    )
                )
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Primary Black Pill Button: Same style, dimensions, typography, and vertical line (32.dp) as 1st page
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = Color(0xFF131315).copy(alpha = 0.20f),
                            topLeft = Offset(0f, 6.dp.toPx()),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                        )
                    }
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF131315))
                    .clickable {
                        coroutineScope.launch {
                            try {
                                UserSettingsDataStore(context).updateSalaryCycleStartDay(selectedDay)
                            } catch (_: Exception) {}
                        }
                        onNext()
                    }
                    .padding(vertical = 17.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue",
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Skip for now",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF71717A),
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onNext() }
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Home Indicator Bar
            Box(
                modifier = Modifier
                    .width(134.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131315).copy(alpha = 0.18f))
            )
        }
    }
}

// ─── Step 4: Accounts ────────────────────────────────────────────────────────

private data class AccountOption(
    val emoji: String,
    val label: String,
    val defaultChecked: Boolean
)

@Composable
private fun Step4Accounts(onBack: () -> Unit, onNext: () -> Unit) {
    val accountOptions = remember {
        listOf(
            AccountOption("🏦", "Savings / Bank Account", true),
            AccountOption("💳", "Credit Card", true),
            AccountOption("💵", "Cash / Wallet", true),
            AccountOption("🏠", "Loan", false),
            AccountOption("💰", "Fixed Deposit", false),
        )
    }
    val checked = remember {
        mutableStateMapOf(*accountOptions.map { it.label to it.defaultChecked }.toTypedArray())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
            StepIndicator(total = 6, current = 3)
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Set up your accounts",
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Which account types would you like to start with?",
            color = SubtleGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        accountOptions.forEach { option ->
            val isChecked = checked[option.label] == true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Obsidian)
                    .clickable { checked[option.label] = !isChecked }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = option.emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = option.label,
                    color = White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = { checked[option.label] = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Obsidian,
                        checkedTrackColor = MintTeal,
                        uncheckedThumbColor = SubtleGray,
                        uncheckedTrackColor = Obsidian
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = "Create Accounts",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── Step 5: Permissions ─────────────────────────────────────────────────────

private data class PermissionOption(
    val emoji: String,
    val title: String,
    val description: String
)

@Composable
private fun Step5Permissions(onBack: () -> Unit, onNext: () -> Unit) {
    val permissions = remember {
        listOf(
            PermissionOption("📱", "SMS Access", "Auto-detect transactions from bank SMS"),
            PermissionOption("🔔", "Notifications", "Get bill reminders and due date alerts"),
            PermissionOption("📷", "Camera", "Scan receipts and statements"),
        )
    }
    val granted = remember {
        mutableStateMapOf(*permissions.map { it.title to false }.toTypedArray())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardSurface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = White
                )
            }
            StepIndicator(total = 6, current = 4)
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "A few permissions to unlock the full experience",
            color = White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "All data stays on your device. We never access your messages.",
            color = SubtleGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        permissions.forEach { perm ->
            val isGranted = granted[perm.title] == true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Obsidian)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = perm.emoji, fontSize = 26.sp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = perm.title,
                        color = White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = perm.description,
                        color = SubtleGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isGranted,
                    onCheckedChange = { granted[perm.title] = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Obsidian,
                        checkedTrackColor = MintTeal,
                        uncheckedThumbColor = SubtleGray,
                        uncheckedTrackColor = Obsidian
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        PillButton(
            text = "Grant Permissions",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── Step 6: All Set ─────────────────────────────────────────────────────────

@Composable
private fun Step6AllSet(onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MintTeal.copy(alpha = 0.18f), Obsidian),
                    radius = 700f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Checkmark circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MintTeal.copy(alpha = 0.18f))
                    .border(width = 3.dp, color = MintTeal, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Done",
                    tint = MintTeal,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "You're all set!",
                color = White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Your financial cockpit is ready.\nStart tracking your wealth.",
                color = SubtleGray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            PillButton(
                text = "Open Fortis",
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(0.75f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "This was just a preview of the Welcome Workflow",
                color = SubtleGray.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0C0D0E, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step1WelcomePreview() {
    Step1Welcome(onNext = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step2PinPreview() {
    Step2Pin(onBack = {}, onNext = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step3SalaryCyclePreview() {
    Step3SalaryCycle(onBack = {}, onNext = {})
}
