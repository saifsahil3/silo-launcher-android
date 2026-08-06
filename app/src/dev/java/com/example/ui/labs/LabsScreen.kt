package com.example.ui.labs

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.core.config.EnvironmentConfig
import com.example.core.flag.FeatureFlag
import com.example.core.flag.FeatureFlagRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { FeatureFlagRepository(context) }
    val scope = rememberCoroutineScope()

    var flagsState by remember { mutableStateOf<Map<FeatureFlag, Boolean>>(emptyMap()) }

    LaunchedEffect(Unit) {
        FeatureFlag.entries.forEach { flag ->
            repository.isEnabled(flag).collect { enabled ->
                flagsState = flagsState + (flag to enabled)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Labs",
                            tint = Color(0xFFFFB74D)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Silo Labs (Dev)", color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Build Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BUILD INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Environment: ${EnvironmentConfig.current.environmentName}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Version Name: ${BuildConfig.VERSION_NAME}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Version Code: ${BuildConfig.VERSION_CODE}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Build Type: ${BuildConfig.BUILD_TYPE}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Flavor: ${BuildConfig.FLAVOR}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Package: ${context.packageName}", color = Color.LightGray, fontSize = 14.sp)
                        Text("Storage File: ${EnvironmentConfig.current.preferencesName}", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }

            // Feature Flags Section
            item {
                Text(
                    text = "FEATURE FLAGS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            items(FeatureFlag.entries) { flag ->
                val isEnabled = flagsState[flag] ?: flag.defaultValue
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = flag.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = flag.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    repository.setEnabled(flag, checked)
                                }
                            }
                        )
                    }
                }
            }

            // Developer Reset Actions
            item {
                Text(
                    text = "DEVELOPER ACTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            repository.resetAll()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset All Feature Flags")
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val prefs = context.getSharedPreferences(EnvironmentConfig.current.preferencesName, Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset Launcher Preferences")
                }
            }
        }
    }
}
