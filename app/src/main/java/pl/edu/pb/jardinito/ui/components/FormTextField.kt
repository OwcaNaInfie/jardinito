package pl.edu.pb.jardinito.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.pb.jardinito.ui.theme.colors
import kotlin.reflect.KFunction2

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    validator: ((String) -> Int?)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    debounceMillis: Long = 500,
    valueEnteredPassword: String? = null,
    validatorRepeatedPassword: ((String, String) -> Int?)? = null
) {
    var errorMessage by remember { mutableStateOf<Int?>(null) }
    val errorText = errorMessage?.let { stringResource(it) }
    var hasInteracted by remember { mutableStateOf(false) }
    var hasValidated by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Debounced validation
    LaunchedEffect(value, valueEnteredPassword) {
        if (!hasInteracted) return@LaunchedEffect
        kotlinx.coroutines.delay(debounceMillis)
        errorMessage = when {
            validator != null ->
                validator.invoke(value)

            validatorRepeatedPassword != null && valueEnteredPassword != null ->
                validatorRepeatedPassword.invoke(value, valueEnteredPassword)

            else -> null
        }
        hasValidated = true
    }

    val isError = hasValidated && errorMessage != null
    val isValid = hasValidated && errorMessage == null && value.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {

        // LABEL
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.neutralBlack
            )
            if (required) {
                Text("*", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = {
                if (!hasInteracted) hasInteracted = true
                onValueChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
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
                    isError -> MaterialTheme.colorScheme.error
                    isValid -> colors.primary500
                    else -> colors.neutralBlack
                },
                focusedContainerColor = colors.neutralLight,
                unfocusedContainerColor = colors.neutralLight,
                disabledContainerColor = colors.neutralLight,
                errorContainerColor = colors.neutralLight,
                focusedBorderColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isValid -> colors.primary500
                    else -> colors.primary300
                },
                unfocusedBorderColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isValid -> colors.primary700
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
