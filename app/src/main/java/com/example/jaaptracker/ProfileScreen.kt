// In ProfileScreen.kt
package com.example.jaaptracker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jaap Profiles") },
                actions = {
                    val context = LocalContext.current
                    val email = "Vansh.Sharma.professional@gmail.com"

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Developer: Vansh Sharma",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "mail: click here",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Email Address", email)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        // --- THIS BOX ALLOWS LAYERING THE BACKGROUND TEXT ---
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- 1. THE BACKGROUND TEXT LAYER ---
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Liked the app?",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
                )
                Text(
                    text = "Then do 1001 jaap extra for a week",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
                )
                Text(
                    text = "Om Namoh Narayana, Jai Jai Shri Sita Ram🙏🏽🙏🏻",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.3f)
                )
            }

            // --- 2. THE MAIN CONTENT LAYER (ON TOP) ---
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("New Profile Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.addProfile(newProfileName)
                            newProfileName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Profile", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD PROFILE")
                }
                Spacer(modifier = Modifier.height(24.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(profiles) { profile ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onProfileClick(profile.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowForward, contentDescription = "View History")
                            }
                        }
                    }
                }
            }
        }
    }
}