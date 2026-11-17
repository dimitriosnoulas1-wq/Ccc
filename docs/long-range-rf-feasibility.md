# Long-range, infrastructure-free phone-to-phone RF text — Feasibility Report
Galaxy S26 Ultra ⇄ Galaxy S24 Ultra · Netherlands/EU · prepared 2026-11-17

## 0. Research disclosure (read first)

- **Live web access was used, but only minimally.** I ran four standard web searches and read their result summaries. I did **not** open Samsung, Qualcomm, 3GPP, ETSI, AOSP-source or regulator pages in full. I did not physically test either phone.
- Every claim is tagged with a source class:
  - **[W]** appeared in this session's search results (summary level; secondary sources are noted as such).
  - **[K]** from my own background knowledge of standards/documentation, **not re-verified here**. Treat as a pointer, check before relying on it.
  - **[I]** my engineering inference/calculation (arithmetic shown).
- Where a number is not sourced, I say so. No chipset, band or range figure below is presented as measured.
- Classification labels, as requested: **CONFIRMED / STRONGLY SUPPORTED / UNCONFIRMED / IMPOSSIBLE UNDER CONSTRAINTS**. With this research depth, I label something CONFIRMED only when an authoritative document was seen in search results; otherwise at most STRONGLY SUPPORTED.

---

## 1. Final verdict (answers to §7 of the brief)

| # | Question | Answer | Class |
|---|---|---|---|
| 1 | Can these exact two phones communicate over long-range RF with no infrastructure? | **No.** No radio in them that is both (a) usable without infrastructure and (b) long-range is reachable by software. | IMPOSSIBLE UNDER CONSTRAINTS |
| 2 | Android app alone? | **No.** Public APIs expose only Wi-Fi/BT/NFC/UWB-ranging/telephony(+satellite via carrier). No raw RF or sidelink API. | STRONGLY SUPPORTED |
| 3 | Can existing modem hardware be repurposed? | Physically the modem *could* do sidelink if its firmware implements it; in practice the capability is not enabled/exposed on these consumer products as far as evidence shows. Repurposing needs signed firmware. | UNCONFIRMED (that it exists); IMPOSSIBLE (for an app/user to enable) |
| 4 | Would root help? | **No** for transmit control. Root reaches the application processor side; the modem runs its own signed firmware. Root helps with diagnostics/band-locking at most. | STRONGLY SUPPORTED |
| 5 | Is manufacturer firmware access necessary? | **Yes** — Samsung and Qualcomm (and, for legal operation, a spectrum authorisation) would all be needed. | STRONGLY SUPPORTED |
| 6 | Undocumented but technically credible possibility? | Only one: a Samsung/Qualcomm-enabled **3GPP sidelink (PC5)** mode. Nothing seen suggests it ships on these phones. No credible app-level or root-level hidden path found. | UNCONFIRMED |
| 7 | Strongest technically valid architecture | None satisfies all requirements. The closest *technology* is 3GPP sidelink (Rel-17/18 NR sidelink or LTE PC5), which needs OEM firmware, a regulator-approved band, and (per spec design) pre-configuration/authorisation. Satellite NTN is *not* a candidate: it is infrastructure. | UNCONFIRMED / see §9 |
| 8 | Maximum realistic range | There is no working link, so no demonstrated range. Physics-limited ceilings are in §7 (illustrative, not measured). | — |
| 9 | What prevents it? | (a) No app/root access to modem Tx; (b) no sidelink API or enabled sidelink firmware; (c) licensed spectrum — peer UEs may not legally transmit on MNO bands; (d) the only unlicensed radios in the phone (Wi-Fi/BT/UWB/NFC) are excluded or short-range; (e) no 433/868 MHz radio exists in the phone. | CONFIRMED for (c)–(e) in principle; STRONGLY SUPPORTED overall |

