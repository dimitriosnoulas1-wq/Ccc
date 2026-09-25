# Bitcoin Rainbow (tab Cycle): οδηγίες build για Cursor

Branch: `claude/lucid-knuth-y0tvmx` (PR #26)

## Τι υπάρχει

| Αρχείο | Τι κάνει |
|---|---|
| `app/src/main/java/com/example/ui/rainbow/RainbowData.kt` | Μοντέλο rainbow (τύπος, 9 ζώνες, halvings), ημερομηνίες |
| `.../rainbow/RainbowEvents.kt` | Χρονολογημένα γεγονότα (μόνο ημερομηνία + κείμενο) |
| `.../rainbow/CycleInsights.kt` | Ανάλυση "ίδια μέρα σε κάθε κύκλο" + κανόνας ενημέρωσης ±7% |
| `.../rainbow/RainbowRepository.kt` | blockchain.info / Coinbase + cache σε αρχεία |
| `.../rainbow/RainbowViewModel.kt` | Ιστορικό 1 φορά τη μέρα, live τιμή κάθε 15s |
| `.../rainbow/RainbowChart.kt` | UI (Compose Canvas) |
| `app/src/test/java/com/example/ui/rainbow/RainbowLogicTest.kt` | 21 tests για όλη τη λογική |

Τα 21 tests έχουν ήδη περάσει με standalone Kotlin compiler. Το UI (Compose) δεν έχει
μεταγλωττιστεί, γιατί το περιβάλλον όπου γράφτηκε δεν είχε πρόσβαση στο Google Maven.

## Βήματα

1. `git fetch origin && git checkout claude/lucid-knuth-y0tvmx`
2. Άνοιξε το project στο Cursor και δώσε του το prompt που ακολουθεί.
3. Τρέξε την εφαρμογή, άνοιξε το tab Cycle και έλεγξε τη λίστα ελέγχου στο τέλος.

## Prompt για το Cursor (αντέγραψέ το όπως είναι)

```
This Android app (Kotlin, Jetpack Compose) has a new Bitcoin Rainbow chart in
app/src/main/java/com/example/ui/rainbow/. Read docs/RAINBOW_CURSOR.md first.

1. Build: run `./gradlew :app:assembleDebug`. If there are compile errors, fix ONLY the
   compile errors with the smallest possible change. Do not change behaviour, numbers,
   dates, texts, thresholds or the model constants while fixing them.

2. Tests: run `./gradlew :app:testDebugUnitTest --tests "com.example.ui.rainbow.*"`.
   All tests must pass. Never delete, skip or weaken a test to make it pass; if one
   fails, explain why before changing anything.

3. Verify the rainbow model against the official Blockchaincenter chart
   (https://www.blockchaincenter.net/en/bitcoin-rainbow-chart/ ; current version is "V2",
   Nov 2022). Look at the page's JavaScript and report:
   - the exact regression formula and constants,
   - how the 9 band boundaries are computed,
   - the 9 band names and colors.
   Compare them with the block marked "ΡΥΘΜΙΣΕΙΣ ΜΟΝΤΕΛΟΥ" in RainbowData.kt (A, B, ORIGIN,
   EDGES, BANDS). Show me the differences and WAIT for my OK before editing. If the
   official band model cannot be expressed as fixed offsets (EDGES), explain the exact
   formula and propose the minimal change to baseLog10/edgePrice/bandIndex, and update
   the tests in RainbowLogicTest accordingly.

4. Verify every event in RainbowEvents.kt against reliable sources (date in UTC and
   description). List any event whose date or description is wrong or uncertain,
   with a source. Do not add prices or percentages to event texts: those are computed
   from data on purpose.

5. Run the app and confirm the checklist in docs/RAINBOW_CURSOR.md.
```

## Λίστα ελέγχου στην εφαρμογή

- [ ] Το chart φορτώνει ιστορικό από το 2010 και οι 9 ζώνες φαίνονται.
- [ ] Γραμμές halving: 2012, 2016, 2020, 2024 συνεχείς· 2028, 2032 κόκκινες διακεκομμένες.
- [ ] Η σημερινή κουκκίδα πάλλεται στο τέλος της γραμμής.
- [ ] Κουκκίδες με διαγώνια γραμμή και ετικέτα 2012 / 2016 / 2020 στην ίδια μέρα κάθε κύκλου.
- [ ] 1 δάχτυλο: tooltip με ημερομηνία, τιμή, ζώνη, "Μέρα Χ μετά το halving" και γεγονός αν υπάρχει κοντά.
- [ ] 2 δάχτυλα: zoom και μετακίνηση. Κουμπιά Όλα / Κύκλος / Σήμερα / ✕.
- [ ] Κάρτα "Σήμερα": ζώνη, τιμή, μέρα, "Live τώρα" που αλλάζει κάθε ~15s.
- [ ] Το σημείο στο rainbow ΔΕΝ αλλάζει κάθε 15s. Αλλάζει μία φορά τη μέρα, ή αν η live τιμή απέχει ≥7% ή αλλάξει ζώνη.
- [ ] Κάρτες για κύκλους 2020, 2016, 2012 με τιμή/ζώνη της ίδιας μέρας, 30 μέρες πριν/μετά, απόσταση από το υψηλό, γεγονότα ±45 μέρες.
- [ ] Κλείσε το ίντερνετ και άνοιξε ξανά την εφαρμογή: το chart πρέπει να εμφανίζεται από το cache με μήνυμα "Χωρίς σύνδεση".

## Γραφικά (δεύτερο PR, πάνω στο #29)

Αρχεία: `RainbowChart.kt` (UI), `RainbowVisuals.kt` (υπολογισμοί), `RainbowVisualsTest.kt` (5 tests).

- [ ] Μετά από σήμερα οι ζώνες είναι αχνές, με ετικέτα «ΜΕΛΛΟΝ · μόνο ο τύπος».
- [ ] Mini-map κάτω από το chart: όλο το ιστορικό με χρώματα ζωνών και πλαίσιο για το ορατό κομμάτι. Άγγιγμα ή σύρσιμο μετακινεί την προβολή.
- [ ] Τα κουμπιά Όλα / Κύκλος / Σήμερα αλλάζουν την προβολή ομαλά (animation).
- [ ] Διπλό πάτημα στο chart: επιστροφή στην αρχική προβολή.
- [ ] Κουμπί Ghost: οι κύκλοι 2012/2016/2020 σχεδιάζονται πάνω στον τρέχοντα, μόνο ως τη σημερινή μέρα-μέτρησης, με εξήγηση από κάτω.
- [ ] Όταν σέρνεις το tooltip και αλλάζει ζώνη, το κινητό κάνει μικρή δόνηση.
- [ ] Κάρτα «Σήμερα»: μπάρα με τις 9 ζώνες και κουκκίδα στη θέση της τιμής.
- [ ] Τα κουμπιά χωράνε (αν όχι, η σειρά κουμπιών κάνει οριζόντιο scroll).

## Ρυθμίσεις που αλλάζουν εύκολα

- Όριο μεγάλης κίνησης: `LiveGate.BIG_MOVE` (0.07 = 7%) στο `CycleInsights.kt`
- Παράθυρο "κοντινών" γεγονότων: `CycleInsights.NEAR_DAYS` (45)
- Παράθυρο μεταβολής πριν/μετά: `CycleInsights.WINDOW_DAYS` (30)
- Σταθερές μοντέλου rainbow: μόνο στο block "ΡΥΘΜΙΣΕΙΣ ΜΟΝΤΕΛΟΥ" στο `RainbowData.kt`
