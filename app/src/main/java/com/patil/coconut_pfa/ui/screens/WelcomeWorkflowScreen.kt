package com.patil.coconut_pfa.ui.screens

import android.Manifest
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
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
import androidx.compose.ui.graphics.PathEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import com.patil.coconut_pfa.data.local.datastore.UserSettingsDataStore
import com.patil.coconut_pfa.data.local.db.CoconutDatabase
import com.patil.coconut_pfa.data.local.entity.AccountEntity
import com.patil.coconut_pfa.data.local.entity.CardDetailsEntity
import com.patil.coconut_pfa.data.local.entity.DbAccountInstitution
import com.patil.coconut_pfa.data.local.entity.DbAccountType
import com.patil.coconut_pfa.data.local.security.PinHasher
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.PlusJakartaSans
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.delay
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
    onCompleteWelcomeWorkflow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    var currentStep by remember { mutableIntStateOf(0) }

    // Restore Dialog States
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var restorePasswordInput by remember { mutableStateOf("") }
    var restoreSummary by remember { mutableStateOf<com.patil.coconut_pfa.data.repository.BackupSummary?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showNewPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            restorePasswordInput = ""
            showRestorePasswordDialog = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> Step1Welcome(
                    onNext = { currentStep = 1 },
                    onRestoreBackup = { restoreLauncher.launch(arrayOf("*/*")) }
                )
                1 -> Step2Pin(onBack = { currentStep = 0 }, onNext = { currentStep = 2 })
                2 -> Step3SalaryCycle(onBack = { currentStep = 1 }, onNext = { currentStep = 3 })
                3 -> Step4Accounts(onBack = { currentStep = 2 }, onNext = { currentStep = 4 })
                4 -> Step5Permissions(onBack = { currentStep = 3 }, onNext = { currentStep = 5 })
                5 -> Step6AllSet(
                    onBack = { currentStep = 4 },
                    onNext = {
                        onCompleteWelcomeWorkflow()
                        onNavigate(Screen.Home)
                    }
                )
            }
        }

        // Restore Password Dialog
        if (showRestorePasswordDialog && selectedRestoreUri != null) {
            AlertDialog(
                onDismissRequest = { showRestorePasswordDialog = false },
                title = { Text("Enter Backup Password") },
                text = {
                    Column {
                        Text("Enter the password used when creating this backup file.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = restorePasswordInput,
                            onValueChange = { restorePasswordInput = it },
                            label = { Text("Backup Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val summary = com.patil.coconut_pfa.data.repository.BackupManager.inspectBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                    restoreSummary = summary
                                    showRestorePasswordDialog = false
                                    showRestoreConfirmDialog = true
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Incorrect password or invalid backup: ${e.localizedMessage}")
                                }
                            }
                        }
                    ) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestorePasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Restore Confirmation Dialog
        if (showRestoreConfirmDialog && restoreSummary != null) {
            val s = restoreSummary!!
            val formattedDate = remember(s.createdAt) {
                java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(s.createdAt))
            }
            AlertDialog(
                onDismissRequest = { showRestoreConfirmDialog = false },
                title = { Text("Confirm Data Restore") },
                text = {
                    Column {
                        Text("Backup created on: $formattedDate", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Accounts: ${s.accountCount}")
                        Text("• Transactions: ${s.transactionCount}")
                        Text("• Categories: ${s.categoryCount}")
                        Text("• Bills: ${s.billCount}")
                        Text("• Goals: ${s.goalCount}")
                        Text("• Persons/IOUs: ${s.personCount}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("WARNING: Restoring will replace all current data in your app.", color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            scope.launch {
                                try {
                                    com.patil.coconut_pfa.data.repository.BackupManager.restoreBackup(context, selectedRestoreUri!!, restorePasswordInput)
                                    showRestoreConfirmDialog = false
                                    snackbarHostState.showSnackbar("Data restored successfully!")
                                    showNewPinDialog = true
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Restore failed: ${e.localizedMessage}")
                                }
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    ) {
                        Text("Replace All Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Set New PIN Dialog after restore
        if (showNewPinDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Set New Security PIN") },
                text = {
                    Column {
                        Text("PINs are not included in backups for security. Please set a new 4-digit PIN.", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                if (filtered.length <= 4) newPinInput = filtered
                            },
                            label = { Text("4-Digit PIN") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            if (newPinInput.length == 4) {
                                scope.launch {
                                    val salt = PinHasher.generateSalt()
                                    val hash = PinHasher.hashPin(newPinInput, salt)
                                    val saltB64 = PinHasher.saltToBase64(salt)
                                    UserSettingsDataStore(context).updatePin(hash, saltB64)
                                    showNewPinDialog = false
                                    onCompleteWelcomeWorkflow()
                                    onNavigate(Screen.Home)
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a valid 4-digit PIN.") }
                            }
                        },
                        enabled = newPinInput.length == 4
                    ) {
                        Text("Save PIN & Open")
                    }
                }
            )
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
    var showInfoDialog by remember { mutableStateOf(false) }

    // Educational Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Master PIN & Vault",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131315)
                )
            },
            text = {
                Text(
                    text = "Your Master PIN encrypts your financial data locally on this device using Android's hardware Secure Element.\n\nIt ensures offline privacy and enables seamless biometric access.",
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
            // Header Row: Round Back Button, Step 1 Badge, Round Info Button
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
                        text = "STEP 2 OF 6",
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

            Spacer(modifier = Modifier.height(42.dp))

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

            Spacer(modifier = Modifier.height(40.dp))

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
                                                if (pinDigits.size == 4) {
                                                    val pinStr = pinDigits.joinToString("")
                                                    val salt = PinHasher.generateSalt()
                                                    val hashB64 = PinHasher.hashPin(pinStr, salt)
                                                    val saltB64 = PinHasher.saltToBase64(salt)
                                                    coroutineScope.launch {
                                                        try {
                                                            UserSettingsDataStore(context).updatePin(hashB64, saltB64)
                                                            UserSettingsDataStore(context).updateBiometricEnabled(biometricEnabled)
                                                        } catch (_: Exception) {}
                                                    }
                                                    onNext()
                                                }
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
        var tempIncome by remember { mutableStateOf(expectedIncome.replace(",", "")) }
        val presets = listOf("2500", "4500", "6500", "8500", "10000")

        val formatIncomeVal: (Int) -> String = { valInt ->
            val formatter = java.text.DecimalFormat("#,##0")
            formatter.format(valInt)
        }

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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set your estimated monthly net take-home salary",
                        fontFamily = PlusJakartaSans,
                        fontSize = 13.sp,
                        color = Color(0xFF666B72),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Income Value Display
                    val currentValInt = tempIncome.toIntOrNull() ?: 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF4F5F7))
                            .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(20.dp))
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$",
                                fontFamily = PlusJakartaSans,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006651)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatIncomeVal(currentValInt),
                                fontFamily = PlusJakartaSans,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF131315)
                            )
                            Text(
                                text = ".00",
                                fontFamily = PlusJakartaSans,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8E95A0)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Salary Chips Row
                    Text(
                        text = "QUICK PRESETS",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E95A0),
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            val presetInt = preset.toInt()
                            val isSelected = currentValInt == presetInt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF131315) else Color(0xFFF4F5F7))
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF131315) else Color(0xFFE8EAED),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { tempIncome = preset }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$${formatIncomeVal(presetInt)}",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF666B72)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stepper Adjustment Row (-500 / +500)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF4F5F7))
                                .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(14.dp))
                                .clickable {
                                    val newVal = (currentValInt - 500).coerceAtLeast(0)
                                    tempIncome = newVal.toString()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Decrease",
                                    tint = Color(0xFF131315),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$500",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF131315)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF4F5F7))
                                .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(14.dp))
                                .clickable {
                                    val newVal = currentValInt + 500
                                    tempIncome = newVal.toString()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Increase",
                                    tint = Color(0xFF131315),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$500",
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF131315)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Precision Direct Text Field Entry
                    OutlinedTextField(
                        value = tempIncome,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            tempIncome = if (clean.length <= 7) clean else tempIncome
                        },
                        label = { Text("Exact Amount ($)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanInt = tempIncome.toIntOrNull() ?: 4500
                        expectedIncome = formatIncomeVal(cleanInt)
                        showIncomeDialog = false
                    }
                ) {
                    Text(
                        text = "Save Income",
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
            shape = RoundedCornerShape(24.dp)
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

            Spacer(modifier = Modifier.height(30.dp))

            // Headline & Explainer Text
            Text(
                text = "Salary Cycle",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF131315),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
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

            Spacer(modifier = Modifier.height(50.dp))

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

            Spacer(modifier = Modifier.height(40.dp))

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
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Value Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF4F5F7))
                            .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(16.dp))
                            .clickable { showIncomeDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$$expectedIncome",
                            fontFamily = PlusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF131315)
                        )
                        Text(
                            text = ".00",
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp,
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

@Composable
private fun Step4Accounts(onBack: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var bankSelected by remember { mutableStateOf(true) }
    var cardSelected by remember { mutableStateOf(true) }
    var cashSelected by remember { mutableStateOf(false) }
    var investmentsSelected by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }

    // Educational Info Dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Why Initial Accounts?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131315)
                )
            },
            text = {
                Text(
                    text = "Setting up your initial accounts gives Fortis the foundation to calculate your real-time net worth, track spending across categories, and reconcile transactions automatically.\n\nYou can select the accounts you use most, customize starting balances, or edit and add more accounts anytime in the Accounts tab.",
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
            // Header Row: Round Back Button, Step 4 Badge, Round Info Button
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
                        text = "STEP 4 OF 6",
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
                current = 3,
                activeColor = Color(0xFF18181B),
                inactiveColor = Color(0xFFE4E4E7)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Headline & Explainer Text
            Text(
                text = "Initial Accounts",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF131315),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Select a few accounts to kickstart your tracking.",
                fontFamily = PlusJakartaSans,
                color = Color(0xFF71717A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 15.dp)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // 4 Account Cards List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Bank Account
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(
                            width = if (bankSelected) 2.dp else 1.dp,
                            color = if (bankSelected) Color(0xFF10B981).copy(alpha = 0.85f) else Color(0xFFE8EAED),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { bankSelected = !bankSelected }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFECFDF5))
                                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Bank Account",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF131315)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0xFFF4F5F7))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "PRIMARY",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 0.5.sp,
                                                color = Color(0xFF52525B)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Checking & Savings",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E95A0)
                                    )
                                }
                            }

                            // Selection Checkmark
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (bankSelected) {
                                            Modifier.background(Color(0xFF10B981))
                                        } else {
                                            Modifier.border(2.dp, Color(0xFFD4D4D8), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (bankSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }


                    }
                }

                // Card 2: Credit Card (Hero Dark Obsidian Card Treatment from Stitch)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF141416))
                        .drawBehind {
                            // Soft ambient glows matching Stitch project
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 1.05f, size.height * 1.05f),
                                    radius = size.width * 0.55f
                                )
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF9692).copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.05f, size.height * 0.05f),
                                    radius = size.width * 0.50f
                                )
                            )
                        }
                        .border(
                            width = if (cardSelected) 1.5.dp else 1.dp,
                            color = if (cardSelected) Color(0xFFFF9692).copy(alpha = 0.70f) else Color(0xFF27272A),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { cardSelected = !cardSelected }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.10f))
                                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CreditCard,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9692),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Credit Card",
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "VISA",
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 0.6.sp,
                                                color = Color(0xFFD4D4D8)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Offline limit & dues",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                            }

                            // Selection Checkmark
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (cardSelected) {
                                            Modifier.background(Color(0xFFFF9692))
                                        } else {
                                            Modifier.border(2.dp, Color(0xFF52525B), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cardSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF141416),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }


                    }
                }

                // Card 3: Cash & Wallets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(
                            width = if (cashSelected) 2.dp else 1.dp,
                            color = if (cashSelected) Color(0xFFF59E0B).copy(alpha = 0.85f) else Color(0xFFE8EAED),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { cashSelected = !cashSelected }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFFFFBEB))
                                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Payments,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Cash & Wallets",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131315)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Petty cash, emergency bills",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E95A0)
                                    )
                                }
                            }

                            // Selection Checkmark
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (cashSelected) {
                                            Modifier.background(Color(0xFF10B981))
                                        } else {
                                            Modifier.border(2.dp, Color(0xFFD4D4D8), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cashSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }


                    }
                }

                // Card 4: Investments & Savings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(
                            width = if (investmentsSelected) 2.dp else 1.dp,
                            color = if (investmentsSelected) Color(0xFF3B82F6).copy(alpha = 0.85f) else Color(0xFFE8EAED),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { investmentsSelected = !investmentsSelected }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Investments & Savings",
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF131315)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Stocks, deposits & crypto",
                                        fontFamily = PlusJakartaSans,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8E95A0)
                                    )
                                }
                            }

                            // Selection Checkmark
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (investmentsSelected) {
                                            Modifier.background(Color(0xFF10B981))
                                        } else {
                                            Modifier.border(2.dp, Color(0xFFD4D4D8), CircleShape)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (investmentsSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }


                    }
                }
            }
        }

        // Bottom CTA Footer with Black Button (matching 1st & 3rd page dimensions, typography & vertical line)
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
            // Primary Black Pill Button: Same style, dimensions, typography, and vertical line (32.dp) as 1st & 3rd page
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
                                val db = CoconutDatabase.getInstance(context)
                                val currentDate = KolkataDateTimeUtils.currentDateString()
                                if (bankSelected) {
                                    val amountPaise = 0L
                                    db.accountDao().insert(
                                        AccountEntity(
                                            id = "acc_1",
                                            name = "Bank Account",
                                            type = DbAccountType.BANK,
                                            institution = DbAccountInstitution.HDFC,
                                            last4 = "4321",
                                            openingBalancePaise = amountPaise,
                                            openingDate = currentDate,
                                            icon = "account_balance",
                                            colour = 0xFF10B981L
                                        )
                                    )
                                }
                                if (cardSelected) {
                                    val limitPaise = 0L
                                    db.accountDao().insert(
                                        AccountEntity(
                                            id = "acc_3",
                                            name = "Credit Card",
                                            type = DbAccountType.CREDIT_CARD,
                                            institution = DbAccountInstitution.HDFC,
                                            last4 = "1122",
                                            openingBalancePaise = 0L,
                                            openingDate = currentDate,
                                            icon = "credit_card",
                                            colour = 0xFFFF9692L
                                        )
                                    )
                                    db.cardDetailsDao().insert(
                                        CardDetailsEntity(
                                            accountId = "acc_3",
                                            creditLimitPaise = limitPaise,
                                            statementDay = 15,
                                            dueDay = 5
                                        )
                                    )
                                }
                                if (cashSelected) {
                                    val amountPaise = 0L
                                    db.accountDao().insert(
                                        AccountEntity(
                                            id = "acc_5",
                                            name = "Cash & Wallets",
                                            type = DbAccountType.CASH,
                                            institution = DbAccountInstitution.OTHER,
                                            openingBalancePaise = amountPaise,
                                            openingDate = currentDate,
                                            icon = "payments",
                                            colour = 0xFFF59E0BL
                                        )
                                    )
                                }
                                if (investmentsSelected) {
                                    val amountPaise = 0L
                                    db.accountDao().insert(
                                        AccountEntity(
                                            id = "acc_inv_1",
                                            name = "Investments & Savings",
                                            type = DbAccountType.INVESTMENT,
                                            institution = DbAccountInstitution.OTHER,
                                            openingBalancePaise = amountPaise,
                                            openingDate = currentDate,
                                            icon = "show_chart",
                                            colour = 0xFF3B82F6L
                                        )
                                    )
                                }
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

