package com.tamexpenses.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.YearMonth
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Core look: chunky border + notched corners + hard drop shadow
// ---------------------------------------------------------------------------

private fun DrawScope.notch(c: Color, x: Float, y: Float, w: Float, h: Float, n: Float) {
    drawRect(c, Offset(x + n, y), Size(w - 2 * n, h))
    drawRect(c, Offset(x, y + n), Size(w, h - 2 * n))
}

/** Reserves [px] at the end/bottom for the shadow, then draws border, fill and shadow. */
fun Modifier.pixelBox(fill: Color, border: Color, shadow: Color? = border, px: Dp = 3.dp): Modifier =
    this.padding(end = px, bottom = px).drawBehind {
        val p = px.toPx()
        val w = size.width
        val h = size.height
        if (shadow != null) notch(shadow, p, p, w, h, p)
        notch(border, 0f, 0f, w, h, p)
        notch(fill, p, p, w - 2 * p, h - 2 * p, p)
    }

@Composable
fun Modifier.tap(onClick: () -> Unit): Modifier =
    this.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)

@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    fill: Color = LocalPal.current.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val pal = LocalPal.current
    var m = modifier.pixelBox(fill, pal.border, pal.shadow)
    if (onClick != null) m = m.tap(onClick)
    Column(m.padding(12.dp), content = content)
}

@Composable
fun PixelButton(
    text: String,
    modifier: Modifier = Modifier,
    fill: Color = Tam.Gold,
    textColor: Color = Tam.Ink,
    size: Int = 9,
    onClick: () -> Unit
) {
    val pal = LocalPal.current
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val d = if (pressed) 3.dp else 0.dp
    Box(
        modifier
            .offset(x = d, y = d)
            .pixelBox(fill, pal.border, if (pressed) null else pal.shadow)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        PText(text, size = size, color = textColor, pixel = true, align = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------
// Sprites
// ---------------------------------------------------------------------------

@Composable
fun Sprite(rows: List<String>, palette: Map<Char, Color>, modifier: Modifier = Modifier, outline: Color? = null) {
    Canvas(modifier) {
        val pad = if (outline != null) 1 else 0
        val c0 = rows.maxOf { it.length }
        val cols = c0 + pad * 2
        val rws = rows.size + pad * 2
        val cell = minOf(size.width / cols, size.height / rws)
        val ox = (size.width - cell * cols) / 2f
        val oy = (size.height - cell * rws) / 2f
        fun filled(r: Int, c: Int): Boolean =
            r in rows.indices && c in 0 until rows[r].length && palette.containsKey(rows[r][c])
        for (r in -pad until rows.size + pad) {
            for (c in -pad until c0 + pad) {
                val color: Color? = if (filled(r, c)) palette[rows[r][c]]
                else if (outline != null && (filled(r - 1, c) || filled(r + 1, c) || filled(r, c - 1) || filled(r, c + 1))) outline
                else null
                if (color != null) {
                    drawRect(color, Offset(ox + (c + pad) * cell, oy + (r + pad) * cell), Size(cell + 0.6f, cell + 0.6f))
                }
            }
        }
    }
}

@Composable
fun Mascot(modifier: Modifier = Modifier) {
    Sprite(Sprites.mascot, Sprites.mascotPal, modifier, Tam.Deep)
}

@Composable
fun CatIcon(cat: String, income: Boolean, modifier: Modifier = Modifier) {
    val color = catColor(cat, income)
    val art: Pair<List<String>, Map<Char, Color>> = when (cat) {
        "Food" -> Sprites.burger to Sprites.burgerPal
        "Transport" -> Sprites.bus to mapOf('X' to color, 'w' to Color.White, 'K' to Tam.Ink)
        "School" -> Sprites.book to mapOf('X' to color, 'w' to Color.White)
        "Fun" -> Sprites.star to mapOf('Y' to Tam.Gold)
        "Job" -> Sprites.case to Sprites.casePal
        "Gift" -> Sprites.gift to Sprites.giftPal
        else -> Sprites.coin to Sprites.coinPal
    }
    Sprite(art.first, art.second, modifier, Tam.Ink)
}

@Composable
fun CatTile(cat: String, income: Boolean, size: Dp = 46.dp) {
    val pal = LocalPal.current
    val tint = lerp(pal.surface, catColor(cat, income), 0.35f)
    Box(Modifier.size(size).pixelBox(tint, pal.border, null, 2.dp).padding(5.dp)) {
        CatIcon(cat, income, Modifier.fillMaxSize())
    }
}

@Composable
fun NavIcon(rows: List<String>, color: Color, cut: Color, modifier: Modifier = Modifier) {
    Sprite(rows, mapOf('X' to color, 'o' to cut), modifier)
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

@Composable
fun PixelBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 16.dp, segments: Int = 20) {
    val pal = LocalPal.current
    Canvas(modifier.fillMaxWidth().height(height)) {
        val p = 2.dp.toPx()
        val gap = 1.5.dp.toPx()
        drawRect(pal.border, Offset.Zero, size)
        val sw = (size.width - 2 * p) / segments
        var n = (fraction.coerceIn(0f, 1f) * segments).roundToInt()
        if (fraction > 0f && n == 0) n = 1
        for (i in 0 until segments) {
            drawRect(
                if (i < n) color else pal.track,
                Offset(p + i * sw, p),
                Size(sw - gap, size.height - 2 * p)
            )
        }
    }
}

@Composable
fun PixelTabs(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val pal = LocalPal.current
    Row(modifier.fillMaxWidth().pixelBox(pal.track, pal.border, null).padding(4.dp)) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f)
                    .background(if (on) Tam.Gold else Color.Transparent)
                    .tap { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                PText(label, size = 9, pixel = true, color = if (on) Tam.Ink else pal.sub)
            }
        }
    }
}

