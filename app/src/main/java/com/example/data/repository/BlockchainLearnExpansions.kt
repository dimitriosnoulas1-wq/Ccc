package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.BlockchainChapter

/**
 * Adds a second page of mechanics to free chapters 1–17 so each lesson
 * fills the screen. Pro chapters 18–22 are already written long in the
 * repository and are left untouched here.
 */
object BlockchainLearnExpansions {

    fun expand(language: AppLanguage, chapters: List<BlockchainChapter>): List<BlockchainChapter> {
        return chapters.map { chapter ->
            val extra = extraFor(language, chapter.id) ?: return@map chapter
            chapter.copy(content = chapter.content.trimEnd() + "\n\n" + extra)
        }
    }

    private fun extraFor(language: AppLanguage, id: Int): String? {
        val pack = when (language) {
            AppLanguage.GREEK -> greek
            AppLanguage.ENGLISH -> english
            AppLanguage.GERMAN -> german
            AppLanguage.FRENCH -> french
            AppLanguage.SPANISH -> spanish
            AppLanguage.ITALIAN -> italian
        }
        return pack[id]
    }

    private val greek = mapOf(
        1 to "Το δημόσιο ιστορικό δεν είναι αντίγραφο ασφαλείας στο σύννεφο. Είναι συμφωνία κανόνων: ποια συναλλαγή μπήκε, ποια είσοδος έχει ήδη ξοδευτεί, ποιο block ακολουθεί.\n\nΓι’ αυτό το Learn ξεκινά από εδώ. Πριν από γραφήματα και ημέρες κύκλου πρέπει να είναι σαφές τι μετράει ως έγκυρη μεταφορά αξίας.",
        2 to "Ένα full node μπορεί να απορρίψει παράνομο block ακόμη κι αν το έβγαλε μεγάλο mining pool. Η ισχύς του pool δεν αντικαθιστά τους κανόνες.\n\nΟι πλατφόρμες φύλαξης είναι πάλι κεντρικές βάσεις για τα κλειδιά που κρατούν. Το chain από κάτω μπορεί να είναι κατανεμημένο και εσύ να κάθεσαι σε έναν θεματοφύλακα.",
        3 to "Το ίδιο μήνυμα δίνει πάντα το ίδιο hash. Δύο διαφορετικά μηνύματα σχεδόν ποτέ δεν δίνουν το ίδιο αποτύπωμα, αν η συνάρτηση είναι σωστή.\n\nΌταν ένας explorer δείχνει «block hash», δείχνουν αυτό το αποτύπωμα της επικεφαλίδας. Δεν είναι κωδικός ξεκλειδώματος του νομίσματος.",
        4 to "Αν χάσεις το ιδιωτικό κλειδί, κανένα τμήμα υποστήριξης του δικτύου δεν το επαναφέρει. Δεν υπάρχει «reset password» στο πρωτόκολλο.\n\nCustodial λογαριασμός σημαίνει ότι άλλος κρατά το κλειδί. Τότε η κυριαρχία είναι συμβόλαιο με την εταιρεία, όχι μόνο υπογραφή στο chain.",
        5 to "Μια συναλλαγή στο mempool μπορεί να αντικατασταθεί ή να μείνει απλήρωτη αν το fee είναι χαμηλό. «Απεστάλη» στο wallet δεν σημαίνει «κλείδωσε στο ιστορικό».\n\nΟι επιβεβαιώσεις μετράνε πόσα blocks χτίστηκαν από πάνω. Το πόσο τελικό είναι εξαρτάται από το chain και από το πόσο ρίσκο δέχεται ο παραλήπτης.",
        6 to "Στο UTXO κάθε έξοδος ξοδεύεται ολόκληρη. Αν έχεις 0,08 και στέλνεις 0,03, φτιάχνεις νέα έξοδο ρέστου πίσω στη δική σου διεύθυνση.\n\nΣτο μοντέλο λογαριασμών αλλάζει ένας αριθμός κατάστασης. Γι’ αυτό ένα explorer Ethereum δείχνει υπόλοιπο, ενώ ένα explorer Bitcoin δείχνει αδιάθετες εξόδους.",
        7 to "Το Merkle root επιτρέπει απόδειξη ότι μια συναλλαγή ανήκει στο block χωρίς να κατεβάσεις όλες τις άλλες.\n\nΤο όριο μεγέθους / gas δεν είναι «κακία των miners». Είναι το ταβάνι χώρου της σελίδας ιστορικού. Όταν η ζήτηση ξεπερνά αυτό το ταβάνι, ανεβαίνουν τα fees.",
        8 to "Μικρά reorgs συμβαίνουν όταν δύο έγκυρα blocks εμφανιστούν σχεδόν μαζί. Το δίκτυο διαλέγει μετά ποιο κλαδί συνεχίζει.\n\nΤο «αμετάβλητο» είναι κόστος ανατροπής, όχι θεολογία. Σε μεγάλο PoW το κόστος είναι hashrate. Σε PoS είναι stake και slashing.",
        9 to "Light client και mobile wallet ρωτούν συχνά RPC τρίτου. Βλέπεις υπόλοιπο που σου λέει μια υπηρεσία.\n\nΑν θέλεις να ελέγχεις τους κανόνες μόνος σου, τρέχεις full node. Αυτό είναι βαρύτερο. Δεν το κάνει το κουμπί «άνοιξε wallet».",
        10 to "Consensus είναι ο κανόνας επιλογής ιστορίας όταν υπάρχουν δύο έγκυρα κλαδιά, όχι δημοψήφισμα σε forum.\n\nΗ κοινωνική διακυβέρνηση (ποιον client τρέχει ο κόσμος) είναι άλλο στρώμα. Μπορεί να οδηγήσει σε fork με νέους κανόνες.",
        11 to "Ο γρίφος του Bitcoin είναι σκόπιμα αυθαίρετος: hash μέχρι να πέσει κάτω από στόχο. Δεν λύνει επιστημονικές εξισώσεις.\n\nΗ προσαρμογή δυσκολίας κρατά τον μέσο χρόνο block σταθερό όταν αλλάζει το hashrate. Χωρίς αυτήν, τα blocks θα έτρεχαν ή θα κόλλαγαν.",
        12 to "Το slashing τιμωρεί διπλή υπογραφή ή αντικρουόμενα blocks. Το κόστος επίθεσης είναι κεφάλαιο που μπορεί να καεί, όχι ρεύμα.\n\nΔιαφορετικά δίκτυα PoS έχουν άλλα slots, epochs και ποινές. Μην μεταφέρεις τους κανόνες του Ethereum σε κάθε chain.",
        13 to "Κάθε κόμβος έχει το δικό του mempool. Δεν υπάρχει μία παγκόσμια ουρά.\n\nΥψηλότερο fee αγοράζει προτεραιότητα στον παραγωγό block, όχι «πιο έγκυρη» συναλλαγή. Άκυρη υπογραφή απορρίπτεται και με μεγάλο fee.",
        14 to "Η seed phrase παράγει πολλά κλειδιά. Όποιος την έχει, ξαναφτιάχνει τις διευθύνσεις.\n\nScreenshot, cloud backup και «το έστειλα σε μένα στο mail» είναι συχνοί τρόποι κλοπής. Το seed είναι το ταμείο, όχι υπενθύμιση.",
        15 to "Ένα token ERC-20 ζει όσο ζει το συμβόλαιο και το host chain. Αν το συμβόλαιο έχει admin key, ο κανόνας μπορεί να αλλάξει από έναν κάτοχο.\n\nNative coin πληρώνει το fee του ίδιου του πρωτοκόλλου. Token συνήθως πληρώνει fee στο native coin, όχι «στον εαυτό του».",
        16 to "Η εκτέλεση είναι ντετερμινιστική: οι validators τρέχουν τον ίδιο κώδικα στα ίδια δεδομένα και πρέπει να δουν την ίδια νέα κατάσταση.\n\nBug, oracle και admin keys μένουν. «Αυτόματο» σημαίνει χωρίς υπάλληλο στη μέση, όχι χωρίς ρίσκο.",
        17 to "L2 και rollups κερδίζουν ρυθμό εισάγοντας νέα μέρη: sequencer, prover, γέφυρα. Άλλαξε το σύνολο εμπιστοσύνης, δεν το εξαφάνισες.\n\nΤο Learn σταματά εδώ για το πρωτόκολλο. Τα επόμενα πέντε κεφάλαια (Pro) εξηγούν μηχανές ανταλλακτηρίου — μόχλευση, funding, βιβλίο, έκδοση, κάλυψη ζημιάς — χωρίς εντολή αγοράς."
    )

