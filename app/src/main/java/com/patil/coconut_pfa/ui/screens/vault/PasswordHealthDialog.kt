package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.vault.PasswordHealthReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordHealthDialog(
    report: PasswordHealthReport,
    onDismiss: () -> Unit,
    onItemClick: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Password Health Check",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Found ${report.totalIssues} password issues across your vault items.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (report.totalIssues == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "Great job! All your vault passwords are strong, unique, and up to date.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (report.weakItems.isNotEmpty()) {
                        item {
                            Text("Weak Passwords (${report.weakItems.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        items(report.weakItems) { issue ->
                            HealthIssueRow(title = issue.title, description = issue.description, onClick = {
                                onItemClick(issue.item.id)
                                onDismiss()
                            })
                        }
                    }

                    if (report.reusedItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reused Passwords (${report.reusedItems.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        items(report.reusedItems) { issue ->
                            HealthIssueRow(title = issue.title, description = issue.description, onClick = {
                                onItemClick(issue.item.id)
                                onDismiss()
                            })
                        }
                    }

                    if (report.oldItems.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Old Passwords (${report.oldItems.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        items(report.oldItems) { issue ->
                            HealthIssueRow(title = issue.title, description = issue.description, onClick = {
                                onItemClick(issue.item.id)
                                onDismiss()
                            })
                        }
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HealthIssueRow(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