**Plain statement:** under your requirement set, the project is not buildable on these two phones. Not "hard" — blocked by the combination of closed modem firmware, licensed spectrum, and the absence of a long-range unlicensed radio you are allowed to use.

---

## 2. Device verification

| Item | S24 Ultra | S26 Ultra |
|---|---|---|
| SoC | Snapdragon 8 Gen 3 for Galaxy; Qualcomm device-finder page exists for the model [W: qualcomm.com/snapdragon/device-finder/samsung-galaxy-s24-ultra]. | Snapdragon 8 Elite Gen 5 for Galaxy (SM8850) reported [W: GSMArena, Qualcomm device finder, technave]. **Regional split conflicts**: one source says Snapdragon globally; Columbia IT's spec sheet lists Exynos 2600 for rest-of-world [W]. **A Dutch unit may therefore be Exynos-based. UNCONFIRMED — check the exact SKU (SM-S948B vs …U) and FCC/teardown.** |
| Modem | Snapdragon X75 expected for the Snapdragon S24 Ultra [W: Qualcomm X75 product brief 87-27161-1; secondary reports]. Not confirmed by a Samsung document in my results. | **Not verified.** Android Authority's FCC article refers to X85-only "Smart Transmit Plus" [W, secondary]; I found no source naming the S26 Ultra modem, and an Exynos variant would use a Samsung modem. Do **not** assume X80 or X85. |
| UWB | Present [K; spec listings] | Present [W: spec listing] |
| Satellite | Samsung said emergency satellite was **not** a launched feature of the S24 series; Qualcomm's satellite capability on X75 was not enabled in the product, per SamMobile/Android Authority [W, secondary]. | **Conflicting**: one listing says Skylo NTN, T-Mobile lists "T-Satellite Ready" (a US carrier service), PhoneScoop shows none [W]. UNCONFIRMED. |
| FM tuner | No evidence of one on either [K] | same |
| Bands, antenna layouts, PA/front-end vendors | **Not verified** — need Samsung band list per SKU and an FCC teardown | **Not verified** |

I have deliberately not listed band numbers or RF front-end part numbers: I couldn't verify them, and they wouldn't change the verdict (see §4).

**Next evidence needed:** exact model numbers of the two phones in hand (Settings → About phone), Samsung's regional spec page, FCC IDs and their "RF exposure / operational description" exhibits, and the iFixit/TechInsights teardown.

---

## 3. Technology-by-technology

### A. 5G NR sidelink / PC5
- **What it is** [K]: 3GPP direct UE-to-UE interface. LTE PC5 (Rel-14 V2X), NR sidelink (Rel-16 V2X; Rel-17 sidelink for public safety/ProSe with relay in Rel-17/18). Specs: TS 23.287 (V2X), TS 23.303 (ProSe), TS 23.304 (5G ProSe), TS 36.300 (sidelink), TS 38.300/38.321/38.331 (NR sidelink). Spec numbers only; documents not opened here.
- **Out-of-coverage operation is in the design** [K]: UEs use pre-configured sidelink resource pools. But the pre-configuration, the permitted bands and the authorisation come from the operator/UICC/network or device provisioning — not from the user.
- **Hardware support in these phones**: **no evidence found.** My search for "sidelink/PC5" in connection with the S24 Ultra returned nothing relevant [W]. Qualcomm's C-V2X is marketed for automotive chipsets [K]. UNCONFIRMED for X75/S26 modem; likely not enabled.
- **Android API**: none public. The only satellite/NTN framework (`SatelliteManager`) is not sidelink [W: source.android.com/docs/core/connect/satellite]. IMPOSSIBLE for a third-party app.
- **Samsung proprietary API**: none found. UNCONFIRMED.
- **Root**: no. Modem control stays inside baseband firmware [K].
- **Phone-to-phone between these two**: both would have to support the same sidelink band/resource-pool config; the Exynos/Snapdragon difference (if the S26 Ultra is Exynos) adds interoperability risk. UNCONFIRMED; no evidence either way.
- **Spectrum**: the licensed cellular bands belong to MNOs; the 5.9 GHz ITS band is allocated to ITS in the EU [K]. A handset has no 5.9 GHz ITS/n47 radio that I could verify.
- **Class:** hardware UNCONFIRMED; app access IMPOSSIBLE.

