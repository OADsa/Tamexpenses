package com.tamexpenses.app

import androidx.compose.ui.graphics.Color

/** All art is drawn from these little character grids. '.' = transparent. */
object Sprites {
    val mascot = listOf(
        ".YY..........YY.",
        ".YyY........YyY.",
        "..YyY.HHHH.YyY..",
        "..YyYHHHHHHYyY..",
        "GGGGGGGGGGGGGGGG",
        ".GGKWGGGGGGKWGG.",
        ".GGKKGGGGGGKKGG.",
        ".GGKKGGGGGGKKGG.",
        ".GGGGGYYYYGGGGG.",
        ".GGGGYyYYyYGGGG.",
        ".GGGGYYKKYYGGGG.",
        "..GGGGGPPGGGGG..",
        "....GGGGGGGG....",
        "..WWWWWWWWWWWW..",
        "..WWWWGWWWWWWW..",
        "..WWWWWWWWWWWW..",
        "..DDDDDDDDDDDD.."
    )
    val mascotPal = mapOf(
        'G' to Color(0xFF2FB36D), 'D' to Color(0xFF0F5C3A), 'Y' to Color(0xFFF9BE2B),
        'y' to Color(0xFFD58A12), 'H' to Color(0xFFF6DC93), 'K' to Color(0xFF10231D),
        'W' to Color(0xFFFFFFFF), 'P' to Color(0xFFF0647A)
    )

    // --- nav icons (X = main colour, o = cut-out colour) ---
    val home = listOf(
        "...XX...", "..XXXX..", ".XXXXXX.", "XXXXXXXX",
        ".XXXXXX.", ".XXooXX.", ".XXooXX.", ".XXooXX."
    )
    val chart = listOf(
        "......XX", "......XX", "...XX.XX", "...XX.XX",
        "XX.XX.XX", "XX.XX.XX", "XX.XX.XX", "XXXXXXXX"
    )
    val flag = listOf(
        ".X......", ".XXXXX..", ".XXXXXX.", ".XXXXX..",
        ".X......", ".X......", ".X......", "XXX....."
    )
    val gear = listOf(
        "...XX...", ".X.XX.X.", "..XXXX..", "XXX..XXX",
        "XXX..XXX", "..XXXX..", ".X.XX.X.", "...XX..."
    )
    val plus = listOf(
        "...XX...", "...XX...", "...XX...", "XXXXXXXX",
        "XXXXXXXX", "...XX...", "...XX...", "...XX..."
    )

    // --- category icons ---
    val burger = listOf(
        "..BBBB..", ".BBBBBB.", "BBBBBBBB", "LLLLLLLL",
        "MMMMMMMM", "BBBBBBBB", ".BBBBBB.", "........"
    )
    val burgerPal = mapOf('B' to Tam.Gold, 'L' to Tam.Green, 'M' to Color(0xFF8A4B2A))
    val bus = listOf(
        ".XXXXXX.", "XXXXXXXX", "XwwXXwwX", "XXXXXXXX",
        "XXXXXXXX", "XXXXXXXX", ".KK..KK.", ".KK..KK."
    )
    val book = listOf(
        ".XXXXXX.", "XwwwwwwX", "XwXXXXwX", "XwwwwwwX",
        "XwXXXXwX", "XwwwwwwX", "XXXXXXXX", ".XXXXXX."
    )
    val star = listOf(
        "...YY...", "...YY...", "YYYYYYYY", ".YYYYYY.",
        "..YYYY..", "..YYYY..", ".YY..YY.", ".Y....Y."
    )
    val coin = listOf(
        "..XXXX..", ".XYYYYX.", "XYYYYYYX", "XYYXXYYX",
        "XYYXXYYX", "XYYYYYYX", ".XYYYYX.", "..XXXX.."
    )
    val coinPal = mapOf('X' to Tam.GoldDark, 'Y' to Tam.Gold)
    val case = listOf(
        "..KKKK..", "..K..K..", "XXXXXXXX", "XXXXXXXX",
        "XXXYYXXX", "XXXXXXXX", "XXXXXXXX", "XXXXXXXX"
    )
    val casePal = mapOf('X' to Color(0xFF8A4B2A), 'K' to Tam.Ink, 'Y' to Tam.Gold)
    val gift = listOf(
        "..R..R..", "...RR...", "XXXRRXXX", "XXXRRXXX",
        "RRRRRRRR", "XXXRRXXX", "XXXRRXXX", "XXXRRXXX"
    )
    val giftPal = mapOf('X' to Tam.Gold, 'R' to Tam.Red)
}
