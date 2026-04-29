@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Home", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.testTag("home_title"))
        Text(text = "Faz 1 shell is ready.")
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    FitLogicTheme {
        HomeScreen()
    }
}
