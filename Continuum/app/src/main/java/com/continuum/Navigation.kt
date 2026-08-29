package com.continuum

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.continuum.data.Database
import com.continuum.screens.CreateHandoffScreen
import com.continuum.screens.HandoffDetailsScreen
import com.continuum.screens.HomeScreen
import com.continuum.screens.LoginScreen
import com.continuum.screens.RecordsScreen
import com.continuum.screens.RegisterScreen
import com.continuum.ui.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Login
@Serializable
object Register
@Serializable
object Home
@Serializable
object NewHandoff
@Serializable
object HandoffDetails
@Serializable
object Records
@Composable
fun Navigate(viewModel: ViewModel, startPage: Any) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var selectedHandoff by remember {
        mutableStateOf<Database.Handoff?>(null)
    }
    var handoffDraftContent by remember {
        mutableStateOf("")
    }

    var lastBackPressTime = 0L
    val exitToast = Toast.makeText(context, "Double tap to exit", Toast.LENGTH_SHORT)

    BackHandler(enabled = (navController.previousBackStackEntry == null)) {
        val currentTime = System.currentTimeMillis()
        val duration = 2000



        if (currentTime - lastBackPressTime < duration) {
            exitToast.cancel()
            (context as Activity).finish()
        } else {
            lastBackPressTime = currentTime
            exitToast.show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startPage
    ) {

        composable<Login> {
            LoginScreen(
                viewModel = viewModel,
                toRegister = {
                    navController.navigate(route = Register)
                },
                toHome = {
                    navController.navigate(route = Home) {
                        popUpTo(Login) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Register> {
            RegisterScreen(
                viewModel = viewModel,
                toLogin = {
                    navController.popBackStack()
                },
                toHome = {
                    navController.navigate(route = Home) {
                        popUpTo(Login) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                viewModel = viewModel,
                toNewHandoff = {
                    handoffDraftContent = ""
                    navController.navigate(NewHandoff)
                },
                toHandoffFromNote = { noteContent ->
                    handoffDraftContent = noteContent
                    navController.navigate(NewHandoff)
                },
                toHistory = {
                    navController.navigate(Records)
                }
            )
        }

        composable<NewHandoff> {
            CreateHandoffScreen(
                viewModel = viewModel,
                initialContent = handoffDraftContent,
                onBackClick = {
                    navController.popBackStack()
                },
                onSubmitClick = {
                    navController.popBackStack()
                }
            )
        }

        composable<HandoffDetails> {
            selectedHandoff?.let { handoff ->
                HandoffDetailsScreen(
                    viewModel = viewModel,
                    handoff = handoff,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable<Records> {
            RecordsScreen(
                viewModel = viewModel,
                toHome = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onHandoffClick = { handoff ->
                    selectedHandoff = handoff
                    navController.navigate(HandoffDetails)
                }
            )
        }
    }
}