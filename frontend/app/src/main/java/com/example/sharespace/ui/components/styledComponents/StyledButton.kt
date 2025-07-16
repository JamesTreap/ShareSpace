package com.example.sharespace.ui.components.styledComponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import com.example.sharespace.ui.theme.AlertRed
import com.example.sharespace.ui.theme.AquaAccent
import com.example.sharespace.ui.theme.BackgroundAccent
import com.example.sharespace.ui.theme.BorderPrimary
import com.example.sharespace.ui.theme.ButtonRadius
import com.example.sharespace.ui.theme.TextSecondary

enum class ButtonType {
    Primary,
    Secondary,
    Tertiary,
    Danger
}

/* Example usage:
StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Primary
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Primary,
    enabled = false
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Secondary
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Secondary,
    enabled = false
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Tertiary
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Danger
)

StyledButton(
    onClick = { /* Handle click */ },
    text = "Cancel",
    buttonType = ButtonType.Danger,
    enabled = false
)
*/

@Composable
fun StyledButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    buttonType: ButtonType = ButtonType.Primary,
) {
    val containerColor = if (loading) {
        BorderPrimary
    } else if (buttonType == ButtonType.Danger) {
        if (enabled) AlertRed else BorderPrimary
    } else if (buttonType == ButtonType.Primary) {
        if (enabled) AquaAccent else BorderPrimary
    } else if (buttonType == ButtonType.Secondary) {
        if (enabled) BackgroundAccent else BorderPrimary
    } else { // fallback
        Color.Transparent
    }

    val contentColor = if (enabled) {
        if (buttonType == ButtonType.Secondary || buttonType == ButtonType.Tertiary) AquaAccent else BackgroundAccent
    } else {
        TextSecondary
    }

    val border = if (buttonType == ButtonType.Secondary && enabled) BorderStroke(1.dp, BorderPrimary) else null

    Button(
        onClick = onClick,
        enabled = enabled || !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        modifier = modifier,
        shape = RoundedCornerShape(ButtonRadius)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = TextSecondary,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(20.dp)
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonRadius))
            }

            // only display text if not loading
            Text(text)
        }
    }
}