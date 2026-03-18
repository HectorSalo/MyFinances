package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysam.hchirinos.myfinances.bolsadecaracas.data.BvcPortfolioRepository
import com.skysam.hchirinos.myfinances.bolsadecaracas.data.BvcPosition
import com.skysam.hchirinos.myfinances.bolsadecaracas.data.market.MarketQuote
import com.skysam.hchirinos.myfinances.bolsadecaracas.data.market.MarketRepository
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.BolsaDeCaracasUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.BolsaDeCaracasSection
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketFilter
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.MarketStockUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioPositionUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioSettingsUiState
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioStockUi
import com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state.PortfolioUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BolsaDeCaracasViewModel @Inject constructor(
    private val bvcPortfolioRepository: BvcPortfolioRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val allStocks = listOf(
        PortfolioStockUi(symbol = "BDC", name = "Banco de Caracas"),
        PortfolioStockUi(symbol = "ECO", name = "Envases Caracas"),
        PortfolioStockUi(symbol = "FVI", name = "Fondo de Valores Inmobiliarios"),
        PortfolioStockUi(symbol = "PZO", name = "Proagro"),
        PortfolioStockUi(symbol = "ZUL", name = "Cementos Zulianos")
    )

    private var selectedSymbols: List<String> = emptyList()
    private var loadingPortfolioConfig: Boolean = false
    private var errorPortfolioConfig: String? = null
    private var positions: List<BvcPosition> = emptyList()

    private var lastConfigWasNull: Boolean = false

    var portfolioUiState by mutableStateOf(
        PortfolioUiState()
    )
        private set

    var portfolioSettingsUiState by mutableStateOf(
        PortfolioSettingsUiState(
            loading = true,
            availableStocks = allStocks
        )
    )
        private set

    var state by mutableStateOf(BolsaDeCaracasUiState())
        private set

    var marketUiState by mutableStateOf(MarketUiState())
        private set

    init {
        observePortfolioConfig()
        observePositions()
        if (marketUiState.stocks.isEmpty() && !marketUiState.loading) {
            loadMarketQuotes()
        }
    }

    private fun updatePortfolioUiStateWithSymbols(symbols: List<String>) {
        val positionsUi = symbols.map { symbol ->
            val stock = allStocks.find { it.symbol == symbol }
            val pos = positions.find { it.symbol == symbol }
            PortfolioPositionUi(
                symbol = symbol,
                name = stock?.name ?: symbol,
                quantity = pos?.quantity?.toString() ?: "-",
                price = pos?.averagePrice?.toString() ?: "-",
                gain = "--"
            )
        }
        portfolioUiState = PortfolioUiState(
            selectedSymbols = symbols,
            loading = loadingPortfolioConfig,
            error = errorPortfolioConfig,
            positions = positionsUi,
            portfolioNotCreated = lastConfigWasNull && positions.isEmpty()
        )
    }

    fun onToggleStockSelection(symbol: String) {
        val newSelected = if (selectedSymbols.contains(symbol)) {
            selectedSymbols - symbol
        } else {
            selectedSymbols + symbol
        }
        // Actualización optimista de la UI
        selectedSymbols = newSelected
        updatePortfolioUiStateWithSymbols(newSelected)
        viewModelScope.launch {
            loadingPortfolioConfig = true
            updatePortfolioUiStateWithSymbols(newSelected)
            val result = bvcPortfolioRepository.savePortfolioConfig(newSelected)
            loadingPortfolioConfig = false
            result.exceptionOrNull()?.let {
                errorPortfolioConfig = it.message
            }
            updatePortfolioUiStateWithSymbols(newSelected)
        }
    }

    private fun observePortfolioConfig() {
        viewModelScope.launch {
            bvcPortfolioRepository.observePortfolioConfig().collect { result ->
                loadingPortfolioConfig = false
                result.onSuccess { config ->
                    lastConfigWasNull = (config == null)
                    selectedSymbols = config?.selectedSymbols ?: emptyList()
                    errorPortfolioConfig = null
                    updatePortfolioUiStateWithSymbols(selectedSymbols)
                    portfolioSettingsUiState = PortfolioSettingsUiState(
                        loading = false,
                        availableStocks = allStocks.map { stock ->
                            stock.copy(isSelected = selectedSymbols.contains(stock.symbol))
                        }
                    )
                }
                result.onFailure { error ->
                    errorPortfolioConfig = error.message
                    updatePortfolioUiStateWithSymbols(selectedSymbols)
                    // Estado consistente y explícito en caso de error
                    portfolioSettingsUiState = PortfolioSettingsUiState(
                        loading = false,
                        availableStocks = allStocks.map { stock ->
                            stock.copy(isSelected = selectedSymbols.contains(stock.symbol))
                        }
                    )
                }
            }
        }
    }

    private fun observePositions() {
        viewModelScope.launch {
            bvcPortfolioRepository.observePositions().collect { result ->
                result.onSuccess { loadedPositions ->
                    positions = loadedPositions
                    updatePortfolioUiStateWithSymbols(selectedSymbols)
                }
                // Si hay error, no actualizamos positions, pero podrías manejar error si lo deseas
            }
        }
    }

    fun savePosition(symbol: String, quantity: Double, averagePrice: Double) {
        viewModelScope.launch {
            val position = BvcPosition(
                symbol = symbol,
                quantity = quantity,
                averagePrice = averagePrice,
                updatedAt = com.google.firebase.Timestamp.now()
            )
            bvcPortfolioRepository.savePosition(position)
            // No es necesario actualizar manualmente positions, el flujo reactivo lo hará
        }
    }

    fun onTabSelected(section: BolsaDeCaracasSection) {
        if (state.selectedSection == section) return
        state = state.copy(selectedSection = section)
    }

    fun onMarketSearchQueryChanged(query: String) {
        val filtered = filterMarketStocks(query, marketUiState.selectedFilter)
        marketUiState = marketUiState.copy(searchQuery = query, visibleStocks = filtered)
    }

    fun onMarketFilterSelected(filter: MarketFilter) {
        val filtered = filterMarketStocks(marketUiState.searchQuery, filter)
        marketUiState = marketUiState.copy(selectedFilter = filter, visibleStocks = filtered)
    }

    private fun loadMarketQuotes() {
        viewModelScope.launch {
            Log.d(BVC_MARKET_TAG, "ViewModel: cargando cotizaciones reales...")
            marketUiState = marketUiState.copy(loading = true, marketError = null)

            val result = marketRepository.getMarketQuotes()

            result.onSuccess { quotes ->
                Log.d(BVC_MARKET_TAG, "ViewModel: ${quotes.size} cotizaciones recibidas")
                quotes.take(5).forEach { quote ->
                    Log.d(BVC_MARKET_TAG, "ViewModel: ${quote.symbol} = ${quote.lastPrice}")
                }

                val stocksUi = quotes.map(::mapMarketQuoteToUi)
                val visibleStocks = filterMarketStocks(
                    query = marketUiState.searchQuery,
                    filter = marketUiState.selectedFilter,
                    baseStocks = stocksUi
                )

                marketUiState = marketUiState.copy(
                    loading = false,
                    marketError = null,
                    stocks = stocksUi,
                    visibleStocks = visibleStocks
                )

                Log.d(
                    BVC_MARKET_TAG,
                    "ViewModel: estado actualizado con ${stocksUi.size} acciones y ${visibleStocks.size} visibles"
                )
            }

            result.onFailure { error ->
                Log.e(BVC_MARKET_TAG, "ViewModel: error cargando cotizaciones", error)
                marketUiState = marketUiState.copy(
                    loading = false,
                    marketError = error.message
                )
            }
        }
    }

    private fun mapMarketQuoteToUi(quote: MarketQuote): MarketStockUi {
        return MarketStockUi(
            symbol = quote.symbol,
            name = quote.symbol,
            price = quote.lastPrice.toString(),
            openPrice = "--",
            changePercent = quote.percentChange?.let { "$it%" } ?: "--",
            changeAbsolute = quote.change?.toString() ?: "--",
            volume = quote.volume?.let { it.toLong().toString() } ?: "--",
            amount = "--",
            trades = "--"
        )
    }

    private fun filterMarketStocks(
        query: String,
        filter: MarketFilter,
        baseStocks: List<MarketStockUi> = marketUiState.stocks
    ): List<MarketStockUi> {
        var result = baseStocks
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { it.symbol.lowercase().contains(q) || it.name.lowercase().contains(q) }
        }
        result = when (filter) {
            MarketFilter.UP -> result.sortedByDescending { it.changePercent.removeSuffix("%+").removeSuffix("%") .replace("+","").toDoubleOrNull() ?: 0.0 }
            MarketFilter.DOWN -> result.sortedBy { it.changePercent.removeSuffix("%-").removeSuffix("%") .replace("-","").toDoubleOrNull() ?: 0.0 }
            MarketFilter.VOLUME -> result.sortedByDescending { it.volume.replace(".","").toIntOrNull() ?: 0 }
            MarketFilter.CASH -> result.sortedByDescending { it.amount.replace("Bs.","").replace(".","").replace(",","").trim().toIntOrNull() ?: 0 }
            MarketFilter.OPERATIONS -> result.sortedByDescending { it.trades.toIntOrNull() ?: 0 }
            else -> result
        }
        return result
    }

    companion object {
        private const val BVC_MARKET_TAG = "BVC_MARKET"
    }
}
