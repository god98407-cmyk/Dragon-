package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyName: String,
    val valueContent: String,
    val category: String, // "preference", "habit", "workflow", "fact"
    val timestamp: Long = System.currentTimeMillis()
)
