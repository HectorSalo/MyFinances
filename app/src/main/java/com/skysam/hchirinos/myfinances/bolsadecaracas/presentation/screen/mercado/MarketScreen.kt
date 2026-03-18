package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.screen.mercado

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skysam.hchirinos.myfinances.R
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.components.PlaceholderCard
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketFilter
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketStockUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketUiState

@Composable
fun MarketScreen(
    state: MarketUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (MarketFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Buscador
        BasicTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            decorationBox = { innerTextField ->
                if (state.searchQuery.isEmpty()) {
                    Text("Buscar por nombre o símbolo", color = Color.Gray)
                }
                innerTextField()
            }
        )
        // Filtros
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MarketFilter.values().forEach { filter ->
                val selected = filter == state.selectedFilter
                OutlinedButton(
                    onClick = { onFilterSelected(filter) },
                    enabled = !selected
                ) {
                    Text(text = filter.name)
                }
            }
        }
        // Lista o estado vacío
        when {
            state.loading -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(id = R.string.loading))
                }
            }
            !state.marketError.isNullOrBlank() -> {
                PlaceholderCard(
                    title = stringResource(id = R.string.bolsa_de_caracas_tab_mercado),
                    body = state.marketError
                )
            }
            state.visibleStocks.isEmpty() -> {
                PlaceholderCard(
                    title = stringResource(id = R.string.bolsa_de_caracas_tab_mercado),
                    body = stringResource(id = R.string.market_empty)
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.visibleStocks.forEach { stock ->
                        MarketStockCard(stock)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketStockCard(stock: MarketStockUi, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "${stock.symbol} - ${stock.name}", modifier = Modifier.padding(bottom = 4.dp))
            Text(text = "Precio actual: ${stock.price}")
            Text(text = "Apertura: ${stock.openPrice}")
            Text(text = "Variación: ${stock.changePercent} (${stock.changeAbsolute})")
            Text(text = "Volumen: ${stock.volume}")
            Text(text = "Efectivo: ${stock.amount}")
            Text(text = "Operaciones: ${stock.trades}")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MarketScreenPreview() {
    MarketScreen(
        state = MarketUiState(
            loading = false,
            searchQuery = "",
            selectedFilter = MarketFilter.ALL,
            stocks = listOf(
                MarketStockUi("ABC", "Acción ABC", "Bs. 1.000", "+2%", "1000"),
                MarketStockUi("XYZ", "Acción XYZ", "Bs. 2.000", "-1%", "500")
            )
        ),
        onSearchQueryChanged = {},
        onFilterSelected = {}
    )
}
