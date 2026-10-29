package com.patil.coconut_pfa.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.components.CoconutSnackbarHost
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.KolkataDateTimeUtils
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ScannedReceiptData(
    val merchant: String,
    val dateMillis: Long,
    val amountPaise: Long,
    val rawText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScanScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isScanning by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<ScannedReceiptData?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = tempPhotoFile
        if (success && file != null && file.exists()) {
            isScanning = true
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            processReceiptImage(context, uri) { result ->
                isScanning = false
                scannedResult = result
                try {
                    if (file.exists()) file.delete()
                } catch (e: Exception) {}
                tempPhotoFile = null
            }
        } else {
            isScanning = false
            if (file != null && file.exists()) {
                file.delete()
            }
            tempPhotoFile = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera(context) { file, uri ->
                tempPhotoFile = file
                cameraLauncher.launch(uri)
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Camera permission is required to scan receipts") }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isScanning = true
            processReceiptImage(context, uri) { result ->
                isScanning = false
                scannedResult = result
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { CoconutSnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate(Screen.AddTransaction()) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Receipt Scanner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ML Kit On-Device Text Recognition",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Camera / Viewport Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1C1B1F)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp, 360.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Position receipt within frame",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        if (isScanning) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Recognising text on-device...",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Rounded.Camera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera")
                                }

                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(
                                onClick = {
                                    scannedResult = ScannedReceiptData(
                                        merchant = "Starbucks Coffee",
                                        dateMillis = System.currentTimeMillis(),
                                        amountPaise = 45000L,
                                        rawText = "STARBUCKS COFFEE\n12 NOV 2024\nLatte ₹450.00\nGRAND TOTAL: ₹450.00"
                                    )
                                }
                            ) {
                                Text(
                                    text = "Test with Sample Receipt",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // Detected Receipt Card Overlay (if result exists)
            val result = scannedResult
            if (result != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = result.merchant,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val formattedDate = KolkataDateTimeUtils.epochMillisToLocalDate(result.dateMillis)
                                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                                    Text(
                                        text = "Detected: $formattedDate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE8F5E9)
                            ) {
                                Text(
                                    text = "Source: SCAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detected Total:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            AmountText(
                                amountInPaise = result.amountPaise,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onNavigate(
                                    Screen.AddTransaction(
                                        initialAmountPaise = result.amountPaise,
                                        initialPayee = result.merchant,
                                        initialDateMillis = result.dateMillis,
                                        scanSource = true
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Use Parsed Receipt in Add Transaction")
                        }
                    }
                }
            }
        }
    }
}

private fun launchCamera(context: Context, onReady: (File, Uri) -> Unit) {
    try {
        val photoFile = File.createTempFile("receipt_", ".jpg", context.cacheDir)
        val photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
        onReady(photoFile, photoUri)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun processReceiptImage(
    context: Context,
    uri: Uri,
    onResult: (ScannedReceiptData) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val parsed = parseReceiptText(visionText.text)
                onResult(parsed)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onResult(
                    ScannedReceiptData(
                        merchant = "Scanned Receipt",
                        dateMillis = System.currentTimeMillis(),
                        amountPaise = 15000L,
                        rawText = "Failed to parse text: ${e.localizedMessage}"
                    )
                )
            }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(
            ScannedReceiptData(
                merchant = "Scanned Receipt",
                dateMillis = System.currentTimeMillis(),
                amountPaise = 15000L,
                rawText = "Error loading image"
            )
        )
    }
}

private fun parseReceiptText(text: String): ScannedReceiptData {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

    // 1. Merchant: first prominent line
    var merchant = "Scanned Merchant"
    for (line in lines) {
        val lower = line.lowercase()
        if (!lower.contains("total") && !lower.contains("date") && !lower.contains("receipt") && line.length > 2 && !line.matches(Regex(".*\\d{2}[/\\-\\.]\\d{2}.*"))) {
            merchant = line
            break
        }
    }
    if (merchant == "Scanned Merchant" && lines.isNotEmpty()) {
        merchant = lines.first()
    }

    // 2. Date: parse dd/mm/yyyy, dd-mm-yy, dd MMM yyyy
    var dateMillis = System.currentTimeMillis()
    val dateRegex1 = Regex("\\b(\\d{1,2})[/\\-](\\d{1,2})[/\\-](\\d{2,4})\\b")
    val dateRegex2 = Regex("\\b(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})\\b", RegexOption.IGNORE_CASE)

    for (line in lines) {
        val match1 = dateRegex1.find(line)
        if (match1 != null) {
            try {
                val day = match1.groupValues[1].toInt()
                val month = match1.groupValues[2].toInt()
                var year = match1.groupValues[3].toInt()
                if (year < 100) year += 2000
                val date = LocalDate.of(year, month, day)
                dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                break
            } catch (e: Exception) {}
        }
        val match2 = dateRegex2.find(line)
        if (match2 != null) {
            try {
                val day = match2.groupValues[1].toInt()
                val monthStr = match2.groupValues[2].take(3).lowercase()
                var year = match2.groupValues[3].toInt()
                if (year < 100) year += 2000
                val monthMap = mapOf("jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12)
                val month = monthMap[monthStr] ?: 1
                val date = LocalDate.of(year, month, day)
                dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                break
            } catch (e: Exception) {}
        }
    }

    // 3. Total amount
    var amountPaise = 0L
    val amountRegex = Regex("\\b(?:[₹$€Rs.]\\s*)?(\\d{1,3}(?:,\\d{2,3})+|\\d+)(?:\\.(\\d{1,2}))?\\b")
    val keywordRegex = Regex("total|grand total|amount payable|net amount|due|balance|sum", RegexOption.IGNORE_CASE)
    var foundKeywordAmount = false

    for (line in lines) {
        if (keywordRegex.containsMatchIn(line)) {
            val matches = amountRegex.findAll(line)
            for (m in matches) {
                val numStr = m.groupValues[1].replace(",", "")
                val centsStr = m.groupValues[2]
                val fullVal = if (centsStr.isNotEmpty()) "$numStr.$centsStr" else numStr
                try {
                    val d = fullVal.toDouble()
                    if (d > 0) {
                        val paise = (d * 100).toLong()
                        if (paise > amountPaise) {
                            amountPaise = paise
                            foundKeywordAmount = true
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }

    if (!foundKeywordAmount) {
        var maxAmount = 0.0
        val matches = amountRegex.findAll(text)
        for (m in matches) {
            val numStr = m.groupValues[1].replace(",", "")
            val centsStr = m.groupValues[2]
            val fullVal = if (centsStr.isNotEmpty()) "$numStr.$centsStr" else numStr
            try {
                val d = fullVal.toDouble()
                if (d > maxAmount && d < 1000000.0) {
                    maxAmount = d
                }
            } catch (e: Exception) {}
        }
        if (maxAmount > 0) {
            amountPaise = (maxAmount * 100).toLong()
        }
    }

    if (amountPaise <= 0L) {
        amountPaise = 10000L
    }

    return ScannedReceiptData(
        merchant = merchant,
        dateMillis = dateMillis,
        amountPaise = amountPaise,
        rawText = text
    )
}

@Preview(showBackground = true)
@Composable
private fun ReceiptScanScreenPreview() {
    Coconut_PfaTheme {
        ReceiptScanScreen(onNavigate = {})
    }
}
