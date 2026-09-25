package com.example.ui.rainbow

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

private val INK = Color(0xFF111111)
private val MUTED = Color(0xFF6B7280)
private val CHART_HEIGHT = 460.dp
private val RIGHT_PAD = 58.dp
private val BOTTOM_PAD = 36.dp
private val TOP_PAD = 6.dp

// ─── Section (chart + card) για το tab Cycle ─────────────────────────────────
/**
 * Native Bitcoin Rainbow chart με πραγματικό ιστορικό (blockchain.info) και
 * live τιμή (Coinbase, κάθε 15s). Μπαίνει σε LazyColumn item, άρα δεν έχει δικό του scroll.
 */
@Composable
fun RainbowCycleSection(
    greek: Boolean,
    modifier: Modifier = Modifier,
    vm: RainbowViewModel = viewModel(),
) {
    val s by vm.state.collectAsState()
    val today = remember { DateUtil.today() }
    val series = remember(s.history, s.live) { buildSeries(s.history, s.live, today) }

    // Polling μόνο όσο το section είναι composed (ορατό στη λίστα).
    LaunchedEffect(vm) { vm.pollLive() }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            series.isEmpty() && s.loading -> PlaceholderCard {
                CircularProgressIndicator(color = Color(0xFF22D3EE))
            }
            series.isEmpty() -> PlaceholderCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        (if (greek) "Δεν φορτώθηκαν τα δεδομένα: " else "Could not load data: ") + (s.error ?: ""),
                        color = INK
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = vm::load) { Text(if (greek) "Δοκίμασε ξανά" else "Retry") }
                }
            }
            else -> {
                RainbowChartCard(series, today, greek)
                RainbowStatusCard(series, today, s.lastUpdate, greek)
            }
        }
    }
}

@Composable
private fun PlaceholderCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Box(Modifier.fillMaxWidth().height(CHART_HEIGHT).padding(24.dp), Alignment.Center) { content() }
    }
}

// ─── Chart card ──────────────────────────────────────────────────────────────
@Composable
fun RainbowChartCard(series: List<PricePoint>, today: Long, greek: Boolean) {
    val first = series.first().day.toDouble()
    val end = (RainbowModel.HALVINGS.last().day + 150).toDouble()
    val lastHalving = RainbowModel.currentHalving(today).day.toDouble()

    var xMin by remember { mutableStateOf(first) }
    var xMax by remember { mutableStateOf(end) }
    var scrubDay by remember { mutableStateOf<Long?>(null) }

    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "pulse"
    )
    val paints = remember { ChartPaints() }
    val cycles = remember(series, today) { RainbowModel.compareCycles(series, today) }
    val yRange = remember(series, xMin, xMax) { logRange(series, xMin, xMax) }

    fun setRange(a: Double, b: Double) {
        xMin = a.coerceAtLeast(first); xMax = b.coerceAtMost(end); scrubDay = null
    }

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Bitcoin Rainbow Price Model", fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                    color = INK, modifier = Modifier.weight(1f)
                )
                Text("LOG SCALE", fontSize = 11.sp, color = MUTED, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(10.dp))

            Box(
                Modifier.fillMaxWidth().height(CHART_HEIGHT).pointerInput(first, end) {
                    val rightPad = RIGHT_PAD.toPx()
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val plotW = size.width - rightPad
                        fun toDay(x: Float) =
                            (xMin + (x / plotW).coerceIn(0f, 1f) * (xMax - xMin)).roundToLong()
                        scrubDay = toDay(down.position.x)
                        down.consume()
                        var multi = false
                        while (true) {
                            val ev = awaitPointerEvent()
                            val pressed = ev.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break
                            if (pressed.size >= 2) {
                                // 2 δάχτυλα: zoom γύρω από το κέντρο + pan
                                if (!multi) { multi = true; scrubDay = null }
                                val zoom = ev.calculateZoom()
                                val pan = ev.calculatePan()
                                val c = ev.calculateCentroid(useCurrent = true)
                                val span = xMax - xMin
                                val newSpan = (span / zoom).coerceIn(45.0, end - first)
                                val fx = (c.x / plotW).coerceIn(0f, 1f).toDouble()
                                val focal = xMin + fx * span
                                val nMin = (focal - fx * newSpan - pan.x / plotW * newSpan)
                                    .coerceIn(first, end - newSpan)
                                xMin = nMin; xMax = nMin + newSpan
                                ev.changes.forEach { it.consume() }
                            } else if (!multi) {
                                // 1 δάχτυλο: tooltip
                                scrubDay = toDay(pressed[0].position.x)
                                pressed[0].consume()
                            }
                        }
                    }
                }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRainbow(series, cycles, xMin, xMax, yRange, scrubDay, greek, paints)
                }
                // Ξεχωριστό layer για τον παλμό ώστε να μην ξαναζωγραφίζονται οι ζώνες σε κάθε frame.
                Canvas(Modifier.fillMaxSize()) {
                    drawLivePulse(series.last(), xMin, xMax, yRange, pulse)
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RangeChip(if (greek) "Όλα" else "All") { setRange(first, end) }
                RangeChip(if (greek) "Κύκλος" else "Cycle") { setRange(lastHalving - 150, today + 400.0) }
                RangeChip(if (greek) "Σήμερα" else "Today") { setRange(today - 400.0, today + 120.0) }
                RangeChip("✕") { scrubDay = null }
            }
            Text(
                if (greek) "1 δάχτυλο: λεπτομέρειες · 2 δάχτυλα: zoom & μετακίνηση"
                else "1 finger: details · 2 fingers: zoom & pan",
                fontSize = 11.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun RangeChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = INK)
    }
}