### B. LTE Direct / ProSe
- Rel-12 ProSe discovery + communication [K: TS 23.303]. Qualcomm's "LTE Direct" was an operator-managed discovery-only service, centrally controlled by the network [K]. No public Android API [K]. Needs operator involvement → conflicts with "no infrastructure". Commercially defunct [K].
- **Class:** IMPOSSIBLE UNDER CONSTRAINTS.

### C. Satellite
- Android's satellite framework (Android 15+) provides `SatelliteManager`, an `ISatellite` HAL, P2P text (SMS/MMS/RCS) over NTN, with constrained data; modems must pass Android verification; onboarding is via carriers/network operators [W: source.android.com/docs/core/connect/satellite].
- That is **satellite-network infrastructure**: a satellite plus operator/ground station (e.g. Skylo geostationary service sold through carriers [W, secondary]). It does not provide a phone-to-phone mode independent of those, and the API is gated to carrier/system integration [K; verify permission levels].
- Whether the S24 Ultra has any active satellite function: reported as not launched [W, secondary]. S26 Ultra: conflicting [W].
- A direct device-to-device mode "independent of satellites" — **none documented**.
- **Class:** violates "no infrastructure" → IMPOSSIBLE UNDER CONSTRAINTS. (It is the only thing here that can deliver *long-range text*, which is why it matters: it exists *because* infrastructure is required.)

### D. Wi-Fi Aware / Wi-Fi Direct
- Work without Internet and without an access point [K]. **Disallowed**: they are Wi-Fi.
- (For the record: also short — on the order of hundreds of metres line-of-sight at best, not kilometres [K, not measured].)
- **Class:** DISALLOWED BY CONSTRAINT.

### E. UWB
- Hardware is present on both phones [W/K]. Android public API: `androidx.core.uwb` — ranging (distance/angle). Session parameters must be exchanged out-of-band; **no application payload channel** [K: developer.android.com UWB / androidx.core.uwb docs; not re-opened].
- The IEEE 802.15.4z/4ab PHY can carry data in principle [K], but the Android API doesn't expose it. Samsung proprietary access: nothing found.
- **EU regulation**: UWB is permitted on 6–8.5 GHz and 8.5–9 GHz with a very low emission mask — average about −41.3 dBm/MHz EIRP [K: Commission Decision 2019/785; verify]. That limit is a physical ceiling on range: **tens of metres LOS**, not kilometres [I].
- **Class:** arbitrary data over UWB — UNCONFIRMED/inaccessible; long range — IMPOSSIBLE.

### F. Other RF
- **FM**: no tuner evidence on either phone; no public Android FM API [K]. Receive-only on phones that had it; **no transmit hardware** in modern phones [K]. Low-power FM transmitters in the EU are limited to nanowatt-order ERP [K; verify, EN 301 357/national rules]. Not a text link even where legal.
- **NFC** (13.56 MHz, magnetic near-field, ETSI EN 300 330 [K]): centimetres. Excluded by your rules and by physics.
- **Modem diagnostic interfaces**: Qualcomm DIAG/QXDM, Samsung service menus and AT commands exist [K]. They let engineers log, lock bands, and read/write configuration. They do **not** turn the modem into an arbitrary transmitter: a cellular UE transmits only in response to downlink synchronisation and network grants (RACH/PUSCH timing), and engineering "call-box" test modes need an external base-station emulator [K]. A call-box is external hardware and transmits in licensed bands only in shielded labs.
- **Vendor-specific radios**: none found. Samsung's SDKs (Knox, Accessory, Quick Share, SmartThings) wrap Wi-Fi/BT/UWB [K]. UNCONFIRMED that any other exists.

