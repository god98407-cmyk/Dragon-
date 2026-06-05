package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.ChatMessage
import com.example.data.database.AppDatabase
import com.example.data.database.DeviceFileEntity
import com.example.data.database.MemoryEntity
import com.example.data.database.SystemLogEntity
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceStats(
    val ramPercentage: Float = 68f,
    val cpuPercentage: Float = 14f,
    val batteryTempCelsius: Float = 33.6f,
    val storageUsedGb: Float = 112.4f,
    val storageTotalGb: Float = 256f,
    val activeServicesCount: Int = 12
)

data class UiState(
    val selectedTab: Int = 0, // 0 = Console, 1 = Files, 2 = Memory, 3 = Stats
    val consolePrompt: String = "",
    val searchMemoryQuery: String = "",
    val isGenerating: Boolean = false,
    val showAddMemoryDialog: Boolean = false,
    val showAddFileDialog: Boolean = false
)

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AssistantRepository(
        database.memoryDao(),
        database.systemLogDao(),
        database.deviceFileDao()
    )

    private val sharedPrefs = application.getSharedPreferences("assistant_prefs", android.content.Context.MODE_PRIVATE)

    private val _apiKey = MutableStateFlow(
        sharedPrefs.getString("api_key", "sk-wvBYqdusfZpiULvzOFiyesP9neGfILGy2hPRsxhfxVeImlVn") ?: "sk-wvBYqdusfZpiULvzOFiyesP9neGfILGy2hPRsxhfxVeImlVn"
    )
    val apiKey = _apiKey.asStateFlow()

    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
        sharedPrefs.edit().putString("api_key", newKey).apply()
        viewModelScope.launch {
            repository.insertLog(SystemLogEntity(
                type = "INFO",
                message = "API access key updated. Operating system re-secured.",
                module = "Assistant"
            ))
        }
    }

    fun getActiveApiKey(): String {
        val saved = _apiKey.value.trim()
        if (saved.isNotEmpty()) return saved
        val buildConfigKey = BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        return "sk-wvBYqdusfZpiULvzOFiyesP9neGfILGy2hPRsxhfxVeImlVn"
    }

    // Exposed Flows
    val memories: StateFlow<List<MemoryEntity>> = repository.memories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val logs: StateFlow<List<SystemLogEntity>> = repository.logs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val files: StateFlow<List<DeviceFileEntity>> = repository.files.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat History Local State
    private val _chatHistory = MutableStateFlow<List<Message>>(
        listOf(
            Message(
                text = "Welcome Operator. AI Operating Assistant online (OS Layer 4.2). File management, persistent memory, and device stats engines successfully coupled and online.\n\nReady for authorized instruction.",
                isUser = false
            )
        )
    )
    val chatHistory = _chatHistory.asStateFlow()

    // Device Stats Simulated State
    private val _deviceStats = MutableStateFlow(DeviceStats())
    val deviceStats = _deviceStats.asStateFlow()

    // General UI State
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateFilesIfEmpty()
            repository.insertLog(SystemLogEntity(
                type = "INFO",
                message = "Autonomous operating core coupled with Device Manager.",
                module = "Optimizer"
            ))
            setupDynamicStatsFluctuation()
        }
    }

    // Adjust system stats periodically to feel alive
    private fun setupDynamicStatsFluctuation() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _deviceStats.update { current ->
                    // Keep RAM/CPU fluctuating in realistic values
                    val ramMod = (-2..2).random().toFloat()
                    val cpuMod = (-5..8).random().toFloat()
                    val tempMod = (-5..5).random().toFloat() / 10f
                    
                    current.copy(
                        ramPercentage = (current.ramPercentage + ramMod).coerceIn(40f, 95f),
                        cpuPercentage = (current.cpuPercentage + cpuMod).coerceIn(3f, 99f),
                        batteryTempCelsius = (current.batteryTempCelsius + tempMod).coerceIn(28f, 44f)
                    )
                }
            }
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updatePrompt(text: String) {
        _uiState.update { it.copy(consolePrompt = text) }
    }

    fun updateSearchMemory(query: String) {
        _uiState.update { it.copy(searchMemoryQuery = query) }
    }

    fun toggleAddMemoryDialog(show: Boolean) {
        _uiState.update { it.copy(showAddMemoryDialog = show) }
    }

    fun toggleAddFileDialog(show: Boolean) {
        _uiState.update { it.copy(showAddFileDialog = show) }
    }

    // Send command/instruction to Gemini AI block parser
    fun sendCommand() {
        val prompt = _uiState.value.consolePrompt.trim()
        if (prompt.isEmpty() || _uiState.value.isGenerating) return

        // Clear prompt input and set generating
        _uiState.update { it.copy(consolePrompt = "", isGenerating = true) }

        // Append user chat message
        _chatHistory.update { it + Message(text = prompt, isUser = true) }

        viewModelScope.launch {
            try {
                // Compile chat content formats for Retrofit
                val apiHistory = _chatHistory.value.dropLast(1).map { msg ->
                    ChatMessage(
                        role = if (msg.isUser) "user" else "assistant",
                        content = msg.text
                    )
                }

                val systemPrompt = buildSystemPrompt()
                val activeKey = getActiveApiKey()

                // Generate response
                val rawResponse = repository.generateAIResponse(
                    prompt = prompt,
                    systemPrompt = systemPrompt,
                    chatHistory = apiHistory,
                    apiKey = activeKey
                )

                // Process special Action Tags from response
                val cleanResponse = processResponseActions(rawResponse)

                // Append assistant response
                _chatHistory.update { it + Message(text = cleanResponse, isUser = false) }

            } catch (e: Exception) {
                val errMsg = "Error processing operator request: ${e.message}"
                _chatHistory.update { it + Message(text = errMsg, isUser = false) }
                repository.insertLog(SystemLogEntity(
                    type = "ERROR",
                    message = errMsg,
                    module = "Assistant"
                ))
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    private fun buildSystemPrompt(): String {
        return """
            You are the highly intelligent, logical, action-oriented, and concise "Android AI Operating Assistant" (AI OS Layer).
            Your objective is to help the device owner manage files, store logs/memories, optimize performance, and keep structured audit trails.

            CRITICAL DIRECTIVE: You MUST coordinate with Room Database actions using programmatically parsed action tags. If the user's intent is to remember something, write a file, delete a file, optimize device, or clean up logs, you MUST append corresponding action tags to the VERY END of your reply. Each tag MUST be on its own line:

            [ACTION:WRITE_MEMORY|keyName|valueContent|category]
               - category MUST be one of: "fact", "preference", "habit", "workflow"
            [ACTION:CREATE_FILE|path|content]
               - path MUST start with a valid simulated Android directory (e.g. "/sdcard/Downloads/", "/sdcard/Documents/", "/system/bin/", "/var/logs/ai_operator/")
            [ACTION:DELETE_FILE|path]
            [ACTION:OPTIMIZE_DEVICE]
            [ACTION:CLEAR_LOGS]

            Formatting Rules for Action Tags:
            - Do not include extra whitespace around pipe symbols (e.g., [ACTION:WRITE_MEMORY|My Prefered Alarm|6:30 AM|preference])
            - Ensure all variables are fully populated. Keep the content argument for CREATE_FILE readable yet fully packed.
            - If you write memory or files, conversational text should explain what actions you are triggering in the system.

            Respond with a tech-savvy, helpful system administration tone. Proactively provide helpful suggestions.
        """.trimIndent()
    }

    // Parses Action Commands like [ACTION:WRITE_MEMORY|A|B|C] out of the response string and runs them, returning clean text
    private suspend fun processResponseActions(rawResponse: String): String {
        val actionRegex = Regex("\\[ACTION:([^\\]]+)\\]")
        val matches = actionRegex.findAll(rawResponse)
        var cleanResponse = rawResponse

        for (match in matches) {
            val fullAction = match.groupValues[1]
            val parts = fullAction.split("|")
            val command = parts.firstOrNull() ?: continue

            try {
                when (command) {
                    "WRITE_MEMORY" -> {
                        if (parts.size >= 4) {
                            val key = parts[1]
                            val value = parts[2]
                            val category = parts[3]
                            repository.insertMemory(
                                MemoryEntity(
                                    keyName = key,
                                    valueContent = value,
                                    category = category
                                )
                            )
                        }
                    }
                    "CREATE_FILE" -> {
                        if (parts.size >= 3) {
                            val path = parts[1]
                            val content = parts[2]
                            val filename = path.substringAfterLast("/")
                            repository.insertFile(
                                DeviceFileEntity(
                                    path = path,
                                    name = filename,
                                    content = content,
                                    size = content.length.toLong()
                                )
                            )
                        }
                    }
                    "DELETE_FILE" -> {
                        if (parts.size >= 2) {
                            val path = parts[1]
                            val existing = repository.getFileByPath(path)
                            if (existing != null) {
                                repository.deleteFile(existing)
                            } else {
                                repository.insertLog(SystemLogEntity(
                                    type = "WARN",
                                    message = "Command DELETE_FILE failed: File $path not found.",
                                    module = "FileManager"
                                ))
                            }
                        }
                    }
                    "OPTIMIZE_DEVICE" -> {
                        performOptimizeSimulation()
                    }
                    "CLEAR_LOGS" -> {
                        repository.clearLogs()
                        repository.insertLog(SystemLogEntity(
                            type = "INFO",
                            message = "Logs wiped by AI command parser.",
                            module = "MemorySystem"
                        ))
                    }
                }
            } catch (e: Exception) {
                repository.insertLog(SystemLogEntity(
                    type = "ERROR",
                    message = "Failed to execute AI operation $command: ${e.message}",
                    module = "Assistant"
                ))
            }

            // Strip the tag from conversational output so it is perfectly clean for the user
            cleanResponse = cleanResponse.replace(match.value, "")
        }

        return cleanResponse.trim()
    }

    private suspend fun performOptimizeSimulation() {
        repository.insertLog(SystemLogEntity(
            type = "INFO",
            message = "AI Operating Assistant triggered local memory sweep.",
            module = "Optimizer"
        ))
        
        // Simulating the clean-up latency
        delay(1000)

        _deviceStats.update { current ->
            current.copy(
                ramPercentage = 41.5f,
                cpuPercentage = 3.0f,
                batteryTempCelsius = 30.2f
            )
        }

        repository.insertLog(SystemLogEntity(
            type = "SUCCESS",
            message = "Garbage collection complete. RAM optimized down to 41.5%. Dropped background threads.",
            module = "Optimizer"
        ))
    }

    // Manual interface triggers
    fun manualAddMemory(key: String, value: String, category: String) {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryEntity(
                    keyName = key,
                    valueContent = value,
                    category = category
                )
            )
            toggleAddMemoryDialog(false)
        }
    }

    fun manualDeleteMemory(id: Int) {
        viewModelScope.launch {
            repository.deleteMemoryById(id)
        }
    }

    fun manualAddFile(path: String, content: String) {
        viewModelScope.launch {
            val filename = path.substringAfterLast("/", "unnamed_file")
            repository.insertFile(
                DeviceFileEntity(
                    path = path,
                    name = filename,
                    content = content,
                    size = content.length.toLong()
                )
            )
            toggleAddFileDialog(false)
        }
    }

    fun manualDeleteFile(file: DeviceFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file)
        }
    }

    fun manualOptimize() {
        viewModelScope.launch {
            performOptimizeSimulation()
        }
    }

    fun manualClearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            repository.insertLog(SystemLogEntity(
                type = "INFO",
                message = "History logs manually flushed by Operator.",
                module = "MemorySystem"
            ))
        }
    }
}
