package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.BlockchainChapter
import com.example.data.model.BlockchainDiagramType

object BlockchainLearnRepository {

    fun getChapters(language: AppLanguage): List<BlockchainChapter> {
        return when (language) {
            AppLanguage.GREEK -> greekChapters
            AppLanguage.GERMAN -> germanChapters
            AppLanguage.FRENCH -> frenchChapters
            AppLanguage.SPANISH -> spanishChapters
            AppLanguage.ITALIAN -> italianChapters
            AppLanguage.ENGLISH -> englishChapters
        }
    }

    private val greekChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Ψηφιακή σπανιότητα και double spending",
            content = "Ένα ψηφιακό αρχείο αντιγράφεται χωρίς να καταστρέφεται το πρωτότυπο. Γι’ αυτό ένα «αρχείο-νόμισμα» στο κινητό δεν μπορεί από μόνο του να είναι χρήμα: ο ίδιος άνθρωπος θα το έστελνε δύο φορές.\n\nΤο κλασικό πρόβλημα λέγεται double spending: η ίδια μονάδα αξίας να ξοδευτεί δύο φορές. Οι τράπεζες το λύνουν με κεντρικό καθολικό. Το blockchain το λύνει με δημόσιο ιστορικό που πολλοί επαληθεύουν.\n\nΤο blockchain δεν «δημιουργεί μαγικά» σπανιότητα. Ορίζει κανόνες για το ποια συναλλαγή μετράει ως έγκυρη και ποια απορρίπτεται.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Σχήμα: το ίδιο ψηφιακό αντικείμενο μπορεί να υπάρχει σε δύο αντίγραφα.",
            diagramExtraNote = "Ψηφιακό αρχείο αντιγράφεται. Τα λεφτά σε τράπεζα δεν είναι αρχείο στον δίσκο σου.",
            realExample = "Αν στείλεις την ίδια μονάδα σε δύο παραλήπτες πριν το δίκτυο κλειδώσει μία εκδοχή, μόνο μία εκδοχή μπορεί να μείνει στο αποδεκτό ιστορικό.",
            commonMistake = "«Το bitcoin είναι αρχείο στο κινητό μου.» Λάθος. Στο κινητό είναι κλειδιά. Το υπόλοιπο είναι εγγραφές στο κοινό ιστορικό."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Κεντρική βάση vs κατανεμημένο καθολικό",
            content = "Κεντρική βάση: ένας φορέας κρατά το επίσημο βιβλίο (τράπεζα, εταιρεία πληρωμών). Είναι αποδοτικό, αλλά χρειάζεσαι εμπιστοσύνη σε αυτόν τον φορέα και είναι ένα σημείο ελέγχου / αποτυχίας.\n\nΚατανεμημένο καθολικό: πολλά ανεξάρτητα μηχανήματα κρατούν αντίγραφο των ίδιων εγγραφών και ελέγχουν τις νέες με κοινούς κανόνες.\n\n«Αποκεντρωμένο» δεν σημαίνει ότι δεν υπάρχει καμία εξουσία πουθενά. Σημαίνει ότι δεν υπάρχει ένας υποχρεωτικός διαχειριστής για να ισχύσει μια έγκυρη πληρωμή.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Σχήμα: ένας server απέναντι σε δίκτυο κόμβων με το ίδιο βιβλίο.",
            diagramExtraNote = null,
            realExample = "Visa ενημερώνει τη δική της βάση. Στο Bitcoin, χιλιάδες full nodes ελέγχουν τους ίδιους κανόνες.",
            commonMistake = "«Blockchain σημαίνει ότι κανείς δεν έχει δύναμη.» Λάθος. Υπάρχουν miners/validators, developers, exchanges. Το πρωτόκολλο όμως δεν απαιτεί μία εταιρεία για να περάσει έγκυρη συναλλαγή."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Κρυπτογραφικό hash",
            content = "Hash function παίρνει δεδομένα οποιουδήποτε μεγέθους και βγάζει μια σταθερού μήκους συμβολοσειρά (digest).\n\nΙδιότητες που χρησιμοποιεί το blockchain: εύκολο να υπολογιστεί προς τα εμπρός, πρακτικά αδύνατο να βρεις δύο διαφορετικά μηνύματα με το ίδιο hash (collision resistance), και μικρή αλλαγή εισόδου αλλάζει όλη την έξοδο (avalanche).\n\nΤο Bitcoin χρησιμοποιεί SHA-256. Το Ethereum χρησιμοποιεί Keccak-256. Το hash δεν είναι «κρυπτογράφηση που ξεκλειδώνει». Δεν υπάρχει κλειδί που επαναφέρει το αρχικό κείμενο από το hash.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Σχήμα: οποιαδήποτε είσοδος -> σταθερό αποτύπωμα.",
            diagramExtraNote = "Μικρή αλλαγή στα δεδομένα -> εντελώς διαφορετικό hash. Δεν «ξε-hashάρεται» πρακτικά.",
            realExample = "Άλλαξε ένα ψηφίο σε μια συναλλαγή. Το hash του block αλλάζει. Γι’ αυτό μια σιωπηλή αλλοίωση φαίνεται.",
            commonMistake = "«Το hash κρύβει τα δεδομένα και μόνο ο ιδιοκτήτης τα διαβάζει.» Λάθος. Πολλά hashes στο blockchain συνοδεύουν δημόσια δεδομένα. Το hash είναι αποτύπωμα, όχι χρηματοκιβώτιο."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Κλειδιά και ψηφιακή υπογραφή",
            content = "Ασύμμετρη κρυπτογραφία: ιδιωτικό κλειδί και δημόσιο κλειδί. Το ιδιωτικό μένει μυστικό. Από αυτό παράγεται το δημόσιο. Από το δημόσιο (με επιπλέον βήματα) παράγεται η διεύθυνση.\n\nΨηφιακή υπογραφή: με το ιδιωτικό κλειδί υπογράφεις ένα μήνυμα (συναλλαγή). Οποιος έχει το δημόσιο κλειδί επαληθεύει ότι το μήνυμα το υπέγραψε ο κάτοχος του ιδιωτικού, χωρίς να μάθει το ιδιωτικό.\n\nΣε Bitcoin/Ethereum η κυριαρχία πάνω στα νομίσματα είναι κυριαρχία πάνω στο κλειδί, όχι «λογαριασμός στην εταιρεία του app».",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Σχήμα: ιδιωτικό κλειδί υπογράφει. Δημόσιο επαληθεύει.",
            diagramExtraNote = "Ιδιωτικό κλειδί -> ΜΕΝΕΙ ΜΥΣΤΙΚΟ",
            realExample = "Υπογράφεις «στείλε 0,01 BTC στη διεύθυνση X». Οι nodes ελέγχουν την υπογραφή. Αν είναι έγκυρη και οι είσοδοι αδιάθετες, η συναλλαγή είναι τυπικά αποδεκτή.",
            commonMistake = "«Η διεύθυνση είναι σαν password.» Λάθος. Η διεύθυνση είναι δημόσια. Το μυστικό είναι το ιδιωτικό κλειδί / seed."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Τι περιέχει μια συναλλαγή",
            content = "Η συναλλαγή είναι δεδομένα που λένε στο δίκτυο πώς να ενημερώσει το καθολικό: ποιες εισόδους ξοδεύει, ποιες εξόδους δημιουργεί, πόσα πάνε πού, και την υπογραφή εξουσιοδότησης.\n\nΔεν μεταφέρεται «αρχείο νομίσματος». Μεταφέρεται το δικαίωμα στο υπόλοιπο σύμφωνα με τους κανόνες του πρωτοκόλλου.\n\nΚάθε έγκυρη συναλλαγή παίρνει αναγνωριστικό (txid), συνήθως hash των δεδομένων της. Μέχρι να μπει σε block, είναι υποψήφια, όχι οριστικά «κλειδωμένη» στον ίδιο βαθμό.",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Σχήμα: βασικά πεδία μιας συναλλαγής.",
            diagramExtraNote = "Η συναλλαγή είναι μήνυμα στο δίκτυο, όχι «αρχείο νομίσματος» στο κινητό.",
            realExample = "Πληρωμή σε καφέ: το wallet φτιάχνει tx, την υπογράφει, την στέλνει σε node. Οι miners/validators την επιλέγουν σε block.",
            commonMistake = "«Μόλις πατήσω αποστολή, είναι αμετάκλητο σε 0 δευτερόλεπτα.» Λάθος. Υπάρχει μετάδοση, mempool, και μετά επιβεβαιώσεις. Το πόσο «τελικό» είναι εξαρτάται από το chain και τις επιβεβαιώσεις."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. UTXO μοντέλο vs Account μοντέλο",
            content = "Bitcoin (UTXO): Το υπόλοιπο δεν είναι ένας αριθμός σε λογαριασμό. Είναι σύνολο από αδιάθετες εξόδους προηγούμενων συναλλαγών (UTXOs). Μια νέα συναλλαγή καταναλώνει συγκεκριμένα UTXO και φτιάχνει νέα.\n\nEthereum (μοντέλο λογαριασμών): Κάθε διεύθυνση έχει κατάσταση: υπόλοιπο, και για smart contracts κώδικα / αποθήκευση. Μια συναλλαγή τροποποιεί αυτή την κατάσταση.\n\nΚαι τα δύο λύνουν το double spending με διαφορετική λογιστική. Γι’ αυτό ο τρόπος που «φαίνεται» το υπόλοιπο διαφέρει μεταξύ δικτύων.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Σχήμα: δύο διαφορετικά λογιστικά μοντέλα για το ίδιο πρόβλημα.",
            diagramExtraNote = null,
            realExample = "Στο Bitcoin μπορεί να έχεις 3 UTXO που αθροίζουν 0,12 BTC. Στο Ethereum βλέπεις ένα ενιαίο υπόλοιπο ETH στη διεύθυνση.",
            commonMistake = "«Όλα τα chains δουλεύουν σαν τραπεζικός λογαριασμός.» Λάθος. Το Bitcoin σκόπιμα δεν είναι βάση λογαριασμών."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. Τι περιέχει ένα block",
            content = "Block = πακέτο συναλλαγών + επικεφαλίδα (header). Η επικεφαλίδα περιέχει τουλάχιστον: αναφορά στο προηγούμενο block (previous hash), σύνοψη των συναλλαγών (συνήθως Merkle root), χρονική σήμανση, και δεδομένα consensus (π.χ. nonce σε PoW).\n\nMerkle tree: Τα hashes των συναλλαγών συνδυάζονται ανά δύο μέχρι μία κορυφή (root). Έτσι αποδεικνύεται ότι μια tx ανήκει στο block χωρίς να στείλεις όλες τις συναλλαγές.\n\nΤο μέγεθος του block / gas limit περιορίζει πόσες συναλλαγές χωράνε. Γι’ αυτό υπάρχουν fees και αναμονή.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Σχήμα: επικεφαλίδα και σώμα συναλλαγών.",
            diagramExtraNote = null,
            realExample = "Ένα block Bitcoin παράγεται κατά μέσο όρο κάθε ~10 λεπτά. Ένα slot στο Ethereum είναι πολύ πιο σύντομο. Αυτά είναι παράμετροι σχεδιασμού.",
            commonMistake = "«Το block είναι το νόμισμα.» Λάθος. Το block είναι σελίδα ιστορικού. Το νόμισμα είναι μονάδα μέσα στο ιστορικό."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. Πώς συνδέονται τα blocks (previous hash)",
            content = "Κάθε νέο block περιλαμβάνει στην επικεφαλίδα του το hash του αμέσως προηγούμενου block. Έτσι σχηματίζεται αλυσίδα.\n\nΑν αλλάξεις μια συναλλαγή σε παλιό block, αλλάζει το hash του, άρα σπάει η σύνδεση με το επόμενο block. Για να γίνει αποδεκτή η αλλαγή, πρέπει να ξαναχτιστεί εναλλακτική αλυσίδα που το δίκτυο θα θεωρήσει έγκυρη — πράγμα πρακτικά απαγορευτικά ακριβό σε μεγάλα δίκτυα, όχι «μαγικά αδύνατο στη θεωρία».\n\nΤο αμετάβλητο είναι οικονομικό / υπολογιστικό, όχι θεολογικό.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Σχήμα: κάθε κρίκος δείχνει τον προηγούμενο.",
            diagramExtraNote = "Κάθε header περιέχει hash προηγούμενου block. Αλλαγή στο παρελθόν σπάει την αλυσίδα μπροστά.",
            realExample = "6 confirmations στο Bitcoin σημαίνει ότι πάνω από το block σου χτίστηκαν άλλα 5. Κάθε επιπλέον block κάνει την ανατροπή εκθετικά δυσκολότερη.",
            commonMistake = "«Τίποτα δεν αλλάζει ποτέ σε ένα blockchain.» Λάθος. Υπάρχουν reorgs μικρού βάθους, forks με αλλαγή κανόνων. Το παρελθόν είναι δύσκολο να ξαναγραφτεί στα μεγάλα chains, όχι απόλυτα ιερό."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Κόμβοι και Peer-to-Peer δίκτυο",
            content = "Node (κόμβος) = πρόγραμμα που μιλάει το πρωτόκολλο. Full node κατεβάζει και ελέγχει κανόνες + ιστορικό (ή επαρκές τμήμα ανά πρωτόκολλο). Light client βασίζεται σε λιγότερα δεδομένα και εμπιστεύεται τρίτους περισσότερο.\n\nΤο δίκτυο είναι peer-to-peer: οι κόμβοι ανταλλάσσουν συναλλαγές και blocks μεταξύ τους χωρίς υποχρεωτικό κεντρικό server.\n\nΈνα mobile wallet συχνά ΔΕΝ είναι full node. Ρωτάει εξωτερική υπηρεσία (RPC, indexer). Αυτό είναι πρακτικό, αλλά δεν σημαίνει «τρέχω το Bitcoin μόνος μου».",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Σχήμα: κόμβοι συνδέονται μεταξύ τους, όχι σε έναν αφέντη.",
            diagramExtraNote = null,
            realExample = "Ένα full node στο Bitcoin απορρίπτει block με παράνομη ανταμοιβή, ακόμη κι αν το παρήγαγε τεράστιο mining pool.",
            commonMistake = "«Άνοιξα ένα app wallet, άρα είμαι κόμβος του δικτύου.» Συνήθως όχι. Είσαι κάτοχος κλειδιών που μιλάει με κόμβο κάποιου άλλου."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Γιατί χρειάζεται Consensus",
            content = "Πολλοί κόμβοι, καθυστερήσεις δικτύου, και αντικρουόμενες συναλλαγές: χωρίς κανόνα για το ποιο block ακολουθεί, το καθολικό διασπάται σε ασυμβίβαστες εκδοχές.\n\nConsensus = κοινός κανόνας για το τι είναι έγκυρο block, και κανόνας επιλογής αλυσίδας όταν υπάρχουν ανταγωνιστικά έγκυρα blocks (π.χ. βαρύτερη αλυσίδα σε PoW, κανόνες fork-choice στο Ethereum).\n\nConsensus δεν σημαίνει «όλοι συμφωνούν πολιτικά». Σημαίνει ότι το λογισμικό καταλήγει στην ίδια ιστορία όταν τηρούνται οι ίδιοι κανόνες.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Σχήμα: πρόταση block -> έλεγχος κανόνων -> μία αποδεκτή αλυσίδα.",
            diagramExtraNote = "Χωρίς κοινό κανόνα, κάθε κόμβος θα κρατούσε διαφορετικό βιβλίο.",
            realExample = "Δύο miners βρίσκουν έγκυρο block σχεδόν ταυτόχρονα. Προσωρινά υπάρχουν δύο κλαδιά. Το επόμενο block θα ξεκαθαρίσει ποιο κλαδί θα συνεχιστεί.",
            commonMistake = "«Consensus σημαίνει ψηφοφορία κατόχων νομισμάτων σε forum.» Λάθος. Η κοινωνική διακυβέρνηση είναι άλλο πράγμα από τους κανόνες consensus του πρωτοκόλλου."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — Ο πραγματικός μηχανισμός",
            content = "Στο PoW του Bitcoin, ο miner δοκιμάζει διαφορετικές τιμές στο header (nonce κ.λπ.) και υπολογίζει hashes μέχρι το hash να είναι μικρότερο από έναν στόχο (difficulty target).\n\nΗ δυσκολία προσαρμόζεται αυτόματα ώστε ο μέσος χρόνος ανά block να μένει σταθερός (~10 λεπτά), ανεξάρτητα από το πόσα μηχανήματα σκάβουν (hashrate).\n\nΗ ενέργεια δεν είναι «διακόσμηση». Είναι το φυσικό κόστος που κάνει το ξαναγράψιμο του παρελθόντος ακριβό. Το PoW δεν αποδεικνύει ότι μια συναλλαγή είναι «ηθικά καλή». Αποδεικνύει ότι ξοδεύτηκε υπολογιστικό έργο σύμφωνα με τους κανόνες.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Σχήμα: δοκιμή nonce μέχρι το hash να πέσει κάτω από τον στόχο δυσκολίας.",
            diagramExtraNote = null,
            realExample = "Αν διπλασιαστεί το hashrate του δικτύου, τα blocks θα έβγαιναν σε 5 λεπτά. Η προσαρμογή δυσκολίας επαναφέρει τον ρυθμό στα 10 λεπτά.",
            commonMistake = "«Το mining λύνει χρήσιμες μαθηματικές εξισώσεις για την επιστήμη.» Στο Bitcoin ο γρίφος είναι σκόπιμα αυθαίρετος υπολογισμός hash."
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — Ο πραγματικός μηχανισμός",
            content = "Στο PoS, η πρόταση και επικύρωση blocks γίνεται από validators που έχουν δεσμεύσει κεφάλαιο (stake) στο πρωτόκολλο.\n\nΑντί για ρεύμα ως κύριο κόστος επίθεσης, το κόστος είναι οικονομικό: slashing / απώλεια δεσμευμένων κεφαλαίων αν ο validator υπογράψει αντικρουόμενα blocks ή παραβιάσει κανόνες.\n\nΥπάρχουν διάφορες υλοποιήσεις. Το Ethereum μετά το 2022 χρησιμοποιεί PoS με slots, epochs, proposers, attestations. Άλλα δίκτυα λειτουργούν διαφορετικά. Μην το απλοποιείς σε «ψηφίζουν οι πλούσιοι» χωρίς να δεις τον συγκεκριμένο μηχανισμό.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Σχήμα: δέσμευση κεφαλαίου -> επιλογή προτείνοντος -> επικύρωση / slashing.",
            diagramExtraNote = null,
            realExample = "Ένας validator στο Ethereum που προτείνει δύο διαφορετικά blocks στο ίδιο slot τιμωρείται αυτόματα με slashing μέρους του stake του.",
            commonMistake = "«Το PoS είναι απλά τράπεζα.» Τεχνικά ανακριβές. Είναι διαφορετικός αλγόριθμος consensus με άλλα ρίσκα και άλλες άμυνες."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool και transaction fees",
            content = "Mempool = η ουρά στη μνήμη του κόμβου με συναλλαγές που έλαβε αλλά δεν έχουν μπει ακόμη σε block. Κάθε κόμβος έχει το δικό του τοπικό mempool, δεν είναι μία κεντρική αποθήκη.\n\nΤο fee πληρώνει για τον σπάνιο χώρο μέσα στο block (bytes στο Bitcoin, gas στο Ethereum). Όταν η ζήτηση ξεπερνά τον χώρο, τα fees ανεβαίνουν.\n\nΥψηλότερο fee δεν κάνει τη συναλλαγή «πιο έγκυρη». Την κάνει πιο ελκυστική στους block producers για να την επιλέξουν γρήγορα.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Σχήμα: αναμονή έξω από το block, προτεραιότητα με βάση το fee.",
            diagramExtraNote = null,
            realExample = "Όταν υπάρχει συνωστισμός στο Ethereum, μια απλή μεταφορά απαιτεί περισσότερα gwei gas fee για να μπει στο επόμενο block.",
            commonMistake = "«Το δίκτυο χρεώνει fee σαν προμήθεια τράπεζας.» Δεν υπάρχει εταιρεία. Το fee πάει στους παραγωγούς blocks (και σε κάποια chains ένα μέρος καίγεται).",
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Wallets και seed phrase",
            content = "Wallet είναι λογισμικό (ή υλικό) που διαχειρίζεται κλειδιά και φτιάχνει υπογεγραμμένες συναλλαγές. Δεν «φυλάει τα bitcoin μέσα στην εφαρμογή» με την έννοια θυρίδας.\n\nΗ seed phrase (BIP-39 σε πολλά wallets) είναι ανθρώπινη μορφή ενός μυστικού από το οποίο παράγονται πολλά κλειδιά (HD wallets, BIP-32/44 κ.λπ.). Όποιος έχει τη φράση μπορεί να ξαναφτιάξει τα κλειδιά.\n\nCustodial: άλλος κρατά τα κλειδιά. Non-custodial: τα κλειδιά στη συσκευή σου. Άλλο μοντέλο ασφάλειας, άλλο μοντέλο ευθύνης.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Σχήμα: seed -> κλειδιά -> διευθύνσεις -> υπογραφή.",
            diagramExtraNote = "Όποιος έχει το seed ελέγχει τα νομίσματα. Το app δεν είναι η τράπεζα.",
            realExample = "Hardware wallet υπογράφει στη συσκευή. Το ιδιωτικό κλειδί δεν βγαίνει στον υπολογιστή αν η συσκευή είναι σωστή και ακέραια.",
            commonMistake = "«Έκανα screenshot το seed για ασφάλεια.» Αυτό είναι συχνά ο τρόπος που κλέβονται χρήματα. Το seed είναι το ταμείο."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Εγγενές coin vs token",
            content = "Εγγενές coin (native asset): μονάδα του ίδιου του πρωτοκόλλου. BTC στο Bitcoin. ETH στο Ethereum. Πληρώνει fees και έχει ρόλο στο consensus ή στην οικονομία του chain.\n\nToken: λογιστική μονάδα που ορίζεται από έξυπνο συμβόλαιο πάνω σε ένα chain (π.χ. ERC-20). Η ζωή του εξαρτάται από το chain και από τον κώδικα του συμβολαίου.\n\nΈνα token δεν είναι «άλλο blockchain» από μόνο του. Είναι εφαρμογή πάνω σε υπάρχον blockchain — εκτός αν το project έχει και δικό του chain.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Σχήμα: coin του πρωτοκόλλου απέναντι σε token συμβολαίου.",
            diagramExtraNote = null,
            realExample = "USDT στο Ethereum είναι token ERC-20. Το ETH με το οποίο πληρώνεις το gas είναι το εγγενές νόμισμα.",
            commonMistake = "«Κάθε coin έχει το δικό του blockchain.» Λάθος για χιλιάδες tokens."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Smart contracts",
            content = "Smart contract = πρόγραμμα αποθηκευμένο στο chain, με κατάσταση που αλλάζει όταν κάποιος στέλνει έγκυρη συναλλαγή-κλήση. Ο κώδικας εκτελείται από τους validators/miners σύμφωνα με τους κανόνες της VM (π.χ. EVM).\n\n«Αυτόματο» δεν σημαίνει «χωρίς ρίσκο». Bugs, κακή διακυβέρνηση admin keys, εξαρτήσεις από oracles, και μη αναστρέψιμα λάθη είναι πραγματικά.\n\nΤο Bitcoin έχει περιορισμένο script, όχι γενική EVM. Γι’ αυτό πολλά συμβόλαια ζουν σε άλλα δίκτυα.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Σχήμα: κώδικας στο chain -> κλήση -> νέα κατάσταση.",
            diagramExtraNote = null,
            realExample = "Uniswap είναι σύνολο συμβολαίων. Η ανταλλαγή γίνεται με κλήση συνάρτησης, όχι με υπάλληλο.",
            commonMistake = "«Το συμβόλαιο είναι νομικό έγγραφο που σε προστατεύει στα δικαστήρια.» Τεχνικά είναι κώδικας. Η νομική προστασία είναι άλλο στρώμα, όχι ιδιότητα του EVM."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Πραγματικά όρια του συστήματος",
            content = "Κλίμακα: δημόσιο L1 δεν επεξεργάζεται απεριόριστες tx ανά δευτερόλεπτο χωρίς κόστος σε αποκέντρωση ή σε fees. Γι’ αυτό υπάρχουν L2, sidechains, διαφορετικά designs — το καθένα με άλλες υποθέσεις εμπιστοσύνης.\n\nΕνέργεια / κεφάλαιο: το PoW καίει ενέργεια. Το PoS δεσμεύει κεφάλαιο. Δεν υπάρχει consensus χωρίς κόστος επίθεσης.\n\nΙδιωτικότητα: τα περισσότερα δημόσια chains είναι ψευδώνυμα, όχι ανώνυμα. Οι ροές φαίνονται.\n\nForks: το λογισμικό μπορεί να χωρίσει κοινότητα και ιστορία. Η «μία αλήθεια» είναι όποια αλυσίδα συνεχίζουν να τρέχουν οι συμμετέχοντες.\n\nΑυτά δεν ακυρώνουν το εργαλείο. Ορίζουν πού είναι κατάλληλο και πού όχι.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Σχήμα: τα όρια είναι μέρος του συστήματος, όχι υποσημείωση.",
            diagramExtraNote = null,
            realExample = "Ένα rollup μαζεύει πολλές tx εκτός του ακριβού L1 και δημοσιεύει συμπυκνωμένα δεδομένα πίσω στο L1. Κερδίζει χωρητικότητα, αλλά εισάγει νέα μέρη (sequencer κ.λπ.).",
            commonMistake = "«Το blockchain λύνει τα πάντα: ταχύτητα, απορρήτο, διακυβέρνηση, φτηνές πληρωμές μαζί.» Δεν υπάρχει τέτοιο δωρεάν γεύμα."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Μηχανική Ρευστοποιήσεων & Leverage",
            content = "Στα παράγωγα κρυπτονομισμάτων (perpetual futures), η μόχλευση (leverage) επιτρέπει άνοιγμα θέσεων με κλάσμα του απαιτούμενου κεφαλαίου. Όταν η αγορά κινείται αντίθετα και το margin εξαντληθεί, ο μηχανισμός ρευστοποίησης κλείνει αυτόματα τη θέση.\n\nΟι αλυσιδωτές ρευστοποιήσεις (liquidation cascades) δημιουργούν βίαιες εκτινάξεις τιμών (long/short squeezes), όπου οι εντολές stop-loss και liquidations ενεργούν ως επιθετικές market εντολές.\n\nΗ διαχείριση κινδύνου και η κατανόηση του Liquidation Heatmap είναι κρίσιμα για την επιβίωση στο trading παραγώγων.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Σχήμα: Margin εξαντλείται -> Trigger Liquidation -> Βίαιο Market Sell/Buy.",
            diagramExtraNote = null,
            realExample = "Σε απότομη πτώση 5%, θέσεις με μόχλευση 20x ρευστοποιούνται αυτόματα, δημιουργώντας πτωτικό waterfall.",
            commonMistake = "«Η υψηλή μόχλευση αυξάνει απλώς τα κέρδη.» Στην πραγματικότητα πολλαπλασιάζει γεωμετρικά την πιθανότητα ολικής απώλειας κεφαλαίου."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Δυναμική Funding Rates & Perpetual Swaps",
            content = "Τα Perpetual Futures δεν έχουν ημερομηνία λήξης. Για να συγκλίνει η τιμή του συμβολαίου με την spot τιμή, χρησιμοποιείται ο μηχανισμός του Funding Rate.\n\nΌταν το funding rate είναι θετικό, οι Longs πληρώνουν τους Shorts κάθε 8 ώρες, υποδεικνύοντας υπερθέρμανση και υπερβολική αισιοδοξία. Όταν είναι αρνητικό, οι Shorts πληρώνουν τους Longs (απαισιοδοξία / short squeeze potential).\n\nΑκραίες τιμές funding rate αποτελούν συχνά αξιόπιστους δείκτες αντιστροφής τάσης στην αγορά.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Σχήμα: Perpetual Price > Spot -> Θετικό Funding (Longs πληρώνουν Shorts).",
            diagramExtraNote = null,
            realExample = "Σε bull runs το funding φτάνει συχνά το +0.05% ανά 8ωρο, καθιστώντας τις leveraged θέσεις μη βιώσιμες μακροπρόθεσμα.",
            commonMistake = "«Το funding rate είναι προμήθεια που κρατάει το ανταλλακτήριο.» Είναι peer-to-peer πληρωμή ανάμεσα στους traders."
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Ροή Εντολών & Βάθος Αγοράς",
            content = "Το Order Book (βιβλίο εντολών) αποτυπώνει την πρόθεση αγοράς (Bids) και πώλησης (Asks) σε διαφορετικά επίπεδα τιμών. Το Market Depth δείχνει πόση ρευστότητα υπάρχει πριν μετακινηθεί η τιμή.\n\nΟι institutional traders και οι market makers χρησιμοποιούν Iceberg εντολές και TWAP/VWAP αλγορίθμους για να εκτελούν μεγάλους όγκους χωρίς να μετακινούν επιθετικά την τιμή.\n\nΗ ανάλυση Liquidity Clusters (συγκεντρώσεις ρευστότητας) αποκαλύπτει πού είναι τοποθετημένες οι μαζικές εντολές των market makers.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Σχήμα: Orderbook Depth -> Bid/Ask Clusters -> Slippage & Execution.",
            diagramExtraNote = null,
            realExample = "Μια εντολή πώλησης 1.000 BTC σε ρηχό βιβλίο εντολών θα προκαλέσει slippage 3-4% αν εκτελεστεί ως market order.",
            commonMistake = "«Οι εντολές στο orderbook είναι εγγυημένες συναλλαγές.» Πολλές εντολές είναι spoofing ή ακυρώνονται πριν εκτελεστούν."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Μακροοικονομικοί Κύκλοι Halving",
            content = "Το Bitcoin Halving λαμβάνει χώρα κάθε 210.000 blocks (περίπου 4 έτη), μειώνοντας την ανταμοιβή των εξορυκτών (block reward) στο μισό (από 50 BTC το 2009 σε 3.125 BTC το 2024).\n\nΑυτό το προγραμματισμένο σοκ προσφοράς (supply shock) δημιουργεί 4ετείς μακροοικονομικούς κύκλους: Συσσώρευση (Accumulation), Παραβολική Άνοδος (Bull Run), Διανομή (Distribution) και Αρκούδα (Bear Market).\n\nΗ κατανόηση του σημείου που βρισκόμαστε στον κύκλο επιτρέπει μακροπρόθεσμο στρατηγικό σχεδιασμό κεφαλαίου.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Σχήμα: Block Reward / 2 -> Supply Shock -> 4-Year Fractal Cycle.",
            diagramExtraNote = null,
            realExample = "Στα προηγούμενα halvings (2012, 2016, 2020), η κορύφωση της τιμής εμφανίστηκε 12-18 μήνες μετά το εκάστοτε halving.",
            commonMistake = "«Το halving ανεβάζει ακαριαία την τιμή την ίδια ημέρα.» Η επίδραση του supply shock απαιτεί μήνες για να γίνει αισθητή."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Θεσμική Διαχείριση Κινδύνου",
            content = "Η επιβίωση και η κερδοφορία στις αγορές κρυπτονομισμάτων δεν καθορίζονται από τις σωστές προβλέψεις, αλλά από τη διαχείριση του μέγιστου drawdown και του ρίσκου ανά θέση.\n\nΟι θεσμικοί επενδυτές χρησιμοποιούν σταθερό κανόνα 1-2% μέγιστου ρίσκου ανά συναλλαγή, υπολογίζοντας το position size βάσει του stop-loss και όχι βάσει επιθυμητού κέρδους.\n\nΗ διαφοροποίηση, η προστασία κεφαλαίου και η αποφυγή συναισθηματικών αποφάσεων (FOMO / FUD) αποτελούν το θεμέλιο της επαγγελματικής διαχείρισης.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Σχήμα: Portfolio Size -> Fixed Risk % -> Dynamic Position Sizing.",
            diagramExtraNote = null,
            realExample = "Αν ρισκάρεις 1% ανά trade, χρειάζονται 100 συνεχόμενες αποτυχίες για να μηδενίσεις το κεφάλαιό σου.",
            commonMistake = "«Για να βγάλω γρήγορα χρήματα πρέπει να βάζω 'all-in' σε ένα coin.» Αυτή είναι η πιο σίγουρη συνταγή ολικής καταστροφής."
        )
    )

    private val englishChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Digital Scarcity & Double Spending",
            content = "A digital file is copied without destroying the original. That is why a \"currency-file\" on a phone cannot be money by itself: the same person could send it twice.\n\nThe classic problem is called double spending: the same unit of value being spent twice. Banks solve this with a central ledger. Blockchain solves this with a public history verified by many.\n\nBlockchain does not \"magically create\" scarcity. It defines rules for which transaction counts as valid and which is rejected.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Diagram: The same digital object can exist in two copies.",
            diagramExtraNote = "Digital files copy easily. Money in a bank is not a file on your disk.",
            realExample = "If you send the same unit to two recipients before the network locks a version, only one version can remain in the accepted history.",
            commonMistake = "\"Bitcoin is a file on my phone.\" Wrong. The phone holds keys. The balance is entries in the shared history."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Centralized Database vs Distributed Ledger",
            content = "Centralized database: A single entity maintains the official ledger (bank, payment company). It is efficient, but requires trust in that entity and represents a single point of control / failure.\n\nDistributed ledger: Multiple independent machines maintain copies of the same records and verify new ones with shared rules.\n\n\"Decentralized\" does not mean there is no authority anywhere. It means there is no mandatory administrator required for a valid payment to settle.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Diagram: One central server vs a network of nodes with the same ledger.",
            diagramExtraNote = null,
            realExample = "Visa updates its own database. In Bitcoin, thousands of full nodes verify the exact same rules.",
            commonMistake = "\"Blockchain means no one has power.\" Wrong. There are miners/validators, developers, exchanges. But the protocol does not require a single company to approve a valid transaction."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Cryptographic Hash",
            content = "A hash function takes data of any arbitrary size and outputs a fixed-length character string (digest).\n\nProperties used by blockchain: easy to compute forwards, practically impossible to find two different messages with the same hash (collision resistance), and a small input change alters the entire output (avalanche effect).\n\nBitcoin uses SHA-256. Ethereum uses Keccak-256. A hash is not \"encryption that gets unlocked\". There is no key that recovers the original text from the hash.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Diagram: Any input -> fixed fingerprint.",
            diagramExtraNote = "Small change in data -> completely different hash. Cannot be practically un-hashed.",
            realExample = "Change one digit in a transaction. The block's hash changes completely. This is why silent tampering is immediately visible.",
            commonMistake = "\"The hash hides data and only the owner can read it.\" Wrong. Many hashes on blockchain accompany public data. A hash is a fingerprint, not a safe box."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Keys & Digital Signatures",
            content = "Asymmetric cryptography: private key and public key. The private key stays secret. From it, the public key is derived. From the public key (with extra steps), the address is derived.\n\nDigital signature: With the private key you sign a message (transaction). Anyone with the public key verifies that the message was signed by the holder of the private key, without learning the private key.\n\nIn Bitcoin/Ethereum, ownership of coins is ownership of the key, not an \"account at the app's company\".",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Diagram: Private key signs. Public key verifies.",
            diagramExtraNote = "Private key -> STAYS SECRET",
            realExample = "You sign \"send 0.01 BTC to address X\". Nodes check the signature. If valid and inputs are unspent, the transaction is formally accepted.",
            commonMistake = "\"The address is like a password.\" Wrong. The address is public. The secret is the private key / seed phrase."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Anatomy of a Transaction",
            content = "A transaction is data telling the network how to update the ledger: which inputs it spends, which outputs it creates, how much goes where, and the authorization signature.\n\nNo \"coin file\" is transferred. What transfers is the right to the balance according to the protocol rules.\n\nEvery valid transaction receives an identifier (txid), usually a hash of its data. Until included in a block, it is pending and not locked to the same degree.",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Diagram: Basic fields of a transaction.",
            diagramExtraNote = "A transaction is a network message, not a \"coin file\" on a phone.",
            realExample = "Paying for coffee: the wallet creates a tx, signs it, and broadcasts it to a node. Miners/validators include it in a block.",
            commonMistake = "\"Once I hit send, it is irreversible in 0 seconds.\" Wrong. There is propagation, mempool, and subsequent block confirmations. Finality depends on the chain and confirmation depth."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. UTXO Model vs Account Model",
            content = "Bitcoin (UTXO): The balance is not a single number on an account. It is a set of Unspent Transaction Outputs. A new transaction consumes specific UTXOs and creates new ones.\n\nEthereum (account model): Each address has a state: balance, and for smart contracts additional code/storage. A transaction modifies this state.\n\nBoth models prevent double spending with different accounting logic. That is why \"how the balance appears\" varies across networks and wallets.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Diagram: Two different accounting models for the same problem.",
            diagramExtraNote = null,
            realExample = "In Bitcoin, you can have multiple UTXO chunks totaling 0.12 BTC. In Ethereum, you typically see a single ETH balance on the address.",
            commonMistake = "\"All chains operate like a bank account.\" Wrong. Bitcoin intentionally avoids being an account database."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. What a Block Contains",
            content = "Block = transaction payload + header. The header contains at least: reference to the previous block (previous hash), transaction summary (usually Merkle root), timestamp, and consensus data (e.g., nonce in PoW).\n\nMerkle tree: Transaction hashes are paired in twos up to a single root. This proves a transaction belongs to the block without needing to transmit all transactions.\n\nBlock size / gas limit restricts how many transactions fit. That is why fees and waiting times exist.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Diagram: Block header and block body.",
            diagramExtraNote = null,
            realExample = "A Bitcoin block targets ~10 minutes. An Ethereum slot is much shorter. These are protocol parameters, not decoration.",
            commonMistake = "\"The block is the coin.\" Wrong. The block is a history ledger page. The coin is a unit in the ledger."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. How Blocks are Chained (Previous Hash)",
            content = "Every new block includes in its header the hash of the previous block. This forms a continuous chain.\n\nIf you modify a transaction in an older block, its hash changes, breaking the link with all subsequent blocks. For the change to be accepted, an alternative chain must be rebuilt that the network considers valid — practically prohibitively expensive on large networks, rather than \"magically impossible in theory\".\n\nImmutability is economic / computational, not theological.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Diagram: Each link points to the previous one.",
            diagramExtraNote = "Each header contains previous block's hash. Changing past breaks the chain ahead.",
            realExample = "6 confirmations on Bitcoin means 6 blocks stacked on top of your transaction. Each makes a reorganization exponentially more expensive.",
            commonMistake = "\"Nothing ever changes in a blockchain.\" Wrong. Shallow reorgs happen. Hard forks change rules. The past is difficult to rewrite on large networks, not holy."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Nodes & Peer-to-Peer Networks",
            content = "Node = A program that speaks the protocol. A full node downloads and verifies rules + history (or sufficient subset per protocol). A light client relies on less data and more trust in third parties.\n\nThe network is peer-to-peer: nodes exchange transactions and blocks with each other without a mandatory central server.\n\nA mobile wallet is often NOT a full node. It queries external services (RPC, indexers). This is practical, but does not mean \"you are running Bitcoin\".",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Diagram: Nodes connect to each other, not to a single master.",
            diagramExtraNote = null,
            realExample = "A Bitcoin full node rejects a block containing an illegal subsidy, even if produced by a massive mining pool.",
            commonMistake = "\"I opened a wallet app, so I am a network node.\" Usually wrong. You are a key holder talking to someone else's node."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Why Consensus is Required",
            content = "Multiple nodes, network latency, and conflicting transactions: without a rule for which block comes next, the ledger fragments into conflicting versions.\n\nConsensus = A shared set of rules for a valid block, and a chain selection rule when competing valid blocks exist (e.g., heaviest chain in Bitcoin PoW, specific fork-choice rules in Ethereum).\n\nConsensus is not \"everyone agreeing politically\". It means software accepts the same history when following identical rules and observing identical data.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Diagram: Block proposal -> Acceptance rule -> Single accepted history.",
            diagramExtraNote = "Without a common rule, each node would maintain a different book.",
            realExample = "Two miners find a valid block almost simultaneously. Temporarily, two tip branches exist. The next block determines which branch is extended.",
            commonMistake = "\"Consensus means coin holders voting on social forums.\" Wrong. Social governance is separate from consensus rules encoded inside the protocol."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — The Real Mechanism",
            content = "In Bitcoin PoW, the miner alters header fields (nonce, etc.) and recalculates hashes until the hash is smaller than the current target (difficulty).\n\nDifficulty adjusts so average block time stays close to target (~10 minutes for Bitcoin), regardless of total network computational power (hashrate).\n\nEnergy is not \"decoration\". It is the physical cost that makes rewriting past history prohibitively expensive. PoW does not prove a transaction is morally good. It proves work was expended according to protocol rules.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Diagram: Test nonce values until hash meets the difficulty target.",
            diagramExtraNote = null,
            realExample = "If hashrate doubles and difficulty stayed unchanged, blocks would produce twice as fast. Difficulty adjustment corrects this automatically.",
            commonMistake = "\"Mining is solving useful scientific equations.\" In Bitcoin, the puzzle is intentionally arbitrary except for being computationally difficult to solve and trivial to verify."
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — The Real Mechanism",
            content = "In PoS, block proposal and validation are handled by validators who have locked capital (stake) inside the protocol.\n\nInstead of electricity as the primary attack cost, the cost is financial: slashing / loss of staked capital if a validator signs conflicting blocks or violates consensus rules.\n\nMany implementations exist. Ethereum post-2022 uses PoS with slots, epochs, proposers, and attestations. Other networks differ. Do not oversimplify to \"rich people voting\" without studying the specific mechanism.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Diagram: Staked capital -> Proposer selection -> Attestation / Finalization.",
            diagramExtraNote = null,
            realExample = "An Ethereum validator proposing two conflicting blocks at the exact same height is slashed and penalized by the protocol.",
            commonMistake = "\"PoS is just a bank.\" Technically inaccurate. It is a distinct consensus algorithm with different attack vectors (e.g. long-range attacks) and distinct defenses."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool & Transaction Fees",
            content = "Mempool = The memory queue of transactions received by a node that have not yet been included in a block. Each node maintains its own local mempool view; it is not a single global repository.\n\nThe fee pays for scarce block space (bytes in Bitcoin, gas in Ethereum). When transaction demand exceeds available space, fees rise.\n\nA higher fee does not make a transaction \"more genuine\". It simply increases the probability of fast inclusion by the block builder.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Diagram: Waiting outside the block, prioritized by fee.",
            diagramExtraNote = null,
            realExample = "During Ethereum congestion, a simple ETH transfer costs higher gas fees in currency terms, even though computational execution is identical.",
            commonMistake = "\"The network charges fees like a bank to profit the blockchain corporation.\" There is no company owning Bitcoin. Fees go to block producers (and in some protocols, a portion is burned)."
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Wallets & Seed Phrases",
            content = "A wallet is software (or hardware) that manages cryptographic keys and builds signed transactions. It does not \"store bitcoins inside the app\" like a physical safe.\n\nThe seed phrase (BIP-39 in most wallets) is a human-readable representation of an entropy master secret from which countless keys are derived (HD wallets, BIP-32/44). Anyone with the seed phrase can recreate all private keys.\n\nCustodial: A third party holds the keys. Non-custodial: Keys remain exclusively on your device. Different security models, different liability models.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Diagram: Seed phrase -> Private keys -> Public addresses -> On-device signing.",
            diagramExtraNote = "Whoever holds the seed controls the funds. The app is not a bank.",
            realExample = "A hardware wallet signs transactions locally. The private key never leaves the device if hardware integrity is maintained.",
            commonMistake = "\"I took a screenshot of my seed phrase for safety.\" This is frequently how funds are stolen. The seed phrase is the vault master key."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Native Coin vs Token",
            content = "Native coin (native asset): The fundamental base currency of the protocol itself. BTC on Bitcoin, ETH on Ethereum. Used to pay transaction fees and plays an integral role in consensus and chain security.\n\nToken: An accounting unit defined by a smart contract living on top of an existing blockchain (e.g., ERC-20). Its lifecycle and security depend entirely on the host chain and smart contract code.\n\nA token is not a separate blockchain on its own. It is an application layer deployed on an existing chain.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Diagram: Protocol native coin vs Smart contract token.",
            diagramExtraNote = null,
            realExample = "USDT on Ethereum is an ERC-20 token. The ETH used to pay the transaction gas fee is the native base currency.",
            commonMistake = "\"Every crypto coin has its own blockchain.\" False for thousands of tokens that exist solely inside smart contracts."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Smart Contracts",
            content = "Smart contract = A program deployed directly on-chain, whose state mutates whenever an account broadcasts a valid contract call transaction. The bytecode executes deterministically across network validators following Virtual Machine rules (e.g., EVM).\n\n\"Automated\" does not mean \"risk-free\". Software bugs, compromised admin keys, oracle dependencies, and irreversible execution are real operational risks.\n\nBitcoin uses a restrictive Script language without a general Turing-complete EVM. Consequently, complex composable contracts reside on dedicated smart contract networks.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Diagram: On-chain code -> Execution call + fee -> State mutation.",
            diagramExtraNote = null,
            realExample = "Uniswap is a collection of smart contracts. Token swaps execute through function calls rather than human brokers.",
            commonMistake = "\"A smart contract is a legal document protecting you in court.\" Technically, it is program code. Legal enforceability is a separate legal layer, not an intrinsic EVM property."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Real Physical & Economic Limits",
            content = "Throughput: A public Layer-1 cannot process infinite transactions per second without sacrificing decentralization or driving fees high. This necessitates Layer-2 rollups, sidechains, and modular architectures — each with distinct trust assumptions.\n\nEnergy / Capital: PoW expends physical electricity. PoS locks economic capital. Consensus cannot exist without tangible attack costs.\n\nPrivacy: Most public blockchains are pseudonymous, not anonymous. On-chain transaction flows are transparent and auditable.\n\nForks: Software and social divergence can split consensus and history. \"The one truth\" is simply whichever chain active network participants choose to validate.\n\nThese constraints do not invalidate blockchain technology. They define precisely where it is suitable and where it is not.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Diagram: Constraints are architectural properties of the system, not footnotes.",
            diagramExtraNote = null,
            realExample = "A rollup batches thousands of transactions off expensive L1 and posts condensed state proofs back to L1, expanding capacity while introducing new components (sequencers, provers).",
            commonMistake = "\"Blockchain solves everything: infinite speed, complete anonymity, free instant payments simultaneously.\" There is no such free lunch in distributed computing."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Liquidation Engineering & Leverage",
            content = "In perpetual futures trading, leverage enables capital-efficient exposure. However, when adverse price moves exhaust the maintenance margin, liquidation engines forcefully close positions to protect the exchange solvency.\n\nCascading liquidations trigger violent price squeezes (long/short squeezes), transforming triggered stop-losses and liquidations into aggressive market orders that vacuum remaining orderbook depth.\n\nMastering risk boundaries, liquidation heatmaps, and margin buffers is mandatory for capital preservation.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Diagram: Margin depletion -> Liquidation threshold -> Market order cascade.",
            diagramExtraNote = null,
            realExample = "A 5% sudden drop in a volatile token triggers 20x liquidation cascades, driving flash wicks on derivatives exchanges.",
            commonMistake = "\"High leverage creates guaranteed wealth faster.\" It exponentially scales the probability of total account wipeout (zero margin)."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Funding Rate Dynamics & Perpetual Swaps",
            content = "Unlike traditional futures contracts, perpetual swaps have no expiry date. To tether the contract mark price to spot index prices, exchanges utilize the periodic Funding Rate mechanism.\n\nWhen funding is positive, Long traders pay Short traders every 8 hours, signaling an overheated market. When funding is negative, Shorts pay Longs (bearish exhaustion / squeeze setup).\n\nExtreme funding spikes frequently serve as reliable market mean-reversion and cycle turning signals.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Diagram: Mark Price > Index Price -> Positive Funding (Longs pay Shorts).",
            diagramExtraNote = null,
            realExample = "During peak parabolic bull euphoria, funding rates can reach +0.10% per 8h, making long positions mathematically untenable over weeks.",
            commonMistake = "\"Funding rate is a fee kept by the exchange broker.\" Funding is a peer-to-peer equilibrium payment directly between traders."
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Quantum Order Flow & Depth",
            content = "An order book aggregates resting market intentions: limit buy bids and limit sell asks across granular price steps. Market Depth visualizes the aggregate resting liquidity required to move prices.\n\nInstitutional players execute large block sizes through algorithmic splitting: TWAP (Time-Weighted), VWAP (Volume-Weighted), and Iceberg orders that mask true execution size.\n\nLiquidity cluster analysis exposes institutional absorption walls and resting stop clusters.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Diagram: Orderbook depth distribution -> Liquidity walls -> Slippage dynamics.",
            diagramExtraNote = null,
            realExample = "Executing a $50M market sell into a thin order book with only $5M depth causes severe price slippage.",
            commonMistake = "\"All visible limit orders represent guaranteed transactions.\" Orderbooks are dynamic and often subject to phantom spoofing and algorithmic cancellations."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Macro Halving Cycles & Supply Dynamics",
            content = "The Bitcoin Halving occurs every 210,000 blocks (~4 years), reducing the miner block subsidy by 50% (from 50 BTC in 2009 down to 3.125 BTC in 2024).\n\nThis programmatic supply shock historically drives 4-year macro market phases: Accumulation, Expansion, Parabolic Distribution, and Bear Capitulation.\n\nPositioning relative to the 4-year halving cycle provides a framework for capital allocation across macro regimes.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Diagram: Halving event -> Daily miner issuance slashed by 50% -> Supply shock expansion.",
            diagramExtraNote = null,
            realExample = "In previous halving cycles (2012, 2016, 2020), macro cycle peaks materialized 12 to 18 months post-halving.",
            commonMistake = "\"Prices pump violently the exact second the halving block is mined.\" Supply restriction requires months of continuous absorption to generate macro price effects."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Institutional Risk & Portfolio Management",
            content = "Long-term profitability in volatile digital assets is governed not by directional prediction accuracy, but by strict drawdown control and asymmetric position sizing.\n\nProfessional portfolio managers cap risk per trade at 1-2% of total equity, calculating order size from stop distance rather than desired profit targets.\n\nCapital preservation, systematic rebalancing, and emotional detachment form the foundation of institutional longevity.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Diagram: Total capital -> 1-2% fixed risk unit -> Invariant stop-loss sizing.",
            diagramExtraNote = null,
            realExample = "By risking only 1% per position, a trader can survive 20 consecutive losing trades with over 81% of capital intact.",
            commonMistake = "\"Going 'all-in' on high conviction alts is the fastest way to succeed.\" In finance, concentration without risk limits is the mathematical certainty of ruin."
        )
    )

    private val germanChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Digitale Knappheit & Double Spending",
            content = "Eine digitale Datei wird kopiert, ohne das Original zu zerstören. Daher kann eine «Währungsdatei» auf dem Handy allein kein Geld sein: Dieselbe Person könnte sie zweimal senden.\n\nDas klassische Problem heißt Double Spending: Dieselbe Werteinheit wird zweimal ausgegeben. Banken lösen dies über ein zentrales Hauptbuch. Die Blockchain löst dies über eine öffentliche Historie, die viele unabhängig überprüfen.\n\nBlockchain erzeugt Knappheit nicht «magisch». Sie definiert feste Regeln dafür, welche Transaktion als gültig gilt und welche abgewiesen wird.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Schema: Dasselbe digitale Objekt kann in zwei Kopien existieren.",
            diagramExtraNote = "Digitale Dateien lassen sich kopieren. Bankguthaben ist keine Datei auf deiner Festplatte.",
            realExample = "Wenn du dieselbe Einheit an zwei Empfänger sendest, bevor das Netzwerk eine Version einloggt, bleibt nur eine einzige Version in der akzeptierten Historie.",
            commonMistake = "«Bitcoin ist eine Datei auf meinem Smartphone.» Falsch. Auf dem Smartphone liegen kryptografische Schlüssel. Das Guthaben sind Einträge im gemeinsamen Hauptbuch."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Zentrale Datenbank vs. Verteiltes Hauptbuch",
            content = "Zentrale Datenbank: Eine einzige Instanz führt das offizielle Buch (Bank, Zahlungsdienstleister). Das ist effizient, erfordert aber Vertrauen in diese Instanz und stellt einen Single Point of Failure dar.\n\nVerteiltes Hauptbuch (Distributed Ledger): Viele unabhängige Rechner halten Kopien derselben Einträge und prüfen neue Einträge anhand gemeinsamer Regeln.\n\n«Dezentral» bedeutet nicht, dass es nirgendwo Autorität gibt. Es bedeutet, dass kein zwingender zentraler Administrator nötig ist, damit eine gültige Zahlung abgewickelt wird.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Schema: Ein zentraler Server gegenüber einem Netzwerk von Nodes mit demselben Buch.",
            diagramExtraNote = null,
            realExample = "Visa aktualisiert die eigene interne Datenbank. Bei Bitcoin überprüfen Tausende Full Nodes exakt dieselben Konsensregeln.",
            commonMistake = "«Blockchain bedeutet, dass niemand Macht hat.» Falsch. Es gibt Miner/Validatoren, Entwickler und Börsen. Aber das Protokoll benötigt kein einzelnes Unternehmen zur Bestätigung einer gültigen Transaktion."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Kryptografischer Hash",
            content = "Eine Hash-Funktion nimmt Eingabedaten beliebiger Größe entgegen und erzeugt eine Zeichenkette fester Länge (Digest).\n\nEigenschaften in Blockchains: vorwärts leicht zu berechnen, praktisch unmöglich zwei verschiedene Nachrichten mit demselben Hash zu finden (Kollisionsresistenz) und minimale Eingabeänderungen verändern die gesamte Ausgabe (Lawineneffekt).\n\nBitcoin nutzt SHA-256, Ethereum Keccak-256. Ein Hash ist keine «verschlüsselte Datei zum Entsperren». Es existiert kein Schlüssel, der aus dem Hash den Originaltext wiederherstellt.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Schema: Beliebige Eingabe -> fester digitaler Fingerabdruck.",
            diagramExtraNote = "Minimale Datenänderung -> völlig neuer Hash. Praktisch nicht umkehrbar.",
            realExample = "Ändere eine einzige Ziffer in einer Transaktion. Der Block-Hash ändert sich vollständig. Manipulationsversuche fallen sofort auf.",
            commonMistake = "«Der Hash versteckt Daten und nur der Eigentümer kann sie lesen.» Falsch. Hashes begleiten in der Blockchain meist öffentliche Daten. Ein Hash ist ein Fingerabdruck, kein Tresor."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Schlüssel & Digitale Signaturen",
            content = "Asymmetrische Kryptografie: privater Schlüssel und öffentlicher Schlüssel. Der private Schlüssel bleibt streng geheim. Aus ihm wird der öffentliche Schlüssel abgeleitet, und daraus die Adresse.\n\nDigitale Signatur: Mit dem privaten Schlüssel signierst du eine Nachricht (Transaktion). Jeder mit dem öffentlichen Schlüssel kann mathematisch verifizieren, dass die Signatur vom Inhaber stammt, ohne den privaten Schlüssel zu sehen.\n\nBei Bitcoin/Ethereum bedeutet Eigentum an Coins die alleinige Kontrolle über den privaten Schlüssel, nicht ein «Benutzerkonto beim App-Hersteller».",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Schema: Privater Schlüssel signiert. Öffentlicher Schlüssel prüft.",
            diagramExtraNote = "Privater Schlüssel -> BLEIBT GEHEIM",
            realExample = "Du signierst «sende 0,01 BTC an Adresse X». Nodes prüfen die Signatur. Ist sie gültig und das Guthaben unspent, wird die Transaktion akzeptiert.",
            commonMistake = "«Die Adresse ist wie ein Passwort.» Falsch. Die Adresse ist öffentlich. Das Geheimnis ist der private Schlüssel bzw. die Seed-Phrase."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Aufbau einer Transaktion",
            content = "Eine Transaktion besteht aus Daten, die dem Netzwerk mitteilen, wie das Hauptbuch aktualisiert werden soll: welche Inputs ausgegeben werden, welche Outputs entstehen und die Autorisierungssignatur.\n\nEs wird keine «Münzdatei» übertragen. Übertragen wird das Verfügungsrecht über das Guthaben gemäß den Protokollregeln.\n\nJede gültige Transaktion erhält eine Kennung (txid), meist den Hash ihrer Daten. Bis zur Aufnahme in einen Block ist sie unbestätigt (pending).",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Schema: Grundlegende Felder einer Transaktion.",
            diagramExtraNote = "Eine Transaktion ist eine Netzwerknachricht, keine Münzdatei auf dem Telefon.",
            realExample = "Kaffee bezahlen: Die Wallet erstellt eine Tx, signiert sie und sendet sie an einen Node. Miner/Validatoren nehmen sie in einen Block auf.",
            commonMistake = "«Sobald ich auf Senden klicke, ist es in 0 Sekunden unumkehrbar.» Falsch. Es gibt Weiterleitung, Mempool und Blockbestätigungen. Die Endgültigkeit hängt von der Bestätigungstiefe ab."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. UTXO-Modell vs. Account-Modell",
            content = "Bitcoin (UTXO): Das Guthaben ist keine einzelne Kontozahl, sondern eine Menge nicht ausgegebener Transaktionsausgänge (UTXOs). Eine neue Transaktion verbraucht bestimmte UTXOs und erzeugt neue.\n\nEthereum (Account-Modell): Jede Adresse besitzt einen Zustand (Balance, bei Smart Contracts Code & Speicher). Eine Transaktion verändert diesen Zustand direkt.\n\nBeide Modelle verhindern Double Spending mit unterschiedlicher Buchhaltungslogik. Daher unterscheidet sich die Funktionsweise je nach Netzwerk und Wallet.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Schema: Zwei unterschiedliche Buchhaltungsmodelle für dieselbe Aufgabe.",
            diagramExtraNote = null,
            realExample = "In Bitcoin besitzt du möglicherweise mehrere UTXO-Fragmente, die zusammen 0,12 BTC ergeben. In Ethereum siehst du einen zusammenhängenden ETH-Kontostand.",
            commonMistake = "«Alle Blockchains funktionieren wie ein Bankkonto.» Falsch. Bitcoin verzichtet bewusst auf ein kontobasiertes Datenmodell."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. Was ein Block enthält",
            content = "Block = Transaktionsdaten + Header (Blockkopf). Der Header enthält mindestens: Referenz zum vorherigen Block (Previous Hash), Transaktionszusammenfassung (Merkle Root), Zeitstempel und Konsensdaten (z. B. Nonce bei PoW).\n\nMerkle Tree: Transaktions-Hashes werden paarweise zusammengefasst bis zu einer einzigen Wurzel (Root). So lässt sich die Zugehörigkeit einer Tx beweisen, ohne den gesamten Blockinhalt zu übertragen.\n\nBlockgröße und Gas-Limit begrenzen die Anzahl der Transaktionen. Deshalb entstehen Gebühren und Wartezeiten.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Schema: Block-Header und Transaktions-Body.",
            diagramExtraNote = null,
            realExample = "Ein Bitcoin-Block entsteht im Schnitt alle ~10 Minuten. Ein Ethereum-Slot ist deutlich kürzer. Dies sind feste Protokollparameter.",
            commonMistake = "«Der Block ist die Münze.» Falsch. Der Block ist eine Seite im Hauptbuch. Die Münze ist eine Werteinheit darin."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. Wie Blöcke verkettet sind (Previous Hash)",
            content = "Jeder neue Block enthält in seinem Header den Hash des unmittelbar vorangehenden Blocks. So entsteht eine fortlaufende Kette.\n\nÄnderst du eine Transaktion in einem alten Block, ändert sich dessen Hash. Dadurch bricht die Verknüpfung zu allen nachfolgenden Blöcken. Um die Änderung durchzusetzen, müsste eine alternative Kette neu berechnet werden, die vom Netzwerk akzeptiert wird — bei großen Netzwerken praktisch unbezahlbar teuer.\n\nUnveränderlichkeit ist ein ökonomisch-rechnerisches Sicherheitsprinzip.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Schema: Jedes Glied verweist auf das vorherige.",
            diagramExtraNote = "Jeder Header enthält den Hash des Vorläufers. Änderungen in der Vergangenheit brechen die Kette ab.",
            realExample = "6 Bestätigungen bei Bitcoin bedeuten, dass 5 weitere Blöcke auf deinem Transaktionsblock aufbauen. Jeder Block macht eine Reorganisation exponentiell teurer.",
            commonMistake = "«In einer Blockchain kann sich niemals etwas ändern.» Falsch. Kurze Reorganisationen und Hard Forks existieren. Die Vergangenheit ist schwer umzuschreiben, aber nicht magisch immun."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Nodes & Peer-to-Peer-Netzwerk",
            content = "Node (Knotenpunkt) = Ein Computerprogramm, das das Protokoll ausführt. Ein Full Node lädt und validiert alle Regeln und die Historie. Ein Light Client benötigt weniger Daten und vertraut externen Servern.\n\nDas Netzwerk arbeitet Peer-to-Peer: Nodes tauschen Transaktionen und Blöcke direkt untereinander aus, ohne zentralen Hauptserver.\n\nEine mobile Smartphone-Wallet ist meist KEIN Full Node. Sie fragt externe RPC-Dienste ab. Das ist alltagstauglich, bedeutet aber nicht, dass man Bitcoin selbstständig validiert.",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Schema: Nodes verbinden sich untereinander, nicht mit einem zentralen Master.",
            diagramExtraNote = null,
            realExample = "Ein Bitcoin Full Node weist einen Block mit ungültiger Blockbelohnung sofort ab, selbst wenn er vom größten Mining-Pool erzeugt wurde.",
            commonMistake = "«Ich habe eine Wallet-App geöffnet, also bin ich ein Netzwerkknoten.» Meist falsch. Du bist Schlüsselbesitzer und kommunizierst mit fremden Nodes."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Warum Konsens notwendig ist",
            content = "Viele Nodes, weltweite Netzwerklatenzen und konkurrierende Transaktionen: Ohne eindeutige Regel zur Blockreihenfolge würde sich das Hauptbuch in widersprüchliche Versionen spalten.\n\nKonsens = Gemeinsame Regeln für gültige Blöcke sowie eine Kettenauswahlregel bei konkurrierenden Blöcken (z. B. schwerste Kette bei Bitcoin PoW, Fork-Choice-Regeln bei Ethereum).\n\nKonsens bedeutet keine politische Abstimmung. Es bedeutet, dass Software bei identischen Regeln und Daten zum exakt gleichen Buchungsstand gelangt.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Schema: Block-Vorschlag -> Regelprüfung -> Eine anerkannte Kette.",
            diagramExtraNote = "Ohne gemeinsame Regel würde jeder Node ein anderes Hauptbuch führen.",
            realExample = "Zwei Miner finden fast zeitgleich einen gültigen Block. Temporär existieren zwei Äste. Der nächste Block entscheidet, welcher Ast weitergeführt wird.",
            commonMistake = "«Konsens bedeutet, dass Coin-Halter in Foren abstimmen.» Falsch. Soziale Steuerung unterscheidet sich von den algorithmischen Konsensregeln im Protokoll."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — Der reale Mechanismus",
            content = "Beim Bitcoin PoW probiert der Miner verschiedene Header-Werte (Nonce usw.) aus und berechnet Hashes, bis der Hashwert unter dem aktuellen Schwierigkeitsziel (Difficulty) liegt.\n\nDie Difficulty passt sich automatisch an, damit die durchschnittliche Blockzeit trotz schwankender Rechenleistung (Hashrate) bei rund ~10 Minuten bleibt.\n\nEnergieaufwand ist kein Selbstzweck. Er stellt die physische Hürde dar, die das Umschreiben der Vergangenheit ökonomisch unrentabel macht. PoW beweist erbrachten Rechenaufwand gemäß Protokoll.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Schema: Nonce-Werte testen, bis der Hash unter dem Schwierigkeitsziel liegt.",
            diagramExtraNote = null,
            realExample = "Verdoppelt sich die weltweite Hashrate, würden Blöcke in 5 Minuten entstehen. Die automatische Difficulty-Anpassung regelt die Zeit wieder auf 10 Minuten ein.",
            commonMistake = "«Mining löst komplexe wissenschaftliche Gleichungen.» Bei Bitcoin ist das Rätsel ein bewusst gewählter Hash-Prüfprozess."
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — Der reale Mechanismus",
            content = "Beim PoS übernehmen Validatoren die Blockvorschläge und -bestätigungen, indem sie Kapital (Stake) direkt im Protokoll hinterlegen.\n\nStatt Stromkosten entstehen finanzielle Angriffshürden: Slashing (teilweiser oder vollständiger Verlust des hinterlegten Kapitals), wenn ein Validator widersprüchliche Blöcke signiert oder Regeln verletzt.\n\nEthereum nutzt seit 2022 PoS mit Slots, Epochen, Proposern und Attestations. Andere PoS-Netzwerke nutzen abweichende Varianten.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Schema: Kapital hinterlegen -> Proposer-Auswahl -> Bestätigung / Slashing.",
            diagramExtraNote = null,
            realExample = "Ein Ethereum-Validator, der zwei unterschiedliche Blöcke für denselben Slot signiert, wird vom Protokoll automatisch durch Slashing bestraft.",
            commonMistake = "«PoS funktioniert exakt wie eine Bank.» Technisch unzutreffend. Es handelt sich um einen eigenständigen Konsensalgorithmus mit spezifischen Sicherheitsmodellen."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool & Transaktionsgebühren",
            content = "Mempool = Der Arbeitsspeicher-Wartebereich eines Nodes für empfangene, aber noch nicht in einen Block aufgenommene Transaktionen. Jeder Node verwaltet seinen eigenen lokalen Mempool.\n\nDie Gebühr (Fee) bezahlt den knappen Blockplatz (Bytes bei Bitcoin, Gas bei Ethereum). Übersteigt die Nachfrage das Angebot, steigen die Gebühren.\n\nEine höhere Gebühr macht eine Transaktion nicht gültiger, sondern steigert den wirtschaftlichen Anreiz für Blockproduzenten, sie bevorzugt einzubinden.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Schema: Warten vor dem Block, Priorisierung nach Gebühr.",
            diagramExtraNote = null,
            realExample = "Bei hoher Netzauslastung auf Ethereum steigen die Gas-Preise (Gwei), auch wenn die Rechenoperation einer simplen Überweisung identisch bleibt.",
            commonMistake = "«Das Netzwerk verlangt Gebühren wie eine Bank, um Gewinne einzufahren.» Es gibt kein Unternehmen Bitcoin. Gebühren gehen an Blockproduzenten (oder werden teilweise verbrannt)."
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Wallets & Seed-Phrasen",
            content = "Eine Wallet ist eine Software (oder Hardware), die kryptografische Schlüssel verwaltet und signierte Transaktionen erstellt. Sie speichert keine physischen Bitcoins in der App.\n\nDie Seed-Phrase (BIP-39) ist eine lesbare Darstellung eines kryptografischen Geheimnisses, aus dem sämtliche Schlüssel deterministisch abgeleitet werden (HD Wallets, BIP-32/44). Wer die Seed-Phrase besitzt, besitzt den vollständigen Zugriff.\n\nCustodial: Ein Dritter verwahrt die Schlüssel. Non-Custodial: Die Schlüssel liegen ausschließlich auf deinem eigenen Gerät.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Schema: Seed -> Schlüssel -> Adressen -> Lokale Signatur.",
            diagramExtraNote = "Wer den Seed besitzt, kontrolliert die Coins. Die App ist keine Bank.",
            realExample = "Eine Hardware-Wallet signiert Transaktionen isoliert auf dem Gerät. Der private Schlüssel verlässt den Sicherheitschip niemals.",
            commonMistake = "«Ich habe einen Screenshot meiner Seed-Phrase in der Cloud gespeichert.» Das ist eine häufige Ursache für Totalverluste. Der Seed ist der Generalschlüssel."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Native Coins vs. Tokens",
            content = "Nativer Coin (Basis-Asset): Die Basiseinheit des Protokolls selbst (z. B. BTC bei Bitcoin, ETH bei Ethereum). Wird für Transaktionsgebühren und im Konsensmechanismus genutzt.\n\nToken: Eine Verrechnungseinheit, die über einen Smart Contract auf einer bestehenden Blockchain definiert wird (z. B. ERC-20). Seine Existenz hängt vom Host-Netzwerk und dem Vertragscode ab.\n\nEin Token ist keine eigene Blockchain, sondern eine Anwendungsschicht auf einer bestehenden Chain.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Schema: Protokoll-Coin gegenüber Smart-Contract-Token.",
            diagramExtraNote = null,
            realExample = "USDT auf Ethereum ist ein ERC-20 Token. Das zur Zahlung der Transaktionsgebühr benötigte ETH ist der native Coin.",
            commonMistake = "«Jede Kryptowährung hat ihre eigene Blockchain.» Falsch für Tausende Tokens, die als Smart Contracts existieren."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Smart Contracts",
            content = "Smart Contract = Ein direkt auf der Blockchain gespeichertes Programm. Sein Status ändert sich, sobald eine gültige Transaktion eine Vertragsfunktion aufruft. Der Bytecode wird deterministisch von Validatoren in einer Virtual Machine (z. B. EVM) ausgeführt.\n\n«Automatisiert» bedeutet nicht «risikofrei». Programmierfehler, kompromittierte Admin-Schlüssel, Oracle-Abhängigkeiten und irreversible Ausführung sind reale Risiken.\n\nBitcoin besitzt eine bewusst begrenzte Skriptsprache ohne universelle Turing-Vollständigkeit. Komplexe dezentrale Anwendungen laufen daher auf spezialisierten Smart-Contract-Netzwerken.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Schema: Code auf Chain -> Funktionsaufruf + Gas -> Neuer Zustand.",
            diagramExtraNote = null,
            realExample = "Uniswap ist ein Verbund aus Smart Contracts. Ein Token-Tausch erfolgt durch direkten Funktionsaufruf ohne menschliche Vermittler.",
            commonMistake = "«Ein Smart Contract ist ein juristischer Vertrag, der vor Gericht schützt.» Es ist schlicht ausführbarer Programmcode. Rechtlicher Schutz ist eine separate Ebene."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Reale physikalische & ökonomische Grenzen",
            content = "Skalierbarkeit: Ein öffentliches Layer-1-Netzwerk kann nicht unbegrenzt viele Transaktionen pro Sekunde verarbeiten, ohne Dezentralisierung oder günstige Gebühren aufzugeben. Daher entstehen Layer-2-Rollups und modulare Architekturen.\n\nEnergie / Kapital: PoW verbraucht physische Energie, PoS bindet wirtschaftliches Kapital. Ein sicherer Konsens erfordert messbare Angriffskosten.\n\nPrivatsphäre: Die meisten öffentlichen Blockchains sind pseudonym, nicht anonym. Transaktionsflüsse sind öffentlich einsehbar.\n\nForks: Software- und Gemeinschaftsentwicklungen können sich spalten. Die gültige Historie ist jene Kette, die die Teilnehmer aktiv weiterführen.\n\nDiese Grenzen machen die Technologie nicht unbrauchbar, sondern definieren ihren präzisen Einsatzzweck.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Schema: Grenzen sind Systembestandteile, keine Fußnoten.",
            diagramExtraNote = null,
            realExample = "Ein Rollup bündelt Tausende Transaktionen außerhalb von L1 und übermittelt komprimierte Daten an L1 zurück, um Kapazität zu gewinnen.",
            commonMistake = "«Blockchain löst gleichzeitig Geschwindigkeit, Anonymität, Skalierung und Nullkosten.» In verteilten Systemen gibt es keine Gratis-Lösungen."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Liquidierungs-Mechanik & Hebel",
            content = "Im Krypto-Derivatehandel erlaubt Leverage kapital-effiziente Positionen. Erreicht der Markt die Liquidationsmarke, schließt die Börsen-Engine Positionen automatisch.\n\nKaskadierende Liquidationen erzeugen Long- oder Short-Squeezes, die Liquiditätscluster im Orderbuch durchschlagen.\n\nDisziplinierte Margin-Puffer sind die Grundvoraussetzung für langfristigen Kapitalerhalt.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Schema: Margin-Erschöpfung -> Liquidations-Schwelle -> Marktauftrag-Kaskade.",
            diagramExtraNote = null,
            realExample = "Ein plötzlicher 5%-Ruck führt bei 20x Hebel zur sofortigen Zwangsliquidation.",
            commonMistake = "«Hoher Hebel bedeutet einfach mehr Gewinn.» Er potenziert vor allem das Ausfallrisiko auf 100%."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Finanzierungsraten & Perpetual Swaps",
            content = "Perpetual Swaps besitzen kein Verfallsdatum. Die periodische Funding Rate bindet den Kontraktpreis an den Spot-Index.\n\nPositives Funding bedeutet, dass Longs an Shorts zahlen (überhitzter Markt). Negatives Funding deutet auf Baisse-Erschöpfung hin.\n\nExtreme Funding-Ausschläge dienen häufig als treffsichere Kontra-Indikatoren.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Schema: Kontraktpreis > Spotpreis -> Positives Funding (Long zahlt Short).",
            diagramExtraNote = null,
            realExample = "In Euphorie-Phasen erreicht das Funding +0,08% alle 8 Stunden und verteuert Haltepositionen massiv.",
            commonMistake = "«Funding ist eine Gebühr der Börse.» Es ist eine Peer-to-Peer-Ausgleichszahlung zwischen Tradern."
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Orderflow & Liquiditätscluster",
            content = "Das Orderbuch bündelt Limit-Kauf- und Verkaufsaufträge. Die Markttiefe visualisiert verfügbare Liquidität bei Kursbewegungen.\n\nInstitutionelle Akteure nutzen TWAP- und Iceberg-Algorithmen, um Großaufträge slippage-arm auszuführen.\n\nLiquiditäts-Cluster visualisieren Stop-Loss-Zentren und institutionelle Barrieren.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Schema: Orderbuch-Tiefe -> Bid/Ask-Cluster -> Slippage & Ausführung.",
            diagramExtraNote = null,
            realExample = "Große Market-Verkäufe in dünner Markttiefe führen zu massivem Slippage.",
            commonMistake = "«Sichtbare Limit-Orders werden garantiert ausgeführt.» Oft handelt es sich um flüchtiges Spoofing."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Makro-Halving-Zyklen & Angebotsdynamik",
            content = "Das Bitcoin-Halving halbiert alle 210.000 Blöcke die Miner-Belohnung (von 50 BTC 2009 auf 3,125 BTC 2024).\n\nDieser programmierte Angebotsschock treibt historisch 4-jährige Makrozyklen aus Akkumulation, Expansion und Korrektur.\n\nDie zyklische Einordnung liefert einen robusten Rahmen für strategische Allokation.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Schema: Halving -> Miner-Ausgabe um 50% gekürzt -> Angebotsschock.",
            diagramExtraNote = null,
            realExample = "In vergangenen Zyklen lag das Zyklushoch 12 bis 18 Monate nach dem Halving.",
            commonMistake = "«Der Kurs explodiert exakt am Tag des Halvings.» Der Angebotsschock wirkt kumulativ über Monate."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Institutionelles Risikomanagement",
            content = "Dauerhafte Rentabilität entsteht nicht durch Prognosen, sondern durch strikte Verlustbegrenzung und Position Sizing.\n\nProfessionelle Manager riskieren maximal 1-2% Gesamtkapital pro Trade und leiten Positionsgrößen vom Stop-Loss ab.\n\nKapitalschutz und emotionslose Ausführung sichern institutionelle Langlebigkeit.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Schema: Gesamtkapital -> 1-2% festes Risiko -> Dynamische Positionsgröße.",
            diagramExtraNote = null,
            realExample = "Bei 1% Risiko pro Trade übersteht ein Portfolio 20 Fehltrades mit über 81% Restkapital.",
            commonMistake = "«All-in auf einen Coin ist der schnellste Weg zum Erfolg.» Ohne Risikolimit führt dies mathematisch zum Totalverlust."
        )
    )

    private val frenchChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Rareté numérique & Double dépense",
            content = "Un fichier numérique se copie sans détruire l'original. C'est pourquoi un « fichier-monnaie » sur un téléphone ne peut pas être de l'argent : la même personne pourrait l'envoyer deux fois.\n\nLe problème classique s'appelle la double dépense (double spending) : dépenser deux fois la même unité de valeur. Les banques le résolvent via un registre central. La blockchain le résout via un historique public vérifié par tous.\n\nLa blockchain ne crée pas de rareté par magie. Elle définit des règles strictes déterminant quelle transaction est valide et laquelle est rejetée.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Schéma : Le même objet numérique peut exister en deux copies.",
            diagramExtraNote = "Un fichier numérique se duplique. L'argent en banque n'est pas un fichier sur votre disque.",
            realExample = "Si vous envoyez la même unité à deux destinataires avant la validation par le réseau, une seule version restera dans l'historique accepté.",
            commonMistake = "« Le Bitcoin est un fichier stocké sur mon téléphone. » Faux. Le téléphone détient des clés cryptographiques. Le solde est un ensemble d'écritures dans le registre partagé."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Base de données centrale vs Registre distribué",
            content = "Base de données centralisée : Une seule entité tient le registre officiel (banque, société de paiement). C'est efficace, mais exige une confiance totale et constitue un point de défaillance unique.\n\nRegistre distribué (Distributed Ledger) : De nombreuses machines indépendantes conservent une copie des mêmes enregistrements et vérifient les nouvelles transactions avec des règles communes.\n\n« Décentralisé » ne signifie pas absence totale d'autorité, mais absence d'intermédiaire obligatoire pour qu'un paiement valide soit enregistré.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Schéma : Un serveur central face à un réseau de nœuds partageant le même registre.",
            diagramExtraNote = null,
            realExample = "Visa met à jour sa propre base interne. Sur Bitcoin, des milliers de nœuds complets vérifient exactement les mêmes règles.",
            commonMistake = "« La blockchain signifie que personne n'a de pouvoir. » Faux. Il y a des mineurs/validateurs, des développeurs et des plateformes. Mais le protocole ne dépend d'aucune entreprise pour valider une transaction légitime."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Hachage cryptographique",
            content = "Une fonction de hachage prend des données de taille arbitraire et produit une chaîne de caractères de longueur fixe (digest / empreinte).\n\nPropriétés clés : rapide à calculer dans un sens, impossible de trouver deux messages ayant la même empreinte (résistance aux collisions) et un infime changement en entrée bouleverse toute la sortie (effet avalanche).\n\nBitcoin utilise SHA-256 et Ethereum Keccak-256. Un hash n'est pas un chiffrement que l'on déverrouille : aucune clé ne permet de reconstituer le texte d'origine à partir du hash.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Schéma : N'importe quelle entrée -> empreinte fixe unique.",
            diagramExtraNote = "Légère modification des données -> hash entièrement différent. Non réversible.",
            realExample = "Modifiez un seul caractère dans une transaction et le hash du bloc change totalement, rendant toute falsification immédiatement détectable.",
            commonMistake = "« Le hash cache les données et seul le propriétaire peut les lire. » Faux. Les hashes accompagnent des données publiques. C'est une empreinte, pas un coffre-fort."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Clés & Signatures numériques",
            content = "Cryptographie asymétrique : clé privée et clé publique. La clé privée reste secrète. De celle-ci découle la clé publique, puis l'adresse publique.\n\nSignature numérique : Avec votre clé privée, vous signez une transaction. Quiconque dispose de la clé publique peut vérifier mathématiquement que la signature provient du détenteur légitime, sans jamais connaître la clé privée.\n\nSur Bitcoin/Ethereum, la possession des fonds équivaut au contrôle exclusif de la clé privée, et non à un « compte auprès de l'éditeur de l'application ».",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Schéma : La clé privée signe. La clé publique vérifie.",
            diagramExtraNote = "Clé privée -> RESTE SECRÈTE",
            realExample = "Vous signez « envoyer 0,01 BTC à l'adresse X ». Les nœuds vérifient la signature et acceptent la transaction si les fonds sont disponibles.",
            commonMistake = "« L'adresse est comme un mot de passe. » Faux. L'adresse est publique. Le secret absolu réside dans la clé privée / phrase de récupération."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Anatomie d'une transaction",
            content = "Une transaction est un ensemble de données indiquant au réseau comment mettre à jour le registre : quelles entrées sont consommées, quelles sorties sont créées, et la signature d'autorisation.\n\nAucun « fichier de pièce » n'est déplacé. C'est le droit de disposer du solde qui est transféré selon les règles du protocole.\n\nChaque transaction reçoit un identifiant unique (txid), hash de ses données. Tant qu'elle n'est pas incluse dans un bloc, elle demeure en attente (mempool).",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Schéma : Champs fondamentaux d'une transaction.",
            diagramExtraNote = "Une transaction est un message réseau, pas un fichier monétaire sur téléphone.",
            realExample = "Payer un café : le portefeuille génère la tx, la signe et la diffuse au réseau. Les mineurs/validateurs l'intègrent dans un bloc.",
            commonMistake = "« Dès que je clique sur envoyer, c'est irréversible en 0 seconde. » Faux. Il y a propagation, mempool puis confirmations de blocs."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. Modèle UTXO vs Modèle de compte",
            content = "Bitcoin (UTXO) : Le solde n'est pas un nombre unique sur un compte, mais un ensemble de sorties de transactions non dépensées (UTXO). Une transaction consomme des UTXO existants et en génère de nouveaux.\n\nEthereum (Modèle de compte) : Chaque adresse possède un état direct (solde, code et stockage pour les smart contracts). Une transaction met à jour cet état.\n\nLes deux modèles empêchent la double dépense avec des logiques comptables différentes.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Schéma : Deux modèles comptables distincts pour résoudre le même défi.",
            diagramExtraNote = null,
            realExample = "Sur Bitcoin, vous pouvez détenir 3 fragments UTXO totalisant 0,12 BTC. Sur Ethereum, vous visualisez un solde global unique.",
            commonMistake = "« Toutes les blockchains fonctionnent comme un compte bancaire. » Faux. Bitcoin évite délibérément le modèle basé sur les comptes."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. Contenu d'un bloc",
            content = "Bloc = Ensemble de transactions + En-tête (Header). L'en-tête contient au minimum : la référence au bloc précédent (hash précédent), le résumé des transactions (Merkle root), l'horodatage et les données de consensus (nonce en PoW).\n\nArbre de Merkle : Les hashes des transactions sont combinés par paires jusqu'à une racine unique, prouvant l'appartenance d'une transaction sans transmettre tout le bloc.\n\nLa taille du bloc et la limite de gas restreignent le nombre de transactions intégrables, créant files d'attente et frais.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Schéma : En-tête de bloc et corps des transactions.",
            diagramExtraNote = null,
            realExample = "Un bloc Bitcoin est produit environ toutes les 10 minutes. Un slot Ethereum est bien plus court. Ce sont des paramètres de conception stricts.",
            commonMistake = "« Le bloc est la pièce de monnaie. » Faux. Le bloc est une page du grand livre d'historique."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. Enchaînement des blocs (Previous Hash)",
            content = "Chaque nouveau bloc intègre dans son en-tête le hash du bloc précédent, formant une chaîne continue et indissociable.\n\nModifier une transaction dans un bloc passé change son hash, rompant la chaîne avec tous les blocs suivants. Pour imposer cette modification, il faudrait recalculer une chaîne alternative acceptée par le réseau, ce qui est économiquement hors de portée sur les grands réseaux.\n\nL'immuabilité est une garantie économique et calculatoire.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Schéma : Chaque maillon pointe vers le précédent.",
            diagramExtraNote = "Chaque en-tête contient le hash du précédent. Modifier le passé brise la chaîne.",
            realExample = "6 confirmations sur Bitcoin signifient que 5 blocs ont été ajoutés par-dessus le vôtre, rendant toute réorganisation exponentiellement coûteuse.",
            commonMistake = "« Rien ne change jamais dans une blockchain. » Faux. De courtes réorganisations et des hard forks surviennent. Réécrire le passé est extrêmement difficile, pas surnaturel."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Nœuds & Réseau Peer-to-Peer",
            content = "Nœud (Node) = Logiciel exécutant le protocole. Un nœud complet (Full Node) télécharge et valide toutes les règles et l'historique. Un client léger fait confiance à des tiers.\n\nLe réseau fonctionne en pair-à-pair (P2P) : les nœuds s'échangent transactions et blocs directement sans serveur central obligatoire.\n\nUn portefeuille mobile n'est généralement PAS un nœud complet. Il interroge des services RPC externes.",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Schéma : Les nœuds communiquent entre pairs sans maître central.",
            diagramExtraNote = null,
            realExample = "Un nœud complet Bitcoin rejette instantanément un bloc contenant une création illégale de pièces, même émis par un énorme pool de minage.",
            commonMistake = "« J'ai installé une application wallet, je suis donc un nœud du réseau. » Généralement non. Vous détenez des clés et communiquez avec le nœud d'un tiers."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Nécessité du Consensus",
            content = "Multiplicité des nœuds, latences réseau et transactions concurrentes : sans règle d'ordonnancement, le registre se fragmenterait en versions incompatibles.\n\nConsensus = Ensemble de règles définissant la validité d'un bloc et la chaîne prioritaire en cas de branches concurrentes (chaîne la plus lourde en PoW, règles fork-choice en PoS).\n\nLe consensus n'est pas un débat politique, mais un mécanisme algorithmique garantissant que des logiciels indépendants adoptent exactement le même historique.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Schéma : Proposition -> Vérification des règles -> Registre unique accepté.",
            diagramExtraNote = "Sans règle commune, chaque nœud conserverait un livre de comptes différent.",
            realExample = "Deux mineurs trouvent un bloc valide simultanément. Deux branches coexistent temporairement jusqu'à ce que le bloc suivant tranche la continuité.",
            commonMistake = "« Le consensus est un vote des détenteurs sur les réseaux sociaux. » Faux. La gouvernance sociale est distincte des règles algorithmiques du protocole."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — Le mécanisme réel",
            content = "En PoW (Bitcoin), le mineur teste différentes valeurs dans l'en-tête (nonce) et calcule des hashes jusqu'à obtenir un résultat inférieur à la cible de difficulté (Difficulty Target).\n\nLa difficulté s'ajuste automatiquement afin que le temps moyen entre chaque bloc reste stable (~10 minutes), quelle que soit la puissance de calcul globale (hashrate).\n\nL'énergie dépensée constitue le coût physique réel qui rend la réécriture de l'historique prohibitif.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Schéma : Tester des nonces jusqu'à atteindre la cible de difficulté.",
            diagramExtraNote = null,
            realExample = "Si la puissance du réseau double, l'ajustement automatique de difficulté recalibre le rythme moyen d'émission à 10 minutes.",
            commonMistake = "« Le minage résout des calculs scientifiques utiles. » Dans Bitcoin, l'énigme est délibérément un calcul de hachage arbitraire difficile à trouver mais trivial à vérifier."
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — Le mécanisme réel",
            content = "En PoS, la proposition et la validation des blocs sont assurées par des validateurs ayant immobilisé du capital (stake) dans le protocole.\n\nAu lieu de l'électricité, la barrière de sécurité est financière : le slashing (confiscation du capital déposé) pénalise tout validateur tentant de signer des blocs contradictoires.\n\nEthereum utilise le PoS avec slots, époques, proposants et attestations depuis 2022.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Schéma : Capital engagé -> Choix du proposant -> Attestation / Slashing.",
            diagramExtraNote = null,
            realExample = "Un validateur Ethereum proposant deux blocs contradictoires au même slot subit une pénalité automatique de slashing sur son dépôt.",
            commonMistake = "« Le PoS est simplement le fonctionnement d'une banque. » Techniquement inexact : c'est un algorithme de consensus décentralisé avec ses propres mécanismes de défense."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool & Frais de transaction",
            content = "Mempool = File d'attente en mémoire vive d'un nœud pour les transactions reçues mais non encore intégrées dans un bloc. Chaque nœud gère son propre mempool local.\n\nLes frais rémunèrent l'espace limité dans le bloc (octets sur Bitcoin, gas sur Ethereum). Quand la demande augmente, les frais montent mécaniquement.\n\nDes frais plus élevés n'augmentent pas la validité d'une transaction, mais incitent les producteurs de blocs à l'inclure en priorité.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Schéma : File d'attente avant inclusion, priorisation selon les frais.",
            diagramExtraNote = null,
            realExample = "Lors de fortes congestions sur Ethereum, le prix du gas (Gwei) s'envole même pour une simple transaction de transfert.",
            commonMistake = "« Le réseau prélève des frais comme une entreprise bancaire. » Aucune société ne possède Bitcoin. Les frais vont aux producteurs de blocs (ou sont partiellement brûlés)."
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Portefeuilles & Phrases de récupération (Seed)",
            content = "Un wallet est un logiciel (ou matériel) qui gère des clés cryptographiques et génère des transactions signées. Il ne stocke pas de pièces à l'intérieur de l'application.\n\nLa seed phrase (BIP-39) est la forme lisible d'un secret cryptographique maître permettant de dériver l'ensemble des clés privées (portefeuilles HD, BIP-32/44).\n\nCustodial : Un tiers détient les clés. Non-custodial : Vous conservez le contrôle exclusif de vos clés sur votre appareil.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Schéma : Seed -> Clés -> Adresses -> Signature locale.",
            diagramExtraNote = "Celui qui détient la seed contrôle les fonds. L'app n'est pas une banque.",
            realExample = "Un hardware wallet isole la signature sur l'appareil : la clé privée ne transite jamais sur l'ordinateur connecté.",
            commonMistake = "« J'ai fait une capture d'écran de ma seed phrase dans le cloud. » C'est la principale cause de vol de fonds. La seed phrase est la clé du coffre."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Coin natif vs Token",
            content = "Coin natif : L'unité de base fondamentale du protocole lui-même (BTC sur Bitcoin, ETH sur Ethereum). Sert au paiement des frais et sécurise le consensus.\n\nToken : Unité comptable définie par un contrat intelligent (Smart Contract, ex: ERC-20) déployé sur une blockchain hôte. Sa sécurité dépend du réseau hôte et du code du contrat.\n\nUn token n'est pas une blockchain indépendante, mais une couche applicative.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Schéma : Coin de protocole vs Token de smart contract.",
            diagramExtraNote = null,
            realExample = "L'USDT sur Ethereum est un token ERC-20. L'ETH utilisé pour régler le gas de la transaction est la monnaie native.",
            commonMistake = "« Chaque cryptomonnaie possède sa propre blockchain. » Faux pour les milliers de tokens régis par des smart contracts."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Contrats intelligents (Smart Contracts)",
            content = "Smart contract = Programme enregistré directement sur la blockchain dont l'état change lors de l'exécution d'une transaction valide. Le bytecode est exécuté par tous les validateurs via une machine virtuelle (ex: EVM).\n\n« Automatisé » n'exclut pas les risques : bugs de code, clés d'administration compromises et exécution irréversible sont des facteurs critiques.\n\nBitcoin privilégie un langage Script restreint sans EVM générale pour préserver sa robustesse fondamentale.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Schéma : Code sur la chaîne -> Appel + Frais -> Nouvel état.",
            diagramExtraNote = null,
            realExample = "Uniswap est un ensemble de smart contracts permettant des échanges décentralisés automatisés sans intermédiaire humain.",
            commonMistake = "« Le smart contract est un document juridique officiel. » Techniquement, c'est du code informatique. La validité juridique relève du droit civil, pas de l'EVM."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Limites physiques & économiques réelles",
            content = "Débit : Une couche 1 (Layer-1) publique ne peut traiter un nombre infini de transactions par seconde sans sacrifier la décentralisation. D'où l'essor des solutions Layer-2 et architectures modulaires.\n\nÉnergie / Capital : Le PoW requiert de l'électricité, le PoS mobilise du capital économique. La sécurité repose sur un coût réel d'attaque.\n\nConfidentialité : Les blockchains publiques sont pseudonymes et non anonymes : les flux financiers y sont transparents et auditables.\n\nForks : Le consensus peut diverger lors de mises à jour de règles.\n\nCes limites ne disqualifient pas la technologie, mais précisent son domaine d'application pertinent.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Schéma : Les limites font partie intégrante du système.",
            diagramExtraNote = null,
            realExample = "Un rollup regroupe des milliers de transactions hors de la L1 et publie des preuves condensées sur la L1 pour décupler le débit.",
            commonMistake = "« La blockchain offre à la fois débit infini, anonymat total et gratuité absolue. » Il n'y a pas de repas gratuit dans les systèmes distribués."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Ingénierie des Liquidations & Levier",
            content = "Sur les marchés dérivés, l'effet de levier optimise l'exposition en capital. Lorsque le marché atteint le seuil de liquidation, le moteur de risque ferme automatiquement la position.\n\nLes cascades de liquidation provoquent des retournements brutaux (short/long squeezes) en balayant la profondeur du carnet d'ordres.\n\nUne gestion stricte de la marge de maintien est cruciale pour éviter la faillite du compte.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Schéma : Épuisement de marge -> Déclencheur liquidation -> Vente/Achat forcé.",
            diagramExtraNote = null,
            realExample = "Une chute subite de 5% déclenche les liquidations des positions à levier 20x.",
            commonMistake = "« Le fort levier augmente seulement les gains. » Il augmente surtout le risque de perte totale à 100%."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Dynamique des Taux de Financement",
            content = "Les contrats perpétuels n'ont pas d'échéance. Le Funding Rate équilibre le prix du contrat avec le prix au comptant (spot).\n\nUn taux positif indique que les positions Long paient les Shorts (marché euphorique). Un taux négatif signale un pessimisme extrême propice aux squeezes.\n\nLes pics de funding sont des signaux clés de retournement cyclique.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Schéma : Prix Perpétuel > Spot -> Financement positif (Longs paient Shorts).",
            diagramExtraNote = null,
            realExample = "En euphorie haussière, le taux peut atteindre +0,08% toutes les 8h, pénalisant la conservation des positions longues.",
            commonMistake = "« Le funding est une commission prélevée par la bourse. » C'est un paiement direct entre traders."
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Flux d'Ordres & Clusters de Liquidité",
            content = "Le carnet d'ordres compile les intentions d'achat et de vente. La profondeur montre le volume nécessaire pour faire osciller le cours.\n\nLes investisseurs institutionnels recourent aux ordres Iceberg et algorithmes TWAP/VWAP pour masquer leurs entrées.\n\nL'analyse des clusters de liquidité identifie les zones majeures d'absorption.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Schéma : Carnet d'ordres -> Clusters de liquidité -> Glissement de prix (slippage).",
            diagramExtraNote = null,
            realExample = "Un ordre de vente massif au marché dans un carnet peu liquide subit un slippage sévère.",
            commonMistake = "« Les ordres visibles sont garantis exécutés. » Le spoofing entraîne de fréquentes annulations."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Cycles Macro de Halving & Dynamique d'Offre",
            content = "Le Halving de Bitcoin survient tous les 210 000 blocs (~4 ans), réduisant l'émission des mineurs de moitié (de 50 BTC en 2009 à 3,125 BTC en 2024).\n\nCe choc d'offre programme des cycles macro de 4 ans : Accumulation, Expansion haussière, Distribution et Marché baissier.\n\nComprendre le cycle offre un repère stratégique pour l'allocation patrimoniale.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Schéma : Émission divisée par 2 -> Choc d'offre -> Cycle macro de 4 ans.",
            diagramExtraNote = null,
            realExample = "Lors des cycles précédents, le sommet de marché est apparu 12 à 18 mois après le halving.",
            commonMistake = "« Le prix explose le jour même du halving. » Le choc d'offre met des mois à se matérialiser."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Gestion des Risques Institutionnels",
            content = "La rentabilité pérenne dépend de la maîtrise du drawdown et du dimensionnement des positions, non de prédictions parfaites.\n\nLes gestionnaires limitent le risque à 1-2% du capital par transaction et ajustent la taille selon la distance au stop-loss.\n\nLa discipline émotionnelle et la préservation du capital garantissent la longévité.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Schéma : Capital total -> Risque fixe 1-2% -> Dimensionnement strict de position.",
            diagramExtraNote = null,
            realExample = "En risquant 1% par position, un trader préserve plus de 81% de son capital après 20 pertes consécutives.",
            commonMistake = "« Miser 'all-in' sur un actif est le raccourci vers la richesse. » C'est la garantie mathématique de ruine."
        )
    )

    private val spanishChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Escasez digital y doble gasto",
            content = "Un archivo digital se puede copiar sin destruir el original. Por eso un «archivo-moneda» en un móvil no puede ser dinero por sí mismo: la misma persona podría enviarlo dos veces.\n\nEl problema clásico se llama doble gasto (double spending): gastar la misma unidad de valor dos veces. Los bancos lo resuelven con un libro mayor central. La blockchain lo resuelve mediante un historial público verificado por múltiples nodos.\n\nLa blockchain no crea escasez por arte de magia. Establece reglas claras para determinar qué transacción es válida y cuál queda rechazada.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Esquema: El mismo objeto digital puede existir en dos copias.",
            diagramExtraNote = "Los archivos digitales se copian. El dinero en el banco no es un archivo en tu disco.",
            realExample = "Si envías la misma unidad a dos personas antes de que la red confirme un bloque, solo una versión permanecerá en el historial aceptado.",
            commonMistake = "«Bitcoin es un archivo en mi teléfono.» Falso. En el teléfono se guardan claves privadas. El saldo son registros en el historial compartido."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Base de datos central vs Libro mayor distribuido",
            content = "Base de datos centralizada: Una única entidad mantiene el libro oficial (banco, empresa de pagos). Es eficiente, pero requiere confianza ciega y representa un único punto de fallo.\n\nLibro mayor distribuido (Distributed Ledger): Múltiples máquinas independientes mantienen copias idénticas y verifican las transacciones con reglas compartidas.\n\n«Descentralizado» no significa ausencia de reglas, sino que no existe un administrador obligatorio para que un pago legítimo sea liquidado.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Esquema: Un servidor central frente a una red de nodos con el mismo registro.",
            diagramExtraNote = null,
            realExample = "Visa actualiza su propia base de datos. En Bitcoin, miles de nodos completos verifican exactamente las mismas reglas.",
            commonMistake = "«Blockchain significa que nadie tiene poder.» Falso. Existen mineros/validadores, desarrolladores y exchanges. Pero el protocolo no requiere una empresa para validar transacciones."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Hash criptográfico",
            content = "Una función hash recibe datos de cualquier tamaño y genera una cadena de longitud fija (digest o huella digital).\n\nPropiedades esenciales: fácil de calcular hacia adelante, imposible encontrar dos mensajes distintos con el mismo resultado (resistencia a colisiones) y un cambio mínimo en la entrada altera totalmente la salida (efecto avalancha).\n\nBitcoin utiliza SHA-256 y Ethereum Keccak-256. Un hash no es un archivo cifrado que se desbloquea con llave: no existe una clave para revertir el hash al texto original.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Esquema: Cualquier entrada -> huella digital fija única.",
            diagramExtraNote = "Cambio mínimo en los datos -> hash totalmente distinto. No invertible.",
            realExample = "Cambia un solo carácter en una transacción y el hash del bloque cambiará por completo, haciendo evidente cualquier alteración.",
            commonMistake = "«El hash oculta los datos y solo el dueño puede verlos.» Falso. La mayoría de hashes acompañan a datos públicos. Es una huella, no una caja fuerte."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Claves y firmas digitales",
            content = "Criptografía asimétrica: clave privada y clave pública. La clave privada se mantiene en secreto. De ella se deriva la clave pública y posteriormente la dirección.\n\nFirma digital: Con la clave privada firmas una transacción. Cualquiera con la clave pública puede verificar matemáticamente la autoría sin conocer la clave privada.\n\nEn Bitcoin y Ethereum, la propiedad de los fondos es la posesión exclusiva de la clave privada, no una cuenta en una empresa de software.",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Esquema: La clave privada firma. La clave pública verifica.",
            diagramExtraNote = "Clave privada -> SE MANTIENE EN SECRETO",
            realExample = "Firmas «enviar 0.01 BTC a la dirección X». Los nodos verifican la firma y aceptan la transacción si los fondos no han sido gastados.",
            commonMistake = "«La dirección es como una contraseña.» Falso. La dirección es pública. El secreto es la clave privada o la frase semilla."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Anatomía de una transacción",
            content = "Una transacción es un conjunto de datos que indica a la red cómo actualizar el libro mayor: qué entradas gasta, qué salidas crea y la firma de autorización.\n\nNo se transfiere ningún «archivo de moneda». Se transfiere el derecho de saldo de acuerdo con las reglas del protocolo.\n\nCada transacción válida recibe un identificador único (txid). Hasta ser incluida en un bloque, permanece pendiente en la mempool.",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Esquema: Campos esenciales de una transacción.",
            diagramExtraNote = "Una transacción es un mensaje de red, no un archivo de moneda en el móvil.",
            realExample = "Pagar un café: la wallet genera la transacción, la firma y la transmite a los nodos para que los validadores la incluyan en un bloque.",
            commonMistake = "«En cuanto pulso enviar, es irreversible en 0 segundos.» Falso. Existe propagación, mempool y confirmaciones de bloques."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. Modelo UTXO vs Modelo de cuentas",
            content = "Bitcoin (UTXO): El saldo no es un número único en una cuenta, sino un conjunto de salidas de transacciones no gastadas (UTXOs). Una nueva transacción consume UTXOs previos y crea otros nuevos.\n\nEthereum (Modelo de cuentas): Cada dirección tiene un estado directo con saldo y código. Una transacción modifica directamente ese estado.\n\nAmbos modelos previenen el doble gasto mediante lógicas contables distintas.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Esquema: Dos modelos contables diferentes para el mismo problema.",
            diagramExtraNote = null,
            realExample = "En Bitcoin puedes tener 3 fragmentos UTXO que suman 0.12 BTC. En Ethereum ves un saldo unificado en la dirección.",
            commonMistake = "«Todas las blockchains funcionan como una cuenta bancaria.» Falso. Bitcoin evita deliberadamente el modelo basado en cuentas."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. Qué contiene un bloque",
            content = "Bloque = Conjunto de transacciones + Cabecera (Header). La cabecera incluye al menos: referencia al bloque anterior (previous hash), resumen de transacciones (Merkle root), marca de tiempo y datos de consenso (nonce en PoW).\n\nÁrbol de Merkle: Los hashes se combinan en pares hasta obtener una raíz única, permitiendo verificar la pertenencia de una transacción sin enviar el bloque entero.\n\nEl tamaño de bloque y el límite de gas determinan cuántas transacciones entran, fijando tarifas y tiempos de espera.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Esquema: Cabecera del bloque y cuerpo de transacciones.",
            diagramExtraNote = null,
            realExample = "Un bloque de Bitcoin se genera cada ~10 minutos en promedio. Un slot en Ethereum es mucho más breve.",
            commonMistake = "«El bloque es la moneda.» Falso. El bloque es una página del libro mayor de historial."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. Cómo se encadenan los bloques (Previous Hash)",
            content = "Cada nuevo bloque incluye en su cabecera el hash del bloque inmediatamente anterior, formando una cadena ininterrumpida.\n\nSi alteras una transacción en un bloque antiguo, su hash cambia y rompe el enlace con los bloques posteriores. Para validar el cambio habría que recalcular una cadena alternativa aceptada por la red, algo prácticamente inasumible económicamente en redes grandes.\n\nLa inmutabilidad es una propiedad económica y computacional.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Esquema: Cada eslabón apunta al anterior.",
            diagramExtraNote = "Cada cabecera contiene el hash del bloque previo. Alterar el pasado rompe la cadena.",
            realExample = "6 confirmaciones en Bitcoin implican que se han apilado 5 bloques sobre el tuyo, haciendo casi imposible una reorganización.",
            commonMistake = "«Nada cambia jamás en una blockchain.» Falso. Existen reorganizaciones menores y bifurcaciones (forks). Reescribir el pasado es extremadamente difícil, no mágico."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Nodos y red Peer-to-Peer",
            content = "Nodo (Node) = Programa que ejecuta el protocolo. Un nodo completo (Full Node) descarga y verifica todas las reglas y el historial. Un cliente ligero confía en servicios externos.\n\nLa red es Peer-to-Peer: los nodos intercambian transacciones y bloques directamente sin un servidor central obligatorio.\n\nUna wallet móvil normalmente NO es un nodo completo; consulta servidores RPC externos.",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Esquema: Los nodos se conectan entre sí sin un servidor maestro.",
            diagramExtraNote = null,
            realExample = "Un nodo completo de Bitcoin rechaza un bloque con emisión ilegal de monedas aunque proceda del mayor pool de minería.",
            commonMistake = "«He instalado una app de wallet, por tanto soy un nodo de la red.» Generalmente no. Posees claves que interactúan con nodos de terceros."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Por qué es necesario el Consenso",
            content = "Múltiples nodos, latencia de red y transacciones simultáneas: sin una regla clara de ordenación, el registro se fragmentaría en versiones contradictorias.\n\nConsenso = Reglas compartidas para validar bloques y seleccionar la cadena legítima ante bifurcaciones (cadena con mayor trabajo acumulado en PoW, reglas fork-choice en PoS).\n\nEl consenso garantiza que computadoras independientes alcancen exactamente el mismo historial siguiendo reglas idénticas.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Esquema: Propuesta -> Verificación de reglas -> Un único historial aceptado.",
            diagramExtraNote = "Sin una regla común, cada nodo mantendría un libro diferente.",
            realExample = "Dos mineros encuentran un bloque válido a la vez. Existen temporalmente dos ramas hasta que el siguiente bloque consolida una de ellas.",
            commonMistake = "«Consenso significa votaciones de usuarios en redes sociales.» Falso. La gobernanza social es independiente de las reglas de consenso del protocolo."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — El mecanismo real",
            content = "En el PoW de Bitcoin, el minero prueba valores en la cabecera (nonce) y calcula hashes hasta obtener un valor inferior al objetivo de dificultad (Difficulty Target).\n\nLa dificultad se ajusta automáticamente para mantener el tiempo medio de bloque en ~10 minutos, independientemente de la potencia de cálculo (hashrate) conectada.\n\nLa energía es el coste tangible que hace inviable reescribir el historial pasado.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Esquema: Probar valores nonce hasta cumplir el objetivo de dificultad.",
            diagramExtraNote = null,
            realExample = "Si la potencia de minería se duplica, el ajuste de dificultad recalibra el tiempo medio para devolverlo a 10 minutos.",
            commonMistake = "«La minería resuelve fórmulas científicas útiles.» En Bitcoin el cálculo es deliberadamente una prueba de hash criptográfica."
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — El mecanismo real",
            content = "En PoS, la propuesta y validación de bloques corresponde a validadores que han depositado capital (stake) en el protocolo.\n\nEn lugar de electricidad, la barrera es económica: el slashing (confiscación de fondos depositados) sanciona a los validadores que firmen bloques contradictorios.\n\nEthereum opera bajo PoS mediante slots, épocas, proponentes y atestaciones.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Esquema: Depósito de capital -> Selección de proponente -> Atestación / Slashing.",
            diagramExtraNote = null,
            realExample = "Un validador en Ethereum que proponga dos bloques distintos para el mismo slot recibe una sanción automática de slashing en su depósito.",
            commonMistake = "«PoS es exactamente igual que un banco tradicional.» Técnicamente inexacto: es un algoritmo de consenso distribuido con defensas criptoeconómicas propias."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool y tarifas de transacción",
            content = "Mempool = Cola en memoria de un nodo donde esperan las transacciones recibidas que aún no se han incluido en un bloque. Cada nodo tiene su propia mempool local.\n\nLa comisión paga el espacio escaso dentro del bloque (bytes en Bitcoin, gas en Ethereum). Ante picos de demanda, las tarifas aumentan.\n\nUna comisión mayor no hace más legítima la transacción, solo incentiva a los productores de bloques a incluirla antes.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Esquema: Espera antes del bloque con prioridad por comisión.",
            diagramExtraNote = null,
            realExample = "Durante momentos de saturación en Ethereum, el coste del gas (Gwei) se eleva incluso para transferencias sencillas.",
            commonMistake = "«La red cobra comisiones como una corporación bancaria.» No existe ninguna empresa dueña de Bitcoin; las comisiones van a los productores de bloques."
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Wallets y frases semilla (Seed)",
            content = "Una wallet es software o hardware que gestiona claves criptográficas y genera transacciones firmadas. No guarda monedas dentro de la aplicación.\n\nLa frase semilla (BIP-39) es la representación legible del secreto maestro del que se derivan todas las claves privadas (wallets HD, BIP-32/44).\n\nCustodial: Un tercero custodia tus claves. Non-custodial: Las claves residen exclusivamente en tu propio dispositivo.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Esquema: Seed -> Claves -> Direcciones -> Firma local.",
            diagramExtraNote = "Quien tiene la frase semilla controla los fondos. La app no es un banco.",
            realExample = "Una hardware wallet firma en el propio chip seguro sin exponer la clave privada al ordenador conectado.",
            commonMistake = "«Hice una captura de pantalla de mi seed phrase y la subí a la nube.» Es la causa más frecuente de robo de fondos. La semilla es la llave maestra."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Moneda nativa vs Token",
            content = "Moneda nativa (Native Coin): La unidad base del propio protocolo (BTC en Bitcoin, ETH en Ethereum). Se usa para pagar tarifas y asegurar el consenso.\n\nToken: Unidad contable definida mediante un contrato inteligente (ej. ERC-20) alojado en una blockchain existente. Su seguridad depende de la red anfitriona y del código del contrato.\n\nUn token no es una blockchain independiente, sino una aplicación sobre ella.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Esquema: Moneda de protocolo vs Token de contrato inteligente.",
            diagramExtraNote = null,
            realExample = "USDT en Ethereum es un token ERC-20. El ETH con el que pagas el gas de la transacción es la moneda nativa.",
            commonMistake = "«Cada criptomoneda tiene su propia blockchain.» Falso para los miles de tokens que existen dentro de contratos inteligentes."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Contratos inteligentes (Smart Contracts)",
            content = "Smart contract = Programa almacenado en la blockchain cuyo estado se modifica cuando recibe una transacción válida. Los validadores ejecutan el código de forma determinista en una máquina virtual (ej. EVM).\n\n«Automático» no significa libre de riesgos: bugs de programación, claves de administración vulneradas y ejecución irrevocable son riesgos reales.\n\nBitcoin utiliza un lenguaje Script restringido sin máquina virtual general para maximizar la seguridad del dinero base.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Esquema: Código en cadena -> Llamada + Gas -> Nuevo estado.",
            diagramExtraNote = null,
            realExample = "Uniswap es un conjunto de contratos inteligentes que permite intercambios automáticos de tokens sin intermediarios humanos.",
            commonMistake = "«Un smart contract es un contrato legal que te defiende en un tribunal.» Técnicamente es código informático ejecutable. La validez legal pertenece a otro ámbito."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Límites físicos y económicos reales",
            content = "Rendimiento: Una capa 1 (Layer-1) pública no puede procesar transacciones infinitas por segundo sin comprometer la descentralización o encarecer tarifas. Por ello surgen las Layer-2 y diseños modulares.\n\nEnergía / Capital: PoW consume energía física; PoS inmoviliza capital económico. El consenso requiere un coste real de ataque.\n\nPrivacidad: La mayoría de blockchains públicas son seudónimas, no anónimas: los flujos de transacciones son públicos y auditables.\n\nForks: Las actualizaciones pueden dividir a la comunidad y al historial.\n\nEstos límites definen con precisión dónde la tecnología es óptima y dónde no.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Esquema: Los límites son parte del diseño del sistema.",
            diagramExtraNote = null,
            realExample = "Un rollup agrupa miles de transacciones fuera de L1 y envía pruebas comprimidas a L1 para aumentar la capacidad.",
            commonMistake = "«Blockchain soluciona todo a la vez: velocidad infinita, anonimato total y pagos gratis.» No existen soluciones mágicas sin compromisos en sistemas distribuidos."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Ingeniería de Liquidaciones y Apalancamiento",
            content = "En los futuros perpetuos, el apalancamiento optimiza el capital disponible. Si el precio alcanza el margen de mantenimiento, el motor liquida la posición de forma automática.\n\nLas liquidaciones en cadena provocan 'squeezes' violentos que devoran la profundidad del libro de órdenes.\n\nEl control riguroso del margen es la regla fundamental de supervivencia financiera.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Esquema: Agotamiento de margen -> Umbral de liquidación -> Cascada de órdenes de mercado.",
            diagramExtraNote = null,
            realExample = "Una caída del 5% liquida instantáneamente posiciones apalancadas a 20x.",
            commonMistake = "«Mayor apalancamiento equivale simplemente a más ganancias.» Aumenta exponencialmente el riesgo de pérdida total."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Dinámica de Tasas de Financiación",
            content = "Los contratos perpetuos no tienen vencimiento. La tasa de financiación (Funding Rate) vincula el precio del contrato al precio spot.\n\nUna tasa positiva indica que las posiciones largas pagan a las cortas (mercado sobrecalentado). Una tasa negativa refleja pesimismo extremo.\n\nPicos extremos de financiación actúan como señales clave de cambio de tendencia.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Esquema: Precio Perpetuo > Spot -> Financiación positiva (Largos pagan a Cortos).",
            diagramExtraNote = null,
            realExample = "En fases eufóricas, la financiación de +0,08% cada 8 horas hace insostenible mantener posiciones alcistas.",
            commonMistake = "«La financiación es una comisión que cobra el exchange.» Es una liquidación entre operadores.",
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Flujo de Órdenes y Grupos de Liquidez",
            content = "El libro de órdenes reúne órdenes límite de compra y venta. La profundidad muestra el volumen requerido para desplazar el precio.\n\nInversores institucionales recurren a órdenes Iceberg y algoritmos TWAP/VWAP para evitar el deslizamiento de precios (slippage).\n\nLos grupos de liquidez señalan acumulaciones de stops y barreras de absorción.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Esquema: Libro de órdenes -> Grupos de liquidez -> Deslizamiento de precio.",
            diagramExtraNote = null,
            realExample = "Vender a mercado sin profundidad suficiente genera un deslizamiento de precio severo.",
            commonMistake = "«Todas las órdenes visibles se ejecutarán.» Muchas órdenes son canceladas mediante spoofing."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Ciclos Macro de Halving y Dinámica de Oferta",
            content = "El Halving de Bitcoin se produce cada 210.000 bloques (~4 años), reduciendo la emisión minera al 50% (de 50 BTC en 2009 a 3,125 BTC en 2024).\n\nEste choque de oferta genera ciclos de 4 años: Acumulación, Expansión alcista, Distribución y Mercado bajista.\n\nComprender el ciclo macro orienta la asignación patrimonial estratégica.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Esquema: Emisión reducida al 50% -> Choque de oferta -> Ciclo de 4 años.",
            diagramExtraNote = null,
            realExample = "En ciclos anteriores, el máximo del mercado se registró de 12 a 18 meses tras el halving.",
            commonMistake = "«El precio se dispara en el mismo bloque del halving.» El choque de oferta requiere meses de absorción."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Gestión de Riesgos Institucionales",
            content = "La rentabilidad sostenible depende del control del drawdown y del tamaño de posición, no de pronósticos infallibles.\n\nLos gestores limitan el riesgo por operación al 1-2% del capital y calibran el tamaño según la distancia al stop-loss.\n\nLa disciplina emocional y la preservación del capital garantizan la longevidad.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Esquema: Capital total -> Riesgo fijo 1-2% -> Tamaño de posición estructurado.",
            diagramExtraNote = null,
            realExample = "Arriesgando solo el 1% por operación, el inversor conserva más del 81% de su cartera tras 20 pérdidas seguidas.",
            commonMistake = "«Ir 'all-in' en un activo es el atajo a la riqueza.» Es la certeza matemática de ruina financiera."
        )
    )

    private val italianChapters = listOf(
        BlockchainChapter(
            id = 1,
            title = "1. Scarsità digitale & Double Spending",
            content = "Un file digitale può essere duplicato senza distruggere l'originale. Per questo un «file-moneta» su uno smartphone non può essere denaro di per sé: la stessa persona potrebbe inviarlo due volte.\n\nIl problema classico si chiama doppia spesa (double spending): spendere due volte la stessa unità di valore. Le banche lo risolvono con un registro centrale. La blockchain lo risolve tramite una cronologia pubblica verificata da più nodi indipendenti.\n\nLa blockchain non crea scarsità per magia. Definisce regole precise per stabilire quale transazione è valida e quale viene respinta.",
            diagramType = BlockchainDiagramType.DOUBLE_SPENDING,
            diagramCaption = "Schema: Lo stesso oggetto digitale può esistere in due copie.",
            diagramExtraNote = "I file digitali si copiano facilmente. Il denaro in banca non è un file sul tuo disco.",
            realExample = "Se invii la stessa unità a due destinatari prima della conferma di un blocco, solo una versione rimarrà nella cronologia accettata.",
            commonMistake = "«Bitcoin è un file salvato sul mio smartphone.» Falso. Sul telefono risiedono chiavi crittografiche. Il saldo è un insieme di scritture nel registro condiviso."
        ),
        BlockchainChapter(
            id = 2,
            title = "2. Database centralizzato vs Registro distribuito",
            content = "Database centralizzato: Un unico ente gestisce il registro ufficiale (banca, gestore di pagamenti). È efficiente, ma richiede fiducia incondizionata e rappresenta un singolo punto di vulnerabilità.\n\nRegistro distribuito (Distributed Ledger): Molteplici computer indipendenti conservano una copia degli stessi dati e verificano le nuove transazioni secondo regole comuni.\n\n«Decentralizzato» non significa assenza di regole, ma assenza di un intermediario obbligatorio per la validità di un pagamento.",
            diagramType = BlockchainDiagramType.CENTRALIZED_VS_DISTRIBUTED,
            diagramCaption = "Schema: Un server centrale rispetto a una rete di nodi con lo stesso registro.",
            diagramExtraNote = null,
            realExample = "Visa aggiorna il proprio database interno. In Bitcoin, migliaia di nodi completi verificano esattamente le stesse regole.",
            commonMistake = "«Blockchain significa che nessuno ha potere.» Falso. Esistono miner/validatori, sviluppatori ed exchange. Ma il protocollo non necessita di una singola azienda per confermare una transazione valida."
        ),
        BlockchainChapter(
            id = 3,
            title = "3. Hash crittografico",
            content = "Una funzione hash riceve dati di qualsiasi dimensione e restituisce una stringa di lunghezza fissa (digest / impronta).\n\nProprietà chiave: calcolo rapido in avanti, impossibilità pratica di trovare due messaggi con lo stesso hash (resistenza alle collisioni) e un minimo cambiamento nei dati modifica l'intero risultato (effetto valanga).\n\nBitcoin utilizza SHA-256 ed Ethereum Keccak-256. Un hash non è un file cifrato da sbloccare con una chiave: non esiste chiave in grado di risalire al testo originale dall'hash.",
            diagramType = BlockchainDiagramType.CRYPTOGRAPHIC_HASH,
            diagramCaption = "Schema: Qualsiasi input -> impronta digitale fissa univoca.",
            diagramExtraNote = "Minima modifica ai dati -> hash completamente diverso. Non invertibile.",
            realExample = "Modifica una singola cifra in una transazione e l'hash del blocco cambierà del tutto, rendendo evidente qualsiasi manomissione.",
            commonMistake = "«L'hash nasconde i dati e solo il proprietario può leggerli.» Falso. Gli hash accompagnano spesso dati pubblici. È un'impronta digitale, non una cassaforte."
        ),
        BlockchainChapter(
            id = 4,
            title = "4. Chiavi & Firme digitali",
            content = "Crittografia asimmetrica: chiave privata e chiave pubblica. La chiave privata rimane rigorosamente segreta. Da essa si ricava la chiave pubblica e successivamente l'indirizzo.\n\nFirma digitale: Con la chiave privata firmi una transazione. Chiunque disponga della chiave pubblica può verificare l'autenticità senza mai conoscere la chiave privata.\n\nSu Bitcoin ed Ethereum il possesso dei fondi coincide con il controllo della chiave privata, non con un account presso l'azienda dell'app.",
            diagramType = BlockchainDiagramType.KEYS_AND_SIGNATURES,
            diagramCaption = "Schema: La chiave privata firma. La chiave pubblica verifica.",
            diagramExtraNote = "Chiave privata -> RIMANE SEGRETA",
            realExample = "Firmi «invia 0,01 BTC all'indirizzo X». I nodi verificano la firma e accettano la transazione se i fondi sono disponibili.",
            commonMistake = "«L'indirizzo è come una password.» Falso. L'indirizzo è pubblico. Il segreto assoluto è la chiave privata o seed phrase."
        ),
        BlockchainChapter(
            id = 5,
            title = "5. Anatomia di una transazione",
            content = "Una transazione è un insieme di dati che indica alla rete come aggiornare il registro: quali input spende, quali output crea e la firma di autorizzazione.\n\nNon viene trasferito alcun «file di moneta». Viene trasferito il diritto sul saldo in conformità con le regole del protocollo.\n\nOgni transazione valida riceve un identificatore univoco (txid). Fino all'inclusione in un blocco, rimane in attesa nella mempool.",
            diagramType = BlockchainDiagramType.TRANSACTION_FIELDS,
            diagramCaption = "Schema: Campi fondamentali di una transazione.",
            diagramExtraNote = "Una transazione è un messaggio di rete, non un file di moneta sullo smartphone.",
            realExample = "Pagare un caffè: il wallet genera la transazione, la firma e la invia ai nodi per l'inclusione in un blocco.",
            commonMistake = "«Appena premo invia, è irreversibile in 0 secondi.» Falso. Esiste propagazione, mempool e successive conferme dei blocchi."
        ),
        BlockchainChapter(
            id = 6,
            title = "6. Modello UTXO vs Modello ad account",
            content = "Bitcoin (UTXO): Il saldo non è un numero unico su un conto, ma un insieme di output di transazione non spesi (UTXO). Una nuova transazione consuma specifici UTXO e ne crea di nuovi.\n\nEthereum (Modello ad account): Ogni indirizzo possiede uno stato diretto con saldo e codice. Una transazione modifica direttamente tale stato.\n\nEntrambi i modelli prevengono la doppia spesa con logiche contabili distinte.",
            diagramType = BlockchainDiagramType.UTXO_VS_ACCOUNT,
            diagramCaption = "Schema: Due modelli contabili differenti per risolvere lo stesso problema.",
            diagramExtraNote = null,
            realExample = "In Bitcoin puoi possedere 3 frammenti UTXO per un totale di 0,12 BTC. In Ethereum visualizzi un saldo complessivo unificato.",
            commonMistake = "«Tutte le blockchain funzionano come un conto bancario.» Falso. Bitcoin evita volutamente il modello a conti."
        ),
        BlockchainChapter(
            id = 7,
            title = "7. Cosa contiene un blocco",
            content = "Blocco = Dati delle transazioni + Intestazione (Header). L'intestazione contiene: riferimento al blocco precedente (hash precedente), riepilogo transazioni (Merkle root), timestamp e dati di consenso (nonce in PoW).\n\nAlbero di Merkle: Gli hash delle transazioni vengono combinati a coppie fino a una radice unica, provando l'inclusione di una transazione senza dover trasmettere l'intero blocco.\n\nLa dimensione del blocco e il gas limit determinano quante transazioni possono essere incluse, regolando commissioni e attese.",
            diagramType = BlockchainDiagramType.BLOCK_ANATOMY,
            diagramCaption = "Schema: Intestazione del blocco e corpo delle transazioni.",
            diagramExtraNote = null,
            realExample = "Un blocco Bitcoin viene prodotto mediamente ogni ~10 minuti. Uno slot Ethereum è molto più rapido.",
            commonMistake = "«Il blocco è la moneta.» Falso. Il blocco è una pagina del libro mastro storico."
        ),
        BlockchainChapter(
            id = 8,
            title = "8. Come si collegano i blocchi (Previous Hash)",
            content = "Ogni nuovo blocco include nella propria intestazione l'hash del blocco precedente, formando una catena continua.\n\nModificare una transazione in un blocco passato ne altera l'hash, spezzando il collegamento con tutti i blocchi successivi. Per convalidare la modifica occorrerebbe ricalcolare un'intera catena alternativa accettata dalla rete, operazione economicamente insostenibile su grandi reti.\n\nL'immutabilità è una garanzia economica e computazionale.",
            diagramType = BlockchainDiagramType.BLOCK_CHAINING,
            diagramCaption = "Schema: Ogni anello punta a quello precedente.",
            diagramExtraNote = "Ogni intestazione contiene l'hash del blocco precedente. Modificare il passato spezza la catena.",
            realExample = "6 conferme su Bitcoin significano che sono stati costruiti 5 blocchi sopra il tuo, rendendo una riorganizzazione estremamente costosa.",
            commonMistake = "«Nulla cambia mai in una blockchain.» Falso. Possono verificarsi riorganizzazioni minori e fork. Riscrivere il passato è estremamente difficile, non sovrannaturale."
        ),
        BlockchainChapter(
            id = 9,
            title = "9. Nodi & Rete Peer-to-Peer",
            content = "Nodo (Node) = Software che esegue il protocollo. Un nodo completo (Full Node) scarica e verifica tutte le regole e la cronologia. Un client leggero si affida a terze parti.\n\nLa rete è Peer-to-Peer: i nodi si scambiano transazioni e blocchi direttamente senza un server centrale obbligatorio.\n\nUn wallet su smartphone di norma NON è un nodo completo, ma interroga servizi RPC esterni.",
            diagramType = BlockchainDiagramType.P2P_NODES,
            diagramCaption = "Schema: I nodi comunicano tra pari senza un server centrale.",
            diagramExtraNote = null,
            realExample = "Un nodo completo Bitcoin rifiuta immediatamente un blocco con emissione illegale di monete anche se prodotto dalla mining pool più grande del mondo.",
            commonMistake = "«Ho scaricato un'app wallet, quindi sono un nodo della rete.» Generalmente no. Gestisci chiavi che comunicano con nodi altrui."
        ),
        BlockchainChapter(
            id = 10,
            title = "10. Perché è necessario il Consenso",
            content = "Molteplici nodi, latenze di rete e transazioni concorrenti: senza una regola chiara di ordinamento, il registro si dividerebbe in versioni contrastanti.\n\nConsenso = Regole condivise per stabilire la validità di un blocco e la catena legittima in caso di rami concorrenti (catena con maggior lavoro in PoW, regole fork-choice in PoS).\n\nIl consenso assicura che computer indipendenti adottino esattamente la stessa cronologia applicando identiche regole.",
            diagramType = BlockchainDiagramType.CONSENSUS_FLOW,
            diagramCaption = "Schema: Proposta blocco -> Verifica regole -> Unica cronologia accettata.",
            diagramExtraNote = "Senza una regola comune, ogni nodo conserverebbe un libro mastro diverso.",
            realExample = "Due miner trovano un blocco valido quasi simultaneamente. Esistono temporaneamente due rami fino a quando il blocco successivo consolida la catena.",
            commonMistake = "«Consenso significa votazione degli utenti sui social network.» Falso. La governance sociale è separata dalle regole algoritmiche del protocollo."
        ),
        BlockchainChapter(
            id = 11,
            title = "11. Proof of Work — Il meccanismo reale",
            content = "Nel PoW di Bitcoin, il miner varia i campi dell'intestazione (nonce) e calcola hash finché il valore ottenuto non scende sotto l'obiettivo di difficoltà (Difficulty Target).\n\nLa difficoltà si adegua automaticamente per mantenere il tempo medio di blocco attorno a ~10 minuti, indipendentemente dalla potenza di calcolo (hashrate) totale.\n\nL'energia impiegata è il costo tangibile che rende economicamente proibitiva la riscrittura della cronologia passata.",
            diagramType = BlockchainDiagramType.PROOF_OF_WORK,
            diagramCaption = "Schema: Testare valori nonce finché l'hash rispetta il target di difficoltà.",
            diagramExtraNote = null,
            realExample = "Se la potenza di calcolo della rete raddoppia, l'adeguamento automatico della difficoltà riporterà il tempo medio a 10 minuti.",
            commonMistake = "«Il mining risolve complessi calcoli scientifici utili.» In Bitcoin l'enigma è una verifica crittografica di hash mirata alla sicurezza.",
        ),
        BlockchainChapter(
            id = 12,
            title = "12. Proof of Stake — Il meccanismo reale",
            content = "Nel PoS, la proposta e la validazione dei blocchi sono affidate a validatori che hanno vincolato capitale (stake) nel protocollo.\n\nAl posto dell'elettricità, la barriera protettiva è economica: lo slashing (confisca del capitale vincolato) punisce i validatori che firmano blocchi contrastanti.\n\nEthereum opera in PoS con slot, epoche, proponenti e attestazioni dal 2022.",
            diagramType = BlockchainDiagramType.PROOF_OF_STAKE,
            diagramCaption = "Schema: Capitale vincolato -> Selezione proponente -> Attestazione / Slashing.",
            diagramExtraNote = null,
            realExample = "Un validatore Ethereum che propone due blocchi differenti per lo stesso slot subisce una sanzione automatica di slashing sul proprio stake.",
            commonMistake = "«Il PoS funziona esattamente come una banca.» Tecnicamente inesatto: è un algoritmo di consenso distribuito con precise difese crittografiche ed economiche."
        ),
        BlockchainChapter(
            id = 13,
            title = "13. Mempool & Commissioni di transazione",
            content = "Mempool = Coda nella memoria RAM di un nodo in cui attendono le transazioni ricevute non ancora incluse in un blocco. Ogni nodo gestisce la propria mempool locale.\n\nLa commissione remunera lo spazio limitato all'interno del blocco (byte in Bitcoin, gas in Ethereum). Quando la domanda aumenta, le tariffe salgono.\n\nUna commissione più elevata non rende la transazione più autentica, ma incentiva i produttori di blocchi a includerla prioritariamente.",
            diagramType = BlockchainDiagramType.MEMPOOL_FEES,
            diagramCaption = "Schema: Attesa prima del blocco con priorità per commissione.",
            diagramExtraNote = null,
            realExample = "Nei momenti di congestione su Ethereum, il costo del gas (Gwei) aumenta anche per semplici trasferimenti di token.",
            commonMistake = "«La rete preleva commissioni a scopo di lucro aziendale.» Non esiste alcuna società proprietaria di Bitcoin; le commissioni vanno ai produttori di blocchi."
        ),
        BlockchainChapter(
            id = 14,
            title = "14. Wallet & Seed Phrase",
            content = "Un wallet è un software o dispositivo hardware che gestisce chiavi crittografiche e compone transazioni firmate. Non custodisce monete fisiche all'interno dell'app.\n\nLa seed phrase (BIP-39) è la rappresentazione leggibile del segreto master da cui vengono generate tutte le chiavi private (wallet deterministici HD, BIP-32/44).\n\nCustodial: Una terza parte detiene le chiavi. Non-custodial: Le chiavi risiedono unicamente sul tuo dispositivo.",
            diagramType = BlockchainDiagramType.WALLET_SEED_HIERARCHY,
            diagramCaption = "Schema: Seed -> Chiavi -> Indirizzi -> Firma in locale.",
            diagramExtraNote = "Chi possiede la seed phrase controlla i fondi. L'app non è una banca.",
            realExample = "Un hardware wallet firma la transazione all'interno del chip sicuro senza mai esportare la chiave privata sul computer connesso.",
            commonMistake = "«Ho fatto uno screenshot della mia seed phrase salvandolo sul cloud.» È la principale causa di furto fondi. La seed phrase è la chiave della cassaforte."
        ),
        BlockchainChapter(
            id = 15,
            title = "15. Coin nativa vs Token",
            content = "Coin nativa (Native Asset): L'unità di base del protocollo stesso (BTC su Bitcoin, ETH su Ethereum). Utilizzata per pagare le commissioni e garantire la sicurezza del consenso.\n\nToken: Unità contabile definita tramite uno smart contract (es. ERC-20) ospitato su una blockchain esistente. La sua sicurezza dipende dalla rete ospitante e dal codice del contratto.\n\nUn token non costituisce una blockchain a sé stante, ma un'applicazione sopra di essa.",
            diagramType = BlockchainDiagramType.COIN_VS_TOKEN,
            diagramCaption = "Schema: Coin di protocollo vs Token di smart contract.",
            diagramExtraNote = null,
            realExample = "USDT su Ethereum è un token ERC-20. L'ETH con cui si paga il gas della transazione è la moneta nativa.",
            commonMistake = "«Ogni criptovaluta ha la propria blockchain.» Falso per le migliaia di token gestiti tramite smart contract."
        ),
        BlockchainChapter(
            id = 16,
            title = "16. Smart Contract",
            content = "Smart contract = Programma salvato direttamente sulla blockchain il cui stato si aggiorna all'invio di una transazione valida. Il codice viene eseguito in modo deterministico dai validatori in una macchina virtuale (es. EVM).\n\n«Automatizzato» non significa privo di pericoli: bug nel codice, chiavi amministrative compromesse ed esecuzione irrevocabile sono rischi concreti.\n\nBitcoin adotta un linguaggio Script limitato senza EVM generale per massimizzare la sicurezza monetaria di base.",
            diagramType = BlockchainDiagramType.SMART_CONTRACT_STATE,
            diagramCaption = "Schema: Codice on-chain -> Chiamata + Gas -> Nuovo stato.",
            diagramExtraNote = null,
            realExample = "Uniswap è un insieme di smart contract che consente scambi automatici tra token senza intermediari umani.",
            commonMistake = "«Uno smart contract è un contratto legale vincolante in tribunale.» Tecnicamente è codice software eseguibile. La tutela legale appartiene a un livello separato."
        ),
        BlockchainChapter(
            id = 17,
            title = "17. Limiti fisici ed economici reali",
            content = "Scalabilità: Una Layer-1 pubblica non può elaborare transazioni infinite al secondo senza compromettere la decentralizzazione o aumentare le commissioni. Da qui la nascita delle Layer-2 e delle architetture modulari.\n\nEnergia / Capitale: PoW consuma energia fisica, PoS vincola capitale economico. Il consenso necessita di un costo d'attacco tangibile.\n\nPrivacy: La maggior parte delle blockchain pubbliche è pseudonima e non anonima: i flussi di transazioni sono pubblici e verificabili.\n\nFork: Gli aggiornamenti possono dividere la community e la cronologia.\n\nQuesti limiti non sminuiscono la tecnologia, ma ne delimitano l'ambito di applicazione ideale.",
            diagramType = BlockchainDiagramType.SYSTEM_LIMITS,
            diagramCaption = "Schema: I limiti sono parte integrante del sistema.",
            diagramExtraNote = null,
            realExample = "Un rollup raggruppa migliaia di transazioni fuori da L1 e trasmette prove compresse a L1 per ampliare la capacità.",
            commonMistake = "«La blockchain risolve contemporaneamente velocità infinita, anonimato totale e gratuità.» Non esistono pasti gratis nei sistemi distribuiti."
        ),
        BlockchainChapter(
            id = 18,
            title = "18. Ingegneria delle Liquidazioni e Leva",
            content = "Nei contratti derivati perp, la leva finanziaria ottimizza l'efficienza del capitale. Quando la perdita latente esaurisce il margine di mantenimento, il motore di rischio chiude forzatamente la posizione.\n\nLe liquidazioni a catena scatenano violenti 'squeeze' che prosciugano la liquidità dell'order book.\n\nIl controllo inflessibile della leva è il pilastro primario per conservare il patrimonio.",
            diagramType = BlockchainDiagramType.LIQUIDATION_ENGINEERING,
            diagramCaption = "Schema: Esaurimento margine -> Trigger liquidazione -> Cascata di ordini a mercato.",
            diagramExtraNote = null,
            realExample = "Una repentina flessione del 5% azzera all'istante le posizioni con leva 20x.",
            commonMistake = "«Una leva elevata aumenta solo i profitti.» In realtà accresce in modo esponenziale il rischio di azzeramento conto."
        ),
        BlockchainChapter(
            id = 19,
            title = "19. Dinamica dei Tassi di Finanziamento",
            content = "I contratti perpetual non hanno scadenza prefissata. Il meccanismo periodico del Funding Rate riallinea il prezzo del contratto con l'indice spot di mercato.\n\nTassi positivi implicano che le posizioni Long pagano le Short (fase euforica). Tassi negativi indicano pessimismo con potenziale di rimbalzo.\n\nPicchi anomali di funding segnalano frequentemente punti d'inversione macro.",
            diagramType = BlockchainDiagramType.FUNDING_DYNAMICS,
            diagramCaption = "Schema: Prezzo Perpetual > Spot -> Funding positivo (Long pagano Short).",
            diagramExtraNote = null,
            realExample = "Durante l'euforia di mercato, un funding del +0,08% ogni 8 ore rende gravosa la conservazione di posizioni rialziste.",
            commonMistake = "«Il funding è una commissione trattenuta dall'exchange.» Si tratta di un pagamento tra trader."
        ),
        BlockchainChapter(
            id = 20,
            title = "20. Flusso degli Ordini e Cluster di Liquidità",
            content = "L'order book ordina le proposte d'acquisto (Bid) e di vendita (Ask). La profondità quantifica la liquidità necessaria per muovere il prezzo.\n\nI desk istituzionali utilizzano ordini Iceberg e algoritmi TWAP/VWAP per dissimulare il loro volume reale.\n\nL'analisi dei cluster di liquidità rivela barriere di assorbimento e zone di stop.",
            diagramType = BlockchainDiagramType.QUANTUM_ORDER_FLOW,
            diagramCaption = "Schema: Profondità order book -> Cluster di liquidità -> Dinamica dello slippage.",
            diagramExtraNote = null,
            realExample = "Un ordine a mercato massiccio su un book sottile genera uno slippage rovinoso.",
            commonMistake = "«Tutti gli ordini a book saranno eseguiti.» Spesso le proposte visibili sono spoofing ritirato prima del fill."
        ),
        BlockchainChapter(
            id = 21,
            title = "21. Cicli Macro di Halving e Dinamica dell'Offerta",
            content = "L'Halving di Bitcoin si verifica ogni 210.000 blocchi (~4 anni), dimezzando il sussidio ai miner (da 50 BTC nel 2009 a 3,125 BTC nel 2024).\n\nQuesto shock programmato dell'offerta ha storicamente generato cicli quadriennali: Accumulazione, Espansione, Distribuzione e Bear Market.\n\nInquadrare il ciclo offre una guida strategica per la gestione del capitale.",
            diagramType = BlockchainDiagramType.MACRO_HALVING_CYCLES,
            diagramCaption = "Schema: Blocco Halving -> Emissione miner dimezzata del 50% -> Shock dell'offerta.",
            diagramExtraNote = null,
            realExample = "Nei cicli precedenti, il vertice massimo è stato raggiunto tra i 12 e i 18 mesi successivi all'halving.",
            commonMistake = "«Il prezzo esplode nel secondo esatto dell'halving.» La contrazione dell'offerta richiede mesi di assorbimento continuo."
        ),
        BlockchainChapter(
            id = 22,
            title = "22. Gestione del Rischio Istituzionale",
            content = "La profittabilità costante non dipende da previsioni infallibili, ma dal controllo scrupoloso del drawdown e dal dimensionamento prudente delle posizioni.\n\nI professionisti limitano il rischio all'1-2% del capitale per trade, dimensionando l'esposizione sulla distanza dello stop-loss.\n\nLa salvaguardia del capitale e il distacco emotivo rappresentano la vera chiave del successo nel lungo termine.",
            diagramType = BlockchainDiagramType.INSTITUTIONAL_RISK,
            diagramCaption = "Schema: Capitale totale -> Rischio fisso 1-2% -> Dimensionamento rigoroso dello stop.",
            diagramExtraNote = null,
            realExample = "Rischiando solo l'1% per trade, si può superare una sequenza di 20 perdite preservando oltre l'81% del conto.",
            commonMistake = "«Puntare 'all-in' su un token è il metodo più rapido per arricchirsi.» È la via più celere verso la rovina economica."
        )
    )
}