---

## 4. The critical hardware question — three access levels

| Level | What is possible | What is blocked |
|---|---|---|
| 1. Public Android APIs | Telephony state/SMS via a SIM+network; satellite APIs (system/carrier-gated); Wi-Fi, BT, NFC, UWB ranging; audio/camera/sensors | Raw RF, PHY/waveform, frequency selection, power control, sidelink. No `SatelliteManager`-style or `ProSe` equivalent for peer radio. |
| 2. Samsung proprietary (Knox, Samsung Mobile SDK) | Device management, accessory/BT/Wi-Fi abstractions | Nothing found that touches the modem transmitter. UNCONFIRMED. |
| 3. Modem-level (DIAG/AT/engineering menus, root) | Logging, NV/config changes, band locks | Transmit control: the modem firmware is a separate signed image on its own processor; it enforces regulatory/network behaviour. Altering it = replacing signed firmware (needs Samsung/Qualcomm keys). **Access to an RF chip ≠ control of its transmitter.** |

What needs **manufacturer cooperation**: enabling sidelink in modem firmware, exposing a HAL/API for it, shipping the regional band/pool configuration, and RED re-certification of the changed radio configuration.

---

## 5. Regulation — EU and Netherlands

All items below are **[K] unless marked [W]**; verify with RDI before any transmission experiment.

- **RED 2014/53/EU**: radio equipment placed on the EU market must conform; if you materially change a radio's operation (frequency/power/modulation), you become the manufacturer responsible for conformity. Smartphone modem modification is therefore a certification matter, not just a code matter.
- **Dutch regulator**: Rijksinspectie Digitale Infrastructuur (RDI), formerly Agentschap Telecom [W]. Governing instruments: Telecommunicatiewet, Nationaal Frequentieplan (NFP), and the Besluit/Regeling on radio equipment.
- **Licensed vs licence-exempt**: cellular bands are licensed to operators. Peer devices transmitting on them without operator authorisation is unlawful. Licence-exempt SRD bands (e.g. 433.05–434.79 MHz, 863–870 MHz sub-bands, 2.4 GHz, 5 GHz, 6 GHz) have limits for power and duty cycle (e.g., ETSI EN 300 220 for sub-GHz SRD, EN 300 328 for 2.4 GHz) [K; one secondary forum figure for 868 MHz — 500 mW ERP with 10 % duty cycle for a sub-band — is **unverified** [W]. Duty-cycle may be replaced by listen-before-talk techniques [W, Agentschap Telecom brochure snippet]].
- **The phone has no 433/868 MHz radio** that I could verify. Those bands are irrelevant unless hardware is added, which your rules forbid.
- **Amateur bands** are licensed and need a licence + qualified operator; the phone can't tune them.
- Do not transmit on cellular frequencies; do not bypass SAR/power limits. I'm not recommending either.

---

## 6. Why each viable approach fails

| Approach | Why it fails the stated requirements |
|---|---|
| NR/LTE sidelink | No API, no evidence of enabled firmware, licensed/ITS spectrum, authorisation required |
| LTE Direct | Network-managed; obsolete |
| Satellite NTN | Requires satellite infrastructure + operator; emergency/service-gated |
| Wi-Fi/BT (incl. Aware, Direct, BLE Coded) | Forbidden by you |
| UWB | No data API; EU emission mask → tens of metres |
| FM | No transmitter |
| NFC | Centimetres |
| Modem diagnostics / root | No Tx control; legal bar |

---

## 7. Range physics (illustrative; no device was tested)

**Free-space path loss:** FSPL(dB) = 32.44 + 20·log10(d_km) + 20·log10(f_MHz) [K, standard].

**Radio horizon (4/3 earth):** d_km ≈ 4.12·(√h₁ + √h₂), heights in m [K, standard approximation].

