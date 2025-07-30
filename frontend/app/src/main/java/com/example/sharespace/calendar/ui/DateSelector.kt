package com.example.sharespace.room.ui.roomSummary.components

// Import your theme colors (adjust path if necessary)
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharespace.core.ui.components.ButtonType
import com.example.sharespace.core.ui.components.StyledButton
import com.example.sharespace.core.ui.theme.AquaAccent
import com.example.sharespace.core.ui.theme.BackgroundAccent
import com.example.sharespace.core.ui.theme.BorderPrimary
import com.example.sharespace.core.ui.theme.TextSecondary

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Date: ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, // Explicitly use M3 MaterialTheme
            modifier = Modifier.weight(1f)
        )

        // Use your StyledButton for the "Change" button
        StyledButton(
            onClick = { showDatePicker = true },
            text = "Change",
            icon = Icons.Default.DateRange,
            buttonType = ButtonType.Secondary // Using secondary type as requested
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                .toEpochMilli()
        )

        // Colors based on your StyledButton's Secondary type
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = BackgroundAccent, // Dialog background
            titleContentColor = AquaAccent, // Title text "Select Date"
            headlineContentColor = AquaAccent, // Large date display at the top
            weekdayContentColor = TextSecondary, // Text for Mon, Tue etc.
            subheadContentColor = AquaAccent, // Month/year switcher text

            yearContentColor = TextSecondary, // Default year color
            currentYearContentColor = AquaAccent, // Current year in selection
            selectedYearContentColor = BackgroundAccent, // Text on selected year
            selectedYearContainerColor = AquaAccent, // Background of selected year

            dayContentColor = TextSecondary, // Default day number color
            disabledDayContentColor = TextSecondary.copy(alpha = 0.5f),
            selectedDayContentColor = BackgroundAccent, // Text color on the selected day (e.g., white/light if AquaAccent is dark)
            selectedDayContainerColor = AquaAccent, // Background of the selected day (the greenish circle)

            disabledSelectedDayContentColor = TextSecondary.copy(alpha = 0.7f),
            disabledSelectedDayContainerColor = BorderPrimary.copy(alpha = 0.5f),

            todayContentColor = AquaAccent, // Color for today's date (if not selected)
            todayDateBorderColor = BorderPrimary, // Border for today's date (like your button's border)

            dayInSelectionRangeContentColor = BackgroundAccent, // If using date range
            dayInSelectionRangeContainerColor = AquaAccent.copy(alpha = 0.3f) // If using date range
        )

        // Confirm/Cancel button text color should also align
        val dialogButtonContentColor = AquaAccent

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate =
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onDateSelected(localDate)
                    }
                    showDatePicker = false
                },
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = dialogButtonContentColor)
            ) {
                Text("OK")
            }
        }, dismissButton = {
            TextButton(
                onClick = { showDatePicker = false },
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = dialogButtonContentColor)
            ) {
                Text("Cancel")
            }
        }, colors = datePickerColors // Apply to Dialog
        ) {
            DatePicker(
                state = datePickerState, colors = datePickerColors // Apply to DatePicker itself
            )
        }
    }
}
