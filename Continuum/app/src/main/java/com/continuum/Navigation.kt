package com.continuum

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.continuum.screens.HomeScreen
import com.continuum.screens.LoginScreen
import com.continuum.screens.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable
object Login
@Serializable
object Register
@Serializable
object Home

@Composable
fun Navigate(db : Database) {
    val navController = rememberNavController()

    // TODO Make sure startDestination is set back to Login
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                db,
                toRegister = { navController.navigate(route = Register) },
                toHome = { navController.navigate(route = Home) {
                    popUpTo(Login) {
                        inclusive = true
                    }
                }}
            )
        }
        composable<Register> {
            RegisterScreen(
                db,
                toLogin = { navController.popBackStack() },
                toHome = { navController.navigate(route = Home) {
                    popUpTo(Login) {
                        inclusive = true
                    }
                }}
            )
        }
        composable<Home> {
            HomeScreen(
                db,
            )
        }
    }
}
