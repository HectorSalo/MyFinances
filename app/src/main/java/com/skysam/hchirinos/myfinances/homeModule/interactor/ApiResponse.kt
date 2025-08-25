package com.skysam.hchirinos.myfinances.homeModule.interactor

/**
 * Created by Hector Chirinos on 19/11/2024.
 */

data class ApiResponse(
    val fuente: String,
    val nombre: String,
    val promedio: Float,
    val fechaActualizacion: String
)

data class DateTime(
    val date: String,
    val time: String
)

data class Monitors(
    val bcv: MonitorData,
    val enparalelovzla: MonitorData
)

data class MonitorData(
    val change: Float,
    val color: String,
    val image: String,
    val last_update: String,
    val percent: Float,
    val price: Float,
    val price_old: Float,
    val symbol: String,
    val title: String
)

