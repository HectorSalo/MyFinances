package com.skysam.hchirinos.myfinances.bolsadecaracas.data

import com.google.firebase.Timestamp

data class BvcPosition(
    val symbol: String = "",
    val quantity: Double = 0.0,
    val averagePrice: Double = 0.0,
    val updatedAt: Timestamp? = null
)

