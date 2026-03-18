package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.BolsaDeCaracasScreen
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.viewmodel.BolsaDeCaracasViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BolsaDeCaracasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: BolsaDeCaracasViewModel = hiltViewModel()
            BolsaDeCaracasScreen(
                state = vm.state,
                onTabSelected = vm::onTabSelected,
                portfolioUiState = vm.portfolioUiState,
                marketUiState = vm.marketUiState,
                portfolioSettingsUiState = vm.portfolioSettingsUiState,
                onToggleStockSelection = vm::onToggleStockSelection,
                onSearchQueryChanged = vm::onMarketSearchQueryChanged,
                onFilterSelected = vm::onMarketFilterSelected
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BolsaDeCaracasActivityPreview() {
    BolsaDeCaracasScreen()
}
