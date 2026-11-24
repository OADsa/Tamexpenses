package com.tamexpenses.app

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val dayFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

fun dayLabel(day: Long): String {
    val t = today()
    return when (day) {
        t -> "TODAY"
        t - 1 -> "YESTERDAY"
        else -> LocalDate.ofEpochDay(day).format(dayFmt).uppercase()
    }
}

fun insight(s: AppState): String {
    val exp = s.monthTxns(s.month, false)
    if (exp.isEmpty()) return "TAP + TO LOG YOUR FIRST EXPENSE!"
    for (b in s.budgets) {
        if (s.spentIn(b.category, s.month) > b.limit) return "OVER BUDGET: ${b.category.uppercase()}!"
    }
    val top = exp.groupBy { it.category }.maxByOrNull { e -> e.value.sumOf { it.amount } } ?: return "KEEP TRACKING!"
    val pct = (top.value.sumOf { it.amount } / exp.sumOf { it.amount } * 100).roundToInt()
    return "${top.key.uppercase()} IS $pct% OF YOUR SPENDING"
}

// ---------------------------------------------------------------------------
// HOME
// ---------------------------------------------------------------------------

@Composable
fun HeroCard(s: AppState) {
    val pal = LocalPal.current
    val bal = s.fmt(s.balance())
    val week = s.weekNet()
    val weekText = (if (week >= 0) "+" else "-") + s.fmt0(kotlin.math.abs(week)).removePrefix("-") + " THIS WEEK"
    Box(Modifier.fillMaxWidth().pixelBox(Tam.HeroGreen, pal.border, pal.shadow, 4.dp)) {
        Skyline(Modifier.matchParentSize().padding(4.dp), Tam.GreenDark.copy(alpha = 0.55f))
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).pixelBox(Tam.Gold, pal.border, null, 2.dp).padding(2.dp)) {
                    Mascot(Modifier.fillMaxSize())
                }
                Spacer(Modifier.weight(1f))
                MonthPicker(s.month) { s.month = it }
            }
            Spacer(Modifier.height(14.dp))
            PText("CURRENT BALANCE", size = 9, pixel = true, color = Color.White, modifier = Modifier.fillMaxWidth(), align = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            PText(
                bal,
                Modifier.fillMaxWidth(),
                size = (400f / bal.length).coerceIn(28f, 54f).toInt(),
                color = Color.White,
                align = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.pixelBox(Tam.Gold, pal.border, null, 2.dp).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    PText(weekText, size = 20, color = Tam.Ink)
                }
            }
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
fun MoneyCard(label: String, amount: String, income: Boolean, modifier: Modifier = Modifier) {
    val pal = LocalPal.current
    PixelCard(modifier) {
        CatTile(if (income) "Allowance" else "Food", income, 40.dp)
        Spacer(Modifier.height(8.dp))
        PText(label, size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(4.dp))
        PText(amount, size = 28, color = if (income) posColor(pal) else Tam.Red, maxLines = 1)
    }
}

@Composable
fun TxnRow(s: AppState, t: Txn, onDelete: (() -> Unit)? = null) {
    val pal = LocalPal.current
    PixelCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CatTile(t.category, t.income)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                PText(t.title, size = 24, maxLines = 1)
                PText(t.category.uppercase(), size = 18, color = pal.sub)
            }
            PText(
                (if (t.income) "+" else "-") + s.fmt(t.amount),
                size = 24,
                color = if (t.income) posColor(pal) else Tam.Red,
                maxLines = 1
            )
            if (onDelete != null) {
                Spacer(Modifier.width(10.dp))
                PixelButton("X", fill = Tam.Red, textColor = Color.White, size = 8, onClick = onDelete)
            }
        }
    }
}

@Composable
fun DayHeader(s: AppState, day: Long, list: List<Txn>) {
    val pal = LocalPal.current
    val net = list.sumOf { if (it.income) it.amount else -it.amount }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        PText(dayLabel(day), Modifier.weight(1f), size = 8, pixel = true, color = pal.sub)
        PText("NET " + s.fmt0(net), size = 20, color = pal.sub)
    }
}

