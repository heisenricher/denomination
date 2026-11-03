package com.rupeecounter.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rupeecounter.app.RupeeCounterApplication
import com.rupeecounter.app.ui.counter.CounterScreen
import com.rupeecounter.app.ui.counter.CounterViewModel
import com.rupeecounter.app.ui.records.RecordsViewModel
import com.rupeecounter.app.ui.records.SavedRecordsScreen
import com.rupeecounter.app.ui.theme.RupeeCounterTheme

enum class AppScreen {
    COUNTER,
    SAVED_RECORDS
}

class MainActivity : ComponentActivity() {

    private val app by lazy { application as RupeeCounterApplication }

    private val counterViewModel: CounterViewModel by viewModels {
        CounterViewModel.provideFactory(app.repository, app.calculateTotalUseCase)
    }

    private val recordsViewModel: RecordsViewModel by viewModels {
        RecordsViewModel.provideFactory(app.repository, app.calculateTotalUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RupeeCounterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.COUNTER) }

                    // Hardware back button behavior
                    BackHandler(enabled = currentScreen != AppScreen.COUNTER) {
                        currentScreen = AppScreen.COUNTER
                    }

                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.COUNTER -> {
                                CounterScreen(
                                    viewModel = counterViewModel,
                                    onNavigateToRecords = { currentScreen = AppScreen.SAVED_RECORDS }
                                )
                            }
                            AppScreen.SAVED_RECORDS -> {
                                SavedRecordsScreen(
                                    viewModel = recordsViewModel,
                                    onNavigateBack = { currentScreen = AppScreen.COUNTER }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
