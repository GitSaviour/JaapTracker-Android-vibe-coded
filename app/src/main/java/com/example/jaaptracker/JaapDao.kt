// In JaapDao.kt
package com.example.jaaptracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JaapDao {
    // Profile operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<Profile>>

    // Log operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: JaapLog)

    @Update
    suspend fun updateLog(log: JaapLog)

    @Query("SELECT * FROM jaap_logs WHERE profileId = :profileId AND date = :date LIMIT 1")
    suspend fun getLogForDate(profileId: Long, date: LocalDate): JaapLog?

    @Query("SELECT * FROM jaap_logs WHERE profileId = :profileId ORDER BY date DESC")
    fun getLogsForProfile(profileId: Long): Flow<List<JaapLog>>

    // --- NEW FUNCTION ---
    // This efficiently calculates the sum directly in the database
    @Query("SELECT SUM(count) FROM jaap_logs WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getSumForDateRange(profileId: Long, startDate: LocalDate, endDate: LocalDate): Int?
}