    private val english = mapOf(
        1 to "A public ledger is not a cloud backup. It is an agreement on rules: which transfer entered history, which input was already spent, which block comes next.\n\nThat is why Learn starts here. Before charts and cycle-day counts, it has to be clear what counts as a valid transfer of value.",
        2 to "A full node can reject an illegal block even if a large mining pool produced it. Pool power does not replace the rules.\n\nA custodial platform is still a central database for the keys it holds. The chain underneath can be distributed while you sit with a custodian.",
        3 to "The same message always yields the same hash. Two different messages almost never share a digest if the function is sound.\n\nWhen an explorer shows a \"block hash\", it is showing that header digest. It is not an unlock code for the coins.",
        4 to "If you lose the private key, no network help desk restores it. The protocol has no password reset.\n\nA custodial account means someone else holds the key. Then control is a contract with a company, not only a signature on chain.",
        5 to "A mempool transaction can be replaced or sit unpaid if the fee is low. \"Sent\" in a wallet is not the same as \"locked in history\".\n\nConfirmations count how many blocks were built on top. How final that is depends on the chain and on how much risk the receiver accepts.",
        6 to "In UTXO every output is spent in full. If you hold 0.08 and send 0.03, you create a change output back to your own address.\n\nIn the account model a single state number changes. That is why an Ethereum explorer shows a balance while a Bitcoin explorer shows unspent outputs.",
        7 to "The Merkle root lets you prove a transaction sits in a block without downloading every other transaction.\n\nA size or gas limit is not miner malice. It is the page-size ceiling of history. When demand exceeds that ceiling, fees rise.",
        8 to "Shallow reorgs happen when two valid blocks appear almost together. The network then chooses which branch continues.\n\nImmutability is the cost of reversal, not theology. On large PoW that cost is hashrate. On PoS it is stake and slashing.",
        9 to "Light clients and mobile wallets often ask a third-party RPC. The balance you see is what that service reports.\n\nIf you want to check the rules yourself, you run a full node. That is heavier. The \"open wallet\" button does not do it.",
        10 to "Consensus is the rule for choosing history when two valid branches exist, not a forum poll.\n\nSocial governance — which client people run — is a different layer. It can produce a fork with new rules.",
        11 to "Bitcoin's puzzle is deliberately arbitrary: hash until the digest falls under a target. It does not solve scientific equations.\n\nDifficulty adjustment keeps average block time steady when hashrate changes. Without it, blocks would race or stall.",
        12 to "Slashing punishes double-signing or conflicting blocks. The attack cost is capital that can be burned, not electricity.\n\nDifferent PoS networks use different slots, epochs, and penalties. Do not copy Ethereum's rules onto every chain.",
        13 to "Each node has its own mempool. There is no single global queue.\n\nA higher fee buys priority with the block producer. It does not make an invalid signature valid.",
        14 to "A seed phrase derives many keys. Whoever has the phrase can rebuild the addresses.\n\nScreenshots, cloud backups, and \"I mailed it to myself\" are common theft paths. The seed is the vault, not a reminder note.",
        15 to "An ERC-20 token lives as long as its contract and host chain live. If the contract has an admin key, one holder can change the rule.\n\nThe native coin pays the protocol's own fee. A token usually pays that fee in the native coin, not \"in itself\".",
        16 to "Execution is deterministic: validators run the same code on the same data and must see the same new state.\n\nBugs, oracles, and admin keys remain. \"Automatic\" means no clerk in the middle, not no risk.",
        17 to "L2s and rollups buy throughput by adding new parts: sequencer, prover, bridge. They change the trust set. They do not erase it.\n\nLearn stops here for the protocol. The next five Pro chapters explain venue engines — leverage, funding, the book, issuance, loss cover — with no buy or sell call."
    )

