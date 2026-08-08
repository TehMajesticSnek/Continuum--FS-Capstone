package com.continuum

import android.provider.ContactsContract
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
fun Navigate(db : Database) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                db,
                toRegister = { navController.navigate(route = Register) }
            )
        }
        composable<Register> {
            RegisterScreen(
                db,
                toLogin = { navController.popBackStack() }
            )
        }
    }
}
