# Infrastructure-free text link, Galaxy S26 Ultra ⇄ Galaxy S24 Ultra — Feasibility Report

**Evidence status (read first).** This report was written from documented platform behaviour, public specifications and peer-reviewed/academic literature as I know them. I did **not** have a physical device, a teardown, or live web access in this session. Every claim is tagged:

- **[C] Confirmed**: stated in primary documentation (AOSP / developer.android.com / a standard / a published paper). URLs listed in §9.
- **[S] Spec-level / secondary**: widely reported, not re-verified here. Re-check against Samsung's spec sheet or an FCC filing teardown.
- **[U] Unconfirmed**: hypothesis or my inference. Needs a measurement.

The S26 Ultra is the weakest-evidenced device. Everything I say about its chipset is **[U]** until checked against its FCC ID / teardown.

---

## 0. Verdict

| Question | Answer |
|---|---|
| Can the wired USB-C earphones + cable act as an RF antenna / RF link between two phones? | **No.** Not with this hardware and Android's API surface. (§3) |
| Is there any RF path that avoids Wi-Fi, BT, cellular, SIM and external hardware? | **Yes, one: NFC** (13.56 MHz, near-field). UWB hardware exists but exposes no data channel. (§2, §4) |
| Is a *useful-range* (> ~10 cm) text link possible under the constraints? | **Yes, but not via RF**: acoustic (speaker→mic) and optical (screen→front camera) work with stock apps' permissions. (§4) |
| Does root change the answer? | **No**, with one narrow exception that is irrelevant here. (§6) |
| Best viable architecture | **Pluggable transport under one framed, CRC-checked, ACK'd protocol.** Primary: acoustic (ggwave-style FSK). Secondary: NFC HCE for ≤4 cm. Tertiary: optical. (§7) |

The central hypothesis (earphone cable participates in RF) is **rejected**. Reasons are in §3; the short version is that the cable is an *unintentional* radiator/receiver at best, the phones have no wideband RF front-end connected to it, and the one real "headphone-as-antenna" case (FM *receive*) has no transmit counterpart and no public API.

---

## 1. Phase 1 — Hardware inventory

### 1.1 Radios present (both phones)

| Block | S24 Ultra | S26 Ultra | Third-party access |
|---|---|---|---|
| Cellular modem | Snapdragon X75 [S] | Qualcomm X-series, generation not verified [U] | Only through telephony APIs; no raw RF |
| Wi-Fi / BT | Qualcomm FastConnect 7800 class [S] | Newer FastConnect/Samsung-integrated [U] | Wi-Fi/BT APIs — **forbidden by constraints** |
| NFC | NXP-class NFC controller [S], 13.56 MHz | present [S] | `android.nfc.*` — **allowed** |
| UWB | Present [S] | Present [S, likely] | `androidx.core.uwb` — ranging only |
| GNSS | multi-band [S] | multi-band [S] | Receive-only |
| FM tuner | Not exposed; see §3.4 [U] | [U] | No public Android API |
| IR blaster | Samsung flagships dropped it years ago [S] | [S] | — |
| Qi coil (Wireless PowerShare) | Yes (S24 Ultra) [S] | Yes [S] | Power only; in-band Qi signalling is inside charger IC firmware [U] |

Chipset detail: S24 Ultra is Snapdragon 8 Gen 3 for Galaxy globally [S]. S26 Ultra SoC region split is **[U]**; do not rely on it. It does not change any conclusion below because every candidate is gated by the *API surface*, not the SoC.

### 1.2 Architecture distinction (what the user asked me to keep separate)

- **Antenna**: passive conductor matched to a band. A headphone cable is a conductor, not matched to any transmit band.
- **RF front-end**: PA/LNA/filters/switches between antenna and transceiver. Phones' front-ends are connected to dedicated antenna feeds (cellular, Wi-Fi/BT, GNSS, NFC loop, UWB), not to the 3.5 mm/USB-C connector.
- **Transceiver/baseband**: modulation, protocol timing, regulatory limits. In cellular and Wi-Fi these are firmware-locked and certification-bound (FCC/CE).
- **Audio over USB-C**: USB Audio Class digital stream, or Audio Adapter Accessory Mode analog on SBU pins [C: USB Type-C spec; AOSP USB audio docs]. Neither routes to any RF block.

