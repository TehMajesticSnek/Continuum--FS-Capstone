package com.continuum

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.continuum.data.Database
import com.continuum.data.UserPreferences
import com.continuum.ui.ViewModel
import com.continuum.ui.theme.ContinuumTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        val db = Database()
        val userData = UserPreferences(applicationContext) // Selected team mostly. Settings are separate
        var startPage: Any = Login

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        enableEdgeToEdge()

        setContent {
            val viewModel: ViewModel = viewModel(factory = ViewModel.Factory(userData, db))

            var isCheckingAuth by remember { mutableStateOf(true) }

            db.activeTeam = viewModel.selectedTeam.collectAsState().value

            splashScreen.setKeepOnScreenCondition {
                isCheckingAuth
            }

            LaunchedEffect(Unit) {
                if (viewModel.db.isLoggedIn()) {
                    startPage = Home
                }
                isCheckingAuth = false
            }

            ContinuumTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!isCheckingAuth) {
                        Navigate(viewModel = viewModel, startPage = startPage)
                    }
                }
            }
        }
    }
}