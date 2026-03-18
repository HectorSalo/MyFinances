package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.configuracion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.components.PlaceholderCard
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.components.PortfolioStockItem
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioSettingsUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioStockUi

@Composable
fun PortfolioSettingsScreen(
    state: PortfolioSettingsUiState,
    onToggleStockSelection: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(id = R.string.text_cargando_cotizacion))
            }
        }
        state.availableStocks.isEmpty() -> {
            PlaceholderCard(
                title = stringResource(id = R.string.bolsa_de_caracas_tab_configurar_portafolio),
                body = "No hay acciones disponibles para configurar."
            )
        }
        else -> {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Selecciona las acciones de tu portafolio:")
                state.availableStocks.forEach { stock ->
                    PortfolioStockItem(
                        stock = stock,
                        onToggle = onToggleStockSelection
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PortfolioSettingsScreenPreview() {
    PortfolioSettingsScreen(
        state = PortfolioSettingsUiState(
            loading = false,
            availableStocks = listOf(
                PortfolioStockUi("BDC", "Banco de Caracas", true),
                PortfolioStockUi("ECO", "Envases Caracas", false)
            )
        ),
        onToggleStockSelection = {}
    )
}