### 1.3 Existing non-network data hardware

The only radio in this list that moves arbitrary data without infrastructure *and* is exposed to apps is **NFC**. Samsung's own Quick Share and Android's Nearby Connections both ride on BT/Wi-Fi → violate constraints [C].

---

## 2. What Android exposes (third-party apps)

| Capability | API | Status |
|---|---|---|
| NFC reader/writer | `NfcAdapter.enableReaderMode`, `IsoDep` | [C] developer.android.com/develop/connectivity/nfc |
| NFC card emulation | `HostApduService` (HCE) | [C] |
| NFC peer-to-peer (Android Beam/SNEP push) | `setNdefPushMessage` | **Removed in Android 14** [C]. Do not design on it. |
| UWB ranging | `androidx.core.uwb` (`UwbManager`, permission `UWB_RANGING`) | [C] Ranging (distance/angle) only. Session parameters must be exchanged out-of-band. No application payload channel in the public API. |
| Microphone / speaker | `AudioRecord`, `AudioTrack`; `RECORD_AUDIO` | [C] |
| Camera / torch | CameraX/Camera2, `CameraManager.setTorchMode`; `CAMERA` | [C] |
| Ambient light / accel / mag | `SensorManager` | [C] (rate-limited to 200 Hz w/o `HIGH_SAMPLING_RATE_SENSORS`) |
| Raw modem / PHY / baseband | none | [C] absent from public SDK; the RIL is system-only |
| FM radio | none public | [C] absent from SDK |
| USB host/accessory | `UsbManager`, Android Open Accessory | [C] but needs a cable between phones (see §4) |

No "undocumented API" is proposed anywhere in this report.

---

## 3. The earphone hypothesis, tested

1. **Mechanism.** USB-C earphones carry audio as digital USB Audio Class (the DAC sits in the plug/cable) or, rarely, analog via Audio Adapter Accessory Mode [C]. The wires carry baseband audio (≲ 24 kHz) or USB full-/high-speed differential data. Neither is an RF modulation chain.
2. **Antenna.** A ~1.2 m unshielded pair is electrically a poor, un-tuned, intentionally-not-radiating conductor. It couples EMI (the audible "GSM buzz" is exactly that) — that is *reception of interference through a non-linear audio path*, not a communications receiver.
3. **No front-end connection.** There is no switch or matching network between the connector and any phone transceiver [S; confirm in teardown].
4. **The real precedent — FM.** Phones that include an FM tuner used the wired headset as the antenna. This is a *receive-only, broadcast FM* feature [C: widely documented]. It cannot transmit, needs a tuner chip routed to the jack/connector, and Android has no public API for it. USB-C headsets do not provide this path in the same way. Whether an S24/S26 has any active FM block is **[U]**; even if it did, it would not give a phone-to-phone link.
5. **Academic covert-channel work** shows cables/buses radiate unintentionally but all of it needs an external receiver: USBee (USB data bus as transmitter, received by SDR) [C: Guri et al. 2016], AirHopper (display cable → FM in a phone *receiver*) [C: Guri et al. 2014], GSMem [C: Guri et al., USENIX Security 2015]. These require a specialised emitter program on a PC and a wideband receiver. A phone has neither a controllable emitter on its USB data lines at those frequencies nor a wideband receiver. **[U]** whether a phone's own USB lines could be driven to emit; and even then the receiving phone has no matching receiver.
6. **Regulatory.** Deliberately using a non-intentional radiator as a transmitter is a Part 15 / CE issue (47 CFR Part 15 [C]). Not a technical blocker, but a legal one.

