package com.systemleveling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.systemleveling.app.navigation.AppNavGraph
import com.systemleveling.core.debug.DebugOverlay
import com.systemleveling.core.designsystem.theme.SystemLevelingTheme
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.di.dataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep splash visible until we know where to navigate
        var isReady = false
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isReady }

        super.onCreate(savedInstanceState)

        setContent {
            SystemLevelingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = md_theme_dark_background
                ) {
                    var postSplashDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        val isOnboardedKey = booleanPreferencesKey("isOnboarded")
                        val isOnboarded = applicationContext.dataStore.data
                            .map { it[isOnboardedKey] ?: false }
                            .first()
                        postSplashDestination = if (isOnboarded) "home" else "onboarding"
                        isReady = true
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        postSplashDestination?.let { dest ->
                            AppNavGraph(startDestination = dest, postSplashDestination = dest)
                        }
                        DebugOverlay(modifier = Modifier.align(Alignment.TopEnd))
                    }
                }
            }
        }
    }
}
