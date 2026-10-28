package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

/**
 * Bottom Navigation Bar for Coconut PFA with 5 destinations.
 * Features a raised FloatingActionButton in the center for quick '+' Add Transaction action.
 */
@Composable
fun CoconutBottomBar(
    currentDestination: Screen,
    onNavigateToTab: (Screen) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home tab
            BottomNavItem(
                icon = Icons.Rounded.Home,
                label = "Home",
                selected = currentDestination == Screen.Home,
                onClick = { onNavigateToTab(Screen.Home) },
                modifier = Modifier.weight(1f)
            )

            // 2. Transactions tab
            BottomNavItem(
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                label = "Transactions",
                selected = currentDestination is Screen.Transactions,
                onClick = { onNavigateToTab(Screen.Transactions()) },
                modifier = Modifier.weight(1f)
            )

            // 3. Center Raised '+' Add Button
            Box(
                modifier = Modifier.weight(1.2f),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onAddClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 4. Payments tab
            BottomNavItem(
                icon = Icons.Rounded.Payments,
                label = "Payments",
                selected = currentDestination == Screen.Payments,
                onClick = { onNavigateToTab(Screen.Payments) },
                modifier = Modifier.weight(1f)
            )

            // 5. Accounts tab
            BottomNavItem(
                icon = Icons.Rounded.AccountBalance,
                label = "Accounts",
                selected = currentDestination == Screen.Accounts,
                onClick = { onNavigateToTab(Screen.Accounts) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoconutBottomBarPreview() {
    Coconut_PfaTheme {
        CoconutBottomBar(
            currentDestination = Screen.Home,
            onNavigateToTab = {},
            onAddClick = {}
        )
    }
}
