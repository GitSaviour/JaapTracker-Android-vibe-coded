// In HistoryScreen.kt
package com.example.jaaptracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: JaapViewModel, profileId: Long) {
    // Load logs for this profile when the screen appears
    LaunchedEffect(profileId) { viewModel.loadLogsForProfile(profileId) }

    val logs by viewModel.logsForSelectedProfile.collectAsState()
    val totalInRange by viewModel.totalInRange.collectAsState()
    var newCount by remember { mutableStateOf("") }

    // State for the date pickers
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Jaap History") }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            // This LazyColumn holds all content and makes it scrollable
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {

                // --- Top Section: Total & Adding Logs ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Jaap Count (All Time)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondary)
                    Text(logs.sumOf { it.count }.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newCount,
                        onValueChange = { newCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Add Today's Jaap Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val countToAdd = newCount.toIntOrNull()
                        if (countToAdd != null && countToAdd > 0) {
                            viewModel.addJaapCount(profileId, countToAdd, LocalDate.now())
                            newCount = ""
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("LOG COUNT FOR TODAY")
                    }
                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                }

                // --- Date Range Section ---
                item {
                    Text("Calculate Total for Date Range", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(startDate?.format(formatter) ?: "Start Date")
                        }
                        Button(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(endDate?.format(formatter) ?: "End Date")
                        }
                    }

                    // Show total when dates are selected
                    if (totalInRange != null && startDate != null && endDate != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Total in selected range:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondary)
                        Text(totalInRange.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                    Text("Daily Log History", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Daily Log History List ---
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(log.date.format(formatter), fontSize = 16.sp)
                            Text(
                                log.count.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Date Picker Dialogs ---
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis?.toLocalDate()
                    // Trigger calculation if both dates are now selected
                    if (endDate != null && startDate != null) {
                        viewModel.calculateSumForDateRange(profileId, startDate!!, endDate!!)
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = datePickerState.selectedDateMillis?.toLocalDate()
                    // Trigger calculation if both dates are now selected
                    if (startDate != null && endDate != null) {
                        viewModel.calculateSumForDateRange(profileId, startDate!!, endDate!!)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

// Helper function to convert Long from DatePicker to LocalDate
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
}