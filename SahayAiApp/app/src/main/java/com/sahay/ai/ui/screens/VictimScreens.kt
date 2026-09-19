package com.sahay.ai.ui.screens

import com.sahay.ai.ui.Localization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.sahay.ai.data.CaseStatus
import com.sahay.ai.data.CheckIn
import com.sahay.ai.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VictimDashboardScreen(navController: NavController, viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(Localization.getString("HOME", viewModel.selectedLanguage)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("SAHAY-AI", viewModel.selectedLanguage), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Box(modifier = Modifier.padding(start=16.dp, end=8.dp).size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, tint = MaterialTheme.colorScheme.onPrimaryContainer, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(Localization.getString("HOME", viewModel.selectedLanguage), style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == Localization.getString("HOME", viewModel.selectedLanguage),
                    onClick = { selectedTab = Localization.getString("HOME", viewModel.selectedLanguage) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ChatBubble, contentDescription = null) },
                    label = { Text(Localization.getString("CHAT", viewModel.selectedLanguage), style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == Localization.getString("CHAT", viewModel.selectedLanguage),
                    onClick = { selectedTab = Localization.getString("CHAT", viewModel.selectedLanguage) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AutoGraph, contentDescription = null) },
                    label = { Text(Localization.getString("JOURNEY", viewModel.selectedLanguage), style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == Localization.getString("JOURNEY", viewModel.selectedLanguage),
                    onClick = { selectedTab = Localization.getString("JOURNEY", viewModel.selectedLanguage) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.SupportAgent, contentDescription = null) },
                    label = { Text(Localization.getString("SUPPORT", viewModel.selectedLanguage), style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == Localization.getString("SUPPORT", viewModel.selectedLanguage),
                    onClick = { selectedTab = Localization.getString("SUPPORT", viewModel.selectedLanguage) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(Localization.getString("PROFILE", viewModel.selectedLanguage), style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == Localization.getString("PROFILE", viewModel.selectedLanguage),
                    onClick = { selectedTab = Localization.getString("PROFILE", viewModel.selectedLanguage) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                Localization.getString("HOME", viewModel.selectedLanguage) -> VictimHomeTab(viewModel, onNavigateToChat = { selectedTab = Localization.getString("CHAT", viewModel.selectedLanguage) })
                Localization.getString("CHAT", viewModel.selectedLanguage) -> VictimChatTab(viewModel)
                Localization.getString("JOURNEY", viewModel.selectedLanguage) -> VictimJourneyTab(viewModel)
                Localization.getString("SUPPORT", viewModel.selectedLanguage) -> VictimSupportTab(viewModel)
                Localization.getString("PROFILE", viewModel.selectedLanguage) -> VictimProfileTab(viewModel, navController)
            }
        }
    }
}

@Composable
fun VictimHomeTab(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    val profile by viewModel.currentProfile.collectAsState()
    var showCheckInDialog by remember { mutableStateOf(false) }
    var showScoreDetailsDialog by remember { mutableStateOf(false) }
    val cases by viewModel.cases.collectAsState()
    val checkIns = profile?.caseId?.let { cases[it]?.checkIns } ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp).verticalScroll(rememberScrollState())) {
        Text(Localization.getString("Good evening.", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(Localization.getString("How are you doing today?", viewModel.selectedLanguage), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(24.dp))
        
        // Today's Check-in Card (Stitch Exact Match)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text(Localization.getString("TODAY'S CHECK-IN", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineMedium.copy(fontSize=20.sp), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text(Localization.getString("Take a moment to tell us how you are feeling.", viewModel.selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showCheckInDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Filled.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(Localization.getString("Start Check-in", viewModel.selectedLanguage), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text(Localization.getString("OTHER WAYS TO CONNECT", viewModel.selectedLanguage), style = MaterialTheme.typography.labelLarge.copy(fontSize=12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start=4.dp))
        Spacer(Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            // VOICE Intent Setup
            val voiceLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val data = result.data
                    val spokenText = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
                    if (!spokenText.isNullOrEmpty()) {
                        onNavigateToChat()
                        viewModel.sendChatMessage(spokenText, com.sahay.ai.data.InputMode.VOICE)
                    }
                }
            }
            
            ConnectionMethodCard(Localization.getString("CHAT", viewModel.selectedLanguage), Localization.getString("Talk to SAHAY", viewModel.selectedLanguage), Icons.Filled.Chat, Modifier.weight(1f)) {
                onNavigateToChat()
            }
            ConnectionMethodCard(Localization.getString("VOICE", viewModel.selectedLanguage), Localization.getString("Speak instead", viewModel.selectedLanguage), Icons.Filled.Mic, Modifier.weight(1f)) {
                try {
                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        val lang = when(viewModel.selectedLanguage) {
                            "Hindi" -> "hi-IN"
                            "Tamil" -> "ta-IN"
                            "Malayalam" -> "ml-IN"
                            "Telugu" -> "te-IN"
                            else -> "en-US"
                        }
                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, lang)
                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                    }
                    voiceLauncher.launch(intent)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Voice recognition not supported on this device.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            ConnectionMethodCard(Localization.getString("CALL", viewModel.selectedLanguage), Localization.getString("Scheduled Call", viewModel.selectedLanguage), Icons.Filled.Call, Modifier.weight(1f)) {
                val trustedNumber = profile?.trustedPersonMobile
                if (!trustedNumber.isNullOrEmpty()) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:$trustedNumber")
                    }
                    context.startActivity(intent)
                } else {
                    android.widget.Toast.makeText(context, "No trusted person number has been added. You can add one from your profile.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BentoCard(Localization.getString("Last Check-in", viewModel.selectedLanguage), "history", if (checkIns.isNotEmpty()) Localization.getString("Today", viewModel.selectedLanguage) else Localization.getString("No Data", viewModel.selectedLanguage), Modifier.weight(1f))
            
            val scoreText = if (checkIns.isEmpty()) Localization.getString("Insufficient data", viewModel.selectedLanguage) else "${profile?.distressScore ?: 0}/100"
            BentoCard(Localization.getString("Distress Score", viewModel.selectedLanguage), "circle", scoreText, Modifier.weight(1f).clickable { if (checkIns.isNotEmpty()) showScoreDetailsDialog = true }, iconColor = if(checkIns.isEmpty() || (profile?.distressScore ?: 0) < 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
        }
        
        Spacer(Modifier.height(24.dp))
        
        DistressTrendChart(
            checkIns = checkIns, 
            viewModel = viewModel, 
            onClick = { showScoreDetailsDialog = true }
        )
        
        Spacer(Modifier.height(24.dp))
    }

    if (showCheckInDialog) {
        CheckInDialog(viewModel = viewModel, onDismiss = { showCheckInDialog = false })
    }
    
    if (showScoreDetailsDialog) {
        ScoreDetailsDialog(checkIn = checkIns.lastOrNull(), viewModel = viewModel, onDismiss = { showScoreDetailsDialog = false })
    }
}

@Composable
fun ConnectionMethodCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFEBEFEE)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun BentoCard(label: String, iconType: String, value: String, modifier: Modifier = Modifier, iconColor: Color = MaterialTheme.colorScheme.outline) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if(iconType == "history") {
                    Icon(Icons.Filled.History, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                } else {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(iconColor))
                }
                Spacer(Modifier.width(8.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VictimChatTab(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val input by viewModel.chatInput.collectAsState()
    var isSending by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    
    DisposableEffect(context) {
        val ttsInstance = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                // Ensure layout doesn't crash on init
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }
    
    LaunchedEffect(messages) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg != null && lastMsg.sender == "AI Support" && lastMsg.inputMode == com.sahay.ai.data.InputMode.VOICE) {
            val text = lastMsg.message
            if (!text.startsWith("System Error:") && !text.startsWith("App Runtime Error:") && text != "Your session has expired. Please log in again.") {
                tts?.let { 
                    val loc = when (viewModel.selectedLanguage) {
                        "Hindi" -> java.util.Locale("hi", "IN")
                        "Tamil" -> java.util.Locale("ta", "IN")
                        "Malayalam" -> java.util.Locale("ml", "IN")
                        "Telugu" -> java.util.Locale("te", "IN")
                        else -> java.util.Locale.US
                    }
                    val result = it.setLanguage(loc)
                    if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                        it.language = java.util.Locale.getDefault()
                    }
                    it.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "AI_REPLY")
                }
            }
        }
    }
    
    // Auto-scroll logic could be added, but standard reverseLayout handles bottom snapping mostly.
    
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), reverseLayout = true) {
            items(messages.reversed()) { msg ->
                val isUser = msg.sender == "You"
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Box(modifier = Modifier.background(if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Text(msg.message, color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        
        if (isSending) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.updateChatInput(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(Localization.getString("Message SAHAY-AI...", viewModel.selectedLanguage)) },
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                enabled = !isSending
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isSending) Color.Gray else MaterialTheme.colorScheme.primary).clickable(enabled = !isSending) { 
                if(input.isNotBlank()){ 
                    val userText = input
                    viewModel.updateChatInput("")
                    isSending = true
                    viewModel.sendChatMessage(userText) { 
                        isSending = false 
                    }
                } 
            }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun VictimJourneyTab(viewModel: MainViewModel) {
    val profile = viewModel.currentProfile.collectAsState().value
    val caseData = profile?.caseId?.let { viewModel.cases.value[it] }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Localization.getString("Your Legal Journey", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Localization.getString("Case Status", viewModel.selectedLanguage), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(Localization.getString(caseData?.status?.display ?: "Unknown", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                
                Text(Localization.getString("Timeline", viewModel.selectedLanguage), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                caseData?.history?.forEach { action ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.padding(top=4.dp).size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(16.dp))
                        Text(Localization.getString(action, viewModel.selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun VictimSupportTab(viewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(Localization.getString("Support Services", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth().clickable { }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(Localization.getString("Emergency SOS", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineMedium.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(Localization.getString("Trigger immediate official response", viewModel.selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(Localization.getString("Trusted Contacts", viewModel.selectedLanguage), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Text("KS", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Kiran Sharma", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun VictimProfileTab(viewModel: MainViewModel, navController: NavController) {
    val profile = viewModel.currentProfile.collectAsState().value
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(Localization.getString("Profile & Settings", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(profile?.name ?: "Unknown", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(profile?.caseId ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Button(
            onClick = { viewModel.logout(); navController.navigate("role_selection") { popUpTo(0) } },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(Localization.getString("Secure Sign Out", viewModel.selectedLanguage))
        }
    }
}

@Composable
fun CheckInDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val profile = viewModel.currentProfile.collectAsState().value
    var checkInText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(Localization.getString("Wellbeing Check-in", viewModel.selectedLanguage)) },
        text = { 
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(Localization.getString("Take a moment to tell us how you are feeling.", viewModel.selectedLanguage))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = checkInText,
                    onValueChange = { checkInText = it },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(Localization.getString("Message SAHAY-AI...", viewModel.selectedLanguage)) }
                )
                if (isSubmitting) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } 
        },
        confirmButton = {
            Button(
                onClick = {
                    if (checkInText.isNotBlank()) {
                        isSubmitting = true
                        viewModel.processCheckInAnalysis(checkInText) {
                            isSubmitting = false
                            onDismiss()
                        }
                    }
                },
                enabled = !isSubmitting && checkInText.isNotBlank()
            ) { Text(Localization.getString("Submit Securely", viewModel.selectedLanguage)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) { Text(Localization.getString("Cancel", viewModel.selectedLanguage)) }
        }
    )
}

@Composable
fun DistressTrendChart(checkIns: List<CheckIn>, viewModel: MainViewModel, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(Localization.getString("Distress Trend", viewModel.selectedLanguage), style = MaterialTheme.typography.headlineMedium.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(Localization.getString("Your distress level across recent check-ins", viewModel.selectedLanguage), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            if (checkIns.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(Localization.getString("Insufficient data", viewModel.selectedLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (checkIns.size == 1) {
                val current = checkIns.first().score
                val riskLevel = com.sahay.ai.data.getDistressLevel(current).name.lowercase().replaceFirstChar { it.titlecase() }
                Text("${current}/100", style = MaterialTheme.typography.headlineMedium, color = if(current < 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                Text("${Localization.getString("Risk Level", viewModel.selectedLanguage)}: ${Localization.getString(riskLevel, viewModel.selectedLanguage)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text(Localization.getString("Complete more check-ins to see your distress trend.", viewModel.selectedLanguage), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                val current = checkIns.last().score
                val prev = checkIns[checkIns.size - 2].score
                val riskLevel = com.sahay.ai.data.getDistressLevel(current).name.lowercase().replaceFirstChar { it.titlecase() }
                val trendString = if(current > prev) "Increasing" else if (current < prev) "Improving" else "Stable"
                val trendArrow = if(current > prev) "↑ " else if (current < prev) "↓ " else "→ "
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${current}/100", style = MaterialTheme.typography.headlineMedium, color = if(current < 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                        Text("${Localization.getString("Risk Level", viewModel.selectedLanguage)}: ${Localization.getString(riskLevel, viewModel.selectedLanguage)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(Localization.getString("Trend", viewModel.selectedLanguage) + ":", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(trendArrow + Localization.getString(trendString, viewModel.selectedLanguage), style = MaterialTheme.typography.bodyLarge, color = if(current > prev) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                val points = checkIns.map { it.score.toFloat() }
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    val maxScore = 100f
                    val stepX = size.width / (points.size - 1).coerceAtLeast(1).toFloat()
                    val scaleY = size.height / maxScore
                    
                    val path = androidx.compose.ui.graphics.Path()
                    points.forEachIndexed { index, score ->
                        val x = index * stepX
                        val y = size.height - (score * scaleY)
                        if(index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        
                        drawCircle(color = Color(0xFF4CAF50), radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                    }
                    
                    drawPath(path, color = Color(0xFF4CAF50), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                }
            }
        }
    }
}

@Composable
fun ScoreDetailsDialog(checkIn: CheckIn?, viewModel: MainViewModel, onDismiss: () -> Unit) {
    if (checkIn == null) {
        onDismiss(); return
    }
    val riskLevel = checkIn.answers["risk_level"] ?: "Low"
    val concernCategory = checkIn.answers["concern_category"]?.split(",")?.joinToString(", ") ?: "None"
    
    val emotional = checkIn.answers["emotional"] ?: "0"
    val fear = checkIn.answers["fear"] ?: "0"
    val safety = checkIn.answers["safety"] ?: "0"
    val engagement = checkIn.answers["engagement"] ?: "0"
    val change = checkIn.answers["change"] ?: "0"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Localization.getString("How your score was calculated", viewModel.selectedLanguage)) },
        text = { 
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("${Localization.getString("Distress Score", viewModel.selectedLanguage)}: ${checkIn.score}/100", style = MaterialTheme.typography.headlineSmall)
                Text("${Localization.getString("Risk Level", viewModel.selectedLanguage)}: ${Localization.getString(riskLevel, viewModel.selectedLanguage)}", color = if(checkIn.score < 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                
                Text(Localization.getString("Contributing indicators:", viewModel.selectedLanguage), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("• ${Localization.getString("Emotional distress", viewModel.selectedLanguage)}\t\t $emotional/40")
                Text("• ${Localization.getString("Fear/anxiety", viewModel.selectedLanguage)}\t\t $fear/25")
                Text("• ${Localization.getString("Safety indicators", viewModel.selectedLanguage)}\t\t $safety/15")
                Text("• ${Localization.getString("Engagement", viewModel.selectedLanguage)}\t\t $engagement/10")
                Text("• ${Localization.getString("Change over time", viewModel.selectedLanguage)}\t\t $change/10")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("• ${Localization.getString("Total", viewModel.selectedLanguage)}\t\t ${checkIn.score}/100", fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(16.dp))
                
                Text(Localization.getString("Potential concerns:", viewModel.selectedLanguage), fontWeight = FontWeight.Bold)
                Text("✓ $concernCategory")
                
                Spacer(Modifier.height(16.dp))
                
                Text(Localization.getString("This is an AI-generated wellbeing indicator and NOT a medical diagnosis.", viewModel.selectedLanguage), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