    private val german = mapOf(
        1 to "Ein öffentliches Register ist kein Cloud-Backup. Es ist eine Regelvereinbarung: welche Übertragung in die Historie kam, welcher Input schon ausgegeben war, welcher Block folgt.\n\nDeshalb beginnt Learn hier. Vor Charts und Tageszählern muss klar sein, was als gültige Wertübertragung zählt.",
        2 to "Ein Full Node kann einen illegalen Block ablehnen, selbst wenn ein großes Pool ihn erzeugt hat. Pool-Macht ersetzt die Regeln nicht.\n\nEine Verwahrplattform bleibt eine zentrale Datenbank für die Schlüssel, die sie hält. Die Kette darunter kann verteilt sein, während du bei einem Verwahrer sitzt.",
        3 to "Dieselbe Nachricht liefert immer denselben Hash. Zwei verschiedene Nachrichten teilen fast nie denselben Digest, wenn die Funktion stimmt.\n\nZeigt ein Explorer einen Block-Hash, zeigt er diesen Header-Abdruck. Das ist kein Entsperrcode für die Coins.",
        4 to "Verlierst du den privaten Schlüssel, stellt kein Netz-Helpdesk ihn wieder her. Das Protokoll hat kein Passwort-Reset.\n\nEin Verwahrkonto heißt: jemand anderes hält den Schlüssel. Dann ist Kontrolle ein Vertrag mit einer Firma, nicht nur eine Signatur on-chain.",
        5 to "Eine Mempool-Transaktion kann ersetzt werden oder unbezahlt bleiben, wenn die Gebühr niedrig ist. «Gesendet» im Wallet ist nicht «in der Historie verriegelt».\n\nBestätigungen zählen, wie viele Blöcke darüber gebaut wurden. Wie endgültig das ist, hängt von der Chain und vom Risiko des Empfängers ab.",
        6 to "Im UTXO wird jeder Output vollständig ausgegeben. Hältst du 0,08 und sendest 0,03, entsteht ein Wechsel-Output zurück an deine Adresse.\n\nIm Kontomodell ändert sich eine Zustandszahl. Deshalb zeigt ein Ethereum-Explorer ein Guthaben, ein Bitcoin-Explorer unspent Outputs.",
        7 to "Die Merkle-Wurzel beweist, dass eine Transaktion im Block sitzt, ohne alle anderen herunterzuladen.\n\nEin Größen- oder Gaslimit ist keine Miner-Bosheit. Es ist die Seitendecke der Historie. Übersteigt die Nachfrage diese Decke, steigen die Gebühren.",
        8 to "Flache Reorgs entstehen, wenn zwei gültige Blöcke fast gleichzeitig erscheinen. Das Netz wählt danach, welcher Zweig weitergeht.\n\nUnveränderlichkeit ist die Kosten der Umkehr, keine Theologie. Bei großem PoW ist das Hashrate. Bei PoS sind es Stake und Slashing.",
        9 to "Light Clients und Mobile-Wallets fragen oft eine Dritt-RPC. Der Saldo, den du siehst, ist der Bericht dieses Dienstes.\n\nWillst du die Regeln selbst prüfen, betreibst du einen Full Node. Das ist schwerer. Der Knopf «Wallet öffnen» tut das nicht.",
        10 to "Konsens ist die Regel zur Wahl der Historie, wenn zwei gültige Zweige existieren, keine Forumsumfrage.\n\nSoziale Governance — welchen Client die Leute laufen lassen — ist eine andere Schicht. Sie kann einen Fork mit neuen Regeln erzeugen.",
        11 to "Bitcoins Rätsel ist absichtlich willkürlich: hashen, bis der Digest unter das Ziel fällt. Es löst keine wissenschaftlichen Gleichungen.\n\nDie Schwierigkeitsanpassung hält die mittlere Blockzeit stabil, wenn sich die Hashrate ändert. Ohne sie würden Blöcke rasen oder stocken.",
        12 to "Slashing bestraft Doppelsignaturen oder widersprüchliche Blöcke. Die Angriffskosten sind Kapital, das verbrannt werden kann, nicht Strom.\n\nAndere PoS-Netze nutzen andere Slots, Epochen und Strafen. Kopiere Ethereums Regeln nicht auf jede Chain.",
        13 to "Jeder Knoten hat sein eigenes Mempool. Es gibt keine einzige globale Warteschlange.\n\nEine höhere Gebühr kauft Priorität beim Blockproduzenten. Sie macht eine ungültige Signatur nicht gültig.",
        14 to "Eine Seed-Phrase leitet viele Schlüssel ab. Wer die Phrase hat, kann die Adressen neu bauen.\n\nScreenshots, Cloud-Backups und «ich mailte es mir» sind häufige Diebstahlwege. Der Seed ist der Tresor, kein Merkzettel.",
        15 to "Ein ERC-20-Token lebt so lange wie sein Vertrag und die Host-Chain. Hat der Vertrag einen Admin-Schlüssel, kann ein Inhaber die Regel ändern.\n\nDer native Coin zahlt die Gebühr des Protokolls selbst. Ein Token zahlt diese Gebühr meist im nativen Coin, nicht «in sich selbst».",
        16 to "Die Ausführung ist deterministisch: Validatoren laufen denselben Code auf denselben Daten und müssen denselben neuen Zustand sehen.\n\nBugs, Orakel und Admin-Schlüssel bleiben. «Automatisch» heißt ohne Sachbearbeiter in der Mitte, nicht ohne Risiko.",
        17 to "L2s und Rollups kaufen Durchsatz, indem sie neue Teile hinzufügen: Sequencer, Prover, Brücke. Sie ändern die Vertrauensmenge. Sie löschen sie nicht.\n\nLearn endet hier für das Protokoll. Die nächsten fünf Pro-Kapitel erklären Börsenmaschinen — Hebel, Funding, Buch, Emission, Verlustdeckung — ohne Kauf- oder Verkaufsorder."
    )