For two handhelds at ~1.5 m: d ≈ 4.12 × (1.22 + 1.22) ≈ **10 km** ceiling, even in perfect LOS [I].
Antenna height each needed for pure-LOS:

| Target | Required height at each end (each side equal) [I] |
|---|---|
| 1 km, 5 km | handheld height is enough geometrically |
| 10 km | ≈ 1.5 m (marginal; any obstruction ends it) |
| 50 km | ≈ 37 m |
| 100 km | ≈ 147 m |

**Link-budget illustration — for a hypothetical handset-class transmitter** (this is *not* a statement that either phone can do it; the transmitter is inaccessible): 23 dBm Tx at the modem, ~−3 dBi each antenna on-body, sensitivity −100 dBm for a narrow low-rate waveform ⇒ ~120 dB budget. At 700 MHz FSPL = 32.44 + 56.9 + 20·log d = 89.3 + 20·log d ⇒ the free-space break-even is ~34 km. Terrain, body loss, clutter, ground reflection and fading shrink that; realistic handheld-to-handheld distances at sub-GHz would be well under that — a few km LOS, far less in urban clutter [I; no measurement]. At 2.4–6 GHz the same budget is worse by 11–19 dB (≈3.5×–9× shorter) [I].

| Distance | Assessment |
|---|---|
| 1 km | Physically plausible only with a sub-GHz-class, low-rate waveform that these phones cannot be made to emit. With the radios you are allowed to use (UWB, NFC): **impossible**. |
| 5 km | Needs LOS and sub-GHz class; not available. |
| 10 km | Geometric ceiling for ground-level handhelds. Not available. |
| 50 km / 100 km | Need elevated antennas (≈ 37 m / 147 m each) in addition to a capable radio. **Physically unsupported** for handheld-to-handheld. |

Data rate, battery drain: no sources exist for a mode that doesn't exist on these devices; I will not guess.

---

## 8. Confirmed / unconfirmed / impossible ledger

**CONFIRMED / STRONGLY SUPPORTED**
- S24 Ultra uses a Qualcomm Snapdragon (device finder page) [W]; S26 Ultra is reported as Snapdragon 8 Elite Gen 5 for Galaxy in some regions [W].
- Android's documented satellite support is NTN, carrier/HAL-gated, with P2P text through network messaging [W].
- Android's public SDK has no sidelink/ProSe/raw-RF API [K + W: nothing found].
- Dutch/EU regulation restricts operation to certified, licence-exempt or licensed bands [K/W].

**UNCONFIRMED**
- S26 Ultra modem model and regional SoC split (could be Exynos in NL).
- Any sidelink hardware/firmware support on either phone.
- Satellite capability on the S26 Ultra.
- Any Samsung-proprietary peer-RF interface.

**IMPOSSIBLE UNDER CONSTRAINTS**
- App-only access to modem Tx.
- Satellite as an infrastructure-free link.
- LTE Direct.
- UWB or FM or NFC as long-range text.
- 50–100 km handheld-to-handheld.

---

## 9. What would change the verdict (not what I'd build)

Only these, none of which you can do alone:
1. **Samsung + Qualcomm (or Samsung System LSI)** ship firmware and an API enabling sidelink on the exact SKUs, plus a regulator-approved band/pool configuration for NL (needs RDI/EU authorisation and RED conformity). Then range would be set by that band/power; an illustrative physics ceiling is in §7.
2. A **different requirement set** (allowing a radio you currently forbid). I've not detailed that, per your rules.

---

## 10. Next verification steps (research only, no development)

1. Record exact model numbers and read Settings → About phone → Baseband version on both.
2. Pull Samsung's regional spec/band list for those SKUs; pull the FCC IDs, RF exhibits and a teardown for the S26 Ultra (modem/PA vendor).
3. Check 3GPP capability reports: a modem capability dump (UE-EUTRA/NR capability via a lawful diagnostic log, receive-only) would show whether any sidelink capability bits are advertised. This transmits nothing.
4. Open and read the primary sources listed below, and update the tags in this report.
5. Ask Samsung/Qualcomm developer relations directly: "is sidelink/PC5 enabled or exposable on SM-S928B/U and SM-S948x?"

