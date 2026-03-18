package com.skysam.hchirinos.myfinances.bolsadecaracas.presentation.state

data class PortfolioUiState(
    val selectedSymbols: List<String> = emptyList(),
    val loading: Boolean = false,
    val emptyState: Boolean = false,
    val error: String? = null,
    val summary: PortfolioSummaryUi = PortfolioSummaryUi(),
    val positions: List<PortfolioPositionUi> = emptyList(),
    val portfolioNotCreated: Boolean = false
)

data class PortfolioSummaryUi(
    val totalValue: String = "--",
    val totalGain: String = "--",
    val totalGainPercent: String = "--"
)

data class PortfolioPositionUi(
    val symbol: String = "SYM",
    val name: String = "Acción Placeholder",
    val quantity: String = "0",
    val price: String = "--",
    val gain: String = "--"
)
