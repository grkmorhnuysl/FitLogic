@file:Suppress("FunctionName", "UnusedPrivateMember")

package com.fitlogic.ai.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlogic.ai.core.designsystem.theme.FitLogicTheme

@Composable
fun FlButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FlButtonVariant = FlButtonVariant.Primary,
    enabled: Boolean = true,
) {
    when (variant) {
        FlButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                Text(text = text)
            }
        }

        FlButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                Text(text = text)
            }
        }

        FlButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
            ) {
                Text(text = text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FlButtonPreview() {
    FitLogicTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlButton(text = "Primary", onClick = {}, modifier = Modifier.fillMaxWidth())
            FlButton(
                text = "Secondary",
                onClick = {},
                variant = FlButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
            )
            FlButton(
                text = "Text",
                onClick = {},
                variant = FlButtonVariant.Text,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Preview",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
