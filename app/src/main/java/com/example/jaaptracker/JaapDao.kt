package com.example.jaaptracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JaapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileRaw(profile: Profile)

    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfilesRaw(): List<Profile>

    @Delete
    suspend fun deleteProfile(profile: Profile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JaapLog)

    @Update
    suspend fun updateLog(log: JaapLog)

    @Delete
    suspend fun deleteLog(log: JaapLog)

    @Query("SELECT * FROM jaap_logs WHERE profileId = :profileId AND date = :date LIMIT 1")
    suspend fun getLogForDate(profileId: Long, date: LocalDate): JaapLog?

    @Query("SELECT * FROM jaap_logs WHERE profileId = :profileId ORDER BY date DESC")
    fun getLogsForProfile(profileId: Long): Flow<List<JaapLog>>

    @Query("SELECT SUM(count) FROM jaap_logs WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getSumForDateRange(profileId: Long, startDate: LocalDate, endDate: LocalDate): Int?

    @Query("SELECT * FROM jaap_logs")
    suspend fun getAllLogsRaw(): List<JaapLog>

    @Query("DELETE FROM profiles")
    suspend fun clearAllProfiles()

    @Query("DELETE FROM jaap_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM sqlite_sequence WHERE name='profiles' OR name='jaap_logs'")
    suspend fun resetSequences()
}