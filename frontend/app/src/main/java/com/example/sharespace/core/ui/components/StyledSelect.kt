package com.example.sharespace.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/*
USAGE:
val intOptions = listOf("1w", "2w", "3w", "4w")
val stringOptions = listOf("Option 1", "Option 2", "Option 3")

// can use either intOptions or stringOptions
StyledSelect(options = intOptions, label = "Choose a number") { selected ->
    println("Selected value: $selected")
}
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledSelect(
    options: List<Any>,
    label: String = "Select an option",
    initialSelected: String? = null,
    onOptionSelected: (Any) -> Unit
) {
    val displayOptions = options.map { it.toString() }
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember {
        mutableStateOf(initialSelected?.takeIf { it in displayOptions } ?: displayOptions.firstOrNull().orEmpty())
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        StyledTextField(
            value = selectedOptionText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            displayOptions.forEachIndexed { index, optionText ->
                DropdownMenuItem(
                    text = { Text(optionText) },
                    onClick = {
                        selectedOptionText = optionText
                        expanded = false
                        onOptionSelected(options[index]) // Return original value (Int or String)
                    }
                )
            }
        }
    }
}