    private val french = mapOf(
        1 to "Un registre public n'est pas une sauvegarde cloud. C'est un accord de règles : quel transfert est entré, quelle entrée était déjà dépensée, quel bloc suit.\n\nC'est pourquoi Learn commence ici. Avant les graphiques et les compteurs de jours, il faut savoir ce qui compte comme transfert de valeur valide.",
        2 to "Un nœud complet peut rejeter un bloc illégal même si un grand pool l'a produit. La puissance du pool ne remplace pas les règles.\n\nUne plateforme custodiale reste une base centrale pour les clés qu'elle tient. La chaîne en dessous peut être distribuée tandis que tu restes chez un dépositaire.",
        3 to "Le même message donne toujours le même hash. Deux messages différents ne partagent presque jamais le même condensé si la fonction est saine.\n\nQuand un explorateur montre un « block hash », il montre cette empreinte d'en-tête. Ce n'est pas un code de déverrouillage des pièces.",
        4 to "Si tu perds la clé privée, aucun support réseau ne la restaure. Le protocole n'a pas de reset de mot de passe.\n\nUn compte custodial signifie qu'un autre tient la clé. Alors le contrôle est un contrat avec une société, pas seulement une signature on-chain.",
        5 to "Une transaction en mempool peut être remplacée ou rester impayée si le frais est bas. « Envoyé » dans un wallet n'est pas « verrouillé dans l'historique ».\n\nLes confirmations comptent combien de blocs ont été construits au-dessus. Le degré de finalité dépend de la chaîne et du risque accepté par le destinataire.",
        6 to "En UTXO chaque sortie est dépensée en entier. Si tu tiens 0,08 et envoies 0,03, tu crées une sortie de monnaie vers ta propre adresse.\n\nDans le modèle compte, un nombre d'état change. Voilà pourquoi un explorateur Ethereum montre un solde et un explorateur Bitcoin des sorties non dépensées.",
        7 to "La racine de Merkle permet de prouver qu'une transaction est dans le bloc sans télécharger toutes les autres.\n\nUne limite de taille ou de gas n'est pas une malice de mineurs. C'est le plafond de page de l'historique. Quand la demande dépasse ce plafond, les frais montent.",
        8 to "De petits reorgs arrivent quand deux blocs valides apparaissent presque ensemble. Le réseau choisit ensuite quelle branche continue.\n\nL'immuabilité est le coût d'une inversion, pas une théologie. En grand PoW ce coût est le hashrate. En PoS ce sont le stake et le slashing.",
        9 to "Les clients légers et wallets mobiles demandent souvent un RPC tiers. Le solde que tu vois est le rapport de ce service.\n\nSi tu veux vérifier les règles toi-même, tu fais tourner un nœud complet. C'est plus lourd. Le bouton « ouvrir le wallet » ne le fait pas.",
        10 to "Le consensus est la règle pour choisir l'histoire quand deux branches valides existent, pas un sondage de forum.\n\nLa gouvernance sociale — quel client les gens exécutent — est une autre couche. Elle peut produire un fork avec de nouvelles règles.",
        11 to "L'énigme de Bitcoin est volontairement arbitraire : hasher jusqu'à ce que le condensé passe sous une cible. Elle ne résout pas d'équations scientifiques.\n\nL'ajustement de difficulté garde le temps de bloc moyen stable quand le hashrate change. Sans lui, les blocs galoperaient ou se figeraient.",
        12 to "Le slashing punit la double signature ou les blocs contradictoires. Le coût d'attaque est du capital qui peut brûler, pas de l'électricité.\n\nD'autres réseaux PoS ont d'autres slots, époques et peines. Ne copie pas les règles d'Ethereum sur chaque chaîne.",
        13 to "Chaque nœud a son propre mempool. Il n'y a pas une file mondiale unique.\n\nUn frais plus élevé achète la priorité chez le producteur de bloc. Il ne rend pas valide une signature invalide.",
        14 to "Une phrase seed dérive beaucoup de clés. Quiconque a la phrase peut reconstruire les adresses.\n\nCaptures d'écran, sauvegardes cloud et « je me le suis envoyé par mail » sont des vols fréquents. La seed est le coffre, pas un pense-bête.",
        15 to "Un jeton ERC-20 vit tant que son contrat et sa chaîne hôte vivent. Si le contrat a une clé admin, un détenteur peut changer la règle.\n\nLa pièce native paie le frais du protocole lui-même. Un jeton paie en général ce frais dans la pièce native, pas « en lui-même ».",
        16 to "L'exécution est déterministe : les validateurs exécutent le même code sur les mêmes données et doivent voir le même nouvel état.\n\nBugs, oracles et clés admin restent. « Automatique » veut dire sans employé au milieu, pas sans risque.",
        17 to "Les L2 et rollups achètent du débit en ajoutant de nouvelles pièces : sequencer, prover, pont. Ils changent l'ensemble de confiance. Ils ne l'effacent pas.\n\nLearn s'arrête ici pour le protocole. Les cinq chapitres Pro suivants expliquent les moteurs de place — levier, funding, carnet, émission, couverture de perte — sans ordre d'achat ou de vente."
    )

