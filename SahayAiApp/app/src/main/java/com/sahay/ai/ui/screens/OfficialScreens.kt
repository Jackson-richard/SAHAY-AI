package com.sahay.ai.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sahay.ai.data.CaseRecord
import com.sahay.ai.data.CaseStatus
import com.sahay.ai.ui.MainViewModel

@Composable
fun OfficialLoginScreen(navController: NavController, viewModel: MainViewModel) {
    // Basic standard layout for admin login since the primary directive targets the dashboard
    var email by remember { mutableStateOf("demo@sahay.gov.in") }
    var password by remember { mutableStateOf("admin123") }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("SAHAY-AI Officer Portal", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Govt Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        if (errorMsg.isNotEmpty()) Text(errorMsg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                viewModel.loginOfficial(email, password) { loginRes ->
                    if (loginRes.first) {
                        android.util.Log.d("ApiDebug", "Navigation Event: Navigate to official_dashboard")
                        navController.navigate("official_dashboard") { popUpTo("role_selection") }
                    } else {
                        android.util.Log.e("SahaySecurity", "Login failed inside UI: ${loginRes.second}")
                        errorMsg = loginRes.second
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Login to Dashboard")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialDashboardScreen(navController: NavController, viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf("OVERVIEW") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("SAHAY-AI", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                Text("GOVT", style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                },
                actions = {
                    Box(modifier = Modifier.padding(end=16.dp)) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        // Alert Badge
                        Box(
                            modifier = Modifier.offset(x = 8.dp, y = (-4).dp).size(16.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("3", color = MaterialTheme.colorScheme.onError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.9f))
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.SpaceDashboard, contentDescription = null) },
                    label = { Text("Overview") },
                    selected = selectedTab == "OVERVIEW",
                    onClick = { selectedTab = "OVERVIEW" },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.EmergencyShare, contentDescription = null) },
                    label = { Text("Flagged") },
                    selected = selectedTab == "FLAGGED",
                    onClick = { selectedTab = "FLAGGED" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AltRoute, contentDescription = null) },
                    label = { Text("Triage") },
                    selected = selectedTab == "TRIAGE",
                    onClick = { selectedTab = "TRIAGE" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
                    label = { Text("Journey") },
                    selected = selectedTab == "JOURNEY",
                    onClick = { selectedTab = "JOURNEY" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ManageAccounts, contentDescription = null) },
                    label = { Text("More") },
                    selected = selectedTab == "MORE",
                    onClick = { selectedTab = "MORE" }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                "OVERVIEW", "FLAGGED" -> AdminOverviewTab(viewModel, navController)
                "TRIAGE", "JOURNEY", "MORE" -> AdminQueueTab(viewModel, navController) // Simplified routing for demo
            }
        }
    }
}

