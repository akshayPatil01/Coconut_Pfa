package com.patil.coconut_pfa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

@Composable
fun ParserTestScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rawSmsText by remember {
        mutableStateOf("Rs. 1,450.00 spent on Demo Card A at Coffee Shop 1 on 12 Nov. Avail Bal: Rs. 2,45,000.00")
    }

    // Dummy parser logic
    val isDebit = !rawSmsText.contains("credited", ignoreCase = true)
    val parsedAmountPaise = remember(rawSmsText) {
        val match = Regex("""(Rs\.?|INR)\s*([\d,]+(\.\d{1,2})?)""").find(rawSmsText)
        val numStr = match?.groupValues?.get(2)?.replace(",", "")
        val doubleVal = numStr?.toDoubleOrNull() ?: 1450.0
        (doubleVal * 100).toLong()
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onNavigate(Screen.Settings) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "SMS Parser Test Lab",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Test SMS regex matching & entity extraction",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Presets
            Text("Sample Preset Test Cases:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = {
                        rawSmsText = "Rs. 2,499.00 debited from a/c XX4321 for Electronics Store on 10 Nov."
                    },
                    label = { Text("Card Debit") }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        rawSmsText = "Rs 12500.00 credited to a/c XX4321 towards Salary adjustment."
                    },
                    label = { Text("Salary Credit") }
                )
            }

            // Raw Text Area
            OutlinedTextField(
                value = rawSmsText,
                onValueChange = { rawSmsText = it },
                label = { Text("Paste Raw SMS Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Live Parser Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Parser Output", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDebit) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = if (isDebit) "EXPENSE / DEBIT" else "INCOME / CREDIT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebit) Color(0xFFC62828) else Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    ParserResultRow(label = "Extracted Amount", value = {
                        AmountText(
                            amountInPaise = parsedAmountPaise,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    })

                    ParserResultRow(label = "Detected Account", valueStr = "Demo Bank Salary ••4321")
                    ParserResultRow(label = "Suggested Category", valueStr = if (isDebit) "Dining / Shopping" else "Income")
                    ParserResultRow(label = "Confidence Score", valueStr = "96% High Confidence")
                }
            }
        }
    }
}

@Composable
private fun ParserResultRow(
    label: String,
    valueStr: String? = null,
    value: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (valueStr != null) {
            Text(text = valueStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        } else value?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun ParserTestScreenPreview() {
    Coconut_PfaTheme {
        ParserTestScreen(onNavigate = {})
    }
}
