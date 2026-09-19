package com.sahay.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sahay.ai.ui.SahayApp
import com.sahay.ai.ui.theme.SahayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.sahay.ai.network.SessionManager.init(applicationContext)
        setContent {
            SahayTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SahayApp()
                }
            }
        }
    }
}
