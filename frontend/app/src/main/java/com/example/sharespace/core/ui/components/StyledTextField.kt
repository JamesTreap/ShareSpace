package com.example.sharespace.core.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import com.example.sharespace.core.ui.theme.BackgroundAccent
import com.example.sharespace.core.ui.theme.BorderPrimary
import com.example.sharespace.core.ui.theme.TextSecondary

/* Example usage:
StyledTextField(
    value = password,
    onValueChange = { password = it },
    label = { Text("Password") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    modifier = Modifier.fillMaxWidth()
)
 */

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    containerColor: Color? = null,
    borderColor: Color? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        isError = isError,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedTextColor = TextSecondary,
            unfocusedTextColor = TextSecondary,
            focusedContainerColor = containerColor ?: BackgroundAccent,
            unfocusedContainerColor = containerColor ?: BackgroundAccent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = borderColor ?: BorderPrimary,
            disabledIndicatorColor = borderColor ?: BorderPrimary,
            errorIndicatorColor = MaterialTheme.colorScheme.error
        )
    )
}