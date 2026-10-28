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

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
        enableEdgeToEdge()
        setContent {
            Coconut_PfaTheme {
                CoconutApp()
            }
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
