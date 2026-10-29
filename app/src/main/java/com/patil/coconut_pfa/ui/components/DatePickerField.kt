package com.patil.coconut_pfa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patil.coconut_pfa.util.KolkataDateTimeUtils

/**
 * Reusable Date Field component that presents a formatted date string and opens a
 * Material3 DatePickerDialog on click for interactive calendar date selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = "Pick Date",
                    modifier = Modifier.clickable(enabled = enabled) {
                        showDatePickerDialog = true
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        // Overlay transparent clickable surface to intercept taps on entire text field
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) {
                    showDatePickerDialog = true
                }
        )
    }

    if (showDatePickerDialog) {
        val initialMillis = remember(value) {
            try {
                if (value.isNotBlank()) KolkataDateTimeUtils.dateStringToEpochMillis(value)
                else KolkataDateTimeUtils.currentEpochMillis()
            } catch (e: Exception) {
                KolkataDateTimeUtils.currentEpochMillis()
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDateStr = KolkataDateTimeUtils.epochMillisToDateString(millis)
                            onDateSelected(selectedDateStr)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