@Composable
fun PixelField(
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    val pal = LocalPal.current
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(color = pal.text, fontSize = 24.sp, fontFamily = BodyFont),
        cursorBrush = SolidColor(Tam.Green),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth()
                    .pixelBox(pal.surface, pal.border, null, 3.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (value.isEmpty()) PText(hint, size = 24, color = pal.sub)
                inner()
            }
        }
    )
}

@Composable
fun ChoiceChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pal = LocalPal.current
    Box(
        modifier
            .pixelBox(if (selected) Tam.Gold else pal.surface, pal.border, if (selected) pal.shadow else null)
            .tap(onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        PText(label, size = 19, color = if (selected) Tam.Ink else pal.text, align = TextAlign.Center)
    }
}

@Composable
fun CatChip(cat: String, income: Boolean, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val pal = LocalPal.current
    Row(
        modifier
            .pixelBox(if (selected) Tam.Gold else pal.surface, pal.border, if (selected) pal.shadow else null)
            .tap(onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatIcon(cat, income, Modifier.size(24.dp))
        Spacer(Modifier.width(8.dp))
        PText(cat.uppercase(), size = 20, color = if (selected) Tam.Ink else pal.text)
    }
}

@Composable
fun CatPicker(cats: List<String>, income: Boolean, selected: String, onSelect: (String) -> Unit) {
    cats.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { c -> CatChip(c, income, c == selected, Modifier.weight(1f)) { onSelect(c) } }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun PixelToggle(on: Boolean, onToggle: () -> Unit) {
    val pal = LocalPal.current
    Row(
        Modifier.width(64.dp)
            .pixelBox(if (on) Tam.Green else pal.track, pal.border, null)
            .tap(onToggle)
            .padding(3.dp)
    ) {
        if (on) Spacer(Modifier.weight(1f))
        Box(Modifier.size(22.dp).background(Tam.Gold))
        if (!on) Spacer(Modifier.weight(1f))
    }
}

fun monthLabel(ym: YearMonth): String =
    ym.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH).uppercase() + " " + ym.year

@Composable
fun MonthPicker(ym: YearMonth, onChange: (YearMonth) -> Unit) {
    val pal = LocalPal.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        PixelButton("<", size = 9, onClick = { onChange(ym.minusMonths(1)) })
        Box(Modifier.pixelBox(pal.surface, pal.border, pal.shadow).padding(horizontal = 10.dp, vertical = 10.dp)) {
            PText(monthLabel(ym), size = 9, pixel = true)
        }
        PixelButton(">", size = 9, onClick = { onChange(ym.plusMonths(1)) })
    }
}

@Composable
fun ScreenTitle(title: String, back: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) {
            PixelButton("<", onClick = back)
            Spacer(Modifier.width(10.dp))
        }
        PText(title, Modifier.weight(1f), size = 14, pixel = true, maxLines = 1)
        if (trailing != null) trailing()
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PText(title, Modifier.weight(1f), size = 11, pixel = true)
        if (action != null) PixelButton(action, size = 8, onClick = onAction)
    }
}

@Composable
fun EmptyBox(title: String, message: String) {
    PixelCard(Modifier.fillMaxWidth(), fill = Tam.Hair) {
        PText(title.uppercase(), size = 10, pixel = true, color = Tam.Ink)
        Spacer(Modifier.height(6.dp))
        PText(message, size = 21, color = Tam.Ink)
    }
}

@Composable
fun PixelDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val pal = LocalPal.current
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.pixelBox(pal.surface, pal.border, pal.shadow, 4.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PText(title, Modifier.weight(1f), size = 11, pixel = true)
                PixelButton("X", fill = Tam.Red, textColor = Color.White, onClick = onDismiss)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Charts / decoration
// ---------------------------------------------------------------------------

/** Mosaic donut: every cell is one big pixel, coloured by which slice it falls in. */
@Composable
fun PixelDonut(slices: List<Pair<Float, Color>>, empty: Color, outline: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val n = 28
        val cs = size.minDimension / n
        val total = slices.sumOf { it.first.toDouble() }.toFloat()
        val twoPi = (2 * PI).toFloat()
        val rOut = n / 2f
        val rIn = rOut * 0.56f
        for (gy in 0 until n) {
            for (gx in 0 until n) {
                val dx = gx + 0.5f - n / 2f
                val dy = gy + 0.5f - n / 2f
                val d = sqrt(dx * dx + dy * dy)
                if (d > rOut || d < rIn) continue
                var color = empty
                if (total > 0f) {
                    var a = atan2(dy, dx) + (PI / 2).toFloat()
                    if (a < 0f) a += twoPi
                    val target = a / twoPi * total
                    var acc = 0f
                    for (s in slices) {
                        acc += s.first
                        if (target <= acc) { color = s.second; break }
                    }
                }
                if (d > rOut - 1f || d < rIn + 1f) color = lerp(color, outline, 0.35f)
                drawRect(color, Offset(gx * cs, gy * cs), Size(cs + 0.5f, cs + 0.5f))
            }
        }
    }
}

/** Little campus skyline for the hero card. */
@Composable
fun Skyline(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        val heights = listOf(3, 5, 4, 7, 5, 3, 6, 8, 5, 4)
        val count = 40
        val u = size.width / count
        for (i in 0 until count) {
            val h = heights[i % heights.size] * u * 0.7f
            drawRect(color, Offset(i * u, size.height - h), Size(u + 0.5f, h))
        }
    }
}
