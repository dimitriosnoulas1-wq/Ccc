package com.example.billing

import com.example.data.model.AppLanguage

/**
 * Paywall wording built from what Google Play actually returns: the formatted prices and,
 * only if the offer has one, the free-trial length. No price or trial is ever hard-coded.
 */
object PaywallText {

    data class Plan(val badge: String?, val button: String, val sub: String)

    /** Days in an ISO-8601 billing period such as P7D, P1W, P1M or P1Y (months as 30 days). */
    fun isoPeriodDays(period: String): Int? {
        val m = Regex("^P(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)W)?(?:(\\d+)D)?$").matchEntire(period) ?: return null
        val (y, mo, w, d) = m.destructured
        if (y.isEmpty() && mo.isEmpty() && w.isEmpty() && d.isEmpty()) return null
        return (y.toIntOrNull() ?: 0) * 365 + (mo.toIntOrNull() ?: 0) * 30 +
            (w.toIntOrNull() ?: 0) * 7 + (d.toIntOrNull() ?: 0)
    }

    fun monthly(lang: AppLanguage, price: String, trialDays: Int?): Plan {
        val t = trialDays?.takeIf { it > 0 }
        return when (lang) {
            AppLanguage.GREEK -> if (t != null) Plan("$t ημέρες δωρεάν", "Έναρξη δωρεάν δοκιμής $t ημερών", "$t ημέρες δωρεάν, μετά $price / μήνα. Ανανεώνεται αυτόματα, ακύρωση όποτε θέλεις.")
                else Plan(null, "Συνδρομή $price / μήνα", "$price / μήνα. Ανανεώνεται αυτόματα, ακύρωση όποτε θέλεις.")
            AppLanguage.GERMAN -> if (t != null) Plan("$t Tage gratis", "$t Tage gratis testen", "$t Tage gratis, danach $price / Monat. Verlängert sich automatisch, jederzeit kündbar.")
                else Plan(null, "Abonnieren für $price / Monat", "$price / Monat. Verlängert sich automatisch, jederzeit kündbar.")
            AppLanguage.FRENCH -> if (t != null) Plan("$t jours gratuits", "Essai gratuit de $t jours", "$t jours gratuits, puis $price / mois. Renouvellement automatique, résiliable à tout moment.")
                else Plan(null, "S'abonner à $price / mois", "$price / mois. Renouvellement automatique, résiliable à tout moment.")
            AppLanguage.SPANISH -> if (t != null) Plan("$t días gratis", "Prueba gratis de $t días", "$t días gratis, luego $price / mes. Se renueva automáticamente, cancela cuando quieras.")
                else Plan(null, "Suscribirse por $price / mes", "$price / mes. Se renueva automáticamente, cancela cuando quieras.")
            AppLanguage.ITALIAN -> if (t != null) Plan("$t giorni gratis", "Prova gratuita di $t giorni", "$t giorni gratis, poi $price / mese. Rinnovo automatico, annulli quando vuoi.")
                else Plan(null, "Abbonati a $price / mese", "$price / mese. Rinnovo automatico, annulli quando vuoi.")
            AppLanguage.ENGLISH -> if (t != null) Plan("$t-day free trial", "Start $t-day free trial", "$t days free, then $price / month. Renews automatically, cancel anytime.")
                else Plan(null, "Subscribe for $price / month", "$price / month. Renews automatically, cancel anytime.")
        }
    }

    fun yearlyButton(lang: AppLanguage, price: String): String = when (lang) {
        AppLanguage.GREEK -> "Ετήσια συνδρομή $price"
        AppLanguage.GERMAN -> "Jährlich für $price"
        AppLanguage.FRENCH -> "Annuel à $price"
        AppLanguage.SPANISH -> "Anual por $price"
        AppLanguage.ITALIAN -> "Annuale a $price"
        AppLanguage.ENGLISH -> "Yearly for $price"
    }

    fun yearlySub(lang: AppLanguage, price: String): String = when (lang) {
        AppLanguage.GREEK -> "$price χρεώνεται ετησίως. Ανανεώνεται αυτόματα, ακύρωση όποτε θέλεις."
        AppLanguage.GERMAN -> "$price jährlich abgerechnet. Verlängert sich automatisch, jederzeit kündbar."
        AppLanguage.FRENCH -> "$price facturé chaque année. Renouvellement automatique, résiliable à tout moment."
        AppLanguage.SPANISH -> "$price facturado al año. Se renueva automáticamente, cancela cuando quieras."
        AppLanguage.ITALIAN -> "$price addebitato ogni anno. Rinnovo automatico, annulli quando vuoi."
        AppLanguage.ENGLISH -> "$price billed yearly. Renews automatically, cancel anytime."
    }

    /** One line for lock screens and Settings; empty prices mean Play has not answered yet. */
    fun priceLine(lang: AppLanguage, monthly: String, yearly: String, trialDays: Int?): String {
        if (monthly.isBlank() || yearly.isBlank()) {
            return when (lang) {
                AppLanguage.GREEK -> "Τιμές στο Google Play"
                AppLanguage.GERMAN -> "Preise bei Google Play"
                AppLanguage.FRENCH -> "Prix sur Google Play"
                AppLanguage.SPANISH -> "Precios en Google Play"
                AppLanguage.ITALIAN -> "Prezzi su Google Play"
                AppLanguage.ENGLISH -> "Prices in Google Play"
            }
        }
        val (month, year) = when (lang) {
            AppLanguage.GREEK -> "μήνα" to "έτος"
            AppLanguage.GERMAN -> "Monat" to "Jahr"
            AppLanguage.FRENCH -> "mois" to "an"
            AppLanguage.SPANISH -> "mes" to "año"
            AppLanguage.ITALIAN -> "mese" to "anno"
            AppLanguage.ENGLISH -> "month" to "year"
        }
        val trial = monthly(lang, monthly, trialDays).badge?.let { " ($it)" }.orEmpty()
        return "$monthly / $month$trial · $yearly / $year"
    }
}