// ─── Geometry ────────────────────────────────────────────────────────────────
/** log10 εύρος του άξονα Y για το ορατό παράθυρο (ζώνες + τιμές). */
private fun logRange(series: List<PricePoint>, xMin: Double, xMax: Double): Pair<Double, Double> {
    val span = xMax - xMin
    var lo = Double.MAX_VALUE; var hi = -Double.MAX_VALUE
    for (k in 0..40) {
        val d = xMin + span * k / 40
        lo = min(lo, log10(RainbowModel.edgePrice(d, 0)))
        hi = max(hi, log10(RainbowModel.edgePrice(d, RainbowModel.EDGES.lastIndex)))
    }
    for (pt in series) {
        if (pt.day < xMin - 2 || pt.day > xMax + 2) continue
        val lp = log10(pt.price); lo = min(lo, lp); hi = max(hi, lp)
    }
    val padY = (hi - lo) * 0.05
    return (lo - padY) to (hi + padY)
}

private class Frame(scope: DrawScope, val xMin: Double, xMax: Double, val lo: Double, val hi: Double) {
    val l = 0f
    val r = scope.size.width - with(scope) { RIGHT_PAD.toPx() }
    val t = with(scope) { TOP_PAD.toPx() }
    val b = scope.size.height - with(scope) { BOTTOM_PAD.toPx() }
    val w = r - l
    val h = b - t
    val span = xMax - xMin
    fun x(d: Double) = l + ((d - xMin) / span * w).toFloat()
    fun y(price: Double) = b - ((log10(price) - lo) / (hi - lo) * h).toFloat()
}

// ─── Drawing ─────────────────────────────────────────────────────────────────
private class ChartPaints {
    val axis = Paint().apply { isAntiAlias = true; color = 0xFF4B5563.toInt() }
    val label = Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
    val tip = Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD }
}

private fun DrawScope.drawLivePulse(
    last: PricePoint, xMin: Double, xMax: Double, yRange: Pair<Double, Double>, pulse: Float,
) {
    val f = Frame(this, xMin, xMax, yRange.first, yRange.second)
    val x = f.x(last.day.toDouble()); val y = f.y(last.price)
    if (x < f.l - 1 || x > f.r + 1) return
    val bandCol = Color(RainbowModel.BANDS[RainbowModel.bandIndex(last.day, last.price)].color)
    clipRect(f.l, f.t, f.r + 24.dp.toPx(), f.b) {
        drawCircle(bandCol.copy(alpha = 0.55f * (1 - pulse)), (6 + 16 * pulse).dp.toPx(), Offset(x, y))
        drawCircle(Color.White, 6.dp.toPx(), Offset(x, y))
        drawCircle(INK, 6.dp.toPx(), Offset(x, y), style = Stroke(2.5.dp.toPx()))
    }
}

