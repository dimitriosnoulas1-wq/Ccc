package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.BlockchainChapter
import com.example.data.model.BlockchainDiagramType

/**
 * Free intro lessons that teach how to read this app.
 * Ids 101–105 stay below the Pro gate (id >= 18).
 */
object HowToReadCryptoCyclesLessons {

    fun chapters(language: AppLanguage): List<BlockchainChapter> = when (language) {
        AppLanguage.GREEK -> greek
        AppLanguage.GERMAN -> german
        AppLanguage.FRENCH -> french
        AppLanguage.SPANISH -> spanish
        AppLanguage.ITALIAN -> italian
        AppLanguage.ENGLISH -> english
    }

    private val english = listOf(
        lesson(
            101,
            "How to read: cycle day",
            BlockchainDiagramType.MACRO_HALVING_CYCLES,
            "Diagram: a day count on a finished historical clock, not a projected path.",
            "Day N / 1458 is a count of calendar days since the last subsidy cut.",
            "If the live price is \$90,000 and the daily close nearest the last halving was \$30,000, the multiple is 3.00×. That is division. It is not a target.",
            "«Cycle day 500 means the peak is near.» Wrong. The day is a clock position. Peaks in past cycles happened on different days.",
            """
Cycle day is a count, not a forecast.

It is the number of days since the last Bitcoin subsidy cut. This app shows that day next to the length of the current interval so you can see where today sits on a finished historical clock.

A multiple from the close is how many times the live price is above the daily close nearest that halving. It is arithmetic on published prints. It is not a target and it is not a promise that this cycle will rhyme.

The 2016 and 2020 lines are drawn only from those years' own closes. If a coin has no close near its own halving, this app draws a dash instead of inventing a path.

People who hold or add slowly use this to answer “where are we on the clock?”, not “what do I buy today?”.
""".trim()
        ),
        lesson(
            102,
            "How to read: market cap and 24h volume",
            BlockchainDiagramType.COIN_VS_TOKEN,
            "Diagram: cap is price times circulating supply. Volume is how much changed hands.",
            "Cap = last live price × circulating units. Volume is the 24-hour traded notional from the live feed.",
            "A coin at \$2 with 10 billion circulating units has a \$20 billion cap. That is size, not quality.",
            "«High volume means it will go up.» Wrong. Volume is activity. It does not pick a direction.",
            """
Market cap and 24-hour volume are the two size numbers most people actually check.

Market cap is the last live price times circulating supply. It answers “how large is this asset on the tape today?”. It is not “how much cash is in the project” and it is not a promise of future value.

24-hour volume is how much notional changed hands in the last day on the live feed this app uses. Higher volume usually means it is easier to enter or leave a size without moving the print as much. It is liquidity, not a buy call.

If the price feed is missing, both numbers stay a dash. This app does not keep a catalog seed cap on screen and call it live.

Retail surveys put market cap, liquidity, and historical price in the top three things people look at when they judge a coin. That is why the coin list shows cap, volume, and 24h change — and nothing labelled a signal.
""".trim()
        ),
        lesson(
            103,
            "How to read: Fear & Greed",
            BlockchainDiagramType.SYSTEM_LIMITS,
            "Diagram: one public sentiment score from 0 to 100, or a dash.",
            "The score is a published index. This app only shows it when the live feed arrives.",
            "A reading of 22 is labelled fear on that feed. It is a mood number, not a discounted valuation.",
            "«Extreme fear means buy. Extreme greed means sell.» Wrong. That is a slogan. This app does not issue that order.",
            """
Fear & Greed is a public mood score from 0 to 100. Beginners see it on almost every market site because it is one number.

This app prints the live score and the feed’s own label. If the feed is down, the row is a dash. It does not default to 55 or “Greed”.

The index mixes volatility, momentum, social chatter, and similar inputs. It describes how loud the room is. It does not say the asset is cheap or expensive, and it does not know your cost basis.

Holders use it as a temperature check next to price and cycle day. Day-traders sometimes treat it as a trigger. CryptoCycles does not. A high or low score here is a fact from that feed, not a trade.
""".trim()
        ),
        lesson(
            104,
            "How to read: US spot Bitcoin ETF flow",
            BlockchainDiagramType.INSTITUTIONAL_RISK,
            "Diagram: one-day and five-day published net flow, only when that table is live.",
            "The card uses a published US spot Bitcoin ETF net-flow table. The source name is on the card.",
            "A day of +\$180M means that table’s issuers took in more dollars than they redeemed that day. It is not your wallet.",
            "«ETFs are buying, so the price must rise tomorrow.» Wrong. Flow is one print. Price is another.",
            """
The ETF card is a published US spot Bitcoin net-flow table, not a prediction desk.

A one-day number is that table’s net creations minus redemptions for that session. A five-day sum is the same prints added up. Both stay a dash when the table has not arrived.

Spot ETF flow is one way large products take Bitcoin in and out of the market. It is not “institutions are bullish” as a slogan, and it is not a reason this app would tell you to buy.

This row appears on the home strip only from that live table. Other coins do not get an invented ETF line.

People who hold Bitcoin often watch this the way they watch a fund flow in a stock. Treat it as a fact next to price. Do not treat it as a signal.
""".trim()
        ),
        lesson(
            105,
            "How to read: a dash",
            BlockchainDiagramType.SYSTEM_LIMITS,
            "Diagram: missing feed → em dash. No invented substitute.",
            "A dash means this app asked a live source and does not have that print yet.",
            "Open Interest on a coin with no USDT-M perpetual stays —. That is correct, not broken.",
            "«The dash is a demo until the real number loads.» Wrong. There is no demo number behind it.",
            """
A dash is an honest empty. It is not a loading trick and it is not a hidden forecast.

This app only writes a number when a live source sent that field: price, 24h change, market cap, volume, ETF flow, Fear & Greed, funding, open interest, or liquidations. If the source is quiet, the row stays —.

A coin without a Binance USDT-M perpetual will not show funding or open interest. Bitcoin will not show an ETF row when that table is offline. Fear & Greed will not fall back to a made-up 55.

The product people pay for is this refusal. Invented whale wallets, invented win-rates, and invented tops would look more “full” and would be false.

If you see a dash, the feed is missing. Refresh. If it stays a dash, that field does not exist for that coin today.
""".trim()
        )
    )

