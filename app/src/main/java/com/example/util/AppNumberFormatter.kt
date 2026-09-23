package com.example.util

import com.example.data.model.AppLanguage
import com.example.data.model.Currency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Single, unified locale-aware number and money formatter for CryptoCycles.
 * 
 * Rules:
 * - UI language Greek (el) -> Locale("el", "GR") (thousands = '.', decimal = ',')
 *   Examples: 77.261,00, 4.100,4 BTC, 321,4 εκ. or $321,4M, +0,32%
 * - UI language English or others -> Locale.US (thousands = ',', decimal = '.')
 *   Examples: 77,261.00, 4,100.4 BTC, $321.4M, +0.32%
 */
object AppNumberFormatter {

    // Current active locale determined by AppLanguage
    @Volatile
    var currentLanguage: AppLanguage = AppLanguage.ENGLISH

    fun getLocale(language: AppLanguage = currentLanguage): Locale {
        return if (language == AppLanguage.GREEK || language.code == "el") {
            Locale.forLanguageTag("el-GR")
        } else {
            Locale.US
        }
    }

    private fun getDecimalFormatSymbols(locale: Locale): DecimalFormatSymbols {
        return DecimalFormatSymbols.getInstance(locale)
    }

    /**
     * Standard price formatter respecting currency symbol and locale.
     */
    fun formatPrice(
        price: Double,
        currency: Currency = Currency.USD,
        language: AppLanguage = currentLanguage,
        decimals: Int? = null
    ): String {
        if (price <= 0.0 || price.isNaN()) return "—"
        val converted = price * currency.rateToUsd
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)

        val pattern = if (decimals != null) {
            when (decimals) {
                0 -> "#,##0"
                1 -> "#,##0.0"
                2 -> "#,##0.00"
                3 -> "#,##0.000"
                4 -> "#,##0.0000"
                6 -> "0.000000"
                else -> "0.00000000"
            }
        } else {
            when {
                converted >= 1000.0 -> "#,##0.00"
                converted >= 1.0 -> "#,##0.00"
                converted >= 0.01 -> "#,##0.0000"
                converted >= 0.0001 -> "0.000000"
                else -> "0.00000000"
            }
        }
        val df = DecimalFormat(pattern, symbols)
        return "${currency.symbol}${df.format(converted)}"
    }

    fun formatPrice(
        price: Float,
        currency: Currency = Currency.USD,
        language: AppLanguage = currentLanguage,
        decimals: Int? = null
    ): String = formatPrice(price.toDouble(), currency, language, decimals)

    /**
     * Raw price formatting with no currency symbol prefix.
     */
    fun formatRawPrice(
        price: Double,
        decimals: Int = 2,
        language: AppLanguage = currentLanguage
    ): String {
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)
        val pattern = when (decimals) {
            0 -> "#,##0"
            1 -> "#,##0.0"
            2 -> "#,##0.00"
            3 -> "#,##0.000"
            4 -> "#,##0.0000"
            6 -> "0.000000"
            else -> "0.00000000"
        }
        val df = DecimalFormat(pattern, symbols)
        return df.format(price)
    }

    fun formatRawPrice(
        price: Float,
        decimals: Int = 2,
        language: AppLanguage = currentLanguage
    ): String = formatRawPrice(price.toDouble(), decimals, language)

    /**
     * Percents: always sign + 2 fraction digits (+0,32% / -38,70% in el, +0.32% in en)
     */
    fun formatPercent(
        percent: Double,
        includeSign: Boolean = true,
        decimals: Int = 2,
        language: AppLanguage = currentLanguage
    ): String {
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)
        val pattern = if (decimals == 1) "0.0" else "0.00"
        val df = DecimalFormat(pattern, symbols)
        val sign = if (includeSign && percent > 0.0) "+" else ""
        return "$sign${df.format(percent)}%"
    }

    fun formatPercent(
        percent: Float,
        includeSign: Boolean = true,
        decimals: Int = 2,
        language: AppLanguage = currentLanguage
    ): String = formatPercent(percent.toDouble(), includeSign, decimals, language)

    /**
     * Compact millions/billions currency formatting (e.g. $321.4M / $321,4M, $2.45B / $2,45B)
     */
    fun formatCompactCurrency(
        amountUsd: Double,
        currency: Currency = Currency.USD,
        language: AppLanguage = currentLanguage,
        currencySymbol: String? = null
    ): String {
        val converted = amountUsd * currency.rateToUsd
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)
        val df2 = DecimalFormat("0.00", symbols)
        val symbol = currencySymbol ?: currency.symbol

        return when {
            converted >= 1_000_000_000_000.0 -> "$symbol${df2.format(converted / 1_000_000_000_000.0)}T"
            converted >= 1_000_000_000.0 -> "$symbol${df2.format(converted / 1_000_000_000.0)}B"
            converted >= 1_000_000.0 -> "$symbol${df2.format(converted / 1_000_000.0)}M"
            converted >= 1_000.0 -> "$symbol${df2.format(converted / 1_000.0)}K"
            else -> "$symbol${df2.format(converted)}"
        }
    }

    fun formatCompactCurrency(
        amountUsd: Float,
        currency: Currency = Currency.USD,
        language: AppLanguage = currentLanguage,
        currencySymbol: String? = null
    ): String = formatCompactCurrency(amountUsd.toDouble(), currency, language, currencySymbol)

    /**
     * Compact token/crypto amount (e.g. 4,100.4 BTC or 4.100,4 BTC)
     */
    fun formatCryptoAmount(
        amount: Double,
        symbol: String = "",
        language: AppLanguage = currentLanguage
    ): String {
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)
        val df = when {
            amount >= 1000.0 -> DecimalFormat("#,##0.0", symbols)
            amount >= 1.0 -> DecimalFormat("#,##0.00", symbols)
            else -> DecimalFormat("0.0000", symbols)
        }
        val formatted = df.format(amount)
        return if (symbol.isNotEmpty()) "$formatted $symbol" else formatted
    }

    /**
     * Compact number formatting without currency symbol (e.g. 1.2M / 1,2M)
     */
    fun formatCompactNumber(
        number: Double,
        language: AppLanguage = currentLanguage
    ): String {
        val locale = getLocale(language)
        val symbols = getDecimalFormatSymbols(locale)
        val df = DecimalFormat("0.00", symbols)

        return when {
            number >= 1_000_000_000.0 -> "${df.format(number / 1_000_000_000.0)}B"
            number >= 1_000_000.0 -> "${df.format(number / 1_000_000.0)}M"
            number >= 1_000.0 -> "${df.format(number / 1_000.0)}K"
            else -> df.format(number)
        }
    }
}
