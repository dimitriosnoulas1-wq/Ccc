package com.example.ui.rainbow

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.toArgb
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

// ─── Παλέτα (σκούρο "glass" στυλ, ίδια επίσημα χρώματα ζωνών) ─────────────────
private val CARD_BG = Color(0xFF0D0F1C)
private val CARD_BG_2 = Color(0xFF141729)
private val LINE = Color(0xFFFFFFFF)
private val TEXT = Color(0xFFF3F4F6)
private val MUTED = Color(0xFF9CA3AF)
private val FAINT = Color(0xFF6B7280)
private val ACCENT = Color(0xFF22D3EE)
private val CRASH = Color(0xFFF87171)
private val DIVIDER = Color(0x1FFFFFFF)

private val CHART_HEIGHT = 460.dp
private val RIGHT_PAD = 58.dp
private val BOTTOM_PAD = 36.dp
private val TOP_PAD = 6.dp

// ─── Section (chart + ανάλυση) για το tab Cycle ──────────────────────────────
/**
 * Native Bitcoin Rainbow chart: πραγματικό ιστορικό (blockchain.info, 1 φορά τη μέρα, με cache)
 * και live τιμή (Coinbase, κάθε 15s). Το σημερινό σημείο αλλάζει μία φορά τη μέρα,
 * ή νωρίτερα αν γίνει κίνηση ±7% ή αλλαγή ζώνης. Μπαίνει σε LazyColumn item.
 */
@Composable
fun RainbowCycleSection(
    greek: Boolean,
    liveUsd: Double = 0.0,
    priceIsLive: Boolean = false,
    modifier: Modifier = Modifier,
    vm: RainbowViewModel = viewModel(),
) {
    val s by vm.state.collectAsState()
    val today = s.today
    val series = remember(s.history, s.shown, today) { buildSeries(s.history, s.shown, today) }
    val insights = remember(series, today) { CycleInsights.compute(series, today) }

    LaunchedEffect(liveUsd, priceIsLive) {
        if (priceIsLive && liveUsd > 0.0) vm.applyLive(liveUsd)
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            series.isEmpty() && s.loading -> PlaceholderCard {
                CircularProgressIndicator(color = ACCENT)
            }
            series.isEmpty() -> PlaceholderCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        (if (greek) "Δεν φορτώθηκαν τα δεδομένα: " else "Could not load data: ") + (s.error ?: ""),
                        color = TEXT
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.refreshHistory(force = true) }) { Text(if (greek) "Δοκίμασε ξανά" else "Retry") }
                }
            }
            else -> {
                RainbowChartCard(series, today, insights, greek)
                RainbowNowCard(s, series, today, insights.lastOrNull(), priceIsLive, greek)
                insights.filter { !it.isCurrent }.reversed().forEach { CycleInsightCard(it, greek) }
            }
        }
    }
}

@Composable
private fun PlaceholderCard(content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Box(Modifier.fillMaxWidth().height(CHART_HEIGHT).padding(24.dp), Alignment.Center) { content() }
    }
}

