package com.sahay.ai.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sahay.ai.ui.screens.RoleSelectionScreen
import com.sahay.ai.ui.screens.VictimLanguageScreen
import com.sahay.ai.ui.screens.VictimConsentScreen
import com.sahay.ai.ui.screens.VictimRegistrationScreen
import com.sahay.ai.ui.screens.VictimConfirmationScreen
import com.sahay.ai.ui.screens.VictimDashboardScreen
import com.sahay.ai.ui.screens.OfficialLoginScreen
import com.sahay.ai.ui.screens.OfficialDashboardScreen
import com.sahay.ai.ui.screens.OfficialCaseDetailScreen

@Composable
fun SahayApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    
    val restoredRole = viewModel.isSessionRestored.collectAsState().value

    if (restoredRole == null) {
        // Splash screen while checking session
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    val startDest = when (restoredRole) {
        "official" -> "official_dashboard"
        "victim" -> "victim_dashboard"
        else -> "role_selection"
    }

    NavHost(navController = navController, startDestination = startDest, modifier = modifier) {
        composable("role_selection") {
            RoleSelectionScreen(navController, viewModel)
        }
        composable("victim_language") {
            VictimLanguageScreen(navController, viewModel)
        }
        composable("victim_consent") {
            VictimConsentScreen(navController, viewModel)
        }
        composable("victim_details") {
            VictimRegistrationScreen(navController, viewModel)
        }
        composable("victim_confirmation") {
            VictimConfirmationScreen(navController, viewModel)
        }
        composable("victim_dashboard") {
            VictimDashboardScreen(navController, viewModel)
        }
        composable("official_login") {
            OfficialLoginScreen(navController, viewModel)
        }
        composable("official_dashboard") {
            OfficialDashboardScreen(navController, viewModel)
        }
        composable("official_case_detail/{caseId}") { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId")
            if (caseId != null) {
                OfficialCaseDetailScreen(navController, viewModel, caseId)
            }
        }
    }
}
