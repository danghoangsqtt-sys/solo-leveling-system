package com.systemleveling.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // Tracks notification deep-link destination; survives onNewIntent without recreating the Activity
    private var deepLinkDest by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("nav_destination")?.let { deepLinkDest = it }
    }

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
                        val navExtra = intent?.getStringExtra("nav_destination")
                        val isOnboarded = settingsManager.isOnboarded.first()
                        if (navExtra != null && isOnboarded) {
                            deepLinkDest = navExtra
                        }
                        postSplashDestination = if (isOnboarded) "home" else "onboarding"
                        isReady = true
                    }

                    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                        postSplashDestination?.let { dest ->
                            AppNavGraph(
                                startDestination = dest,
                                postSplashDestination = dest,
                                deepLinkDest = deepLinkDest,
                                onDeepLinkConsumed = { deepLinkDest = null }
                            )
                        }
                        DebugOverlay(
                            modifier = Modifier.align(Alignment.TopEnd).padding(top = 56.dp, end = 16.dp),
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
