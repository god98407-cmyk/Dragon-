package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.DeviceFileEntity
import com.example.data.database.MemoryEntity
import com.example.data.database.SystemLogEntity
import com.example.ui.theme.CyberAccent
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTertiary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.DeviceStats
import com.example.ui.viewmodel.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainDashboardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val deviceStats by viewModel.deviceStats.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val files by viewModel.files.collectAsState()

    val scope = rememberCoroutineScope()
    var blinkingDotState by remember { mutableStateOf(true) }

    // Fluctuating cyber terminal online pulse
    LaunchedEffect(Unit) {
        while (true) {
            blinkingDotState = !blinkingDotState
            delay(1200)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Flashing neon cyber heart beat
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (blinkingDotState) CyberTertiary else CyberTertiary.copy(
                                        alpha = 0.3f
                                    )
                                )
                        )
                        Column {
                            Text(
                                text = "OPERATOR CORE v4.2",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "AI OS TUNNEL SECURED // SHIELDS UP",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.manualOptimize() },
                        modifier = Modifier.testTag("action_optimize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = CyberTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberDarkBg,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("app_navigation_bar"),
                containerColor = CyberDarkBg,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Console") },
                    label = { Text("Console", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberSecondary.copy(alpha = 0.3f),
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_console_tab")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Files") },
                    label = { Text("Files", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberSecondary.copy(alpha = 0.3f),
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_files_tab")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Memory") },
                    label = { Text("Memory", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberSecondary.copy(alpha = 0.3f),
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_memory_tab")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Stats & Logs") },
                    label = { Text("Stats", fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberSecondary.copy(alpha = 0.3f),
                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                        unselectedTextColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_stats_tab")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CyberDarkBg, Color(0xFF0C0C0C))
                    )
                )
        ) {
            // Main views with smooth animations
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> ConsoleView(viewModel, chatHistory, uiState.isGenerating, uiState.consolePrompt)
                    1 -> FilesView(viewModel, files, uiState.showAddFileDialog)
                    2 -> MemoryView(viewModel, memories, uiState.searchMemoryQuery, uiState.showAddMemoryDialog)
                    3 -> StatsAndLogsView(viewModel, deviceStats, logs)
                }
            }
        }
    }
}

