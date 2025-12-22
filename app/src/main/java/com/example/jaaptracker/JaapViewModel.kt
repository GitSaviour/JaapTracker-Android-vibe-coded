package com.example.jaaptracker

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class JaapViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.jaapDao()

    val allProfiles: StateFlow<List<Profile>> = dao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentProfileId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val logsForSelectedProfile: StateFlow<List<JaapLog>> = _currentProfileId
        .filterNotNull()
        .flatMapLatest { id -> dao.getLogsForProfile(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _totalInRange = MutableStateFlow<Int?>(null)
    val totalInRange: StateFlow<Int?> = _totalInRange.asStateFlow()

    fun loadLogsForProfile(profileId: Long) {
        _currentProfileId.value = profileId
        _totalInRange.value = null
    }

    fun addProfile(name: String) = viewModelScope.launch { dao.insertProfile(Profile(name = name)) }
    fun deleteProfile(profile: Profile) = viewModelScope.launch { dao.deleteProfile(profile) }

    fun addJaapCount(profileId: Long, count: Int, date: LocalDate) = viewModelScope.launch {
        val existing = dao.getLogForDate(profileId, date)
        if (existing != null) {
            dao.updateLog(existing.copy(count = existing.count + count))
        } else {
            dao.insertLog(JaapLog(profileId = profileId, date = date, count = count))
        }
    }

    fun calculateSumForDateRange(profileId: Long, start: LocalDate, end: LocalDate) =
        viewModelScope.launch { _totalInRange.value = dao.getSumForDateRange(profileId, start, end) ?: 0 }

    fun editJaapCount(log: JaapLog, count: Int) = viewModelScope.launch { dao.updateLog(log.copy(count = count)) }
    fun deleteLog(log: JaapLog) = viewModelScope.launch { dao.deleteLog(log) }

    fun exportData(context: android.content.Context, uri: Uri) = viewModelScope.launch {
        try {
            val profiles = dao.getAllProfilesRaw()
            val logs = dao.getAllLogsRaw()
            if (profiles.isEmpty()) return@launch

            val json = Gson().toJson(BackupData(profiles, logs))
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            Toast.makeText(context, "Export Successful!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export Failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun importData(context: android.content.Context, uri: Uri) = viewModelScope.launch {
        try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            val backup = Gson().fromJson(json, BackupData::class.java) ?: return@launch

            db.withTransaction {
                dao.clearAllLogs()
                dao.clearAllProfiles()
                dao.resetSequences()

                var pIdCounter = 1L
                var lIdCounter = 1L

                backup.profiles.forEach { oldP ->
                    val newPId = pIdCounter++
                    dao.insertProfileRaw(Profile(id = newPId, name = oldP.name))

                    backup.logs.filter { it.profileId == oldP.id }.forEach { oldL ->
                        dao.insertLog(JaapLog(
                            id = lIdCounter++,
                            profileId = newPId,
                            date = oldL.date,
                            count = oldL.count
                        ))
                    }
                }
            }
            Toast.makeText(context, "Data Injected Successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Import Failed", Toast.LENGTH_SHORT).show()
        }
    }
}