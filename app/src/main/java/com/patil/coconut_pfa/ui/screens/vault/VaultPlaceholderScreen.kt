package com.patil.coconut_pfa.ui.screens.vault

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.patil.coconut_pfa.navigation.VaultTab
import com.patil.coconut_pfa.ui.components.EmptyState
import com.patil.coconut_pfa.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultPlaceholderScreen(
    initialTab: VaultTab = VaultTab.CARDS,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(if (initialTab == VaultTab.CARDS) 0 else 1) }
    val tabs = listOf("Payment Cards", "Credentials & Logins")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { viewModel.updateLastTouchTime() },
                    onTap = { viewModel.updateLastTouchTime() }
                )
            },
        topBar = {
            TopAppBar(
                title = { Text("Coconut Vault") },
                actions = {
                    IconButton(onClick = { viewModel.lockVault(context) }) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Lock Vault",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            viewModel.updateLastTouchTime()
                        },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> EmptyState(
                        title = "No Payment Cards",
                        description = "Cards added to your vault will be encrypted with AES-256-GCM and stored securely.",
                        icon = Icons.Rounded.CreditCard,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> EmptyState(
                        title = "No Credentials Saved",
                        description = "Logins and passwords saved to your vault will be encrypted and kept offline on this device.",
                        icon = Icons.Rounded.Key,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
