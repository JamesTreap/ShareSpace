package com.example.sharespace.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.BackgroundAccent
import com.example.sharespace.core.ui.theme.BorderPrimary
import com.example.sharespace.core.ui.theme.ButtonRadius
import com.example.sharespace.core.ui.theme.TextPrimary

/* Example usage:
StyledCheckbox(
    onCheckedChange = { /* handle change */ },
    checked = false
)
StyledCheckbox(
    onCheckedChange = { /* handle change */ },
    checked = true
)
var isChecked by remember { mutableStateOf(false) }
StyledCheckbox(checked = isChecked) {
    isChecked = it
}
*/

@Composable
fun StyledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (checked) AquaAccent else BackgroundAccent
    val borderColor = if (checked) Color.Transparent else BorderPrimary
    val checkmarkColor = if (checked) BackgroundAccent else TextPrimary

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(ButtonRadius))
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(ButtonRadius))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "",
            tint = checkmarkColor,
            modifier = Modifier.size(16.dp)
        )
    }
}