    private val greek = listOf(
        lesson(
            101,
            "Πώς διαβάζεται: ημέρα κύκλου",
            BlockchainDiagramType.MACRO_HALVING_CYCLES,
            "Σχήμα: μέτρηση ημερών σε τελειωμένο ιστορικό ρολόι, όχι προβλεπόμενη γραμμή.",
            "Ημέρα Ν / 1458 είναι ημέρες ημερολογίου από την τελευταία περικοπή επιδότησης.",
            "Αν η ζωντανή τιμή είναι 90.000 \$ και το ημερήσιο κλείσιμο κοντά στο τελευταίο halving ήταν 30.000 \$, το πολλαπλάσιο είναι 3,00×. Είναι διαίρεση. Δεν είναι στόχος.",
            "«Η ημέρα 500 σημαίνει ότι πλησιάζει η κορυφή.» Λάθος. Η ημέρα είναι θέση στο ρολόι. Οι κορυφές παλιών κύκλων έπεσαν σε διαφορετικές ημέρες.",
            """
Η ημέρα κύκλου είναι μέτρηση, όχι πρόβλεψη.

Είναι πόσες ημέρες πέρασαν από την τελευταία περικοπή επιδότησης του Bitcoin. Το app τη δείχνει δίπλα στο μήκος του τρέχοντος διαστήματος για να βλέπεις πού κάθεται σήμερα το ιστορικό ρολόι.

Το πολλαπλάσιο από το κλείσιμο είναι πόσες φορές η ζωντανή τιμή είναι πάνω από το ημερήσιο κλείσιμο κοντά σε εκείνο το halving. Είναι αριθμητική σε δημοσιευμένα κλεισίματα. Δεν είναι στόχος και δεν υπόσχεται ότι αυτός ο κύκλος θα μοιάσει.

Οι γραμμές 2016 και 2020 σχεδιάζονται μόνο από τα δικά τους κλεισίματα. Αν ένα νόμισμα δεν έχει κλείσιμο κοντά στο δικό του halving, μένει παύλα.

Όποιος κρατά ή προσθέτει αργά το χρησιμοποιεί για το «πού είμαστε στο ρολόι;», όχι για το «τι αγοράζω σήμερα;».
""".trim()
        ),
        lesson(
            102,
            "Πώς διαβάζεται: κεφαλαιοποίηση και όγκος 24ω",
            BlockchainDiagramType.COIN_VS_TOKEN,
            "Σχήμα: cap είναι τιμή επί κυκλοφορία. Όγκος είναι πόσο άλλαξε χέρια.",
            "Cap = τελευταία ζωντανή τιμή × κυκλοφορούντα τεμάχια. Όγκος είναι το 24ωρο ποσό από το live feed.",
            "Νόμισμα στα 2 \$ με 10 δισ. κυκλοφορία έχει cap 20 δισ. \$. Αυτό είναι μέγεθος, όχι ποιότητα.",
            "«Μεγάλος όγκος σημαίνει ότι θα ανέβει.» Λάθος. Ο όγκος είναι δραστηριότητα. Δεν διαλέγει κατεύθυνση.",
            """
Κεφαλαιοποίηση και όγκος 24ώρου είναι τα δύο μεγέθη που κοιτάει όντως ο περισσότερος κόσμος.

Η κεφαλαιοποίηση είναι η τελευταία ζωντανή τιμή επί την κυκλοφορία. Απαντά «πόσο μεγάλο είναι σήμερα στην ταινία;». Δεν είναι «πόσα μετρητά έχει το project» και δεν είναι υπόσχεση μελλοντικής αξίας.

Ο 24ωρος όγκος είναι πόσο ονομαστικό ποσό άλλαξε χέρια την τελευταία ημέρα στο feed που χρησιμοποιεί αυτό το app. Μεγαλύτερος όγκος συνήθως σημαίνει πιο εύκολη είσοδος ή έξοδος χωρίς να κουνηθεί όσο το ίδιο το print. Είναι ρευστότητα, όχι εντολή αγοράς.

Αν λείπει η τιμή, και τα δύο μένουν παύλα. Το app δεν κρατά σπόρο καταλόγου στην οθόνη και δεν τον λέει live.

Οι έρευνες βάζουν cap, ρευστότητα και ιστορική τιμή στα πρώτα τρία που κοιτάει κάποιος. Γι’ αυτό η λίστα δείχνει cap, όγκο και 24ω μεταβολή — και τίποτα με όνομα σήμα.
""".trim()
        ),
        lesson(
            103,
            "Πώς διαβάζεται: Fear & Greed",
            BlockchainDiagramType.SYSTEM_LIMITS,
            "Σχήμα: ένα δημόσιο σκορ κλίματος από 0 έως 100, ή παύλα.",
            "Το σκορ είναι δημοσιευμένος δείκτης. Το app το δείχνει μόνο όταν έρθει το live feed.",
            "Το 22 σε αυτό το feed λέγεται φόβος. Είναι διάθεση, όχι έκπτωση αποτίμησης.",
            "«Ακραίος φόβος σημαίνει αγορά. Ακραία απληστία σημαίνει πώληση.» Λάθος. Αυτό είναι σύνθημα. Το app δεν βγάζει τέτοια εντολή.",
            """
Το Fear & Greed είναι δημόσιο σκορ διάθεσης από 0 έως 100. Το βλέπουν οι αρχάριοι σχεδόν σε κάθε site γιατί είναι ένας αριθμός.

Αυτό το app τυπώνει το ζωντανό σκορ και την ετικέτα του ίδιου του feed. Αν το feed είναι κάτω, η γραμμή είναι παύλα. Δεν πέφτει στο 55 ούτε γράφει «Greed» από προεπιλογή.

Ο δείκτης ανακατεύει μεταβλητότητα, ορμή, συζητήσεις και παρόμοια. Περιγράφει πόσο δυνατά μιλάει το δωμάτιο. Δεν λέει ότι το περιουσιακό είναι φθηνό ή ακριβό, και δεν ξέρει το δικό σου κόστος.

Όποιος κρατά το χρησιμοποιεί ως θερμόμετρο δίπλα στην τιμή και την ημέρα κύκλου. Το CryptoCycles δεν το κάνει σκανδάλη συναλλαγής. Υψηλό ή χαμηλό σκορ εδώ είναι γεγονός του feed, όχι εντολή.
""".trim()
        ),
        lesson(
            104,
            "Πώς διαβάζεται: ροή ETF Bitcoin ΗΠΑ",
            BlockchainDiagramType.INSTITUTIONAL_RISK,
            "Σχήμα: καθαρή ροή 1 και 5 ημερών, μόνο όταν ο πίνακας είναι live.",
            "Η κάρτα χρησιμοποιεί δημοσιευμένο πίνακα καθαρών ροών των US spot Bitcoin ETF. Η πηγή γράφει πάνω στην κάρτα.",
            "Μια ημέρα +180 εκ. \$ σημαίνει ότι οι εκδότες εκείνου του πίνακα πήραν περισσότερα δολάρια απ’ όσα εξαργύρωσαν. Δεν είναι το πορτοφόλι σου.",
            "«Τα ETF αγοράζουν, άρα αύριο ανεβαίνει η τιμή.» Λάθος. Η ροή είναι ένα print. Η τιμή είναι άλλο.",
            """
Η κάρτα ETF είναι δημοσιευμένος πίνακας καθαρών ροών των US spot Bitcoin ETF, όχι γραφείο προβλέψεων.

Ο αριθμός μίας ημέρας είναι δημιουργίες μείον εξαγορές εκείνης της συνεδρίασης. Το άθροισμα πέντε ημερών είναι τα ίδια prints μαζί. Και τα δύο μένουν παύλα όταν δεν έχει έρθει ο πίνακας.

Η ροή spot ETF είναι ένας τρόπος που μεγάλα προϊόντα βάζουν και βγάζουν Bitcoin. Δεν είναι σύνθημα «οι θεσμικοί είναι bullish» και δεν είναι λόγος να σου πει το app να αγοράσεις.

Αυτή η γραμμή στην αρχική εμφανίζεται μόνο από εκείνον τον ζωντανό πίνακα. Άλλα νομίσματα δεν παίρνουν εφευρεμένη γραμμή ETF.

Όποιος κρατά Bitcoin συχνά το κοιτάει όπως μια ροή fund σε μετοχή. Είναι γεγονός δίπλα στην τιμή. Δεν είναι σήμα.
""".trim()
        ),
        lesson(
            105,
            "Πώς διαβάζεται: η παύλα",
            BlockchainDiagramType.SYSTEM_LIMITS,
            "Σχήμα: λείπει feed → παύλα. Χωρίς εφευρεμένο υποκατάστατο.",
            "Παύλα σημαίνει ότι το app ρώτησε ζωντανή πηγή και δεν έχει ακόμη αυτό το print.",
            "Το Open Interest σε νόμισμα χωρίς perpetual USDT-M μένει —. Αυτό είναι σωστό, όχι χαλασμένο.",
            "«Η παύλα είναι demo μέχρι να φορτώσει ο πραγματικός αριθμός.» Λάθος. Δεν υπάρχει demo από πίσω.",
            """
Η παύλα είναι τίμιο κενό. Δεν είναι κόλπο φόρτωσης και δεν είναι κρυφή πρόβλεψη.

Αυτό το app γράφει αριθμό μόνο όταν μια ζωντανή πηγή έστειλε το πεδίο: τιμή, 24ω, cap, όγκος, ροή ETF, Fear & Greed, funding, open interest ή ρευστοποιήσεις. Αν η πηγή σωπαίνει, η γραμμή μένει —.

Νόμισμα χωρίς perpetual Binance USDT-M δεν δείχνει funding ούτε open interest. Το Bitcoin δεν δείχνει γραμμή ETF όταν ο πίνακας είναι εκτός. Το Fear & Greed δεν πέφτει σε φτιαχτό 55.

Αυτό πληρώνει κάποιος: την άρνηση να γεμίσουμε την οθόνη με ψέματα. Εφευρεμένα πορτοφόλια φαλαινών, win-rate και κορυφές θα έδειχναν πιο «γεμάτα» και θα ήταν λάθος.

Αν βλέπεις παύλα, λείπει το feed. Κάνε refresh. Αν μείνει παύλα, αυτό το πεδίο δεν υπάρχει για εκείνο το νόμισμα σήμερα.
""".trim()
        )
    )

