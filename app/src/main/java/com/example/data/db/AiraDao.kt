package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AiraMemory
import com.example.data.model.AuditLog
import com.example.data.model.AutomationRoutine
import kotlinx.coroutines.flow.Flow

@Dao
interface AiraDao {
    // Memories
    @Query("SELECT * FROM aira_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<AiraMemory>>

    @Query("SELECT * FROM aira_memories WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoriesByCategory(category: String): Flow<List<AiraMemory>>

    @Query("SELECT * FROM aira_memories WHERE `key` = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): AiraMemory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AiraMemory): Long

    @Update
    suspend fun updateMemory(memory: AiraMemory)

    @Query("DELETE FROM aira_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM aira_memories")
    suspend fun clearAllMemories()

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogs(limit: Int = 100): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()

    // Routines
    @Query("SELECT * FROM automation_routines ORDER BY id ASC")
    fun getAllRoutines(): Flow<List<AutomationRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: AutomationRoutine): Long

    @Update
    suspend fun updateRoutine(routine: AutomationRoutine)

    @Query("UPDATE automation_routines SET isEnabled = :enabled WHERE id = :id")
    suspend fun setRoutineEnabled(id: Long, enabled: Boolean)
}