// ─── Chart card ──────────────────────────────────────────────────────────────
@Composable
fun RainbowChartCard(series: List<PricePoint>, today: Long, insights: List<CycleInsights.Insight>, greek: Boolean) {
    val first = series.first().day.toDouble()
    val end = (RainbowModel.HALVINGS.last().day + 150).toDouble()
    val lastHalving = RainbowModel.currentHalving(today).day.toDouble()

    var xMin by remember { mutableStateOf(first) }
    var xMax by remember { mutableStateOf(min(end, today + 900.0)) }
    var scrubDay by remember { mutableStateOf<Long?>(null) }

    // Η γραμμή τιμής "ζωγραφίζεται" από την αρχή ως σήμερα στο πρώτο άνοιγμα.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(1600, easing = FastOutSlowInEasing)) }

    val pulse by rememberInfiniteTransition(label = "live").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "pulse"
    )
    val paints = remember { ChartPaints() }
    val markers = remember(series, today, insights) { buildMarkers(insights, series) }
    val yRange = remember(series, xMin, xMax) { logRange(series, xMin, xMax) }

    fun setRange(a: Double, b: Double) {
        xMin = a.coerceAtLeast(first); xMax = b.coerceAtMost(end); scrubDay = null
    }

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Bitcoin Rainbow Price Chart", fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                        color = TEXT
                    )
                    Text(
                        if (greek) RainbowModel.FORMULA_NOTE_EL else RainbowModel.FORMULA_NOTE_EN,
                        fontSize = 11.sp, color = FAINT
                    )
                }
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
                    drawRainbow(series, insights, markers, xMin, xMax, yRange, scrubDay, reveal.value, greek, paints)
                }
                // Ξεχωριστό layer για τον παλμό ώστε να μην ξαναζωγραφίζονται οι ζώνες σε κάθε frame.
                if (reveal.value >= 1f) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawLivePulse(series.last(), xMin, xMax, yRange, pulse)
                    }
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
                fontSize = 11.sp, color = FAINT, modifier = Modifier.padding(top = 6.dp)
            )
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(CRASH, CircleShape))
                Text(
                    if (greek) " πτώση/κρίση   " else " crash/crisis   ",
                    fontSize = 11.sp, color = FAINT
                )
                Box(Modifier.size(7.dp).background(ACCENT, CircleShape))
                Text(if (greek) " γεγονός   " else " event   ", fontSize = 11.sp, color = FAINT)
                Box(Modifier.size(7.dp).background(Color(0xFFFACC15), CircleShape))
                Text(if (greek) " κορυφή/πάτος κύκλου" else " cycle top/bottom", fontSize = 11.sp, color = FAINT)
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = TEXT)
    }
}

// ─── Markers (γεγονότα + κορυφές/πάτοι) ──────────────────────────────────────
private class Marker(val day: Long, val price: Double, val color: Color, val el: String, val en: String)

private fun buildMarkers(insights: List<CycleInsights.Insight>, series: List<PricePoint>): List<Marker> {
    val out = mutableListOf<Marker>()
    RainbowEvents.ALL.forEach { ev ->
        val p = series.priceNear(ev.day) ?: return@forEach
        val c = if (ev.kind == RainbowEvents.Kind.CRASH) CRASH else ACCENT
        out += Marker(ev.day, p, c, ev.el, ev.en)
    }
    insights.forEach { ins ->
        listOfNotNull(ins.high, ins.low).forEach { x ->
            val (el, en) = CycleInsights.extremeLabel(x, ins.halving)
            out += Marker(x.point.day, x.point.price, Color(0xFFFACC15), el, en)
        }
    }
    return out.sortedBy { it.day }
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
    val axis = Paint().apply { isAntiAlias = true; color = 0xFF9CA3AF.toInt() }
    val label = Paint().apply {
        isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
        setShadowLayer(4f, 0f, 1f, 0xCC000000.toInt())
    }
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
        drawCircle(bandCol.copy(alpha = 0.6f * (1 - pulse)), (6 + 18 * pulse).dp.toPx(), Offset(x, y))
        val p2 = (pulse + 0.5f) % 1f
        drawCircle(bandCol.copy(alpha = 0.4f * (1 - p2)), (6 + 18 * p2).dp.toPx(), Offset(x, y), style = Stroke(1.5.dp.toPx()))
        drawCircle(bandCol, 6.dp.toPx(), Offset(x, y))
        drawCircle(LINE, 6.dp.toPx(), Offset(x, y), style = Stroke(2.5.dp.toPx()))
    }
}

private fun shorten(s: String, max: Int) = if (s.length <= max) s else s.take(max - 1).trimEnd() + "…"

