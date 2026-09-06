package com.sahay.ai.ui.screens

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
    var selectedTab by remember { mutableStateOf("HOME") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SAHAY-AI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
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
                    label = { Text("HOME", style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == "HOME",
                    onClick = { selectedTab = "HOME" },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ChatBubble, contentDescription = null) },
                    label = { Text("CHAT", style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == "CHAT",
                    onClick = { selectedTab = "CHAT" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AutoGraph, contentDescription = null) },
                    label = { Text("JOURNEY", style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == "JOURNEY",
                    onClick = { selectedTab = "JOURNEY" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.SupportAgent, contentDescription = null) },
                    label = { Text("SUPPORT", style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == "SUPPORT",
                    onClick = { selectedTab = "SUPPORT" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("PROFILE", style = MaterialTheme.typography.labelMedium) },
                    selected = selectedTab == "PROFILE",
                    onClick = { selectedTab = "PROFILE" }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                "HOME" -> VictimHomeTab(viewModel, onNavigateToChat = { selectedTab = "CHAT" })
                "CHAT" -> VictimChatTab(viewModel)
                "JOURNEY" -> VictimJourneyTab(viewModel)
                "SUPPORT" -> VictimSupportTab(viewModel)
                "PROFILE" -> VictimProfileTab(viewModel, navController)
            }
        }
    }
}

@Composable
fun VictimHomeTab(viewModel: MainViewModel, onNavigateToChat: () -> Unit) {
    val profile by viewModel.currentProfile.collectAsState()
    var showCheckInDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp).verticalScroll(rememberScrollState())) {
        Text("Good evening.", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Text("How are you doing today?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
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
                        Text("TODAY'S CHECK-IN", style = MaterialTheme.typography.headlineMedium.copy(fontSize=20.sp), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text("Take a moment to tell us how you are feeling.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Start Check-in", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Text("OTHER WAYS TO CONNECT", style = MaterialTheme.typography.labelLarge.copy(fontSize=12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start=4.dp))
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
                        viewModel.updateChatInput(spokenText)
                        onNavigateToChat()
                    }
                }
            }
            
            ConnectionMethodCard("CHAT", "Talk to SAHAY", Icons.Filled.Chat, Modifier.weight(1f)) {
                onNavigateToChat()
            }
            ConnectionMethodCard("VOICE", "Speak instead", Icons.Filled.Mic, Modifier.weight(1f)) {
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
            ConnectionMethodCard("CALL", "Scheduled Call", Icons.Filled.Call, Modifier.weight(1f)) {
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
            BentoCard("Last Check-in", "history", "3 days ago", Modifier.weight(1f))
            BentoCard("Current Wellbeing", "circle", if((profile?.distressScore ?: 0) < 50) "Stable" else "Supported", Modifier.weight(1f), iconColor = if((profile?.distressScore ?: 0) < 50) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
        }
    }

    if (showCheckInDialog) {
        CheckInDialog(viewModel = viewModel, onDismiss = { showCheckInDialog = false })
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
        if (lastMsg != null && lastMsg.first == "AI Support") {
            val text = lastMsg.second
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
                val isUser = msg.first == "You"
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Box(modifier = Modifier.background(if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Text(msg.second, color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
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
                placeholder = { Text("Message SAHAY-AI...") },
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
        Text("Your Legal Journey", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Case Status", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(caseData?.status?.display ?: "Unknown", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                
                Text("Timeline", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                caseData?.history?.forEach { action ->
                    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(modifier = Modifier.padding(top=4.dp).size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        Spacer(Modifier.width(16.dp))
                        Text(action, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun VictimSupportTab(viewModel: MainViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Support Services", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth().clickable { }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Call, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Emergency SOS", style = MaterialTheme.typography.headlineMedium.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("Trigger immediate official response", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Trusted Contacts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text("Profile & Settings", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
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
            Text("Secure Sign Out")
        }
    }
}

@Composable
fun CheckInDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val profile = viewModel.currentProfile.collectAsState().value
    var scoreStr by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wellbeing Check-in") },
        text = { 
            Column {
                Text("On a scale from 0 to 100, where 100 corresponds to extreme distress, how do you feel today?")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scoreStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) scoreStr = it },
                    singleLine = true,
                    label = { Text("Score (0-100)") }
                )
            } 
        },
        confirmButton = {
            Button(onClick = {
                val score = scoreStr.toIntOrNull() ?: return@Button
                if(score in 0..100) {
                    profile?.caseId?.let { id ->
                        viewModel.submitCheckIn(id, mapOf("distress" to scoreStr), score)
                    }
                    onDismiss()
                }
            }) { Text("Submit Securely") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
