package com.continuum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.continuum.screens.CreateAccountScreen
import com.continuum.screens.LoginScreen
import com.continuum.ui.theme.ContinuumTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ContinuumTheme {

                // false means show Login
                // true means show Create Account
                var showCreateAccount by remember {
                    mutableStateOf(false)
                }

                if (showCreateAccount) {

                    CreateAccountScreen(
                        onCreateAccount = {
                            // Backend account creation will come later
                        },
                        onSignInClick = {
                            showCreateAccount = false
                        }
                    )

                } else {

                    LoginScreen(
                        onCreateAccountClick = {
                            showCreateAccount = true
                        }
                    )
                }
            }
        }
    }
}