package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.portafolio

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
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioPositionUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioSummaryUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioUiState

@Composable
fun PortfolioScreen(
    state: PortfolioUiState,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> {
            Column(
                modifier = modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(id = R.string.loading))
            }
        }
        state.portfolioNotCreated -> {
            PlaceholderCard(
                title = stringResource(id = R.string.bolsa_de_caracas_tab_portafolio),
                body = stringResource(id = R.string.portfolio_not_created)
            )
        }
        state.emptyState || state.positions.isEmpty() -> {
            PlaceholderCard(
                title = stringResource(id = R.string.bolsa_de_caracas_tab_portafolio),
                body = stringResource(id = R.string.portfolio_empty_message)
            )
        }
        else -> {
            Column(
                modifier = modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PortfolioSummary(summary = state.summary)
                state.positions.forEach { position ->
                    PortfolioPositionCard(position)
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummary(summary: PortfolioSummaryUi) {
    Text(
        text = "Resumen: Valor: ${summary.totalValue}, Ganancia: ${summary.totalGain} (${summary.totalGainPercent})"
    )
}

@Composable
private fun PortfolioPositionCard(position: PortfolioPositionUi, modifier: Modifier = Modifier) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = position.symbol, modifier = Modifier.padding(bottom = 2.dp))
            Text(text = position.name, modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Cantidad: ${position.quantity}")
            Text(text = "Precio promedio: ${position.price}")
            Text(text = "Valor actual: ${position.gain}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PortfolioScreenPreview() {
    PortfolioScreen(
        state = PortfolioUiState(
            loading = false,
            emptyState = false,
            summary = PortfolioSummaryUi("Bs. 100.000", "+Bs. 5.000", "+5%"),
            positions = listOf(
                PortfolioPositionUi("ABC", "Acción ABC", "10", "Bs. 1.000", "+Bs. 100"),
                PortfolioPositionUi("XYZ", "Acción XYZ", "5", "Bs. 2.000", "-Bs. 50")
            )
        )
    )
}

// La UI ya usa PortfolioPositionUi.quantity y .price, por lo que mostrará los valores reales cargados desde Firestore.
