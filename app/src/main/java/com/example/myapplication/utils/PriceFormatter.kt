package com.example.myapplication.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PriceFormatter {
    fun formatPrice(price: Double): String {
        val symbols = DecimalFormatSymbols(Locale("es", "CL"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        
        val formatter = DecimalFormat("#,##0", symbols)
        return "$${formatter.format(price)}"
    }
}