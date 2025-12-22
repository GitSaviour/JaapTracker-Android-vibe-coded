package com.example.jaaptracker

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    LaunchedEffect(profileId) { viewModel.loadLogsForProfile(profileId) }

    val logs by viewModel.logsForSelectedProfile.collectAsState()
    val totalInRange by viewModel.totalInRange.collectAsState()
    val context = LocalContext.current

    var newCount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedLogToEdit by remember { mutableStateOf<JaapLog?>(null) }
    var showLogDatePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Jaap History") }) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Jaap Count (All Time)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondary)
                    Text(logs.sumOf { it.count }.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = newCount,
                        onValueChange = { newCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Jaap Count to Add") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            if(newCount.isNotBlank()) showLogDatePicker = true
                            else Toast.makeText(context, "Enter count first!", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.weight(1f)) { Text("LOG AT DATE") }

                        Button(onClick = {
                            val count = newCount.toIntOrNull()
                            if (count != null && count > 0) {
                                viewModel.addJaapCount(profileId, count, LocalDate.now())
                                newCount = ""
                            }
                        }, modifier = Modifier.weight(1f)) { Text("LOG TODAY") }
                    }
                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                }

                item {
                    Text("Calculate Total for Date Range", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(startDate?.format(formatter) ?: "Start Date")
                        }
                        Button(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.DateRange, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(endDate?.format(formatter) ?: "End Date")
                        }
                    }
                    if (totalInRange != null && startDate != null && endDate != null) {
                        Spacer(Modifier.height(16.dp))
                        Text("Total: $totalInRange", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                    Text("Daily Log History", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(logs) { log ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { selectedLogToEdit = log; showEditDialog = true }) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(log.date.format(formatter), fontSize = 16.sp)
                            Text(log.count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // --- 1. Edit Log Dialog ---
    if (showEditDialog && selectedLogToEdit != null) {
        EditLogDialog(
            log = selectedLogToEdit!!,
            onDismiss = { showEditDialog = false; selectedLogToEdit = null },
            onSave = { log, count -> viewModel.editJaapCount(log, count); showEditDialog = false },
            onDelete = { log -> viewModel.deleteLog(log); showEditDialog = false }
        )
    }

    // --- 2. Start Date Picker ---
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis?.toLocalDate()
                    if (endDate != null && startDate != null) viewModel.calculateSumForDateRange(profileId, startDate!!, endDate!!)
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // --- 3. End Date Picker ---
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = datePickerState.selectedDateMillis?.toLocalDate()
                    if (startDate != null && endDate != null) viewModel.calculateSumForDateRange(profileId, startDate!!, endDate!!)
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // --- 4. Log At Date Picker (THE MISSING ONE) ---
    if (showLogDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showLogDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.toLocalDate()
                    val count = newCount.toIntOrNull()
                    if (date != null && count != null) {
                        viewModel.addJaapCount(profileId, count, date)
                        newCount = ""
                        showLogDatePicker = false
                    }
                }) { Text("Log This") }
            },
            dismissButton = { TextButton(onClick = { showLogDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLogDialog(log: JaapLog, onDismiss: () -> Unit, onSave: (JaapLog, Int) -> Unit, onDelete: (JaapLog) -> Unit) {
    var text by remember { mutableStateOf(log.count.toString()) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Entry", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDelete(log) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = text, onValueChange = { text = it.filter { c -> c.isDigit() } }, label = { Text("Count") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { text.toIntOrNull()?.let { onSave(log, it) } }) { Text("Save") }
                }
            }
        }
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()