package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state

data class PortfolioSettingsUiState(
    val loading: Boolean = false,
    val availableStocks: List<PortfolioStockUi> = emptyList()
)

data class PortfolioStockUi(
    val symbol: String = "SYM",
    val name: String = "Acción Placeholder",
    val isSelected: Boolean = false
)
