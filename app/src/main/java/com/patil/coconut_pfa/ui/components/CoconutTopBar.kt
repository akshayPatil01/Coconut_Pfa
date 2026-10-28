package com.patil.coconut_pfa.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.patil.coconut_pfa.data.local.datastore.ThemeMode
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme
import com.patil.coconut_pfa.util.PrivacyManager

/**
 * Top App Bar for Coconut PFA.
 * Supports screen-customized header styles with custom title and actions slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoconutTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleTheme: (() -> Unit)? = null,
    titleContent: (@Composable () -> Unit)? = null,
    actionsContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val isPrivacyEnabled by PrivacyManager.isPrivacyEnabled.collectAsState()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    CenterAlignedTopAppBar(
        title = {
            if (titleContent != null) {
                titleContent()
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Open drawer"
                )
            }
        },
        actions = {
            if (actionsContent != null) {
                actionsContent()
            } else {
                if (onToggleTheme != null) {
                    IconButton(onClick = onToggleTheme) {
                        AnimatedContent(
                            targetState = isDark,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.7f)).togetherWith(
                                    fadeOut() + scaleOut(targetScale = 0.7f)
                                )
                            },
                            label = "ThemeToggleAnimation"
                        ) { dark ->
                            Icon(
                                imageVector = if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = if (dark) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (dark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                IconButton(onClick = { PrivacyManager.togglePrivacy() }) {
                    Icon(
                        imageVector = if (isPrivacyEnabled) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (isPrivacyEnabled) "Disable Privacy Mode" else "Enable Privacy Mode",
                        tint = if (isPrivacyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun CoconutTopBarPreview() {
    Coconut_PfaTheme {
        CoconutTopBar(
            title = "Coconut PFA",
            onMenuClick = {},
            onSettingsClick = {},
            onToggleTheme = {}
        )
    }
}
