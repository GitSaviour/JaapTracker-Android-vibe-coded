package com.example.jaaptracker

import android.content.*
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: JaapViewModel, onProfileClick: (Long) -> Unit) {
    val profiles by viewModel.allProfiles.collectAsState()
    var newProfileName by remember { mutableStateOf("") }
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { viewModel.exportData(context, it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importData(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaap Profiles") },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }) {
                        Icon(Icons.Default.Download, "Import", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { exportLauncher.launch("jaap_backup.json") }) {
                        Icon(Icons.Default.Upload, "Export", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(8.dp))
                    val email = "Vansh.Sharma.professional@gmail.com"
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                        Text(text = "Developer: Vansh Sharma", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
                        Text(
                            text = "mail: click here",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Email", email))
                                Toast.makeText(context, "Email copied", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$email") })
                            }
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Liked the app?", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f))
                Text(text = "do 1001 japs extra for a week", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f))
                Text(text = "Om namoh Narayana, Jai Sita Ram🙏🏽🙏🏻", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f), textAlign = TextAlign.Center)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(value = newProfileName, onValueChange = { newProfileName = it }, label = { Text("New Profile Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { if (newProfileName.isNotBlank()) { viewModel.addProfile(newProfileName); newProfileName = "" } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("ADD PROFILE")
                }
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles) { profile ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { onProfileClick(profile.id) }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = profile.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                IconButton(onClick = { profileToDelete = profile }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                }
                                Icon(Icons.Default.ArrowForward, null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (profileToDelete != null) {
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete Profile?") },
            text = { Text("This removes everything for ${profileToDelete?.name}.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteProfile(profileToDelete!!); profileToDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("DELETE") }
            },
            dismissButton = { TextButton(onClick = { profileToDelete = null }) { Text("CANCEL") } }
        )
    }
}