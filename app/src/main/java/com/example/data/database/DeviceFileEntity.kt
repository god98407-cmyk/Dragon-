package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_files")
data class DeviceFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String, // E.g., "/sdcard/Downloads/invoice.pdf"
    val name: String,
    val content: String,
    val size: Long,
    val isFolder: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
