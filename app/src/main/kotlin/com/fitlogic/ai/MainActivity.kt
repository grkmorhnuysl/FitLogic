@file:Suppress("FunctionName")

package com.fitlogic.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme
import com.fitlogic.ai.navigation.FitLogicNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitLogicTheme {
                FitLogicRoot()
            }
        }
    }
}

@Composable
private fun FitLogicRoot() {
    FitLogicNavHost()
}

@Preview(showBackground = true, widthDp = 380)
@Composable
fun FitLogicAppPreview() {
    FitLogicTheme {
        FitLogicRoot()
    }
}
