package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.ChatMessage
import com.example.data.api.ChatCompletionRequest
import com.example.data.api.RetrofitClient
import com.example.data.database.DeviceFileDao
import com.example.data.database.DeviceFileEntity
import com.example.data.database.MemoryDao
import com.example.data.database.MemoryEntity
import com.example.data.database.SystemLogDao
import com.example.data.database.SystemLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AssistantRepository(
    private val memoryDao: MemoryDao,
    private val systemLogDao: SystemLogDao,
    private val deviceFileDao: DeviceFileDao
) {
    val memories: Flow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()
    val logs: Flow<List<SystemLogEntity>> = systemLogDao.getAllLogsFlow()
    val files: Flow<List<DeviceFileEntity>> = deviceFileDao.getAllFilesFlow()

    suspend fun insertMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        memoryDao.insertMemory(memory)
        insertLog(SystemLogEntity(
            type = "SUCCESS",
            message = "Memory saved: ${memory.keyName} -> ${memory.valueContent.take(30)}...",
            module = "MemorySystem"
        ))
    }

    suspend fun deleteMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemory(memory)
        insertLog(SystemLogEntity(
            type = "INFO",
            message = "Memory deleted: ${memory.keyName}",
            module = "MemorySystem"
        ))
    }

    suspend fun deleteMemoryById(id: Int) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemoryById(id)
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> = withContext(Dispatchers.IO) {
        memoryDao.searchMemories(query)
    }

    suspend fun insertLog(log: SystemLogEntity) = withContext(Dispatchers.IO) {
        systemLogDao.insertLog(log)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        systemLogDao.clearAllLogs()
    }

    suspend fun getRecentLogs(limit: Int): List<SystemLogEntity> = withContext(Dispatchers.IO) {
        systemLogDao.getRecentLogs(limit)
    }

    suspend fun insertFile(file: DeviceFileEntity) = withContext(Dispatchers.IO) {
        deviceFileDao.insertFile(file)
        insertLog(SystemLogEntity(
            type = "SUCCESS",
            message = "Simulated file created/updated: ${file.path}",
            module = "FileManager"
        ))
    }

    suspend fun deleteFile(file: DeviceFileEntity) = withContext(Dispatchers.IO) {
        deviceFileDao.deleteFile(file)
        insertLog(SystemLogEntity(
            type = "WARN",
            message = "Simulated file deleted: ${file.path}",
            module = "FileManager"
        ))
    }

    suspend fun getFileByPath(path: String): DeviceFileEntity? = withContext(Dispatchers.IO) {
        deviceFileDao.getFileByPath(path)
    }

    suspend fun clearAllFiles() = withContext(Dispatchers.IO) {
        deviceFileDao.clearAllFiles()
    }

    suspend fun prepopulateFilesIfEmpty() = withContext(Dispatchers.IO) {
        val count = getFileCount()
        if (count == 0) {
            val defaults = listOf(
                DeviceFileEntity(
                    path = "/sdcard/Downloads/daily_backup.zip",
                    name = "daily_backup.zip",
                    content = "[ZIP Bundle] Contains database snapshots and script logs from autonomous backups.",
                    size = 1048576 * 23 // 23MB
                ),
                DeviceFileEntity(
                    path = "/sdcard/Documents/user_habits.txt",
                    name = "user_habits.txt",
                    content = "Preferred alarm: 06:30 AM\nGym sessions: Mon/Wed/Fri 07:00 AM\nCoffee intake: Max 2 cups before 2 PM.",
                    size = 142
                ),
                DeviceFileEntity(
                    path = "/system/bin/optimize_ram.sh",
                    name = "optimize_ram.sh",
                    content = "#!/system/bin/sh\necho 'Optimizing system cache...'\nsync\necho 3 > /proc/sys/vm/drop_caches\necho 'RAM optimization successful.'",
                    size = 156
                ),
                DeviceFileEntity(
                    path = "/var/logs/ai_operator/session_logs.json",
                    name = "session_logs.json",
                    content = "{\"session_started\": 1780642861, \"authorized\": true, \"actions_automated\": 42}",
                    size = 83
                ),
                DeviceFileEntity(
                    path = "/sdcard/Pictures/workout_schedule.png",
                    name = "workout_schedule.png",
                    content = "[PNG IMAGE DATA] Custom weekly exercise template.",
                    size = 1048576 * 2 // 2MB
                )
            )
            for (f in defaults) {
                deviceFileDao.insertFile(f)
            }
            insertLog(SystemLogEntity(
                type = "INFO",
                message = "Simulated root volume initialized with standard Android paths.",
                module = "FileManager"
            ))
        }
    }

    private suspend fun getFileCount(): Int {
        // Simple count helper
        return try {
            val list = mutableListOf<DeviceFileEntity>()
            deviceFileDao.getAllFilesFlow().collect {
                list.addAll(it)
                return@collect
            }
            list.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun generateAIResponse(
        prompt: String,
        systemPrompt: String,
        chatHistory: List<ChatMessage> = emptyList(),
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty()) {
            val errMsg = "API operating core offline. Paste your API_KEY to enable automated workflows."
            insertLog(SystemLogEntity(
                type = "ERROR",
                message = errMsg,
                module = "Assistant"
            ))
            return@withContext errMsg
        }

        // Construct complete OpenAI compatible chat messages payload
        val messagesList = mutableListOf<ChatMessage>()
        if (systemPrompt.isNotEmpty()) {
            messagesList.add(ChatMessage(role = "system", content = systemPrompt))
        }
        messagesList.addAll(chatHistory)
        messagesList.add(ChatMessage(role = "user", content = prompt))

        val request = ChatCompletionRequest(
            model = "gemini-3-flash-preview",
            messages = messagesList,
            temperature = 0.3f
        )

        try {
            insertLog(SystemLogEntity(
                type = "API",
                message = "Initiating core action call to gemini-3-flash-preview. Prompt: \"${prompt.take(45)}...\"",
                module = "Assistant"
            ))

            val authHeader = "Bearer $apiKey"
            val response = RetrofitClient.service.generateContent(authHeader, request)
            val resultText = response.choices?.firstOrNull()?.message?.content
                ?: "Empty response from operating engine."

            insertLog(SystemLogEntity(
                type = "SUCCESS",
                message = "Action reasoning complete. Response length: ${resultText.length} chars.",
                module = "Assistant"
            ))

            resultText
        } catch (e: Exception) {
            val errorDetails = e.localizedMessage ?: e.message ?: "Unknown API connection error"
            Log.e("AssistantRepository", "API Error: $errorDetails", e)
            insertLog(SystemLogEntity(
                type = "ERROR",
                message = "API request failed: $errorDetails",
                module = "Assistant"
            ))
            "Execution Error: $errorDetails\n\nEnsure INTERNET is accessible and your API_KEY is valid."
        }
    }
}
