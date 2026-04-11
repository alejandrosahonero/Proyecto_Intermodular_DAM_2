package com.alejandrosahonero.courthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alejandrosahonero.courthub.ui.navigation.NavGraph
import com.alejandrosahonero.courthub.ui.theme.CourtHubTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourtHubTheme {
                NavGraph()
            }
        }
    }
}