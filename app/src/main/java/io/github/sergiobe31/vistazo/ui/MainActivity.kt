package io.github.sergiobe31.vistazo.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sergiobe31.vistazo.ui.HomeViewModel.Destination
import io.github.sergiobe31.vistazo.ui.theme.VistazoTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VistazoTheme {
                val destination by viewModel.destination.collectAsStateWithLifecycle()
                when (destination) {
                    Destination.Home -> HomeScreen(viewModel)
                    Destination.History -> HistoryScreen(viewModel)
                    Destination.Settings -> SettingsScreen(viewModel)
                    Destination.Estimate -> EstimateScreen(viewModel)
                }
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_ESTIMATE, false) == true) {
            viewModel.openEstimateFromNotification()
        }
    }

    companion object {
        const val EXTRA_OPEN_ESTIMATE = "io.github.sergiobe31.vistazo.EXTRA_OPEN_ESTIMATE"
    }
}
