package com.example.jaaptracker

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "jaap_logs",
    indices = [Index("profileId")] // Keep the index for speed
)
data class JaapLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long, // This now acts as a simple number, not a "strict link"
    val date: LocalDate,
    val count: Int
)