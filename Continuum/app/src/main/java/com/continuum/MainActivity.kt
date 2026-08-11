package com.continuum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue // <-- NEW
import androidx.compose.runtime.mutableStateOf // <-- NEW
import androidx.compose.runtime.remember // <-- NEW
import androidx.compose.runtime.setValue // <-- NEW
import com.continuum.screens.CreateAccountScreen // <-- NEW
import com.continuum.screens.LoginScreen
import com.continuum.ui.theme.ContinuumTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ContinuumTheme {

                // <-- NEW: false means show Login
                // <-- NEW: true means show Create Account
                var showCreateAccount by remember {
                    mutableStateOf(false)
                }

                // <-- NEW
                if (showCreateAccount) {

                    CreateAccountScreen(
                        onCreateAccount = {
                            // Backend account creation will come later
                        },
                        onSignInClick = {
                            showCreateAccount = false // <-- NEW
                        }
                    )

                } else {

                    LoginScreen(
                        onCreateAccountClick = {
                            showCreateAccount = true // <-- NEW
                        }
                    )
                }
            }
        }
    }
}