@Composable
fun HomeScreen(s: AppState, go: (String) -> Unit) {
    val pal = LocalPal.current
    val ym = s.month
    val inc = s.sumMonth(ym, true)
    val exp = s.sumMonth(ym, false)
    val groups = s.monthTxns(ym).sortedByDescending { it.day }.groupBy { it.day }.toList().take(3)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { HeroCard(s) }
        item { SectionHeader("YOUR MONEY") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MoneyCard("INCOME", s.fmt(inc), true, Modifier.weight(1f))
                MoneyCard("EXPENSES", s.fmt(exp), false, Modifier.weight(1f))
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth()
                    .pixelBox(Tam.Deep, pal.border, Tam.Green)
                    .tap { go("report") }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mascot(Modifier.size(34.dp))
                Spacer(Modifier.width(10.dp))
                PText("TAM SAYS: " + insight(s), Modifier.weight(1f), size = 20, color = Tam.Gold)
                PText(">", size = 12, pixel = true, color = Tam.Gold)
            }
        }
        item { SectionHeader("TRANSACTIONS", "VIEW ALL") { go("txns") } }
        if (groups.isEmpty()) {
            item { EmptyBox("No transactions yet", "Tap the gold + button to log your first coin.") }
        } else {
            groups.forEach { (day, list) ->
                item { DayHeader(s, day, list) }
                items(list, key = { it.id }) { t -> TxnRow(s, t) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ALL TRANSACTIONS
// ---------------------------------------------------------------------------

@Composable
fun TxnsScreen(s: AppState, back: () -> Unit) {
    val ctx = LocalContext.current
    val groups = s.monthTxns(s.month).sortedByDescending { it.day }.groupBy { it.day }.toList()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("TRANSACTIONS", back) }
        item { MonthPicker(s.month) { s.month = it } }
        if (groups.isEmpty()) {
            item { EmptyBox("Nothing here", "No transactions in this month.") }
        } else {
            groups.forEach { (day, list) ->
                item { DayHeader(s, day, list) }
                items(list, key = { it.id }) { t ->
                    TxnRow(s, t) {
                        s.removeTxn(t.id)
                        Toast.makeText(ctx, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// REPORT
// ---------------------------------------------------------------------------

@Composable
fun ReportScreen(s: AppState) {
    val pal = LocalPal.current
    var tab by remember { mutableStateOf(0) }
    val income = tab == 1
    val ym = s.month
    val list = s.monthTxns(ym, income)
    val prev = s.monthTxns(ym.minusMonths(1), income)
    val total = list.sumOf { it.amount }
    val cats = if (income) INCOME_CATS else EXPENSE_CATS
    val sums = cats.map { c -> c to list.filter { it.category == c }.sumOf { it.amount } }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
    val totalText = s.fmt(total)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PText("REPORT", Modifier.weight(1f), size = 14, pixel = true)
                MonthPicker(ym) { s.month = it }
            }
        }
        item { PixelTabs(listOf("EXPENSES", "INCOME"), tab) { tab = it } }
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                    PixelDonut(
                        sums.map { it.second.toFloat() to catColor(it.first, income) },
                        empty = pal.track, outline = pal.border,
                        modifier = Modifier.fillMaxSize()
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PText(if (income) "TOTAL INCOME" else "TOTAL EXPENSES", size = 8, pixel = true, color = pal.sub)
                        Spacer(Modifier.height(4.dp))
                        PText(totalText, size = (190f / totalText.length * 1.6f).coerceIn(22f, 34f).toInt(), maxLines = 1)
                    }
                }
            }
        }
        item { SectionHeader(if (income) "ALL INCOME" else "ALL EXPENSES") }
        if (sums.isEmpty()) {
            item { EmptyBox("No data yet", "Add some ${if (income) "income" else "expenses"} this month to fill the chart.") }
        }
        items(sums, key = { it.first }) { (cat, amt) ->
            val before = prev.filter { it.category == cat }.sumOf { it.amount }
            val share = (amt / total).toFloat()
            PixelCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CatTile(cat, income)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        PText(cat.uppercase(), size = 10, pixel = true)
                        PText("${(share * 100).roundToInt()}% OF TOTAL", size = 19, color = pal.sub)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        PText(s.fmt(amt), size = 26, maxLines = 1)
                        if (before > 0) {
                            val d = ((amt - before) / before * 100).roundToInt()
                            val good = if (income) d >= 0 else d <= 0
                            Box(Modifier.pixelBox(if (good) Tam.Green else Tam.Red, pal.border, null, 2.dp).padding(horizontal = 6.dp, vertical = 1.dp)) {
                                PText((if (d >= 0) "+" else "") + d + "% VS LAST MO", size = 16, color = Color.White)
                            }
                        } else {
                            PText("NEW", size = 18, color = pal.sub)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                PixelBar(share, catColor(cat, income))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ADD TRANSACTION
// ---------------------------------------------------------------------------

@Composable
fun AddScreen(s: AppState, back: () -> Unit, done: () -> Unit) {
    val pal = LocalPal.current
    val ctx = LocalContext.current
    var type by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("Food") }
    var daysAgo by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf("") }
    val income = type == 1

    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(18.dp)
    ) {
        ScreenTitle(if (income) "NEW INCOME" else "NEW EXPENSE", back)
        PixelTabs(listOf("EXPENSE", "INCOME"), type) {
            type = it
            cat = if (it == 1) "Allowance" else "Food"
        }
        Spacer(Modifier.height(16.dp))
        PText("WHAT IS IT?", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(6.dp))
        PixelField(title, { title = it }, if (income) "e.g. Weekly allowance" else "e.g. Canteen lunch")
        Spacer(Modifier.height(14.dp))
        PText("AMOUNT (${s.symbol()})", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(6.dp))
        PixelField(amount, { amount = it }, "0.00", keyboard = KeyboardType.Decimal)
        Spacer(Modifier.height(14.dp))
        PText("CATEGORY", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(8.dp))
        CatPicker(if (income) INCOME_CATS else EXPENSE_CATS, income, cat) { cat = it }
        Spacer(Modifier.height(8.dp))
        PText("WHEN?", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceChip("TODAY", daysAgo == 0, Modifier.weight(1f)) { daysAgo = 0 }
            ChoiceChip("YESTERDAY", daysAgo == 1, Modifier.weight(1f)) { daysAgo = 1 }
            ChoiceChip("2 DAYS AGO", daysAgo == 2, Modifier.weight(1f)) { daysAgo = 2 }
        }
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PText(error, size = 21, color = Tam.Red)
        }
        Spacer(Modifier.height(20.dp))
        PixelButton("SAVE", Modifier.fillMaxWidth(), size = 12) {
            val v = amount.replace(",", "").toDoubleOrNull()
            if (title.isBlank() || v == null || v <= 0) {
                error = "Enter a name and an amount above 0."
            } else {
                s.addTxn(title.trim(), v, cat, income, today() - daysAgo)
                Toast.makeText(ctx, if (income) "Income saved!" else "Expense saved!", Toast.LENGTH_SHORT).show()
                done()
            }
        }
    }
}
