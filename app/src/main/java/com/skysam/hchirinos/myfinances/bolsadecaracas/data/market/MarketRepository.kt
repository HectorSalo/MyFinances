package com.skysam.hchirinos.myfinances.bolsadecaracas.data.market

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para cotizaciones de mercado.
 * Orquesta la obtención desde la fuente remota.
 */
@Singleton
class MarketRepository @Inject constructor(
    private val remoteDataSource: MarketRemoteDataSource
) {
    suspend fun getMarketQuotes(): Result<List<MarketQuote>> {
        return remoteDataSource.fetchMarketQuotes()
    }
}