    private val spanish = mapOf(
        1 to "Un libro público no es una copia de seguridad en la nube. Es un acuerdo de reglas: qué transferencia entró, qué entrada ya se gastó, qué bloque sigue.\n\nPor eso Learn empieza aquí. Antes de gráficos y contadores de días debe quedar claro qué cuenta como transferencia de valor válida.",
        2 to "Un nodo completo puede rechazar un bloque ilegal aunque lo haya producido un pool grande. El poder del pool no sustituye las reglas.\n\nUna plataforma custodial sigue siendo una base central de las claves que guarda. La cadena de debajo puede ser distribuida mientras tú estás con un custodio.",
        3 to "El mismo mensaje da siempre el mismo hash. Dos mensajes distintos casi nunca comparten el mismo resumen si la función es sólida.\n\nCuando un explorador muestra un «block hash», muestra esa huella de cabecera. No es un código de desbloqueo de las monedas.",
        4 to "Si pierdes la clave privada, ningún soporte de la red la restaura. El protocolo no tiene restablecer contraseña.\n\nUna cuenta custodial significa que otro guarda la clave. Entonces el control es un contrato con una empresa, no solo una firma en la cadena.",
        5 to "Una transacción en el mempool puede sustituirse o quedarse sin pagar si la tarifa es baja. «Enviado» en el wallet no es «cerrado en el historial».\n\nLas confirmaciones cuentan cuántos bloques se construyeron encima. Lo definitivo que sea depende de la cadena y del riesgo que acepte el receptor.",
        6 to "En UTXO cada salida se gasta entera. Si tienes 0,08 y envías 0,03, creas una salida de cambio hacia tu propia dirección.\n\nEn el modelo de cuenta cambia un número de estado. Por eso un explorador de Ethereum muestra saldo y uno de Bitcoin muestra salidas no gastadas.",
        7 to "La raíz de Merkle permite probar que una transacción está en el bloque sin bajar todas las demás.\n\nUn límite de tamaño o de gas no es maldad de mineros. Es el techo de página del historial. Cuando la demanda supera ese techo, suben las tarifas.",
        8 to "Hay reorgs cortos cuando aparecen dos bloques válidos casi a la vez. La red elige después qué rama continúa.\n\nLa inmutabilidad es el coste de revertir, no teología. En PoW grande ese coste es hashrate. En PoS es stake y slashing.",
        9 to "Los clientes ligeros y wallets móviles suelen preguntar a un RPC de terceros. El saldo que ves es el informe de ese servicio.\n\nSi quieres comprobar las reglas tú, ejecutas un nodo completo. Eso pesa más. El botón «abrir wallet» no lo hace.",
        10 to "El consenso es la regla para elegir historia cuando hay dos ramas válidas, no una encuesta de foro.\n\nLa gobernanza social — qué cliente ejecuta la gente — es otra capa. Puede producir un fork con reglas nuevas.",
        11 to "El acertijo de Bitcoin es deliberadamente arbitrario: hashear hasta que el resumen caiga bajo un objetivo. No resuelve ecuaciones científicas.\n\nEl ajuste de dificultad mantiene estable el tiempo medio de bloque cuando cambia el hashrate. Sin él, los bloques correrían o se atascarían.",
        12 to "El slashing castiga la doble firma o bloques contradictorios. El coste de ataque es capital que puede quemarse, no electricidad.\n\nOtras redes PoS usan otros slots, épocas y penas. No copies las reglas de Ethereum a cada cadena.",
        13 to "Cada nodo tiene su propio mempool. No hay una cola mundial única.\n\nUna tarifa más alta compra prioridad con el productor de bloques. No vuelve válida una firma inválida.",
        14 to "Una frase semilla deriva muchas claves. Quien tiene la frase puede reconstruir las direcciones.\n\nCapturas, copias en la nube y «me lo envié por correo» son robos frecuentes. La semilla es la caja fuerte, no un recordatorio.",
        15 to "Un token ERC-20 vive mientras vivan su contrato y la cadena anfitriona. Si el contrato tiene clave de admin, un titular puede cambiar la regla.\n\nLa moneda nativa paga la tarifa del propio protocolo. Un token suele pagar esa tarifa en la moneda nativa, no «en sí mismo».",
        16 to "La ejecución es determinista: los validadores corren el mismo código sobre los mismos datos y deben ver el mismo estado nuevo.\n\nBugs, oráculos y claves de admin siguen ahí. «Automático» significa sin empleado en medio, no sin riesgo.",
        17 to "Las L2 y los rollups compran caudal añadiendo piezas nuevas: sequencer, prover, puente. Cambian el conjunto de confianza. No lo borran.\n\nLearn se detiene aquí para el protocolo. Los cinco capítulos Pro siguientes explican motores de exchange — apalancamiento, funding, libro, emisión, cobertura de pérdidas — sin orden de compra o venta."
    )

