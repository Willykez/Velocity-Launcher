package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.home.HomeScreen
import com.example.ui.theme.AuraLauncherTheme
import com.example.viewmodel.LauncherViewModel
import com.example.viewmodel.LauncherViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels {
        val app = application as AuraLauncherApplication
        LauncherViewModelFactory(app.repository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            AuraLauncherTheme(themeMode = settings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshInstalledApps()
    }
}