private fun DrawScope.drawRainbow(
    series: List<PricePoint>, cycles: List<CycleCompare>,
    xMin: Double, xMax: Double, yRange: Pair<Double, Double>,
    scrubDay: Long?, greek: Boolean, p: ChartPaints,
) {
    val f = Frame(this, xMin, xMax, yRange.first, yRange.second)
    val (lo, hi) = yRange
    val l = f.l; val r = f.r; val t = f.t; val b = f.b
    val native = drawContext.canvas.nativeCanvas
    p.axis.textSize = 10.sp.toPx()

    drawRect(Color(0xFFD1D5DB), Offset(l, t), Size(f.w, f.h), style = Stroke(1.dp.toPx()))

    clipRect(l, t, r, b) {
        // 9 rainbow ζώνες
        val n = 160
        for (i in RainbowModel.BANDS.indices) {
            val path = Path()
            for (k in 0..n) {
                val d = xMin + f.span * k / n
                val y = f.y(RainbowModel.edgePrice(d, i + 1))
                if (k == 0) path.moveTo(f.x(d), y) else path.lineTo(f.x(d), y)
            }
            for (k in n downTo 0) {
                val d = xMin + f.span * k / n
                path.lineTo(f.x(d), f.y(RainbowModel.edgePrice(d, i)))
            }
            path.close()
            drawPath(path, Color(RainbowModel.BANDS[i].color))
        }

        // Grid τιμών (δυνάμεις του 10) και ετών
        for (e in floor(lo).toInt()..ceil(hi).toInt()) {
            val y = f.y(10.0.pow(e))
            drawLine(Color(0x22000000), Offset(l, y), Offset(r, y), 1.dp.toPx())
        }
        val y0 = DateUtil.civil(xMin.toLong()).first
        val y1 = DateUtil.civil(xMax.toLong()).first
        for (yr in y0..y1 + 1) {
            val x = f.x(DateUtil.epochDay(yr, 1, 1).toDouble())
            drawLine(Color(0x18000000), Offset(x, t), Offset(x, b), 1.dp.toPx())
        }

        // Halvings: συνεχείς για τα πραγματικά, κόκκινες διακεκομμένες για τα εκτιμώμενα
        p.label.textSize = 10.sp.toPx()
        for (hv in RainbowModel.HALVINGS) {
            val x = f.x(hv.day.toDouble())
            if (x < l - 2 || x > r + 2) continue
            if (hv.estimated) {
                drawLine(
                    Color(0xCCDC2626), Offset(x, t), Offset(x, b), 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            } else {
                drawLine(Color(0x99374151), Offset(x, t), Offset(x, b), 1.5.dp.toPx())
            }
            p.label.color = if (hv.estimated) 0xFFDC2626.toInt() else 0xFF111111.toInt()
            val txt = if (hv.estimated) "Halving ${hv.year} (Est)" else "Halving ${hv.year}"
            native.save()
            native.rotate(-90f, x - 4.dp.toPx(), b - 6.dp.toPx())
            native.drawText(txt, x - 4.dp.toPx(), b - 6.dp.toPx(), p.label)
            native.restore()
        }

        // Γραμμή τιμής
        val visible = series.filter { it.day >= xMin - 2 && it.day <= xMax + 2 }
        if (visible.size > 1) {
            val path = Path()
            val stride = max(1, visible.size / max(1, (f.w / 1.2f).toInt()))
            visible.forEachIndexed { i, pt ->
                if (i % stride != 0 && i != visible.lastIndex) return@forEachIndexed
                val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, INK, style = Stroke(2.2.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        }

        // Ίδια μέρα σε κάθε προηγούμενο κύκλο: dot στη γραμμή + διαγώνια κάτω-δεξιά προς ετικέτα έτους
        p.label.textSize = 14.sp.toPx()
        p.label.color = 0xFF111111.toInt()
        cycles.dropLast(1).forEach { c ->
            val price = c.price ?: return@forEach
            val x = f.x(c.day.toDouble()); val y = f.y(price)
            if (x < l || x > r) return@forEach
            val ex = x + 56.dp.toPx(); val ey = y + 52.dp.toPx()
            drawLine(INK, Offset(x, y), Offset(ex, ey), 1.5.dp.toPx())
            drawCircle(INK, 4.dp.toPx(), Offset(x, y))
            drawCircle(Color(c.halving.color), 5.dp.toPx(), Offset(ex, ey))
            drawCircle(INK, 5.dp.toPx(), Offset(ex, ey), style = Stroke(1.dp.toPx()))
            native.drawText("${c.halving.year}", ex + 9.dp.toPx(), ey + 5.dp.toPx(), p.label)
        }

        // Crosshair + tooltip
        scrubDay?.let { sd ->
            val pt = series.nearest(sd) ?: return@let
            if (abs(pt.day - sd) > 5) return@let
            val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
            drawLine(Color(0x99111111), Offset(x, t), Offset(x, b), 1.dp.toPx())
            drawLine(Color(0x55111111), Offset(l, y), Offset(r, y), 1.dp.toPx())
            drawCircle(Color.White, 6.dp.toPx(), Offset(x, y))
            drawCircle(INK, 6.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))

            val bandI = RainbowModel.bandIndex(pt.day, pt.price)
            val hv = RainbowModel.HALVINGS.lastOrNull { !it.estimated && it.day <= pt.day }
            val dayLine = when {
                hv == null && greek -> "Πριν το 1ο halving"
                hv == null -> "Before the 1st halving"
                greek -> "Μέρα ${pt.day - hv.day} μετά το halving (${hv.year})"
                else -> "Day ${pt.day - hv.day} after the halving (${hv.year})"
            }
            val lines = listOf(
                DateUtil.format(pt.day) to (13.sp.toPx() to 0xFFD1D5DB.toInt()),
                usd(pt.price) to (22.sp.toPx() to 0xFFFFFFFF.toInt()),
                RainbowModel.BANDS[bandI].name to (14.sp.toPx() to RainbowModel.BANDS[bandI].color.toInt()),
                dayLine to (11.sp.toPx() to 0xFF9CA3AF.toInt()),
            )
            val gap = 6.dp.toPx()
            val boxW = lines.maxOf { (s, st) -> p.tip.apply { textSize = st.first }.measureText(s) } + 28.dp.toPx()
            val boxH = lines.sumOf { (it.second.first + gap).toDouble() }.toFloat() + 18.dp.toPx()
            val bx = if (x > l + f.w / 2) l + 10.dp.toPx() else r - boxW - 10.dp.toPx()
            val by = t + 10.dp.toPx()
            drawRoundRect(Color(0xF0222226), Offset(bx, by), Size(boxW, boxH), CornerRadius(16.dp.toPx()))
            var ty = by + 12.dp.toPx()
            lines.forEach { (s, st) ->
                ty += st.first
                p.tip.textSize = st.first; p.tip.color = st.second
                native.drawText(s, bx + 14.dp.toPx(), ty, p.tip)
                ty += gap
            }
        }
    }

    // Ετικέτες Y (δεξιά)
    for (e in floor(lo).toInt()..ceil(hi).toInt()) {
        val y = f.y(10.0.pow(e))
        if (y < t || y > b) continue
        val v = 10.0.pow(e)
        val s = when {
            v >= 1e6 -> "$${(v / 1e6).toInt()}M"
            v >= 1e3 -> "$${(v / 1e3).toInt()}K"
            v >= 1 -> "$${v.toInt()}"
            else -> "$" + String.format(java.util.Locale.US, "%.2f", v)
        }
        native.drawText(s, r + 6.dp.toPx(), y + 4.dp.toPx(), p.axis)
    }
    // Ετικέτες X (έτη)
    val y0 = DateUtil.civil(xMin.toLong()).first
    val y1 = DateUtil.civil(xMax.toLong()).first
    val step = if (f.span > 3000) 2 else 1
    for (yr in y0..y1 + 1) {
        if (yr % step != 1 % step) continue
        val x = f.x(DateUtil.epochDay(yr, 1, 1).toDouble())
        if (x < l || x > r) continue
        native.save()
        native.rotate(40f, x, b + 12.dp.toPx())
        native.drawText("$yr", x, b + 14.dp.toPx(), p.axis)
        native.restore()
    }
}

// ─── Κάρτα: τρέχον band, τιμή, ίδια μέρα σε κάθε κύκλο ───────────────────────
@Composable
private fun RainbowStatusCard(series: List<PricePoint>, today: Long, lastUpdate: Long, greek: Boolean) {
    val last = series.last()
    val band = RainbowModel.BANDS[RainbowModel.bandIndex(last.day, last.price)]
    val hv = RainbowModel.currentHalving(today)
    val n = RainbowModel.daysSinceHalving(today)
    val cycles = remember(series, today) { RainbowModel.compareCycles(series, today) }

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(band.name, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(band.color))
                Text(
                    (if (greek) "Τιμή: " else "Price: ") + usd(last.price),
                    fontSize = 20.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF374151)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (greek) "Μέρα $n μετά το halving ${hv.year}" else "Day $n after the ${hv.year} halving",
                    fontSize = 14.sp, color = MUTED
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Box(
                        Modifier.size(8.dp).background(
                            if (lastUpdate > 0) Color(0xFF10B981) else Color(0xFF9CA3AF), CircleShape
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        when {
                            lastUpdate > 0 && greek -> "Live · ανανέωση κάθε 15s"
                            lastUpdate > 0 -> "Live · refreshes every 15s"
                            greek -> "Σύνδεση στο live feed…"
                            else -> "Connecting to live feed…"
                        },
                        fontSize = 12.sp, color = MUTED
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Color(0xFFE5E7EB))
            Text(
                if (greek) "Μέρα $n σε κάθε κύκλο" else "Day $n in every cycle",
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = INK
            )
            Spacer(Modifier.height(4.dp))
            cycles.forEach { c ->
                val isNow = c.day == today
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(12.dp).background(if (isNow) INK else Color(c.halving.color), CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                isNow && greek -> "Σήμερα"
                                isNow -> "Today"
                                greek -> "Κύκλος ${c.halving.year}"
                                else -> "${c.halving.year} cycle"
                            },
                            color = INK, fontWeight = FontWeight.Medium, fontSize = 15.sp
                        )
                        Text(DateUtil.format(c.day), color = MUTED, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            c.price?.let(::usd) ?: "—", color = INK,
                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                        c.band?.let {
                            Text(
                                RainbowModel.BANDS[it].name, color = Color(RainbowModel.BANDS[it].color),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