// ─── Step 5: Permissions ─────────────────────────────────────────────────────

@Composable
private fun SmsPermissionItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PermissionRow(
        icon = Icons.Rounded.Sms,
        iconColor = Color(0xFF059669),
        iconBgColor = Color(0xFFECFDF5),
        iconBorderColor = Color(0xFFA7F3D0),
        accentColor = Color(0xFF10B981),
        title = "SMS Sync / Detection",
        isEnabled = isEnabled,
        onToggle = onToggle
    )
}

@Composable
private fun BiometricPermissionItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PermissionRow(
        icon = Icons.Rounded.Fingerprint,
        iconColor = Color(0xFFD97706),
        iconBgColor = Color(0xFFFFFBEB),
        iconBorderColor = Color(0xFFFDE68A),
        accentColor = Color(0xFFF59E0B),
        title = "Biometrics",
        isEnabled = isEnabled,
        onToggle = onToggle
    )
}

@Composable
private fun NotificationPermissionItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PermissionRow(
        icon = Icons.Rounded.Notifications,
        iconColor = Color(0xFF2563EB),
        iconBgColor = Color(0xFFEFF6FF),
        iconBorderColor = Color(0xFFBFDBFE),
        accentColor = Color(0xFF3B82F6),
        title = "Local Notifications",
        isEnabled = isEnabled,
        onToggle = onToggle
    )
}

