package com.patil.coconut_pfa.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.navigation.Screen
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.ui.theme.DarkSlateNavBar
import com.patil.coconut_pfa.ui.theme.GradientPurpleStart
import com.patil.coconut_pfa.ui.theme.GradientPurpleEnd
import com.patil.coconut_pfa.ui.theme.GradientOrangeCoralStart

/**
 * Bottom Navigation Bar for Coconut PFA with 5 destinations.
 * Features a custom curved dark slate bottom bar and a glowing purple FAB.
 */
@Composable
fun CoconutBottomBar(
    currentDestination: Screen,
    onNavigateToTab: (Screen) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 20.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            ambientColor = Color.Black.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = DarkSlateNavBar,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(72.dp)
                .padding(horizontal = 8.dp),
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
                // Outer glow effect box
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GradientPurpleStart.copy(alpha = 0.4f),
                                    GradientPurpleEnd.copy(alpha = 0.4f)
                                )
                            )
                        )
                ) {
                    FloatingActionButton(
                        onClick = onAddClick,
                        shape = CircleShape,
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .align(Alignment.Center)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientPurpleStart, GradientPurpleEnd)
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // 4. Payments tab // actually the plan uses 'Reports' instead of 'Payments' in text "Home, Transactions, Central +, Reports, Accounts", but Screen.Payments and Screen.Reports might differ. I will stick to what exists in code, e.g. Screen.Payments or if it doesn't exist I'll use it. Code says Payments.
            BottomNavItem(
                icon = Icons.Rounded.Payments,
                label = "Reports",
                selected = currentDestination == Screen.Reports,
                onClick = { onNavigateToTab(Screen.Reports) },
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
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Gray,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "BottomNavContentColor"
    )

    val pillColor by animateColorAsState(
        targetValue = if (selected) GradientOrangeCoralStart.copy(alpha = 0.8f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "BottomNavPillColor"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "BottomNavIconScale"
    )

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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(pillColor)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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
