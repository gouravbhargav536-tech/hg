package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.data.local.AppDatabase
import com.example.data.repository.OutreachRepository
import com.example.ui.OutreachViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("smart-outreach-123") // Placeholder
                    .setApplicationId("1:123456789:android:123") // Placeholder
                    .setApiKey("placeholder-api-key") // Placeholder
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = OutreachRepository(
            database.leadDao(),
            database.templateDao(),
            database.followUpDao(),
            database.settingsDao(),
            database.campaignDao(),
            database.messageDao()
        )
        val viewModelFactory = OutreachViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OutreachApp(viewModelFactory)
            }
        }
    }
}

@Composable
fun OutreachApp(viewModelFactory: OutreachViewModelFactory) {
    val viewModel: com.example.ui.OutreachViewModel = viewModel(factory = viewModelFactory)
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val settings by viewModel.appSettings.collectAsState()
    
    var startDestination by remember { mutableStateOf(if (auth.currentUser != null) "leads" else "auth") }

    // Onboarding Redirect
    LaunchedEffect(settings.hasCompletedOnboarding) {
        if (!settings.hasCompletedOnboarding) {
            navController.navigate("onboarding") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "leads" || currentRoute == "templates" || currentRoute == "followups" || currentRoute == "broadcast_dashboard") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.People, contentDescription = "Leads") },
                        label = { Text("Leads") },
                        selected = currentRoute == "leads",
                        onClick = { navController.navigate("leads") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Follow-ups") },
                        label = { Text("Follow-ups") },
                        selected = currentRoute == "followups",
                        onClick = { navController.navigate("followups") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ListAlt, contentDescription = "Templates") },
                        label = { Text("Templates") },
                        selected = currentRoute == "templates",
                        onClick = { navController.navigate("templates") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Broadcast") },
                        label = { Text("Broadcast") },
                        selected = currentRoute == "broadcast_dashboard",
                        onClick = { navController.navigate("broadcast_dashboard") { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(onLoginSuccess = { 
                    navController.navigate("leads") {
                        popUpTo("auth") { inclusive = true }
                    }
                })
            }
            composable("onboarding") {
                OnboardingScreen(onFinished = {
                    viewModel.saveSettings(settings.copy(hasCompletedOnboarding = true))
                    val destination = if (auth.currentUser != null) "leads" else "auth"
                    navController.navigate(destination) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                })
            }
            composable("leads") {
                LeadListScreen(
                    viewModel = viewModel,
                    onLogout = {
                        auth.signOut()
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLeadClick = { id -> navController.navigate("leadDetail/$id") },
                    onAddLeadClick = { navController.navigate("addEditLead/-1") }
                )
            }
            composable(
                "leadDetail/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: -1L
                LeadDetailScreen(
                    leadId = leadId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("addEditLead/$id") },
                    onBroadcast = { id -> navController.navigate("broadcast/$id") }
                )
            }
            composable(
                "broadcast/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId") ?: -1L
                MultiChannelOutreachScreen(
                    leadId = leadId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "addEditLead/{leadId}",
                arguments = listOf(navArgument("leadId") { type = NavType.LongType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getLong("leadId")
                AddEditLeadScreen(
                    leadId = leadId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("templates") {
                TemplateListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("followups") {
                FollowUpScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
            composable("broadcast_dashboard") {
                BroadcastDashboardScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