**Conclusion:** rejected. Residual claim worth one afternoon of measurement is only: "does the cable measurably couple *acoustic or magnetic* signals?" — which is not RF. (Earphone *drivers/mic* work as ordinary acoustic transducers, §4.)

---

## 4. Phase 2 — Alternative transports vs. constraints

| Transport | Satisfies constraints? | Notes |
|---|---|---|
| Bluetooth / BLE | ❌ violates (no Bluetooth) | |
| Wi-Fi Direct / Wi-Fi Aware / Quick Share / Nearby Connections | ❌ violates | |
| Cellular, D2D/sidelink (LTE PC5, C-V2X), satellite | ❌ cellular/SIM, and sidelink is not exposed to apps [U] | |
| **NFC (HCE ↔ reader)** | ✅ no network, no extra hardware. Arguably "RF", but not in forbidden list | Range 0–~4 cm [C: NFC Forum/ISO 14443]. See §4.1 |
| **UWB** | ⚠️ hardware allowed, but API gives ranging only. Session setup needs an out-of-band channel (NFC/BLE). Cannot carry text [C: androidx.core.uwb docs] | Useful only as "distance sensor". Not a text transport |
| **Acoustic (data-over-sound)** | ✅ speaker + mic, built in | §4.2 |
| **Optical (screen→camera, torch→camera)** | ✅ built in | §4.3 |
| Vibration → accelerometer (phones touching a table) | ✅ technically; ~single-digit bps [U] | Novelty only |
| USB-C cable phone↔phone (host/accessory) | ⚠️ ambiguous: not a radio, but it is "external hardware" and not wireless. Android Open Accessory exists [C] | Excluded unless you explicitly relax the constraints |
| Qi reverse charging as data link | ❌ no API [U] | |
| Earphone cable as RF link | ❌ §3 | |

### 4.1 NFC HCE (the only RF option)

- Roles are asymmetric: **A = reader** (`enableReaderMode`), **B = card** (`HostApduService`). A device in reader mode doesn't emulate cards [C], so true simultaneous two-way isn't available. Reply path: B queues its reply, A polls with `GET` APDUs and receives it in the APDU response. The reply latency is therefore "A's next poll", not instant.
- Both screens must be on; Samsung's NFC antenna is on the back, upper-middle — alignment must be found by experiment [U].
- Throughput: raw 106–424 kbit/s at the RF layer [C]; usable Android HCE throughput is much lower (extended-length APDUs, IsoDep timeouts) — expected low single-digit kB/s [U]; **measure**.
- Permissions: `android.permission.NFC` only.

### 4.2 Acoustic

- Open implementation: **ggwave** (MIT, github.com/ggerganov/ggwave), multi-tone FSK with Reed-Solomon. Project reports payloads ≲ 140 bytes and on the order of 10–20 bytes/s audible; ultrasound modes slower and less reliable [S].
- Phone speakers/mics sample at 44.1/48 kHz; ultrasonic 18–20 kHz is attenuated by speaker response and by Android audio processing (AGC/NS/AEC) [U — measure, use `UNPROCESSED`/`VOICE_RECOGNITION` source where supported].
- Range: ~1 m in a quiet room, falling quickly with noise [U].
- Half-duplex, both phones can reply by alternating. Wired earphones could be used as speaker/mic, but that is just an acoustic transducer, not RF.
- Permissions: `RECORD_AUDIO` (+ foreground service if backgrounded). No network.

### 4.3 Optical

- **Screen → front camera**: facing screens give a symmetric two-way link (each phone's front camera sees the other's screen). Frame-streamed 2D codes (QR/colour matrix) at ≈5–10 fps with FEC: estimated hundreds of B/s at 10–30 cm [U].
- **Torch → camera**: OOK at camera frame rate (30–60 fps) ⇒ ~15–30 bit/s [U]; works at longer line-of-sight but is slow.
- Permissions: `CAMERA`. Torch needs no extra permission beyond camera.

---

## 5. Hardware / firmware restrictions

