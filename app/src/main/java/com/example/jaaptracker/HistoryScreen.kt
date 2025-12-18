// In HistoryScreen.kt
package com.example.jaaptracker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.* // This imports remember, mutableStateOf, etc.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    // Correctly spelled with 'remember' and 'mutableStateOf'
    var newCount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedLogToEdit by remember { mutableStateOf<JaapLog?>(null) }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Jaap History") }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
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

                    if (totalInRange != null && startDate != null && endDate != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Total in selected range:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondary)
                        Text(totalInRange.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                    Text("Daily Log History", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Daily Log History List (NOW CLICKABLE) ---
                items(logs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { // <-- This makes the card clickable
                                selectedLogToEdit = log
                                showEditDialog = true
                            },
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
                    if (startDate != null && endDate != null) {
                        viewModel.calculateSumForDateRange(profileId, startDate!!, endDate!!)
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // --- NEW DIALOG FOR EDITING A LOG ---
    if (showEditDialog && selectedLogToEdit != null) {
        EditLogDialog(
            log = selectedLogToEdit!!,
            onDismiss = {
                showEditDialog = false
                selectedLogToEdit = null
            },
            onSave = { log, newCount ->
                viewModel.editJaapCount(log, newCount)
                showEditDialog = false
                selectedLogToEdit = null
            }
        )
    }
}

// --- NEW COMPOSABLE FUNCTION FOR THE DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLogDialog(
    log: JaapLog,
    onDismiss: () -> Unit,
    onSave: (JaapLog, Int) -> Unit
) {
    var newCountText by remember { mutableStateOf(log.count.toString()) }
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Edit Jaap Count",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Date: ${log.date.format(formatter)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newCountText,
                    onValueChange = { newCountText = it.filter { c -> c.isDigit() } },
                    label = { Text("New Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val newCount = newCountText.toIntOrNull()
                        if (newCount != null) {
                            onSave(log, newCount)
                        }
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper function to convert Long from DatePicker to LocalDate
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
}