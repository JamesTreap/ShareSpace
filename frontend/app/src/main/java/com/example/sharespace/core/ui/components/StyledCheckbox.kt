package com.example.sharespace.core.ui.components

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

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
    checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier
) {
    Checkbox(
        checked = checked, onCheckedChange = onCheckedChange, colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary
        ), modifier = modifier.semantics { this.role = Role.Checkbox })
}