- Cellular/Wi-Fi/BT PHY and MAC live in baseband/WCN firmware, signed and certified; app processors cannot inject waveforms [C in principle: AOSP HAL architecture; Qualcomm modem is closed]. Even on-network, a UE transmits only on network-granted resources (cellular) — no peer sync without a cell.
- Knox: bootloader unlock on Samsung trips the Knox e-fuse (permanently) and, on US carrier-locked models, unlocking is generally unavailable [S].
- Play Integrity / banking apps fail on rooted devices [S] (irrelevant to the PoC).
- Android audio effects and Samsung audio post-processing may colour acoustic signals [U].

## 6. Does root change anything?

- Root gives access to system services and `/dev` nodes. It does **not** add an RF front-end connection, an antenna, a tuner, or PHY control. Modem firmware remains a separate processor with its own signed image [S].
- Possible gains: disabling audio effects (helps acoustic reliability slightly), sensor rate limits, reading NFC controller logs. None enables a new transport.
- Transmitting arbitrary RF requires a transmitter that can do so legally (47 CFR Part 15/22/24, CE RED). Not available.
- **Conclusion:** root is not a path to the stated goal and I do not recommend it.

---

## 7. Best viable architecture (Phase 3 design — no app build yet)

```
UI (Compose)  ──  ChatRepository (Room, local only)
                        │
               Link layer  (framing, CRC, ACK/retransmit, seq, dedupe)
                        │
          Transport interface  ── send(bytes) / onReceive(bytes) / maxPayload
              ├─ AcousticTransport   (AudioTrack / AudioRecord, FSK)   [primary]
              ├─ NfcHceTransport     (reader on A, HostApduService on B) [≤4 cm]
              └─ OpticalTransport    (CameraX analyzer + screen frames)  [fallback]
```

**Frame** (max payload bounded by transport):

```
| 0xA5 | VER/TYPE(1) | SEQ(1) | LEN(1) | PAYLOAD(0..N, UTF-8) | CRC-16/CCITT(2) |
TYPE: DATA=0, ACK=1, NACK=2
```

- Error detection: CRC-16 on header+payload; transport-level FEC (Reed-Solomon in acoustic/optical) on top.
- ACK: receiver replies `ACK(seq)`; sender retransmits after timeout (e.g. 2× frame time + jitter) up to N tries; duplicates dropped by `SEQ`.
- Two-way: half-duplex turn-taking; any message may piggy-back an ACK.
- No Internet: no `INTERNET` permission in the manifest (this makes the constraint verifiable at install time).

**Required manifest permissions**

| Transport | Permission / feature |
|---|---|
| Acoustic | `RECORD_AUDIO`; `FOREGROUND_SERVICE_MICROPHONE` if backgrounded |
| NFC | `NFC`; `<uses-feature android:name="android.hardware.nfc.hce">` |
| Optical | `CAMERA` |
| (none) | **do not request** `INTERNET`, `BLUETOOTH*`, `NEARBY_WIFI_DEVICES`, `ACCESS_WIFI_STATE` |

**Expected performance (all [U], to be measured):**

| Transport | Range | Throughput | 100-char message |
|---|---|---|---|
| Acoustic audible FSK | ~0.5–1 m quiet | ~10–20 B/s | ~6–15 s |
| NFC HCE | ≤ 4 cm | ~kB/s | < 1 s |
| Optical screen↔camera | 10–30 cm | ~100s B/s | 1–3 s |
| Torch→camera | line-of-sight, metres | ~2–4 B/s | ~30–60 s |

---

## 8. Confirmed / unconfirmed / impossible

**Confirmed capabilities**
- NFC reader+HCE phone-to-phone is possible via public APIs; Android Beam is gone (A14+).
- Speaker/mic and camera/screen transports are available with `RECORD_AUDIO`/`CAMERA` only.
- UWB public API is ranging-only.
- No public API for raw RF, modem, or FM.

