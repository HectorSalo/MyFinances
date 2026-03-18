package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.configuracion.PortfolioSettingsScreen
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.mercado.MarketScreen
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.portafolio.PortfolioScreen
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.BolsaDeCaracasSection
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.BolsaDeCaracasUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketFilter
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioSettingsUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.viewmodel.BolsaDeCaracasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolsaDeCaracasScreen(
    state: BolsaDeCaracasUiState = BolsaDeCaracasUiState(),
    onTabSelected: (BolsaDeCaracasSection) -> Unit = {},
    portfolioUiState: PortfolioUiState = PortfolioUiState(),
    marketUiState: MarketUiState = MarketUiState(),
    portfolioSettingsUiState: PortfolioSettingsUiState = PortfolioSettingsUiState(),
    onToggleStockSelection: (String) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onFilterSelected: (MarketFilter) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.bolsa_de_caracas_title)) }
            )
        }
    ) { padding ->
        BolsaDeCaracasContent(
            state = state,
            contentPadding = padding,
            onTabSelected = onTabSelected,
            portfolioUiState = portfolioUiState,
            marketUiState = marketUiState,
            portfolioSettingsUiState = portfolioSettingsUiState,
            onToggleStockSelection = onToggleStockSelection,
            onSearchQueryChanged = onSearchQueryChanged,
            onFilterSelected = onFilterSelected
        )
    }
}

@Composable
private fun BolsaDeCaracasContent(
    state: BolsaDeCaracasUiState,
    contentPadding: PaddingValues,
    onTabSelected: (BolsaDeCaracasSection) -> Unit,
    portfolioUiState: PortfolioUiState,
    marketUiState: MarketUiState,
    portfolioSettingsUiState: PortfolioSettingsUiState,
    onToggleStockSelection: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (MarketFilter) -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        BolsaDeCaracasTabs(
            selected = state.selectedSection,
            onTabSelected = onTabSelected
        )

        when (state.selectedSection) {
            BolsaDeCaracasSection.PORTAFOLIO -> PortfolioScreen(state = portfolioUiState)
            BolsaDeCaracasSection.MERCADO -> MarketScreen(
                state = marketUiState,
                onSearchQueryChanged = onSearchQueryChanged,
                onFilterSelected = onFilterSelected
            )
            BolsaDeCaracasSection.CONFIGURAR_PORTAFOLIO -> PortfolioSettingsScreen(
                state = portfolioSettingsUiState,
                onToggleStockSelection = onToggleStockSelection
            )
        }
    }
}

@Composable
private fun BolsaDeCaracasTabs(
    selected: BolsaDeCaracasSection,
    onTabSelected: (BolsaDeCaracasSection) -> Unit,
) {
    val tabs = listOf(
        BolsaDeCaracasSection.PORTAFOLIO,
        BolsaDeCaracasSection.MERCADO,
        BolsaDeCaracasSection.CONFIGURAR_PORTAFOLIO,
    )

    TabRow(selectedTabIndex = tabs.indexOf(selected).coerceAtLeast(0)) {
        tabs.forEach { section ->
            Tab(
                selected = section == selected,
                onClick = { onTabSelected(section) },
                text = { Text(text = section.title()) }
            )
        }
    }
}

@Composable
private fun BolsaDeCaracasSection.title(): String = when (this) {
    BolsaDeCaracasSection.PORTAFOLIO -> stringResource(id = R.string.bolsa_de_caracas_tab_portafolio)
    BolsaDeCaracasSection.MERCADO -> stringResource(id = R.string.bolsa_de_caracas_tab_mercado)
    BolsaDeCaracasSection.CONFIGURAR_PORTAFOLIO -> stringResource(
        id = R.string.bolsa_de_caracas_tab_configurar_portafolio
    )
}

@Preview(showBackground = true)
@Composable
private fun BolsaDeCaracasScreenPreview() {
    BolsaDeCaracasScreen()
}
