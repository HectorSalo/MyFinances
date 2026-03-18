package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state

data class MarketUiState(
    val loading: Boolean = false,
    val marketError: String? = null,
    val searchQuery: String = "",
    val selectedFilter: MarketFilter = MarketFilter.ALL,
    val stocks: List<MarketStockUi> = emptyList(), // lista completa
    val visibleStocks: List<MarketStockUi> = emptyList() // lista filtrada/visible
)

enum class MarketFilter { ALL, UP, DOWN, VOLUME, CASH, OPERATIONS }

data class MarketStockUi(
    val symbol: String = "SYM",
    val name: String = "Acción Placeholder",
    val price: String = "--",
    val openPrice: String = "--",
    val changePercent: String = "--",
    val changeAbsolute: String = "--",
    val volume: String = "--",
    val amount: String = "--",
    val trades: String = "--"
)
