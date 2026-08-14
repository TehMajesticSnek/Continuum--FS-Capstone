package com.continuum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.continuum.ui.theme.ContinuumTheme

class MainActivity : ComponentActivity() {

    val db = Database()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        TODO decide what to do about the device system bars
//        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
//
//        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
//        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            ContinuumTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Navigate(db)
                }
            }
        }
    }
}