**Unconfirmed hypotheses (need measurement)**
- S26 Ultra chipset/NFC/UWB specifics.
- Real HCE throughput and the Samsung NFC antenna alignment window.
- Real acoustic range/throughput with Samsung's audio pipeline, with and without USB-C earphones.
- Whether either phone has any active FM tuner path.
- Optical symbol rate at 120 Hz LTPO display vs. camera exposure.

**Impossible / inaccessible**
- Earphone cable as RF transmitter/receiver between these phones.
- App-level raw RF/PHY control (cellular, Wi-Fi, BT) with or without root, short of replacing modem firmware (signed, out of scope, illegal to operate).
- UWB as a text channel via public APIs.
- Cellular sidelink/D2D from an app.

---

## 9. References (verify links before citing)

- Android NFC overview / HCE / reader mode: developer.android.com/develop/connectivity/nfc, …/nfc/hce
- Android 14 Beam removal: `NfcAdapter` API reference (deprecated API 29, removed 14)
- UWB: developer.android.com/develop/connectivity/uwb ; androidx.core.uwb release notes
- AOSP USB audio: source.android.com/docs/core/audio/usb
- USB Type-C spec, Audio Adapter Accessory Mode (USB-IF)
- Android Open Accessory: developer.android.com/develop/connectivity/usb/accessory
- ISO/IEC 14443, NFC Forum Digital Protocol (range ≈ 4 cm, 106–424 kbit/s)
- ggwave: github.com/ggerganov/ggwave
- M. Guri et al., *AirHopper* (MALWARE 2014); *GSMem* (USENIX Security 2015); *USBee* (2016); *MOSQUITO* (2018, speaker↔speaker ultrasound)
- 47 CFR Part 15 (unintentional/intentional radiators)
- Samsung Galaxy S24 Ultra spec sheet; S26 Ultra spec sheet + FCC ID teardown (**to fetch**)

---

## 10. Exact next experiment (≈ 1 day, two phones, no app store)

**Goal:** pick the transport by measurement, not opinion.

1. **Acoustic baseline (30 min, zero code).** Install ggwave's Android demo (or the web demo in Chrome on both phones — it needs only mic/speaker; run it from local files, no Internet needed after download). Send a 16-byte string S26→S24 and back at 10 cm, 50 cm, 1 m; audible and ultrasound modes; with and without the USB-C earphones as the speaker/mic. Log success rate over 20 trials each.
2. **NFC HCE probe (2–3 h).** Minimal app: A `enableReaderMode` + `IsoDep.transceive(SELECT AID)` then 5 × `GET` of 255-byte extended APDUs; B `HostApduService` returning a queued string. Measure (a) alignment window in cm for S26↔S24 back-to-back, (b) bytes/s, (c) failure rate, (d) behaviour with screen off.
3. **Optical probe (2 h).** Screens facing, CameraX analyzer decoding a 2-state flashing region vs. QR-stream at 10 cm; log bit error rate vs. refresh rate.
4. **Earphone hypothesis falsification (1 h, paperwork).** Read the S24 Ultra and S26 Ultra FCC ID filings (operational description, internal photos, block diagrams) and any teardown. Confirm there is no FM tuner or any front-end routed to the USB-C connector. If none appears, close the hypothesis formally. A bench check that needs no extra hardware is not possible: RF emission from the cable can only be observed with an SDR or spectrum analyser, which the constraints exclude.
5. Choose the transport by: success ≥ 95 % at the target range, then throughput.

Stop condition: if acoustic < 80 % success at 30 cm in a quiet room and NFC alignment is unreliable, the constraint set is effectively unsatisfiable in practice and the only remaining option is relaxing a constraint (BLE or a USB-C cable).

---

## 11. Bottom line

The earphone-RF idea doesn't hold. What the two phones can genuinely do under your constraints is exchange a text message by NFC at a few centimetres, or by sound or light at tens of centimetres to about a metre, at a few to tens of bytes per second. That is enough for short text with ACKs, and it is a legitimate PoC. It isn't RF communication at range, and nothing available without root or new hardware changes that.
