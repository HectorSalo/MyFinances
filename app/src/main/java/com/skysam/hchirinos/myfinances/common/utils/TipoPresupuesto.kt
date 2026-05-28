package com.skysam.hchirinos.myfinances.common.utils

/**
 * Valores permitidos para el campo [Constants.BD_TIPO_PRESUPUESTO] en documentos de gastos.
 *
 * Este campo es exclusivo del módulo de gastos. Los documentos de ingresos
 * no lo escriben ni lo leen; los documentos legacy sin el campo se tratan
 * como [GASTO_NORMAL].
 */
object TipoPresupuesto {
    /** Gasto o compromiso normal (valor por defecto / legacy). */
    const val GASTO_NORMAL = "GASTO_NORMAL"

    /** Dinero destinado a capitalizar o ahorro patrimonial. */
    const val AHORRO_CAPITALIZABLE = "AHORRO_CAPITALIZABLE"

    /** Dinero destinado a reducir deudas. */
    const val PAGO_DEUDA = "PAGO_DEUDA"
}
