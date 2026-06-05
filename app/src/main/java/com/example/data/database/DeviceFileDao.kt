package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceFileDao {
    @Query("SELECT * FROM device_files ORDER BY isFolder DESC, path ASC")
    fun getAllFilesFlow(): Flow<List<DeviceFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: DeviceFileEntity)

    @Query("INSERT OR REPLACE INTO device_files (path, name, content, size, isFolder, lastModified) VALUES (:path, :name, :content, :size, :isFolder, :lastModified)")
    suspend fun insertRawFile(path: String, name: String, content: String, size: Long, isFolder: Boolean, lastModified: Long)

    @Delete
    suspend fun deleteFile(file: DeviceFileEntity)

    @Query("DELETE FROM device_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)

    @Query("SELECT * FROM device_files WHERE path = :path LIMIT 1")
    suspend fun getFileByPath(path: String): DeviceFileEntity?

    @Query("DELETE FROM device_files")
    suspend fun clearAllFiles()
}
