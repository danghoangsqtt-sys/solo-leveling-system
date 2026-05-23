package com.systemleveling.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.systemleveling.app.navigation.AppNavGraph
import com.systemleveling.core.debug.DebugOverlay
import com.systemleveling.core.designsystem.theme.SystemLevelingTheme
import com.systemleveling.core.designsystem.theme.md_theme_dark_background
import com.systemleveling.core.notification.NotificationHelper
import com.systemleveling.core.settings.SettingsManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsManager: SettingsManager
    @Inject lateinit var notificationHelper: NotificationHelper

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission granted or denied — channels are already created, worker will fire */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep splash visible until we know where to navigate
        var isReady = false
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isReady }

        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33).
        // Without this, the OS never shows a prompt and all notifications are silently blocked.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            SystemLevelingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = md_theme_dark_background
                ) {
                    var postSplashDestination by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        // Check if launched from notification deep-link
                        val navExtra = intent?.getStringExtra("nav_destination")
                        if (navExtra != null) {
                            postSplashDestination = navExtra
                            isReady = true
                            return@LaunchedEffect
                        }

                        val isOnboarded = settingsManager.isOnboarded.first()
                        postSplashDestination = if (isOnboarded) "home" else "onboarding"
                        isReady = true
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        postSplashDestination?.let { dest ->
                            AppNavGraph(startDestination = dest, postSplashDestination = dest)
                        }
                        DebugOverlay(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onResetOnboarding = {
                                scope.launch {
                                    settingsManager.setOnboarded(false)
                                    postSplashDestination = "onboarding"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
