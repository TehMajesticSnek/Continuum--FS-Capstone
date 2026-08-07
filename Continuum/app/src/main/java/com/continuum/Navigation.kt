package com.continuum

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.continuum.screens.LoginScreen
import com.continuum.screens.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable
object Login
@Serializable
object Register

@Composable
fun Navigate() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                toRegister = { navController.navigate(route = Register) }
            )
        }
        composable<Register> {
            RegisterScreen(
                toLogin = { navController.popBackStack() }
            )
        }
    }
}
