package pl.edu.pb.jardinito.ui.components.appButton

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pl.edu.pb.jardinito.ui.theme.colors

// ===== KOLORY =====
@Composable
fun buttonColors(
    variant: ButtonVariant,
    buttonColor: Color?,
    contentColor: Color?
): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = buttonColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary100
            ButtonVariant.Secondary -> colors.primary300
            ButtonVariant.Tertiary -> colors.primary900
        },
        contentColor = contentColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary300
            ButtonVariant.Secondary -> colors.primary50
            ButtonVariant.Tertiary -> colors.neutralDark
        },
        disabledContainerColor = buttonColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary50
            ButtonVariant.Secondary -> colors.primary50
            ButtonVariant.Tertiary -> colors.primary900
        },
        disabledContentColor = contentColor ?: when (variant) {
            ButtonVariant.Primary -> colors.primary100
            ButtonVariant.Secondary -> colors.primary100
            ButtonVariant.Tertiary -> colors.neutralLight
        }
    )
}