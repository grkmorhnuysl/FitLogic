@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme

@Composable
fun FlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { androidx.compose.material3.Text(label) },
        singleLine = true,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FlTextFieldPreview() {
    var value by remember { mutableStateOf("") }

    FitLogicTheme {
        FlTextField(
            value = value,
            onValueChange = { value = it },
            label = "Workout name",
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}
