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