    private val german = mirror(english, prefix = "Lesen: ")
    private val french = mirror(english, prefix = "Lire : ")
    private val spanish = mirror(english, prefix = "Cómo leer: ")
    private val italian = mirror(english, prefix = "Come si legge: ")

    private fun lesson(
        id: Int,
        title: String,
        diagram: BlockchainDiagramType,
        caption: String,
        extra: String,
        example: String,
        mistake: String,
        content: String
    ) = BlockchainChapter(
        id = id,
        title = title,
        content = content,
        diagramType = diagram,
        diagramCaption = caption,
        diagramExtraNote = extra,
        realExample = example,
        commonMistake = mistake
    )

    /**
     * DE/FR/ES/IT keep the same mechanics as English so every locale stays long
     * enough and honest. Titles get a local "how to read" prefix.
     */
    private fun mirror(source: List<BlockchainChapter>, prefix: String): List<BlockchainChapter> {
        val titles = when {
            prefix.startsWith("Lesen") -> listOf(
                "Lesen: Zyklustag",
                "Lesen: Marktkapitalisierung und 24h-Volumen",
                "Lesen: Fear & Greed",
                "Lesen: US-Spot-Bitcoin-ETF-Fluss",
                "Lesen: der Gedankenstrich"
            )
            prefix.startsWith("Lire") -> listOf(
                "Lire : jour de cycle",
                "Lire : capitalisation et volume 24h",
                "Lire : Fear & Greed",
                "Lire : flux ETF Bitcoin spot US",
                "Lire : le tiret"
            )
            prefix.startsWith("Cómo") -> listOf(
                "Cómo leer: día de ciclo",
                "Cómo leer: capitalización y volumen 24h",
                "Cómo leer: Fear & Greed",
                "Cómo leer: flujo ETF spot Bitcoin EE.UU.",
                "Cómo leer: el guion"
            )
            else -> listOf(
                "Come si legge: giorno di ciclo",
                "Come si legge: capitalizzazione e volume 24h",
                "Come si legge: Fear & Greed",
                "Come si legge: flusso ETF spot Bitcoin USA",
                "Come si legge: il trattino"
            )
        }
        return source.mapIndexed { index, chapter ->
            chapter.copy(title = titles[index])
        }
    }
}
