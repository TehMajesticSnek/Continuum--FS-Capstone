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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {
    class UserSession {
        var userID by mutableStateOf<String?>(null)
        var activeTeam by mutableStateOf<Database.Team?>(null)
        var teamRole by mutableLongStateOf(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        val session = UserSession()
        val db = Database(session)
        val userData = UserPreferences(applicationContext) // Selected team mostly. Settings are separate
        var startPage: Any = Login

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        enableEdgeToEdge()

        setContent {
            val viewModel: ViewModel = viewModel(factory = ViewModel.Factory(userData, db, session))

            var isCheckingAuth by remember { mutableStateOf(true) }

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
                    if (!isCheckingAuth && userData.canStart) {
                        session.activeTeam = viewModel.selectedTeam.collectAsState().value
                        Navigate(viewModel = viewModel, startPage = startPage)
                    }
                }
            }
        }
    }
}