private fun DrawScope.drawRainbow(
    series: List<PricePoint>, insights: List<CycleInsights.Insight>, markers: List<Marker>,
    xMin: Double, xMax: Double, yRange: Pair<Double, Double>,
    scrubDay: Long?, reveal: Float, greek: Boolean, p: ChartPaints,
) {
    val f = Frame(this, xMin, xMax, yRange.first, yRange.second)
    val (lo, hi) = yRange
    val l = f.l; val r = f.r; val t = f.t; val b = f.b
    val native = drawContext.canvas.nativeCanvas
    p.axis.textSize = 10.sp.toPx()

    drawRoundRect(CARD_BG_2, Offset(l, t), Size(f.w, f.h), CornerRadius(10.dp.toPx()))

    clipRect(l, t, r, b) {
        // 9 ζώνες με τα επίσημα χρώματα
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
            drawPath(path, Color(RainbowModel.BANDS[i].color).copy(alpha = 0.88f))
        }

        // Grid τιμών (δυνάμεις του 10) και ετών
        for (e in floor(lo).toInt()..ceil(hi).toInt()) {
            val y = f.y(10.0.pow(e))
            drawLine(Color(0x22FFFFFF), Offset(l, y), Offset(r, y), 1.dp.toPx())
        }
        val y0 = DateUtil.civil(xMin.toLong()).first
        val y1 = DateUtil.civil(xMax.toLong()).first
        for (yr in y0..y1 + 1) {
            val x = f.x(DateUtil.epochDay(yr, 1, 1).toDouble())
            drawLine(Color(0x14FFFFFF), Offset(x, t), Offset(x, b), 1.dp.toPx())
        }

        // Halvings: συνεχείς για τα πραγματικά, κόκκινες διακεκομμένες για τα εκτιμώμενα
        p.label.textSize = 10.sp.toPx()
        for (hv in RainbowModel.HALVINGS) {
            val x = f.x(hv.day.toDouble())
            if (x < l - 2 || x > r + 2) continue
            if (hv.estimated) {
                drawLine(
                    Color(0xE6EF4444), Offset(x, t), Offset(x, b), 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            } else {
                drawLine(Color(0xB3FFFFFF), Offset(x, t), Offset(x, b), 1.5.dp.toPx())
            }
            p.label.color = if (hv.estimated) 0xFFFCA5A5.toInt() else 0xFFFFFFFF.toInt()
            val txt = if (hv.estimated) "Halving ${hv.year} (Est)" else "Halving ${hv.year}"
            native.save()
            native.rotate(-90f, x - 4.dp.toPx(), b - 6.dp.toPx())
            native.drawText(txt, x - 4.dp.toPx(), b - 6.dp.toPx(), p.label)
            native.restore()
        }

        // Γραμμή τιμής με glow (ζωγραφίζεται σταδιακά με το reveal)
        val visible = series.filter { it.day >= xMin - 2 && it.day <= xMax + 2 }
        val revealDay = series.first().day + (series.last().day - series.first().day) * reveal
        if (visible.size > 1) {
            val path = Path()
            val stride = max(1, visible.size / max(1, (f.w / 1.2f).toInt()))
            var started = false
            visible.forEachIndexed { i, pt ->
                if (pt.day > revealDay) return@forEachIndexed
                if (i % stride != 0 && i != visible.lastIndex) return@forEachIndexed
                val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
                if (!started) { path.moveTo(x, y); started = true } else path.lineTo(x, y)
            }
            drawPath(path, Color(0x80FFFFFF), style = Stroke(5.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
            drawPath(path, Color(0xFF0B0B12), style = Stroke(2.4.dp.toPx(), join = StrokeJoin.Round, cap = StrokeCap.Round))
        }

        if (reveal >= 1f) {
            // Γεγονότα / κορυφές / πάτοι πάνω στη γραμμή
            markers.forEach { m ->
                val x = f.x(m.day.toDouble())
                if (x < l || x > r) return@forEach
                val y = f.y(m.price)
                drawCircle(Color(0xFF0B0B12), 4.5.dp.toPx(), Offset(x, y))
                drawCircle(m.color, 3.dp.toPx(), Offset(x, y))
            }

            // Ίδια μέρα σε κάθε προηγούμενο κύκλο: dot στη γραμμή + διαγώνια κάτω-δεξιά σε ετικέτα έτους
            p.label.textSize = 14.sp.toPx()
            p.label.color = 0xFFFFFFFF.toInt()
            insights.filter { !it.isCurrent }.forEach { c ->
                val price = c.price ?: return@forEach
                val x = f.x(c.day.toDouble()); val y = f.y(price)
                if (x < l || x > r) return@forEach
                val ex = x + 56.dp.toPx(); val ey = y + 52.dp.toPx()
                drawLine(LINE, Offset(x, y), Offset(ex, ey), 1.5.dp.toPx())
                drawCircle(LINE, 4.5.dp.toPx(), Offset(x, y))
                drawCircle(Color(0xFF0B0B12), 2.dp.toPx(), Offset(x, y))
                drawCircle(Color(c.halving.color), 6.dp.toPx(), Offset(ex, ey))
                drawCircle(LINE, 6.dp.toPx(), Offset(ex, ey), style = Stroke(1.5.dp.toPx()))
                drawRoundRect(
                    Color(0xCC0B0B12), Offset(ex + 9.dp.toPx(), ey - 10.dp.toPx()),
                    Size(p.label.measureText("${c.halving.year}") + 10.dp.toPx(), 20.dp.toPx()),
                    CornerRadius(6.dp.toPx())
                )
                native.drawText("${c.halving.year}", ex + 14.dp.toPx(), ey + 5.dp.toPx(), p.label)
            }
        }

        // Crosshair + tooltip
        scrubDay?.let { sd ->
            val pt = series.nearest(sd) ?: return@let
            if (abs(pt.day - sd) > 5) return@let
            val x = f.x(pt.day.toDouble()); val y = f.y(pt.price)
            drawLine(Color(0xB3FFFFFF), Offset(x, t), Offset(x, b), 1.dp.toPx())
            drawLine(Color(0x66FFFFFF), Offset(l, y), Offset(r, y), 1.dp.toPx())
            drawCircle(Color(0xFF0B0B12), 6.dp.toPx(), Offset(x, y))
            drawCircle(LINE, 6.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))

            val bandI = RainbowModel.bandIndex(pt.day, pt.price)
            val hv = RainbowModel.cycleOf(pt.day)
            val dayLine = when {
                hv == null && greek -> "Πριν το 1ο halving"
                hv == null -> "Before the 1st halving"
                greek -> "Μέρα ${pt.day - hv.day} μετά το halving (${hv.year})"
                else -> "Day ${pt.day - hv.day} after the halving (${hv.year})"
            }
            val near = markers.filter { abs(it.day - pt.day) <= 3 }.minByOrNull { abs(it.day - pt.day) }
            val lines = buildList<Pair<String, Pair<Float, Int>>> {
                add(DateUtil.format(pt.day, greek) to (13.sp.toPx() to 0xFFD1D5DB.toInt()))
                add(usd(pt.price) to (22.sp.toPx() to 0xFFFFFFFF.toInt()))
                add(RainbowModel.BANDS[bandI].name to (14.sp.toPx() to RainbowModel.BANDS[bandI].color.toInt()))
                add(dayLine to (11.sp.toPx() to 0xFF9CA3AF.toInt()))
                near?.let {
                    add(shorten(if (greek) it.el else it.en, 40) to (11.sp.toPx() to it.color.toArgb()))
                }
            }
            val gap = 6.dp.toPx()
            val boxW = min(
                f.w - 20.dp.toPx(),
                lines.maxOf { (s, st) -> p.tip.apply { textSize = st.first }.measureText(s) } + 28.dp.toPx()
            )
            val boxH = lines.sumOf { (it.second.first + gap).toDouble() }.toFloat() + 18.dp.toPx()
            val bx = if (x > l + f.w / 2) l + 10.dp.toPx() else r - boxW - 10.dp.toPx()
            val by = t + 10.dp.toPx()
            drawRoundRect(Color(0xF0181B2E), Offset(bx, by), Size(boxW, boxH), CornerRadius(16.dp.toPx()))
            drawRoundRect(Color(0x33FFFFFF), Offset(bx, by), Size(boxW, boxH), CornerRadius(16.dp.toPx()), style = Stroke(1.dp.toPx()))
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

// ─── Κάρτα "τώρα": ζώνη, τιμή, μέρα, κανόνας ενημέρωσης ───────────────────────
@Composable
private fun RainbowNowCard(
    s: RainbowUiState, series: List<PricePoint>, today: Long, cur: CycleInsights.Insight?, priceIsLive: Boolean, greek: Boolean,
) {
    val last = series.last()
    val band = RainbowModel.BANDS[RainbowModel.bandIndex(last.day, last.price)]
    val hv = RainbowModel.currentHalving(today)
    val n = RainbowModel.daysSinceHalving(today)

    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                if (greek) "ΣΗΜΕΡΑ · ΜΕΡΑ $n ΜΕΤΑ ΤΟ HALVING ${hv.year}" else "TODAY · DAY $n AFTER THE ${hv.year} HALVING",
                fontSize = 11.sp, color = MUTED, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(band.name, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(band.color))
            Text(
                usd(last.price) + "  ·  " + DateUtil.format(last.day, greek),
                fontSize = 18.sp, fontFamily = FontFamily.Monospace, color = TEXT
            )
            cur?.let { c -> CycleInsights.summary(c, greek).forEach { Text("• $it", fontSize = 13.sp, color = MUTED) } }
            cur?.nearby?.take(3)?.forEach { NearbyRow(it, greek) }

            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = DIVIDER)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(if (priceIsLive && s.live != null) Color(0xFF10B981) else FAINT, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    when {
                        priceIsLive && s.live != null && greek -> "Live τώρα: ${usd(s.live)}"
                        priceIsLive && s.live != null -> "Live now: ${usd(s.live)}"
                        greek -> "Η ζωντανή τιμή λείπει"
                        else -> "Live price missing"
                    },
                    fontSize = 13.sp, color = TEXT, fontFamily = FontFamily.Monospace
                )
            }
            Text(
                if (greek) "Το rainbow ενημερώνεται μία φορά τη μέρα, ή αμέσως σε κίνηση ±7% ή αλλαγή ζώνης."
                else "The rainbow updates once a day, or immediately on a ±7% move or a band change.",
                fontSize = 11.sp, color = FAINT, modifier = Modifier.padding(top = 4.dp)
            )
            if (s.shownReason == LiveGate.Reason.BIG_MOVE || s.shownReason == LiveGate.Reason.BAND_CHANGE) {
                Text(
                    when {
                        s.shownReason == LiveGate.Reason.BIG_MOVE && greek -> "⚡ Μεγάλη κίνηση: το rainbow ενημερώθηκε τώρα"
                        s.shownReason == LiveGate.Reason.BIG_MOVE -> "⚡ Big move: the rainbow just updated"
                        greek -> "⚡ Αλλαγή ζώνης: το rainbow ενημερώθηκε τώρα"
                        else -> "⚡ Band change: the rainbow just updated"
                    },
                    fontSize = 12.sp, color = Color(0xFFFACC15), modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (s.offline) {
                Text(
                    if (greek) "Χωρίς σύνδεση: εμφανίζονται τα αποθηκευμένα δεδομένα."
                    else "Offline: showing saved data.",
                    fontSize = 11.sp, color = CRASH, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// ─── Κάρτα ανά προηγούμενο κύκλο ─────────────────────────────────────────────
@Composable
private fun CycleInsightCard(i: CycleInsights.Insight, greek: Boolean) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CARD_BG)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(Color(i.halving.color), CircleShape))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (greek) "Κύκλος ${i.halving.year} · μέρα ${i.n}" else "${i.halving.year} cycle · day ${i.n}",
                        color = TEXT, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                    Text(DateUtil.format(i.day, greek), color = MUTED, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        i.price?.let(::usd) ?: "—", color = TEXT,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    i.band?.let {
                        Text(
                            RainbowModel.BANDS[it].name, color = Color(RainbowModel.BANDS[it].color),
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            val lines = CycleInsights.summary(i, greek)
            if (lines.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                lines.forEach { Text("• $it", fontSize = 13.sp, color = MUTED) }
            }
            if (i.nearby.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    if (greek) "Κοντά σε αυτή τη μέρα (±${CycleInsights.NEAR_DAYS} μέρες)"
                    else "Near this day (±${CycleInsights.NEAR_DAYS} days)",
                    fontSize = 12.sp, color = FAINT
                )
                i.nearby.forEach { NearbyRow(it, greek) }
            } else {
                Text(
                    if (greek) "Κανένα καταγεγραμμένο γεγονός μέσα σε ±${CycleInsights.NEAR_DAYS} μέρες."
                    else "No recorded event within ±${CycleInsights.NEAR_DAYS} days.",
                    fontSize = 12.sp, color = FAINT, modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NearbyRow(nb: CycleInsights.Nearby, greek: Boolean) {
    val color = when (nb.kind) {
        RainbowEvents.Kind.CRASH -> CRASH
        null -> Color(0xFFFACC15)
        else -> ACCENT
    }
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 5.dp).size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                CycleInsights.offsetText(nb.offset, greek) + " · " + DateUtil.format(nb.day, greek),
                fontSize = 11.sp, color = FAINT
            )
            Text(nb.text(greek), fontSize = 13.sp, color = TEXT, lineHeight = 18.sp)
        }
    }
}
