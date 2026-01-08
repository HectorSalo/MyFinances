package com.skysam.hchirinos.myfinances.homeModule.interactor

/**
 * Created by Hector Chirinos in the home office on 7 ene. 2026
 */
// api.dolarvzla.com/public/exchange-rate
data class DolarVzlaExchangeRateResponse(
    val current: DolarVzlaSnapshot,
    val previous: DolarVzlaSnapshot,
    val changePercentage: DolarVzlaChangePercentage? = null
)

data class DolarVzlaSnapshot(
    val usd: Double,
    val eur: Double,
    val date: String // "2026-01-07"
)

data class DolarVzlaChangePercentage(
    val usd: Double,
    val eur: Double
)

// ve.dolarapi.com/v1/dolares/paralelo
data class DolarApiParaleloResponse(
    val fuente: String? = null,
    val nombre: String? = null,
    val compra: Double? = null,
    val venta: Double? = null,
    val promedio: Double,
    val fechaActualizacion: String // ISO
)
