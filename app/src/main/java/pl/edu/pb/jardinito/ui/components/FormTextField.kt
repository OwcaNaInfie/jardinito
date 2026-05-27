package pl.edu.pb.jardinito.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    errorRes: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    isValid: Boolean = false,
    isError: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val hasError = errorRes != null || isError
    val isValid = isValid
    val errorText = errorRes?.let { stringResource(it) }

    Column(modifier = modifier.fillMaxWidth()) {

        // LABEL
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.neutralBlack
            )
            if (required) {
                Text("*", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = hasError,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            visualTransformation = when {
                isPassword && !passwordVisible -> PasswordVisualTransformation()
                else -> VisualTransformation.None
            },
            trailingIcon = {
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.VisibilityOff
                            else
                                Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            },
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = when {
                    isValid -> colors.primary500
                    hasError -> MaterialTheme.colorScheme.error
                    else -> colors.neutralBlack
                },
                focusedContainerColor = colors.neutralLight,
                unfocusedContainerColor = colors.neutralLight,
                disabledContainerColor = colors.neutralLight,
                errorContainerColor = colors.neutralLight,

                focusedBorderColor = when {
                    isValid -> colors.primary500
                    hasError -> MaterialTheme.colorScheme.error
                    else -> colors.primary300
                },
                unfocusedBorderColor = when {
                    isValid -> colors.primary500
                    hasError -> MaterialTheme.colorScheme.error
                    else -> colors.transparent
                }
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = errorText ?: " ",
            style = MaterialTheme.typography.bodySmall,
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
