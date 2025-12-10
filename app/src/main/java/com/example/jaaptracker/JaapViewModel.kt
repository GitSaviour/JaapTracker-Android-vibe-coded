// In JaapViewModel.kt
package com.example.jaaptracker

import android.app.Application
import androidx.lifecycle.*
import androidx.room.Room
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class JaapViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "jaaptracker-db"
    ).build()

    private val dao = db.jaapDao()

    val allProfiles: StateFlow<List<Profile>> = dao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _logsForSelectedProfile = MutableStateFlow<List<JaapLog>>(emptyList())
    val logsForSelectedProfile: StateFlow<List<JaapLog>> = _logsForSelectedProfile.asStateFlow()

    // --- NEW STATE FOR DATE RANGE ---
    private val _totalInRange = MutableStateFlow<Int?>(null)
    val totalInRange: StateFlow<Int?> = _totalInRange.asStateFlow()

    fun addProfile(name: String) {
        viewModelScope.launch { dao.insertProfile(Profile(name = name)) }
    }

    fun loadLogsForProfile(profileId: Long) {
        // Reset the date range total when loading a new profile
        _totalInRange.value = null
        viewModelScope.launch {
            dao.getLogsForProfile(profileId).collect { logs ->
                _logsForSelectedProfile.value = logs
            }
        }
    }

    fun addJaapCount(profileId: Long, count: Int, date: LocalDate) {
        viewModelScope.launch {
            val existingLog = dao.getLogForDate(profileId, date)
            if (existingLog != null) {
                val updatedLog = existingLog.copy(count = existingLog.count + count)
                dao.updateLog(updatedLog)
            } else {
                val newLog = JaapLog(profileId = profileId, date = date, count = count)
                dao.insertLog(newLog)
            }
        }
    }

    // --- NEW FUNCTION TO CALCULATE THE SUM ---
    fun calculateSumForDateRange(profileId: Long, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            val sum = dao.getSumForDateRange(profileId, startDate, endDate)
            _totalInRange.value = sum ?: 0 // If null (no entries), show 0
        }
    }
}