---

## 11. Sources

**Seen in this session's search results (summaries only; pages not opened):**
- S26 Ultra chipset/UWB/satellite conflicts: [GSMArena S26 Ultra](https://m.gsmarena.com/samsung_galaxy_s26_ultra-ampp-14320.php) · [Qualcomm device finder S26 Ultra](https://www.qualcomm.com/snapdragon/device-finder/samsung-galaxy-s26-ultra) · [PhoneScoop](https://www.phonescoop.com/phones/phone.php?p=7407) · [Columbia IT spec sheet (Exynos 2600 ROW)](https://www.cuit.columbia.edu/sites/www.cuit.columbia.edu/files/content/Samsung%202026/s26U.pdf) · [Android Authority FCC article](https://www.androidauthority.com/galaxy-s26-ultra-fcc-certificate-snapdragon-8-elite-gen-5-3623566) · [T-Mobile T-Satellite Ready listing](https://magentapulse.t-mobile.com/us/en/customer-support/devices/phone/samsung-galaxy-s26-ultra)
- S24 Ultra platform and satellite status: [Qualcomm device finder S24 Ultra](https://www.qualcomm.com/snapdragon/device-finder/samsung-galaxy-s24-ultra) · [Qualcomm X75 product brief](https://docs.qualcomm.com/doc/87-27161-1/87-27161-1_REV_C_Snapdragon_X75_5G_Modem-RF_System_Product_Brief.pdf) · [SamMobile: S24 lacks two-way emergency satellite](https://www.sammobile.com/news/galaxy-s24-lack-two-way-satellite-connectivity-emergencies/) · [GSMArena: Samsung exec on S24 emergency satellite](https://www.gsmarena.com/samsung_exec_confirms_emergency_satellite_tech_on_the_galaxy_s24_series-news-60376.php) (secondary; conflicting)
- Android satellite framework (supports: NTN, `SatelliteManager`, ISatellite HAL, P2P SMS/MMS/RCS, carrier onboarding): [source.android.com/docs/core/connect/satellite](https://source.android.com/docs/core/connect/satellite)
- Dutch regulator context and licence-exempt bands (RDI formerly Agentschap Telecom; secondary forum figure unverified): [RDI/AT report on licence-exempt equipment](https://www.rdi.nl/binaries/rdi/documenten/rapporten/2017/december/6/onderzoek-gebruik-vergunningvrije-apparatuur-nederland/Onderzoek_vergunning_vrij_apparatuur_nederland.pdf) · [AT brochure (via Osmocom list)](https://lists.osmocom.org/hyperkitty/list/openbsc@lists.osmocom.org/message/CHRHNSSFA6TX4WM5FTOGRFI3QC5FROFN/attachment/4/brochure-vergunningsvrije-radiotoepassingen.pdf)

**From background knowledge — NOT opened this session; verify before citing:**
- 3GPP TS 23.303 (ProSe), 23.304 (5G ProSe), 23.287 (V2X), 36.300, 38.300, 38.331 — 3gpp.org/specifications
- ETSI EN 300 220 (SRD 25–1000 MHz), EN 300 328 (2.4 GHz), EN 300 330 (inductive/NFC), EN 302 065 (UWB) — etsi.org
- Commission Implementing Decision (EU) 2019/785 (UWB) and 2013/752/EU (SRD) — eur-lex.europa.eu
- Directive 2014/53/EU (RED) — eur-lex.europa.eu
- Android `androidx.core.uwb`, NFC, SatelliteManager reference — developer.android.com
- Nationaal Frequentieplan / RDI — rdi.nl