// ======================== CONSOLE VIEW ========================
@Composable
fun ConsoleView(
    viewModel: AssistantViewModel,
    chatHistory: List<Message>,
    isGenerating: Boolean,
    consolePrompt: String
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val apiKey by viewModel.apiKey.collectAsState()
    val activeKey = viewModel.getActiveApiKey()
    var showEditKey by remember { mutableStateOf(activeKey.isEmpty()) }
    var keyInputTemp by remember { mutableStateOf(apiKey) }

    // Keep temp input in sync if actual key changes
    LaunchedEffect(apiKey) {
        keyInputTemp = apiKey
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Holographic API Key Coupling UI
        if (showEditKey || activeKey.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurface)
                    .border(
                        1.dp,
                        if (activeKey.isEmpty()) Color.Red.copy(alpha = 0.5f) else CyberPrimary.copy(alpha = 0.4f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeKey.isEmpty()) "⚠️ CENTRAL KEY COUPLING REQUIRED" else "🔒 SECURE KEY COUPLING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeKey.isEmpty()) Color.Red else CyberPrimary
                    )
                    if (activeKey.isNotEmpty()) {
                        IconButton(
                            onClick = { showEditKey = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provide your Bluesminds API Key to activate the operating core workflow automation engine and long-term memory systems.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = keyInputTemp,
                        onValueChange = { keyInputTemp = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("api_key_input_field"),
                        placeholder = {
                            Text("API_KEY (e.g. bm-xxxx...)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f))
                        },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPrimary,
                            unfocusedBorderColor = CyberPrimary.copy(alpha = 0.2f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.4f)
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.updateApiKey(keyInputTemp.trim())
                            showEditKey = false
                        },
                        modifier = Modifier.testTag("api_key_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeKey.isEmpty()) CyberTertiary else CyberPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "BOOT ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1524))
                    .border(1.dp, CyberTertiary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .clickable { showEditKey = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberTertiary)
                    )
                    Text(
                        text = "CORE ENCRYPTED // ACTIVE // NO_PROXY",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "[RE-COUPLE KEY]",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Chat Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(CyberSurface)
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(chatHistory) { message ->
                    val isAssistant = !message.isUser
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isAssistant) Color(0xFF131A2D) else CyberSecondary.copy(
                                        alpha = 0.4f
                                    )
                                )
                                .border(
                                    1.dp,
                                    if (isAssistant) CyberPrimary.copy(alpha = 0.3f) else CyberSecondary.copy(
                                        alpha = 0.7f
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAssistant) "▲ SYSTEM_AGENT" else "▼ USER_OPERATOR",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAssistant) CyberPrimary else Color.White
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp)),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.text,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color.White,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
                
                if (isGenerating) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = CyberPrimary
                            )
                            Text(
                                text = "CORE REASONING ENGINE ACTIVE...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Suggested Action Chips
        val suggestedPrompts = listOf(
            "Write a notes file for workout at /sdcard/Documents/gym.txt",
            "Optimize storage and RAM state now",
            "Log user coffee habits: black coffee, 2 cups before noon",
            "Wipe previous log systems and wipe storage"
        )

        Text(
            text = "DELEGATE OPERATOR WORKFLOWS:",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            suggestedPrompts.take(2).forEach { command ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberSurface)
                        .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable { viewModel.updatePrompt(command) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = command,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Input Field Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = consolePrompt,
                onValueChange = { viewModel.updatePrompt(it) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("console_prompt_input"),
                placeholder = {
                    Text(
                        "Command: 'Write workout logs' or 'Remember my focus...'",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    viewModel.sendCommand()
                    keyboardController?.hide()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberPrimary,
                    unfocusedBorderColor = CyberPrimary.copy(alpha = 0.3f),
                    focusedContainerColor = CyberSurface,
                    unfocusedContainerColor = CyberSurface
                )
            )

            Button(
                onClick = {
                    viewModel.sendCommand()
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .height(56.dp)
                    .testTag("console_send_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Execute")
            }
        }
    }
}

// ======================== FILES VIEW ========================
@Composable
fun FilesView(
    viewModel: AssistantViewModel,
    files: List<DeviceFileEntity>,
    showDialog: Boolean
) {
    var filePath by remember { mutableStateOf("/sdcard/Documents/notes.txt") }
    var fileContent by remember { mutableStateOf("System audit notes:\n- No issues reported.\n- Operator active.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SIMULATED ROOT VOLUME",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${files.size} active records secured",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Button(
                onClick = { viewModel.toggleAddFileDialog(true) },
                modifier = Modifier.testTag("add_file_trigger_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add File", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("CREATE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .background(CyberSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty",
                        tint = CyberPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No simulated files present.",
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Use the command bar to ask AI to write documentation reports.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .testTag("file_card_${file.id}"),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.path,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Size: ${formatSize(file.size)} // Last updated: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified))}",
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.manualDeleteFile(file) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("delete_file_${file.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CyberAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF070B11))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = file.content,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { viewModel.toggleAddFileDialog(false) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, CyberPrimary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "[CMD] NEW RECORD WRITER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberPrimary
                    )

                    OutlinedTextField(
                        value = filePath,
                        onValueChange = { filePath = it },
                        label = { Text("Absolute Target Path", fontFamily = FontFamily.Monospace) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth().testTag("add_file_path_input")
                    )

                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        label = { Text("Payload String Contents", fontFamily = FontFamily.Monospace) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("add_file_content_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.toggleAddFileDialog(false) },
                            modifier = Modifier.testTag("add_file_cancel")
                        ) {
                            Text("ABORT", color = Color.White.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.manualAddFile(filePath, fileContent) },
                            modifier = Modifier.testTag("add_file_confirm"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black)
                        ) {
                            Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1048576 -> String.format("%.1f MB", bytes.toFloat() / 1048576f)
        bytes >= 1024 -> String.format("%.1f KB", bytes.toFloat() / 1024f)
        else -> "$bytes Bytes"
    }
}

// ======================== MEMORY VIEW ========================
@Composable
fun MemoryView(
    viewModel: AssistantViewModel,
    memories: List<MemoryEntity>,
    searchQuery: String,
    showDialog: Boolean
) {
    var memoryKey by remember { mutableStateOf("User Language Preference") }
    var memoryValue by remember { mutableStateOf("German (De-de)") }
    var memoryCategory by remember { mutableStateOf("preference") }

    val categories = listOf("preference", "habit", "workflow", "fact")

    // Filtered memories on client
    val filteredMemories = memories.filter {
        it.keyName.contains(searchQuery, ignoreCase = true) ||
                it.valueContent.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PERSISTENT MEMORY DATABASE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${memories.size} cognitive nodes online",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Button(
                onClick = { viewModel.toggleAddMemoryDialog(true) },
                modifier = Modifier.testTag("add_memory_trigger_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RECORD", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchMemory(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("memory_search_input"),
            placeholder = { Text("Query memory index (e.g. coffee, workflow)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.3f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberPrimary) },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.White),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPrimary,
                unfocusedBorderColor = CyberPrimary.copy(alpha = 0.3f),
                focusedContainerColor = CyberSurface,
                unfocusedContainerColor = CyberSurface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredMemories.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .background(CyberSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = CyberPrimary.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                    Text("Memory index empty.", fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                    Text("Tell the operating engine to remember details.", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMemories) { node ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .testTag("memory_card_${node.id}"),
                        colors = CardDefaults.cardColors(containerColor = CyberSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                               ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (node.category.lowercase()) {
                                                    "preference" -> CyberSecondary.copy(alpha = 0.3f)
                                                    "habit" -> CyberTertiary.copy(alpha = 0.3f)
                                                    "workflow" -> CyberAccent.copy(alpha = 0.3f)
                                                    else -> CyberPrimary.copy(alpha = 0.3f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = node.category.uppercase(),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = node.keyName,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = node.valueContent,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.manualDeleteMemory(node.id) },
                                modifier = Modifier.testTag("delete_memory_${node.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Memory",
                                    tint = CyberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = { viewModel.toggleAddMemoryDialog(false) }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, CyberPrimary, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "[CMD] MEMORY REGISTER MODULE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberPrimary
                    )

                    OutlinedTextField(
                        value = memoryKey,
                        onValueChange = { memoryKey = it },
                        label = { Text("Memory Anchor (Key)", fontFamily = FontFamily.Monospace) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth().testTag("add_memory_key_input")
                    )

                    OutlinedTextField(
                        value = memoryValue,
                        onValueChange = { memoryValue = it },
                        label = { Text("Payload Details (Value)", fontFamily = FontFamily.Monospace) },
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth().testTag("add_memory_value_input")
                    )

                    Column {
                        Text("Category Tag", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val selected = memoryCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) CyberPrimary else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (selected) CyberPrimary else Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .clickable { memoryCategory = cat }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (selected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.toggleAddMemoryDialog(false) },
                            modifier = Modifier.testTag("add_memory_cancel")
                        ) {
                            Text("ABORT", color = Color.White.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.manualAddMemory(memoryKey, memoryValue, memoryCategory) },
                            modifier = Modifier.testTag("add_memory_confirm"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = Color.Black)
                        ) {
                            Text("COMMITTED", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ======================== STATS & AUDIT LOGS VIEW ========================
@Composable
fun StatsAndLogsView(
    viewModel: AssistantViewModel,
    stats: DeviceStats,
    logs: List<SystemLogEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "DEVICE RESOURCE METRICS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // circular stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // RAM Progress
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ACTIVE RAM", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                        CircularProgressIndicator(
                            progress = stats.ramPercentage / 100f,
                            modifier = Modifier.fillMaxSize(),
                            color = if (stats.ramPercentage > 85f) CyberAccent else CyberPrimary,
                            trackColor = Color.White.copy(alpha = 0.05f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${stats.ramPercentage.toInt()}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Text("LOAD INJECTORS", fontSize = 10.sp, color = CyberTertiary, fontFamily = FontFamily.Monospace)
                }
            }

            // CPU Progress
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSurface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("CPU THREADS", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                        CircularProgressIndicator(
                            progress = stats.cpuPercentage / 100f,
                            modifier = Modifier.fillMaxSize(),
                            color = if (stats.cpuPercentage > 75f) CyberAccent else CyberSecondary,
                            trackColor = Color.White.copy(alpha = 0.05f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "${stats.cpuPercentage.toInt()}%",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Text("SCHEDULER UP", fontSize = 10.sp, color = CyberTertiary, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Linear meters card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Temperature
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BATTERY CORE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("${String.format("%.1f", stats.batteryTempCelsius)}°C", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberPrimary)
                }
                LinearProgressIndicator(
                    progress = (stats.batteryTempCelsius - 15f) / 45f,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (stats.batteryTempCelsius > 39f) CyberAccent else CyberPrimary,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )

                // Storage utilization
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("STORAGE SPACE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("${stats.storageUsedGb}GB / ${stats.storageTotalGb}GB", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberPrimary)
                }
                LinearProgressIndicator(
                    progress = stats.storageUsedGb / stats.storageTotalGb,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = CyberSecondary,
                    trackColor = Color.White.copy(alpha = 0.05f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions & Optimization Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REAL-TIME DIAGNOSTIC AUDIT Logs",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace
            )
            TextButton(
                onClick = { viewModel.manualClearLogs() },
                modifier = Modifier.testTag("clear_logs_button"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("WIPE LOGS", color = CyberAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logs terminal scrolling
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .background(Color(0xFF04060A))
                .padding(10.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Awaiting operation logs...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(logs) { log ->
                        val timeStr = SimpleDateFormat("HH:mm:ss.S", Locale.getDefault()).format(Date(log.timestamp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "[$timeStr] ${log.module.uppercase()} // ${log.type}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (log.type) {
                                        "SUCCESS" -> CyberTertiary
                                        "WARN" -> CyberAccent
                                        "ERROR" -> Color.Red
                                        "API" -> CyberSecondary
                                        else -> CyberPrimary
                                    }
                                )
                            }
                            Text(
                                text = log.message,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
