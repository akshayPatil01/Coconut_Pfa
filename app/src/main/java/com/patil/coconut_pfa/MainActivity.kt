package com.patil.coconut_pfa

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.patil.coconut_pfa.ui.CoconutApp
import com.patil.coconut_pfa.ui.theme.Coconut_PfaTheme

import android.content.Intent
import androidx.compose.runtime.mutableStateOf

class MainActivity : FragmentActivity() {
    private val navigateToScreen = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        checkIntentForNavigation(intent)
        enableEdgeToEdge()
        setContent {
            Coconut_PfaTheme {
                CoconutApp(
                    navigateToScreenSignal = navigateToScreen.value,
                    onHandledNavigateToScreen = { navigateToScreen.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntentForNavigation(intent)
    }

    private fun checkIntentForNavigation(intent: Intent?) {
        val nav = intent?.getStringExtra("navigate_to")
        if (nav != null) {
            navigateToScreen.value = nav
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoconutAppPreview() {
    Coconut_PfaTheme {
        CoconutApp()
    }
}
