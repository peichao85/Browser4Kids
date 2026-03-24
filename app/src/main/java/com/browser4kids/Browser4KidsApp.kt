package com.browser4kids

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browser4kids.repository.SettingsRepository
import com.browser4kids.repository.WhitelistRepository
import com.browser4kids.ui.browser.BrowserScreen
import com.browser4kids.ui.password.PasswordDialog
import com.browser4kids.ui.settings.SettingsScreen
import com.browser4kids.ui.welcome.WelcomeScreen
import com.browser4kids.viewmodel.BrowserViewModel
import com.browser4kids.util.AccessControlManager
import com.browser4kids.viewmodel.SettingsViewModel

@Composable
fun Browser4KidsApp() {
    val context = LocalContext.current
    val app = (context.applicationContext as Browser4KidsApplication)
    val settingsRepository = remember { SettingsRepository(context) }
    val whitelistRepository = remember { WhitelistRepository(app.database.whitelistDao()) }
    val browserViewModel: BrowserViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var currentScreen by remember {
        mutableStateOf(
            if (settingsRepository.isSetupComplete()) "browser" else "welcome"
        )
    }
    var showSettingsPasswordDialog by remember { mutableStateOf(false) }

    when (currentScreen) {
        "welcome" -> {
            WelcomeScreen(
                settingsRepository = settingsRepository,
                whitelistRepository = whitelistRepository,
                onSetupComplete = { currentScreen = "browser" }
            )
        }
        "browser" -> {
            BrowserScreen(
                viewModel = browserViewModel,
                onSettingsClick = { showSettingsPasswordDialog = true },
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
            )

            if (showSettingsPasswordDialog) {
                PasswordDialog(
                    blockedUrl = "",
                    settingsRepository = settingsRepository,
                    onPasswordVerified = { _ ->
                        showSettingsPasswordDialog = false
                        currentScreen = "settings"
                    },
                    onDismiss = { showSettingsPasswordDialog = false }
                )
            }
        }
        "settings" -> {
            SettingsScreen(
                viewModel = settingsViewModel,
                accessControlManager = browserViewModel.accessControlManager,
                onBack = { currentScreen = "browser" }
            )
        }
    }
}
