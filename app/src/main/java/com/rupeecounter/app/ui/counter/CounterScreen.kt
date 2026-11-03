package com.rupeecounter.app.ui.counter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rupeecounter.app.ui.counter.components.DenominationSection
import com.rupeecounter.app.ui.counter.components.GrandTotalCard
import com.rupeecounter.app.ui.counter.components.ResetConfirmationDialog
import com.rupeecounter.app.ui.counter.components.SaveRecordDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterScreen(
    viewModel: CounterViewModel,
    onNavigateToRecords: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rupee Counter",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.shareTextSummary(context) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Text Summary"
                        )
                    }
                    IconButton(onClick = onNavigateToRecords) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View Saved Records"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Grand Total Prominent Card at Top
                item(key = "grand_total_card") {
                    GrandTotalCard(
                        calculation = uiState.calculation,
                        onSaveClick = { viewModel.requestSaveRecord() },
                        onResetClick = { viewModel.requestReset() },
                        onExportPdfClick = { viewModel.exportAndSharePdf(context) },
                        onShareClick = { viewModel.shareTextSummary(context) }
                    )
                }

                // Banknotes Section: ₹500, ₹200, ₹100, ₹50, ₹20, ₹10
                item(key = "banknotes_section") {
                    DenominationSection(
                        title = "BANKNOTES",
                        subtitle = "₹500, ₹200, ₹100, ₹50, ₹20, ₹10",
                        items = uiState.calculation.notesItems,
                        subtotalAmount = uiState.calculation.formattedNotesAmount,
                        totalPieces = uiState.calculation.totalNotesCount,
                        onCountStringChanged = { denom, raw ->
                            viewModel.onCountInputStringChanged(denom, raw)
                        },
                        onIncrement = { denom -> viewModel.onIncrement(denom) },
                        onDecrement = { denom -> viewModel.onDecrement(denom) }
                    )
                }

                // Coins Section: ₹20, ₹10, ₹5, ₹2, ₹1
                item(key = "coins_section") {
                    DenominationSection(
                        title = "COINS",
                        subtitle = "₹20, ₹10, ₹5, ₹2, ₹1",
                        items = uiState.calculation.coinsItems,
                        subtotalAmount = uiState.calculation.formattedCoinsAmount,
                        totalPieces = uiState.calculation.totalCoinsCount,
                        onCountStringChanged = { denom, raw ->
                            viewModel.onCountInputStringChanged(denom, raw)
                        },
                        onIncrement = { denom -> viewModel.onIncrement(denom) },
                        onDecrement = { denom -> viewModel.onDecrement(denom) }
                    )
                }
            }
        }

        // Reset Dialog
        if (uiState.showResetDialog) {
            ResetConfirmationDialog(
                onConfirm = { viewModel.confirmReset() },
                onDismiss = { viewModel.dismissResetDialog() }
            )
        }

        // Save Record Dialog
        if (uiState.showSaveDialog) {
            SaveRecordDialog(
                calculation = uiState.calculation,
                onConfirm = { remarks -> viewModel.confirmSaveRecord(remarks) },
                onDismiss = { viewModel.dismissSaveDialog() }
            )
        }
    }
}