@Composable
private fun CameraPermissionItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PermissionRow(
        icon = Icons.Rounded.CameraAlt,
        iconColor = Color(0xFF7C3AED),
        iconBgColor = Color(0xFFF5F3FF),
        iconBorderColor = Color(0xFFDDD6FE),
        accentColor = Color(0xFF8B5CF6),
        title = "Receipt Scanner",
        isEnabled = isEnabled,
        showDivider = false,
        onToggle = onToggle
    )
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    iconBorderColor: Color,
    accentColor: Color,
    title: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!isEnabled) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor)
                        .border(1.dp, iconBorderColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = title,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF131315),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE4E4E7),
                    uncheckedBorderColor = Color.Transparent,
                    checkedBorderColor = Color.Transparent
                )
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF4F5F7))
            )
        }
    }
}

@Composable
private fun Step5Permissions(onBack: () -> Unit, onNext: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showInfoDialog by remember { mutableStateOf(false) }

    var smsEnabled by remember { mutableStateOf(true) }
    var biometricEnabled by remember { mutableStateOf(true) }
    var notifEnabled by remember { mutableStateOf(true) }
    var cameraEnabled by remember { mutableStateOf(true) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.all { it }
        smsEnabled = granted
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notifEnabled = isGranted
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraEnabled = isGranted
    }

    // Educational Info Dialog (Private by Design)
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Why Permissions?",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF131315)
                )
            },
            text = {
                Text(
                    text = "Fortis is built with a zero-knowledge, local-only architecture:\n\n" +
                            "• SMS Sync: Parses transaction alerts locally to auto-log your debits and credits. OTPs and personal chats are strictly filtered out.\n\n" +
                            "• Biometric Vault: Keeps your balances and credentials locked using Android's hardware Secure Element.\n\n" +
                            "• Local Notifications: Delivers timely reminders for bill payment due dates and salary cycles without sending data to any external server.\n\n" +
                            "• Receipt Scanner: Uses on-device ML Kit OCR to extract transaction amounts directly from camera scans.\n\n" +
                            "No remote database. No advertising. 100% private.",
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    color = Color(0xFF52525B),
                    lineHeight = 18.sp
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
            // Header Row: Round Back Button, Step 5 Badge, Round Info Button
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
                        text = "STEP 5 OF 6",
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
                current = 4,
                activeColor = Color(0xFF18181B),
                inactiveColor = Color(0xFFE4E4E7)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Box 1: Headline & Explainer Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(vertical = 22.dp, horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Permissions Overview",
                        fontFamily = PlusJakartaSans,
                        color = Color(0xFF131315),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Grant essential permissions for private tracking.",
                        fontFamily = PlusJakartaSans,
                        color = Color(0xFF71717A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(39.dp))

            // Box 2: 4 Permission Items List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8EAED), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SmsPermissionItem(
                        isEnabled = smsEnabled,
                        onToggle = { enabled ->
                            smsEnabled = enabled
                            if (enabled) {
                                try {
                                    smsPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.READ_SMS
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    )

                    BiometricPermissionItem(
                        isEnabled = biometricEnabled,
                        onToggle = { enabled ->
                            biometricEnabled = enabled
                        }
                    )

                    NotificationPermissionItem(
                        isEnabled = notifEnabled,
                        onToggle = { enabled ->
                            notifEnabled = enabled
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                try {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } catch (_: Exception) {}
                            }
                        }
                    )

                    CameraPermissionItem(
                        isEnabled = cameraEnabled,
                        onToggle = { enabled ->
                            cameraEnabled = enabled
                            if (enabled) {
                                try {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom CTA Footer with Black Button (matching 1st, 3rd, 4th page dimensions, typography & vertical line)
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
            // Primary Black Pill Button: Same style, dimensions, typography, and vertical line (32.dp) as 1st, 3rd, 4th page
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
                                val userSettingsDataStore = UserSettingsDataStore(context)
                                userSettingsDataStore.updateSmsEnabled(smsEnabled)
                                userSettingsDataStore.updateBiometricEnabled(biometricEnabled)
                            } catch (_: Exception) {}
                        }
                        if (smsEnabled) {
                            try {
                                smsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS
                                    )
                                )
                            } catch (_: Exception) {}
                        }
                        if (notifEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            try {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } catch (_: Exception) {}
                        }
                        if (cameraEnabled) {
                            try {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                        text = "Grant & Continue",
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

// ─── Step 6: All Set (Fortis Splash) ─────────────────────────────────────────

private enum class AllSetUnlockState {
    Idle,
    Authenticating,
    Success
}

@Composable
private fun SplashContourBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val scaleX = w / 400f
        val scaleY = h / 800f
        val strokeW = 1.dp.toPx()
        val strokeColor = Color(0xFF87948E)

        // Curve 1 (dashed 3 4, opacity 0.25 * 0.4 = 0.10f)
        val p1 = Path().apply {
            moveTo(-80f * scaleX, 120f * scaleY)
            cubicTo(
                60f * scaleX, 140f * scaleY,
                120f * scaleX, 40f * scaleY,
                260f * scaleX, 100f * scaleY
            )
            cubicTo(
                380f * scaleX, 150f * scaleY,
                420f * scaleX, 80f * scaleY,
                480f * scaleX, 110f * scaleY
            )
        }
        drawPath(
            path = p1,
            color = strokeColor.copy(alpha = 0.15f),
            style = Stroke(
                width = strokeW,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
            )
        )

        // Curve 2 (solid, opacity 0.20 * 0.4 = 0.08f)
        val p2 = Path().apply {
            moveTo(-60f * scaleX, 280f * scaleY)
            cubicTo(
                80f * scaleX, 320f * scaleY,
                180f * scaleX, 210f * scaleY,
                320f * scaleX, 270f * scaleY
            )
            cubicTo(
                420f * scaleX, 310f * scaleY,
                460f * scaleX, 250f * scaleY,
                520f * scaleX, 290f * scaleY
            )
        }
        drawPath(
            path = p2,
            color = strokeColor.copy(alpha = 0.12f),
            style = Stroke(width = strokeW)
        )

        // Curve 3 (dashed 4 6, opacity 0.20 * 0.4 = 0.08f)
        val p3 = Path().apply {
            moveTo(-100f * scaleX, 460f * scaleY)
            cubicTo(
                40f * scaleX, 430f * scaleY,
                140f * scaleX, 520f * scaleY,
                280f * scaleX, 470f * scaleY
            )
            cubicTo(
                390f * scaleX, 430f * scaleY,
                440f * scaleX, 500f * scaleY,
                500f * scaleX, 480f * scaleY
            )
        }
        drawPath(
            path = p3,
            color = strokeColor.copy(alpha = 0.12f),
            style = Stroke(
                width = strokeW,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f), 0f)
            )
        )

        // Curve 4 (solid, opacity 0.15 * 0.4 = 0.06f)
        val p4 = Path().apply {
            moveTo(-40f * scaleX, 640f * scaleY)
            cubicTo(
                100f * scaleX, 680f * scaleY,
                200f * scaleX, 590f * scaleY,
                340f * scaleX, 650f * scaleY
            )
            cubicTo(
                440f * scaleX, 690f * scaleY,
                480f * scaleX, 630f * scaleY,
                540f * scaleX, 670f * scaleY
            )
        }
        drawPath(
            path = p4,
            color = strokeColor.copy(alpha = 0.09f),
            style = Stroke(width = strokeW)
        )
    }
}

@Composable
fun FortisAllSetArtwork(
    scaleFactor: Float = 1f,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(256.dp)
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
    ) {
        val w = size.width
        val scale = w / 256f

        // 1. Ambient soft mint glow behind center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFAFFE2).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(128f * scale, 128f * scale),
                radius = 140f * scale
            ),
            radius = 140f * scale,
            center = Offset(128f * scale, 128f * scale)
        )

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas

            // Shadow paint for Bauhaus shapes
            val shadowPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.BLACK
                alpha = (0.42f * 255).toInt()
                maskFilter = android.graphics.BlurMaskFilter(12f * scale, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }

            val fillPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
            }

            // ─── SHAPE 1: Left Mint Capsule (rotated -12 deg) ───
            val mintLeft = 24f * scale
            val mintTop = 32f * scale
            val mintWidth = 48f * scale
            val mintHeight = 144f * scale
            val mintCenterX = mintLeft + mintWidth / 2f
            val mintCenterY = mintTop + mintHeight / 2f

            // Shadow
            nativeCanvas.save()
            nativeCanvas.translate(0f, 10f * scale)
            nativeCanvas.rotate(-12f, mintCenterX, mintCenterY)
            nativeCanvas.drawRoundRect(
                mintLeft, mintTop, mintLeft + mintWidth, mintTop + mintHeight,
                mintWidth / 2f, mintWidth / 2f,
                shadowPaint
            )
            nativeCanvas.restore()

            // ─── SHAPE 2: Right Coral/Peach Arch (asymmetric rounded) ───
            val coralLeft = 112f * scale
            val coralTop = 24f * scale
            val coralWidth = 112f * scale
            val coralHeight = 144f * scale
            val coralPath = android.graphics.Path().apply {
                val radii = floatArrayOf(
                    60f * scale, 60f * scale, // top-left
                    60f * scale, 60f * scale, // top-right
                    8f * scale, 8f * scale,   // bottom-right
                    60f * scale, 60f * scale  // bottom-left
                )
                addRoundRect(
                    android.graphics.RectF(coralLeft, coralTop, coralLeft + coralWidth, coralTop + coralHeight),
                    radii,
                    android.graphics.Path.Direction.CW
                )
            }

            // Shadow
            nativeCanvas.save()
            nativeCanvas.translate(0f, 12f * scale)
            nativeCanvas.drawPath(coralPath, shadowPaint)
            nativeCanvas.restore()

            // ─── SHAPE 3: Lower Left Cream Pill (with 2 dots) ───
            val creamLeft = 64f * scale
            val creamTop = 192f * scale
            val creamWidth = 112f * scale
            val creamHeight = 48f * scale

            // Shadow
            nativeCanvas.save()
            nativeCanvas.translate(0f, 8f * scale)
            nativeCanvas.drawRoundRect(
                creamLeft, creamTop, creamLeft + creamWidth, creamTop + creamHeight,
                creamHeight / 2f, creamHeight / 2f,
                shadowPaint
            )
            nativeCanvas.restore()

            // ─── SHAPE 4: Floating Accent Circles Shadows ───
            val accentMintCx = (200f + 20f) * scale
            val accentMintCy = (176f + 20f) * scale
            val accentMintR = 20f * scale
            nativeCanvas.save()
            nativeCanvas.translate(0f, 6f * scale)
            nativeCanvas.drawCircle(accentMintCx, accentMintCy, accentMintR, shadowPaint)
            nativeCanvas.restore()

            // ─── SHAPE 5: Center White Donut Shadow ───
            val centerDonutCx = 128f * scale
            val centerDonutCy = 128f * scale
            val centerDonutR = 64f * scale
            nativeCanvas.save()
            nativeCanvas.translate(0f, 14f * scale)
            nativeCanvas.drawCircle(centerDonutCx, centerDonutCy, centerDonutR, shadowPaint)
            nativeCanvas.restore()

            // ════════════════ DRAW VECTOR FILLS ════════════════

            // 1. Fill Left Mint Capsule (#7CE5C4)
            fillPaint.color = android.graphics.Color.parseColor("#7CE5C4")
            nativeCanvas.save()
            nativeCanvas.rotate(-12f, mintCenterX, mintCenterY)
            nativeCanvas.drawRoundRect(
                mintLeft, mintTop, mintLeft + mintWidth, mintTop + mintHeight,
                mintWidth / 2f, mintWidth / 2f,
                fillPaint
            )
            nativeCanvas.restore()

            // 2. Fill Right Coral/Peach Arch (#FFB3AF)
            fillPaint.color = android.graphics.Color.parseColor("#FFB3AF")
            nativeCanvas.drawPath(coralPath, fillPaint)

            // 3. Fill Cream Pill (#FAE0AA)
            fillPaint.color = android.graphics.Color.parseColor("#FAE0AA")
            nativeCanvas.drawRoundRect(
                creamLeft, creamTop, creamLeft + creamWidth, creamTop + creamHeight,
                creamHeight / 2f, creamHeight / 2f,
                fillPaint
            )

            // Cream Pill inner dots:
            fillPaint.color = android.graphics.Color.parseColor("#0E0E10")
            fillPaint.alpha = 255
            nativeCanvas.drawCircle(78f * scale, 216f * scale, 6f * scale, fillPaint)
            fillPaint.alpha = (0.40f * 255).toInt()
            nativeCanvas.drawCircle(164f * scale, 216f * scale, 4f * scale, fillPaint)

            // 4. Floating Accent Dots:
            // - Mint circle (#70D9B9)
            fillPaint.color = android.graphics.Color.parseColor("#70D9B9")
            fillPaint.alpha = 255
            nativeCanvas.drawCircle(accentMintCx, accentMintCy, accentMintR, fillPaint)

            // - Upper Left dark grey dot (#353437)
            fillPaint.color = android.graphics.Color.parseColor("#353437")
            nativeCanvas.drawCircle(14f * scale, 86f * scale, 6f * scale, fillPaint)

            // - Bottom Right subtle muted dot (#BDC9C3, 40% alpha)
            fillPaint.color = android.graphics.Color.parseColor("#BDC9C3")
            fillPaint.alpha = (0.40f * 255).toInt()
            nativeCanvas.drawCircle(204f * scale, 244f * scale, 4f * scale, fillPaint)

            // 5. Center Concentric Circle (On top of all layers)
            // Outer white ring
            fillPaint.color = android.graphics.Color.WHITE
            fillPaint.alpha = 255
            nativeCanvas.drawCircle(centerDonutCx, centerDonutCy, centerDonutR, fillPaint)

            // Middle dark circle
            fillPaint.color = android.graphics.Color.parseColor("#0E0E10")
            nativeCanvas.drawCircle(centerDonutCx, centerDonutCy, 32f * scale, fillPaint)

            // Innermost mint pupil
            fillPaint.color = android.graphics.Color.parseColor("#AAFFE2")
            nativeCanvas.drawCircle(centerDonutCx, centerDonutCy, 12f * scale, fillPaint)
        }
    }
}

