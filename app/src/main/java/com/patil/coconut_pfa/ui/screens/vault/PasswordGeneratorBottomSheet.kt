package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.data.local.security.VaultClipboardManager
import com.patil.coconut_pfa.vault.PasswordGenerator
import com.patil.coconut_pfa.vault.PasswordStrength

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorBottomSheet(
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit
) {
    val context = LocalContext.current
    var length by remember { mutableIntStateOf(20) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var avoidLookAlikes by remember { mutableStateOf(false) }

    val generationResult = remember(length, useUpper, useLower, useNumbers, useSymbols, avoidLookAlikes) {
        PasswordGenerator.generate(
            length = length,
            useUpper = useUpper,
            useLower = useLower,
            useNumbers = useNumbers,
            useSymbols = useSymbols,
            avoidLookAlikes = avoidLookAlikes
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Password Generator",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Password Display Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = generationResult.password,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        length = length
                    }) {
                        Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Regenerate")
                    }
                    IconButton(onClick = {
                        VaultClipboardManager.copyToClipboard(context, "Generated Password", generationResult.password, 30)
                    }) {
                        Icon(imageVector = Icons.Rounded.ContentCopy, contentDescription = "Copy")
                    }
                }
            }

            // Entropy & Strength
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entropy: ${generationResult.entropyBits.toInt()} bits",
                    style = MaterialTheme.typography.bodyMedium
                )
                val badgeColor = when (generationResult.strength) {
                    PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                    PasswordStrength.OK -> MaterialTheme.colorScheme.tertiary
                    PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = generationResult.strength.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Length Slider
            Column {
                Text(text = "Length: $length", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..64f,
                    steps = 56
                )
            }

            // Switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Uppercase (A-Z)")
                Switch(checked = useUpper, onCheckedChange = { useUpper = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lowercase (a-z)")
                Switch(checked = useLower, onCheckedChange = { useLower = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Numbers (0-9)")
                Switch(checked = useNumbers, onCheckedChange = { useNumbers = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Symbols (!@#...)")
                Switch(checked = useSymbols, onCheckedChange = { useSymbols = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Avoid look-alikes (0 O o 1 l I)")
                Switch(checked = avoidLookAlikes, onCheckedChange = { avoidLookAlikes = it })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onUsePassword(generationResult.password)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
