package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INFO", "SUCCESS", "WARN", "API", "ERROR"
    val message: String,
    val module: String, // "FileManager", "Optimizer", "Assistant", "MemorySystem"
    val timestamp: Long = System.currentTimeMillis()
)
