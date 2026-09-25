package com.example.ui.rainbow

/**
 * Χρονολογημένα γεγονότα της αγοράς Bitcoin.
 *
 * Κανόνες ακρίβειας:
 *  - Εδώ γράφεται ΜΟΝΟ η ημερομηνία (UTC) και τι έγινε. Καμία τιμή, κανένα ποσοστό:
 *    αυτά υπολογίζονται από τα πραγματικά ημερήσια δεδομένα στο [CycleInsights].
 *  - Η "μέρα μετά το halving" δεν γράφεται ποτέ με το χέρι· βγαίνει από την ημερομηνία.
 *  - Οι κορυφές/πάτοι κύκλων ΔΕΝ είναι εδώ: βρίσκονται από τα δεδομένα.
 */
object RainbowEvents {

    enum class Kind { CRASH, NEWS, MILESTONE }

    data class Event(val day: Long, val kind: Kind, val el: String, val en: String) {
        fun text(greek: Boolean) = if (greek) el else en
    }

    private fun e(y: Int, m: Int, d: Int, kind: Kind, el: String, en: String) =
        Event(DateUtil.epochDay(y, m, d), kind, el, en)

    val ALL: List<Event> = listOf(
        // ── Κύκλος 2012 ──
        e(2013, 4, 10, Kind.CRASH,
            "Flash crash μετά το ανοιξιάτικο ράλι· το Mt. Gox δεν άντεχε τον όγκο",
            "Flash crash after the spring rally; Mt. Gox buckled under the volume"),
        e(2013, 10, 2, Kind.NEWS,
            "Το FBI κλείνει το Silk Road",
            "The FBI shuts down Silk Road"),
        e(2013, 12, 5, Kind.CRASH,
            "Η Κεντρική Τράπεζα της Κίνας απαγορεύει στις τράπεζες να χειρίζονται bitcoin",
            "China's central bank bars banks from handling bitcoin"),
        e(2014, 2, 24, Kind.CRASH,
            "Το Mt. Gox σταματά κάθε συναλλαγή (πτώχευση στις 28 Φεβ)",
            "Mt. Gox halts all trading (bankruptcy filed 28 Feb)"),
        // ── Κύκλος 2016 ──
        e(2016, 8, 2, Kind.CRASH,
            "Hack στο Bitfinex: κλάπηκαν περίπου 120.000 BTC",
            "Bitfinex hack: about 120,000 BTC stolen"),
        e(2017, 8, 1, Kind.NEWS,
            "Διάσπαση: γεννιέται το Bitcoin Cash",
            "Fork: Bitcoin Cash is created"),
        e(2017, 9, 4, Kind.CRASH,
            "Η Κίνα απαγορεύει τα ICO",
            "China bans ICOs"),
        e(2017, 12, 10, Kind.MILESTONE,
            "Ξεκινούν τα πρώτα bitcoin futures (CBOE· το CME στις 18 Δεκ)",
            "First bitcoin futures launch (CBOE; CME followed on 18 Dec)"),
        e(2018, 11, 15, Kind.CRASH,
            "\"Hash war\" στο Bitcoin Cash· απότομη πτώση σε όλη την αγορά εκείνη την εβδομάδα",
            "Bitcoin Cash \"hash war\"; sharp market-wide drop that week"),
        e(2020, 3, 12, Kind.CRASH,
            "COVID crash (\"Black Thursday\"): μία από τις μεγαλύτερες ημερήσιες πτώσεις",
            "COVID crash (\"Black Thursday\"): one of the largest daily drops"),
        // ── Κύκλος 2020 ──
        e(2020, 8, 11, Kind.MILESTONE,
            "Η MicroStrategy ανακοινώνει την πρώτη αγορά BTC",
            "MicroStrategy announces its first BTC purchase"),
        e(2021, 2, 8, Kind.MILESTONE,
            "Η Tesla αποκαλύπτει αγορά BTC αξίας 1,5 δισ. $",
            "Tesla discloses a \$1.5B BTC purchase"),
        e(2021, 4, 14, Kind.MILESTONE,
            "Η Coinbase εισάγεται στο Nasdaq",
            "Coinbase lists on Nasdaq"),
        e(2021, 5, 12, Kind.CRASH,
            "Η Tesla σταματά να δέχεται πληρωμές σε BTC",
            "Tesla stops accepting BTC payments"),
        e(2021, 5, 19, Kind.CRASH,
            "Crash μετά τις προειδοποιήσεις της Κίνας για τα crypto",
            "Crash after China's crypto warnings"),
        e(2021, 9, 7, Kind.MILESTONE,
            "Το Ελ Σαλβαδόρ κάνει το BTC νόμιμο χρήμα",
            "El Salvador makes BTC legal tender"),
        e(2021, 10, 19, Kind.MILESTONE,
            "Πρώτο bitcoin futures ETF στις ΗΠΑ (BITO)",
            "First US bitcoin futures ETF (BITO)"),
        e(2022, 5, 9, Kind.CRASH,
            "Αρχίζει η κατάρρευση UST/LUNA",
            "UST/LUNA collapse begins"),
        e(2022, 6, 12, Kind.CRASH,
            "Η Celsius παγώνει τις αναλήψεις",
            "Celsius freezes withdrawals"),
        e(2022, 11, 8, Kind.CRASH,
            "Κρίση FTX: παγώνουν οι αναλήψεις (πτώχευση στις 11 Νοε)",
            "FTX crisis: withdrawals halted (bankruptcy on 11 Nov)"),
        e(2023, 3, 10, Kind.CRASH,
            "Κατάρρευση της Silicon Valley Bank· το USDC χάνει προσωρινά το 1 $",
            "Silicon Valley Bank fails; USDC briefly loses its \$1 peg"),
        e(2024, 1, 10, Kind.MILESTONE,
            "Εγκρίνονται τα spot bitcoin ETF στις ΗΠΑ",
            "US spot bitcoin ETFs approved"),
        // ── Κύκλος 2024 ──
        e(2024, 8, 5, Kind.CRASH,
            "Παγκόσμιο sell-off (ξετύλιγμα του yen carry trade)",
            "Global sell-off (yen carry trade unwind)"),
        e(2024, 11, 5, Kind.NEWS,
            "Αμερικανικές εκλογές",
            "US presidential election"),
        e(2024, 12, 5, Kind.MILESTONE,
            "Το BTC περνά πρώτη φορά τα 100.000 $",
            "BTC crosses \$100,000 for the first time"),
        e(2025, 2, 21, Kind.CRASH,
            "Hack στο Bybit (~1,5 δισ. $ σε ETH)",
            "Bybit hack (~\$1.5B in ETH)"),
        e(2025, 3, 6, Kind.MILESTONE,
            "Εκτελεστικό διάταγμα των ΗΠΑ για Στρατηγικό Αποθεματικό Bitcoin",
            "US executive order creates a Strategic Bitcoin Reserve"),
        e(2025, 10, 10, Kind.CRASH,
            "Η μεγαλύτερη μέρα ρευστοποιήσεων στην ιστορία των crypto",
            "The largest liquidation day in crypto history"),
    ).sortedBy { it.day }
}
