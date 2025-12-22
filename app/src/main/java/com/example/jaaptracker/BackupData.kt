package com.example.jaaptracker

// This is a "container" that holds everything for the transfer
data class BackupData(
    val profiles: List<Profile>,
    val logs: List<JaapLog>
)