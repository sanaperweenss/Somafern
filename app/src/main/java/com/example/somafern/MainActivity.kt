package com.example.somafern

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.somafern.ui.navigation.SomafernApp
import com.example.somafern.ui.theme.SomafernTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SomafernTheme {
                SomafernApp()
            }
        }
    }
}