@Composable
fun AdminOverviewTab(viewModel: MainViewModel, navController: NavController) {
    val cases by viewModel.cases.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Sync Banner
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("CCTNS & e-Courts Sync", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(6.dp))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                        }
                        Text("Live Gateway • Latency 280ms", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("SEC. 357A", style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Case Journey Lifecycle & Distress Stratification", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp), color = MaterialTheme.colorScheme.onSurface, lineHeight = 36.sp)
            Text("Track cohort distribution, average resolution velocity, and longitudinal mental wellbeing.", style = MaterialTheme.typography.bodyMedium.copy(fontSize=14.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top=4.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(top=12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick={}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cohort Filters")
                }
                Button(onClick={}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Export Audit Dossier")
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Legal Stages Horizontal Scroll
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Legal Stages Pipeline", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(6.dp))
                    Text("5 Milestones", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Swipe", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                StageCard("01", "Registration Completed", "182", "34.2", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                StageCardWarning("02", "Investigation Active", "314", "58.4")
                Spacer(Modifier.width(12.dp))
                StageCard("03", "Trial Pending", "226", "52.1", MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                StageCard("04", "Compensation Pending", "89", "41.6", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                StageCard("05", "Rehabilitation", "36", "28.5", MaterialTheme.colorScheme.primary)
            }
        }
        
        // Chart Card
        Spacer(Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stage Distress Velocity & Early Warning", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text("Longitudinal curve showing high distress surge during testimony stages", style = MaterialTheme.typography.labelMedium.copy(fontSize=12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(16.dp))
                // Mock Chart area
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val path = Path()
                        path.moveTo(0f, size.height * 0.7f)
                        path.lineTo(size.width * 0.25f, size.height * 0.2f)
                        path.lineTo(size.width * 0.5f, size.height * 0.35f)
                        path.lineTo(size.width * 0.75f, size.height * 0.5f)
                        path.lineTo(size.width, size.height * 0.8f)
                        drawPath(path, color = Color(0xFFBA1A1A), style = Stroke(width = 6f))
                        
                        drawCircle(color = Color(0xFFBA1A1A), radius = 12f, center = Offset(size.width * 0.25f, size.height * 0.2f))
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Active Cohort Roster list (Using our demo cases)
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text("Active Cohort Roster", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("Live surveillance of active beneficiaries", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            
            // Map demo cases dynamically
            cases.values.sortedByDescending { it.distressScore }.forEach { case ->
                CohortQueueCard(case, navController)
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun StageCard(number: String, title: String, cohortStr: String, distress: String, color: Color) {
    Card(modifier = Modifier.width(240.dp).height(160.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("STAGE $number", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ACTIVE COHORT", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(cohortStr, style = MaterialTheme.typography.headlineMedium.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DISTRESS", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(distress, style = MaterialTheme.typography.labelLarge.copy(fontSize=18.sp), color = color)
                }
            }
        }
    }
}

@Composable
fun StageCardWarning(number: String, title: String, cohortStr: String, distress: String) {
    Card(modifier = Modifier.width(255.dp).height(160.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha=0.4f))) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, contentDescription=null, tint=MaterialTheme.colorScheme.onError, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("STAGE $number", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Friction Peak", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("COHORT PEAK", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.error)
                    Text(cohortStr, style = MaterialTheme.typography.headlineMedium.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.error)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DISTRESS", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(distress, style = MaterialTheme.typography.labelLarge.copy(fontSize=18.sp), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun CohortQueueCard(case: CaseRecord, navController: NavController) {
    val isCritical = case.distressScore > 75
    Card(
        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("official_case_detail/${case.caseId}") },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(if (isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Text(case.profile.name.take(2).uppercase(), color = if(isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(case.profile.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(6.dp))
                            if (isCritical) {
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.error, RoundedCornerShape(4.dp)).padding(horizontal=4.dp, vertical=2.dp)) {
                                    Text("CRITICAL", style = MaterialTheme.typography.labelMedium.copy(fontSize=9.sp), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(case.caseId, style = MaterialTheme.typography.labelMedium.copy(fontSize=12.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Distress ${case.distressScore}", style = MaterialTheme.typography.labelMedium.copy(fontSize=10.sp), color = if(isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(if(isCritical) "Panic Index" else "Moderate", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Column {
                        Text("Stage / Status", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(case.status.display, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Box(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Column {
                        Text("Department", style = MaterialTheme.typography.labelMedium.copy(fontSize=11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(case.department, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription=null, tint=MaterialTheme.colorScheme.primary, modifier=Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Assigned: ${case.priority}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isCritical) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Event, contentDescription=null, tint=MaterialTheme.colorScheme.error, modifier=Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Immediate Review", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick={}, modifier = Modifier.weight(1f).height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = if(isCritical) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant), shape= RoundedCornerShape(12.dp)) {
                    Text(if(isCritical) "Emergency Intervention" else "Standard Routing", color = if(isCritical) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant, fontSize=12.sp)
                }
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.ChevronRight, contentDescription=null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AdminQueueTab(viewModel: MainViewModel, navController: NavController) {
    val cases by viewModel.cases.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Complete System Queue", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
        }
        items(cases.values.toList()) { case ->
            CohortQueueCard(case, navController)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialCaseDetailScreen(navController: NavController, viewModel: MainViewModel, caseId: String) {
    val caseData = viewModel.cases.collectAsState().value[caseId]
    var priority by remember { mutableStateOf(caseData?.priority ?: "Normal") }
    var department by remember { mutableStateOf(caseData?.department ?: "Unassigned") }
    var status by remember { mutableStateOf(caseData?.status ?: CaseStatus.REVIEWED) }
    
    if (caseData == null) return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(caseData.caseId) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp).verticalScroll(rememberScrollState())) {
            
            Text("Triage Configuration", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Subject: ${caseData.profile.name}", style = MaterialTheme.typography.labelLarge)
                    Text("Distress: ${caseData.distressScore}", style = MaterialTheme.typography.bodyLarge, color = if(caseData.distressScore>75) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("AI Summary:", style = MaterialTheme.typography.labelLarge)
                    Text(caseData.latestAnalysis?.summary ?: "No Summary", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text("Set Status", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical=8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(CaseStatus.REVIEWED, CaseStatus.TRIAGED).forEach { st ->
                    FilterChip(
                        selected = status == st,
                        onClick = { status = st },
                        label = { Text(st.display) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }
            }
            
            Text("Set Priority", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical=8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal", "Urgent", "Critical").forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.triageCase(caseId, priority, department, caseData.supportCategory, null, "Updated from UI", status)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Triage & Route to Supabase")
            }
        }
    }
}