@Composable
private fun Step6AllSet(
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler(enabled = true) { onBack() }
    }

    var unlockState by remember { mutableStateOf(AllSetUnlockState.Idle) }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    // Pulsing animation for the top green dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Halo pulse animation for the unlock button
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloScale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloAlpha"
    )

    val artScale by animateFloatAsState(
        targetValue = if (unlockState == AllSetUnlockState.Authenticating) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "artScale"
    )

    fun triggerUnlock() {
        if (unlockState != AllSetUnlockState.Idle) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        unlockState = AllSetUnlockState.Authenticating
        coroutineScope.launch {
            delay(500)
            unlockState = AllSetUnlockState.Success
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            delay(400)
            onNext()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131315))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                triggerUnlock()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Background wavy contour lines
        SplashContourBackground(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── TOP BAR ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Glowing pulsing dot + FORTIS OS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFAFFE2).copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = "FORTIS OS",
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        color = Color(0xFFBDC9C3)
                    )
                }

                // Right: Lock icon + v2.4
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color(0xFFBDC9C3),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "v2.4",
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFBDC9C3)
                    )
                }
            }

            // ─── CENTER HERO CONTENT ───────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                FortisAllSetArtwork(
                    scaleFactor = artScale
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Fortis",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    letterSpacing = (-1.2).sp,
                    color = Color(0xFFE5E1E4),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PRIVATE WEALTH ENGINE",
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.8.sp,
                    color = Color(0xFFBDC9C3),
                    textAlign = TextAlign.Center
                )
            }

            // ─── BOTTOM UNLOCK TRIGGER ─────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2C))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            triggerUnlock()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing halo ring (active when idle)
                    if (unlockState == AllSetUnlockState.Idle) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = haloScale
                                    scaleY = haloScale
                                    alpha = haloAlpha
                                }
                                .clip(CircleShape)
                                .background(Color(0xFFAFFE2).copy(alpha = 0.25f))
                        )
                    }

                    Icon(
                        imageVector = if (unlockState != AllSetUnlockState.Idle) Icons.Rounded.LockOpen else Icons.Rounded.Face,
                        contentDescription = "Unlock",
                        tint = if (unlockState != AllSetUnlockState.Idle) Color(0xFFDCC490) else Color(0xFFAFFE2),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        triggerUnlock()
                    }
                ) {
                    Text(
                        text = when (unlockState) {
                            AllSetUnlockState.Idle -> "TAP ANYWHERE TO UNLOCK"
                            AllSetUnlockState.Authenticating -> "AUTHENTICATING..."
                            AllSetUnlockState.Success -> "WELCOME BACK"
                        },
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.8.sp,
                        color = Color(0xFFBDC9C3)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFBDC9C3),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step4AccountsPreview() {
    Step4Accounts(onBack = {}, onNext = {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step5PermissionsPreview() {
    Step5Permissions(onBack = {}, onNext = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF131315, device = "spec:width=390dp,height=844dp,dpi=460")
@Composable
private fun Step6AllSetPreview() {
    Step6AllSet(onNext = {})
}
