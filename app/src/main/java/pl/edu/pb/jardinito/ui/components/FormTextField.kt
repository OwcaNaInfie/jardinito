package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.colors

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    validator: ((String) -> String?)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    debounceMillis: Long = 500
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasInteracted by remember { mutableStateOf(false) }

    // Debounced validation
    LaunchedEffect(value) {
        if (!hasInteracted) return@LaunchedEffect

        kotlinx.coroutines.delay(debounceMillis)
        errorMessage = validator?.invoke(value)
    }

    val isError = errorMessage != null
    val isValid = errorMessage == null && hasInteracted && value.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {

        // LABEL
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.neutralBlack
            )
            if (required) {
                Text(
                    text = " *",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = value,
            onValueChange = {
                if (!hasInteracted) hasInteracted = true
                onValueChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isValid -> Color(0xFF2E7D32) // green
                    else -> MaterialTheme.colorScheme.primary
                },
                unfocusedIndicatorColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isValid -> Color(0xFF2E7D32)
                    else -> MaterialTheme.colorScheme.outline
                }
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ERROR SLOT – zawsze zajmuje miejsce
        Text(
            text = errorMessage ?: " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            minLines = 1
        )
    }
}


@Preview(showBackground = true, apiLevel = 34)
@Composable
private fun FormTextFieldPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormTextField(
            label = "Email",
            value = "",
            onValueChange = {},
            required = true
        )

        FormTextField(
            label = "Password",
            value = "123",
            onValueChange = {},
            required = true
        )
    }
}
