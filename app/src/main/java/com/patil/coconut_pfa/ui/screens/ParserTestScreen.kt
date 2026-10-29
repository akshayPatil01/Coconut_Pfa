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
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.components.AmountText
import com.patil.coconut_pfa.domain.sms.SmsParser
import com.patil.coconut_pfa.domain.sms.ParsedSmsType
import com.patil.coconut_pfa.util.MoneyFormatter

@Composable
fun ParserTestScreen(
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rawSender by remember { mutableStateOf("AD-HDFCBK") }
    var rawSmsText by remember {
        mutableStateOf("Rs 1,450.00 spent on HDFC Bank Card ending 1234 at Coffee Shop on 12-Nov-24. Avail Bal: Rs 2,45,000.00. Ref: 123456789")
    }

    val parsed = remember(rawSender, rawSmsText) {
        SmsParser.parse(rawSender, rawSmsText, System.currentTimeMillis())
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
                        text = "Test live bank SMS parser (nothing is saved)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Presets
            Text("Sample Presets:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = {
                        rawSender = "AX-HDFCBK"
                        rawSmsText = "Rs. 2,499.00 debited from a/c XX4321 for Electronics Store on 10 Nov."
                    },
                    label = { Text("Debit Card") }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        rawSender = "VM-ICICIB"
                        rawSmsText = "Rs 12,500.00 credited to a/c XX9876 towards Salary."
                    },
                    label = { Text("Salary Credit") }
                )
            }

            OutlinedTextField(
                value = rawSender,
                onValueChange = { rawSender = it },
                label = { Text("Sender ID (e.g. AD-HDFCBK)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = rawSmsText,
                onValueChange = { rawSmsText = it },
                label = { Text("Paste Raw SMS Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
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

                        val isIgnored = parsed?.type == ParsedSmsType.IGNORE
                        val isIncome = parsed?.type == ParsedSmsType.CREDIT

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isIgnored -> Color(0xFFEEEEEE)
                                isIncome -> Color(0xFFE8F5E9)
                                else -> Color(0xFFFFEBEE)
                            }
                        ) {
                            Text(
                                text = parsed?.type?.name ?: "UNPARSED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isIgnored -> Color.Gray
                                    isIncome -> Color(0xFF2E7D32)
                                    else -> Color(0xFFC62828)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    ParserResultRow(label = "Institution", valueStr = parsed?.institution ?: "Unknown Bank")
                    ParserResultRow(label = "Amount", value = {
                        if (parsed?.amountPaise != null) {
                            AmountText(
                                amountInPaise = parsed.amountPaise,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text("None", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    })
                    ParserResultRow(label = "Account Last 4", valueStr = parsed?.last4 ?: "Not found")
                    ParserResultRow(label = "Merchant / Payee", valueStr = parsed?.merchant ?: "Not found")
                    ParserResultRow(label = "Available Balance", valueStr = parsed?.availableBalancePaise?.let { MoneyFormatter.format(it) } ?: "Not found")
                    ParserResultRow(label = "Reference No", valueStr = parsed?.refNo ?: "Not found")
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
