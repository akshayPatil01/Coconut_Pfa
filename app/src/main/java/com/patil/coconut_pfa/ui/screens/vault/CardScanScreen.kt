package com.patil.coconut_pfa.ui.screens.vault

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.patil.coconut_pfa.domain.CardTextExtractor
import com.patil.coconut_pfa.domain.ScannedCardData
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGetImage::class)
@Composable
fun CardScanScreen(
    onNavigateBack: () -> Unit,
    onCardScanned: (number: String, expiry: String, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showPermissionRationale by remember { mutableStateOf(!hasCameraPermission) }
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<CameraControl?>(null) }
    
    var scannedData by remember { mutableStateOf<ScannedCardData?>(null) }
    var showMaskedNumber by remember { mutableStateOf(false) }
    var timeoutReached by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        showPermissionRationale = !granted
    }

    // 10 second timeout timer
    LaunchedEffect(hasCameraPermission, scannedData) {
        if (hasCameraPermission && scannedData == null) {
            timeoutReached = false
            delay(10000L)
            if (scannedData == null) {
                timeoutReached = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasCameraPermission) {
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                var lastDetectedNumber by remember { mutableStateOf<String?>(null) }
                var consecutiveMatches by remember { mutableIntStateOf(0) }

                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setTargetResolution(Size(1280, 720))
                                .build()

                            @androidx.camera.core.ExperimentalGetImage
                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && scannedData == null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                    recognizer.process(image)
                                        .addOnSuccessListener { visionText ->
                                            val lines = visionText.text.lines().map { it.trim() }.filter { it.isNotBlank() }
                                            val extracted = CardTextExtractor.extract(lines)
                                            if (extracted != null && extracted.number.isNotEmpty()) {
                                                if (extracted.number == lastDetectedNumber) {
                                                    consecutiveMatches++
                                                    if (consecutiveMatches >= 3) {
                                                        scannedData = extracted
                                                    }
                                                } else {
                                                    lastDetectedNumber = extracted.number
                                                    consecutiveMatches = 1
                                                }
                                            }
                                        }
                                        .addOnFailureListener {
                                            // Ignore frame error
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControlRef = camera.cameraControl
                                camera.cameraControl.enableTorch(torchEnabled)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }

                        IconButton(
                            onClick = {
                                torchEnabled = !torchEnabled
                                cameraControlRef?.enableTorch(torchEnabled)
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                contentDescription = "Torch",
                                tint = Color.White
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .aspectRatio(1.586f)
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Hold the side with the number inside the frame",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        if (timeoutReached) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.DarkGray.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "Many new cards print the number on the back - flip it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Yellow,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Text("Enter manually")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The camera is used only while scanning. Nothing is saved or sent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Grant Camera Permission")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel / Enter manually", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            val result = scannedData
            if (result != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Card Scanned Successfully",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(6.dp).size(16.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Card Number",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val displayNum = if (showMaskedNumber) {
                                        result.number.chunked(4).joinToString(" ")
                                    } else {
                                        val last4 = result.number.takeLast(4)
                                        "•••• •••• •••• $last4"
                                    }
                                    Text(
                                        text = displayNum,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                IconButton(onClick = { showMaskedNumber = !showMaskedNumber }) {
                                    Icon(
                                        imageVector = if (showMaskedNumber) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Toggle Show Number"
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Expiry",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = result.expiry.ifBlank { "N/A" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cardholder Name",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = result.cardholderName.ifBlank { "N/A" },
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onCardScanned(result.number, result.expiry, result.cardholderName)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Use in Add Card (CVV empty & focused)")
                            }
                        }
                    }
                }
            }
        }
    }
}
