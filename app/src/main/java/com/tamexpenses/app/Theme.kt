package com.tamexpenses.app

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/** Palette pulled from the green bull mascot. */
object Tam {
    val Green = Color(0xFF2FB36D)
    val HeroGreen = Color(0xFF1F9D5B)
    val GreenDark = Color(0xFF0F5C3A)
    val Deep = Color(0xFF0B2A1F)
    val Gold = Color(0xFFF9BE2B)
    val GoldDark = Color(0xFFD58A12)
    val Hair = Color(0xFFF6DC93)
    val Ink = Color(0xFF10231D)
    val Red = Color(0xFFE8465A)
    val Orange = Color(0xFFFF7A2F)
    val Blue = Color(0xFF3AA0FF)
    val Purple = Color(0xFF8E6AD8)
    val Pink = Color(0xFFF0647A)
}

class Pal(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val sub: Color,
    val border: Color,
    val shadow: Color,
    val track: Color,
    val dark: Boolean
)

val LightPal = Pal(
    bg = Color(0xFFFFF6D6), surface = Color.White, text = Tam.Ink, sub = Color(0xFF5E7A6E),
    border = Tam.Ink, shadow = Tam.Ink, track = Color(0xFFE3E8D3), dark = false
)
val DarkPal = Pal(
    bg = Color(0xFF0B2A1F), surface = Color(0xFF14402F), text = Color(0xFFFFF6D6), sub = Color(0xFF9CC4B3),
    border = Color(0xFF031510), shadow = Color(0xFF031510), track = Color(0xFF0B2A1F), dark = true
)

val LocalPal = staticCompositionLocalOf { LightPal }

val PixelFont = FontFamily(Font(R.font.press_start_2p))
val BodyFont = FontFamily(Font(R.font.vt323))

/** pixel = true -> Press Start 2P (headings, numbers). Otherwise VT323 (body text). */
@Composable
fun PText(
    text: String,
    modifier: Modifier = Modifier,
    size: Int = 20,
    color: Color = LocalPal.current.text,
    pixel: Boolean = false,
    align: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size.sp,
            fontFamily = if (pixel) PixelFont else BodyFont,
            lineHeight = (size * (if (pixel) 1.5f else 1.05f)).sp,
            textAlign = align
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

fun posColor(p: Pal): Color = if (p.dark) Tam.Green else Tam.GreenDark

fun catColor(cat: String, income: Boolean): Color = when (cat) {
    "Food" -> Tam.Gold
    "Transport" -> Tam.Blue
    "School" -> Tam.Green
    "Fun" -> Tam.Pink
    "Allowance" -> Tam.Green
    "Job" -> Tam.Gold
    "Gift" -> Tam.Pink
    else -> if (income) Tam.Blue else Tam.Purple
}

val EXPENSE_CATS = listOf("Food", "Transport", "School", "Fun", "Other")
val INCOME_CATS = listOf("Allowance", "Job", "Gift", "Other")
