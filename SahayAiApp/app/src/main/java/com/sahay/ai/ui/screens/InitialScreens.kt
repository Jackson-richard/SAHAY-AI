package com.sahay.ai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sahay.ai.ui.MainViewModel
import java.util.Locale

@Composable
fun RoleSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("SAHAY-AI", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Public Trust Infrastructure", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(48.dp))
        Text("Select Portal Access", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp).clickable { navController.navigate("victim_language") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Victim/Citizen", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                    Text("Access your journey & support", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().height(100.dp).clickable { navController.navigate("official_login") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Official Portal", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp), color = MaterialTheme.colorScheme.onSurface)
                    Text("Manage active case queues", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun VictimLanguageScreen(navController: NavController, viewModel: MainViewModel) {
    val languages = listOf("English", "हिन्दी", "தமிழ்", "മലയാളം", "తెలుగు")
    var selected by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Preferred Language", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Select your communication language", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(32.dp))
        
        languages.forEach { lang ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { selected = lang }
                    .background(
                        if (selected == lang) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == lang,
                    onClick = { selected = lang },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.width(12.dp))
                Text(lang, style = MaterialTheme.typography.bodyLarge, color = if(selected == lang) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                viewModel.selectedLanguage = selected
                navController.navigate("victim_consent")
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun VictimConsentScreen(navController: NavController, viewModel: MainViewModel) {
    var checked by remember { mutableStateOf(false) }
    var showRejectMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Your choice. Your control.", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("SAHAY-AI uses your check-ins to understand changes in your wellbeing. Your information is protected and accessed only by authorized personnel.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Row {
                    Icon(Icons.Filled.Verified, contentDescription=null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CONSENT-BASED — You choose whether to participate.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Icon(Icons.Filled.Lock, contentDescription=null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("PRIVATE — Your information is protected.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    Icon(Icons.Filled.PeopleAlt, contentDescription=null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("HUMAN SUPPORT — AI signals are reviewed by authorized people.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { checked = !checked }) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Text("I understand and consent to wellbeing monitoring.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        
        if(showRejectMessage) {
            Spacer(Modifier.height(16.dp))
            Text("Wellbeing monitoring requires your consent to continue.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                viewModel.consentGiven = true
                navController.navigate("victim_details")
            },
            enabled = checked,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Give Consent")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { showRejectMessage = true },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Not Now")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VictimRegistrationScreen(navController: NavController, viewModel: MainViewModel) {
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var caseId by remember { mutableStateOf("") }
    var trustedPhone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Registration & Linkage", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Link your secure case record", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            placeholder = { Text("Enter your full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            placeholder = { Text("Enter your mobile number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = caseId,
            onValueChange = { caseId = it },
            label = { Text("Case ID") },
            placeholder = { Text("Enter your case ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = trustedPhone,
            onValueChange = { trustedPhone = it },
            label = { Text("Trusted Person’s Mobile Number") },
            placeholder = { Text("Enter trusted person’s number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text("You may provide a trusted person’s number for urgent support. This is optional.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp).fillMaxWidth())
        
        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        
        if (errorMsg.contains("already registered", ignoreCase = true)) {
            TextButton(
                onClick = {
                    val cleanCase = caseId.trim().uppercase(Locale.getDefault())
                    if (cleanCase.isEmpty() || mobileNumber.trim().isEmpty()) {
                        errorMsg = "Mobile Number and Case ID are required to recover session safely."
                        return@TextButton
                    }
                    isLoading = true
                    errorMsg = ""
                    viewModel.recoverSession(cleanCase, mobileNumber.trim()) { recRes ->
                        isLoading = false
                        if (recRes.first) {
                            navController.navigate("victim_confirmation") { popUpTo("role_selection") }
                        } else {
                            errorMsg = recRes.second
                        }
                    }
                },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text("Is this you? Log in securely", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val cleanCase = caseId.trim().uppercase(Locale.getDefault())
                if (fullName.trim().isEmpty() || mobileNumber.trim().isEmpty() || cleanCase.isEmpty()) {
                    errorMsg = "Please fill out Name, Mobile, and Case ID."
                    return@Button
                }
                if (!cleanCase.matches(Regex("^SHY-100[1-5]\$"))) {
                    errorMsg = "We could not find this case ID. Please check and try again."
                    return@Button
                }
                
                isLoading = true
                errorMsg = ""
                
                viewModel.validateCaseId(cleanCase) { existsRes ->
                    if (!existsRes.first) {
                        isLoading = false
                        errorMsg = existsRes.second
                    } else {
                        viewModel.submitVictimRegistration(
                            caseId = cleanCase,
                            name = fullName.trim(),
                            phone = mobileNumber.trim(),
                            trustedPhone = trustedPhone.trim()
                        ) { regRes ->
                            isLoading = false
                            if (regRes.first) {
                                android.util.Log.d("ApiDebug", "Navigaton Event: Navigate to victim_confirmation")
                                navController.navigate("victim_confirmation") { popUpTo("role_selection") }
                            } else {
                                android.util.Log.e("SahaySecurity", "Registration failed inside UI: ${regRes.second}")
                                errorMsg = regRes.second
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            else Text("Link Profile")
        }
    }
}

@Composable
fun VictimConfirmationScreen(navController: NavController, viewModel: MainViewModel) {
    val prof = viewModel.currentProfile.collectAsState().value
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("You’re all set", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Your details have been securely saved. You can now begin your wellbeing check-ins and access support.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(32.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Name: ${prof?.name ?: ""}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text("Case ID: ${prof?.caseId ?: ""}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text("Language: ${viewModel.selectedLanguage}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("victim_dashboard") { popUpTo("role_selection") } },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Go to My Dashboard")
        }
    }
}
