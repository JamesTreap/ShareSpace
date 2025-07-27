package com.example.sharespace.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.theme.BackgroundAccent
import java.text.SimpleDateFormat
import java.util.*

/*
Usage:
var selectedDate by remember { mutableStateOf("Select Date") }

DatePickerSelector(
    selectedDate = selectedDate,
    onDateSelected = { date ->
        selectedDate = date
    }
)
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerSelector(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    StyledTextField(
        value = selectedDate,
        onValueChange = { },
        readOnly = true,
        label = { Text("Date") },
        placeholder = { Text("YYYY-MM-DD") },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select date")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        val formattedDate = formatter.format(Date(millis))
                        onDateSelected(formattedDate)

                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = DatePickerDefaults.colors(
                containerColor = BackgroundAccent,
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = BackgroundAccent
                )
            )
        }
    }
}