    private val italian = mapOf(
        1 to "Un registro pubblico non è un backup sul cloud. È un accordo di regole: quale trasferimento è entrato, quale input era già speso, quale blocco segue.\n\nPer questo Learn parte da qui. Prima di grafici e contatori di giorni deve essere chiaro cosa conta come trasferimento di valore valido.",
        2 to "Un full node può rifiutare un blocco illegale anche se l'ha prodotto un grande pool. La potenza del pool non sostituisce le regole.\n\nUna piattaforma custodial resta un database centrale per le chiavi che tiene. La chain sotto può essere distribuita mentre tu stai da un custode.",
        3 to "Lo stesso messaggio dà sempre lo stesso hash. Due messaggi diversi quasi mai condividono lo stesso digest se la funzione è solida.\n\nQuando un explorer mostra un «block hash», mostra quella impronta dell'header. Non è un codice di sblocco delle monete.",
        4 to "Se perdi la chiave privata, nessun help desk della rete la ripristina. Il protocollo non ha reset della password.\n\nUn conto custodial significa che un altro tiene la chiave. Allora il controllo è un contratto con un'azienda, non solo una firma on-chain.",
        5 to "Una transazione in mempool può essere sostituita o restare non pagata se il fee è basso. «Inviato» nel wallet non è «bloccato nella cronologia».\n\nLe conferme contano quanti blocchi sono stati costruiti sopra. Quanto è finale dipende dalla chain e dal rischio che accetta il destinatario.",
        6 to "In UTXO ogni output si spende intero. Se tieni 0,08 e invii 0,03, crei un output di resto verso il tuo indirizzo.\n\nNel modello account cambia un numero di stato. Per questo un explorer Ethereum mostra un saldo e uno Bitcoin mostra output non spesi.",
        7 to "La radice di Merkle permette di provare che una transazione è nel blocco senza scaricare tutte le altre.\n\nUn limite di size o di gas non è malizia dei miner. È il tetto di pagina della cronologia. Quando la domanda supera quel tetto, i fee salgono.",
        8 to "Reorg poco profondi arrivano quando due blocchi validi appaiono quasi insieme. La rete sceglie poi quale ramo continua.\n\nL'immutabilità è il costo di un'inversione, non teologia. Su PoW grande quel costo è hashrate. Su PoS sono stake e slashing.",
        9 to "Light client e wallet mobile chiedono spesso un RPC di terzi. Il saldo che vedi è il report di quel servizio.\n\nSe vuoi controllare le regole da solo, fai girare un full node. È più pesante. Il pulsante «apri wallet» non lo fa.",
        10 to "Il consenso è la regola per scegliere la storia quando esistono due rami validi, non un sondaggio da forum.\n\nLa governance sociale — quale client gira la gente — è un altro strato. Può produrre un fork con regole nuove.",
        11 to "L'enigma di Bitcoin è volutamente arbitrario: hashare finché il digest cade sotto un target. Non risolve equazioni scientifiche.\n\nL'aggiustamento della difficoltà tiene stabile il tempo medio di blocco quando cambia l'hashrate. Senza di esso i blocchi correrebbero o si fermerebbero.",
        12 to "Lo slashing punisce la doppia firma o blocchi in conflitto. Il costo d'attacco è capitale che può bruciare, non elettricità.\n\nAltre reti PoS usano slot, epoche e pene diverse. Non copiare le regole di Ethereum su ogni chain.",
        13 to "Ogni nodo ha il proprio mempool. Non esiste un'unica coda mondiale.\n\nUn fee più alto compra priorità dal produttore di blocchi. Non rende valida una firma non valida.",
        14 to "Una seed phrase deriva molte chiavi. Chi ha la frase può ricostruire gli indirizzi.\n\nScreenshot, backup cloud e «me la sono inviata via mail» sono furti frequenti. La seed è la cassaforte, non un promemoria.",
        15 to "Un token ERC-20 vive finché vivono il suo contratto e la chain ospite. Se il contratto ha una admin key, un titolare può cambiare la regola.\n\nLa coin nativa paga il fee del protocollo stesso. Un token di solito paga quel fee nella coin nativa, non «in se stesso».",
        16 to "L'esecuzione è deterministica: i validator eseguono lo stesso codice sugli stessi dati e devono vedere lo stesso nuovo stato.\n\nBug, oracle e admin key restano. «Automatico» significa senza impiegato in mezzo, non senza rischio.",
        17 to "L2 e rollup comprano throughput aggiungendo pezzi nuovi: sequencer, prover, ponte. Cambiano l'insieme di fiducia. Non lo cancellano.\n\nLearn si ferma qui per il protocollo. I cinque capitoli Pro successivi spiegano motori di venue — leva, funding, book, emissione, copertura perdite — senza ordine di acquisto o vendita."
    )
}
