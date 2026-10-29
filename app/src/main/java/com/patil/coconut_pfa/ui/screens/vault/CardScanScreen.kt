package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.util.CardTextExtractor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardScanScreen(
    onNavigateBack: () -> Unit,
    onCardScanned: (cardNumber: String, expiryMonth: String, expiryYear: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sampleOcrInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Scan Payment Card (OCR)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Point camera at card or paste OCR text below to extract card details automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = sampleOcrInput,
                onValueChange = { sampleOcrInput = it },
                label = { Text("Simulated OCR Text / Card Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("e.g. 4111 2222 3333 4444\n09/28\nJOHN DOE") }
            )

            Button(
                onClick = {
                    val extracted = CardTextExtractor.extract(sampleOcrInput)
                    if (extracted.cardNumber != null) {
                        onCardScanned(
                            extracted.cardNumber ?: "",
                            extracted.expiryMonth ?: "12",
                            extracted.expiryYear ?: "28"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Extract Card Details")
            }
        }
    }
}
