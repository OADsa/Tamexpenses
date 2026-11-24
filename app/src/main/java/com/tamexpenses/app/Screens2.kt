package com.tamexpenses.app

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// LOGIN
// ---------------------------------------------------------------------------

@Composable
fun LoginScreen(s: AppState) {
    val pal = LocalPal.current
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(170.dp).pixelBox(Tam.Gold, pal.border, pal.shadow, 4.dp).padding(10.dp)) {
            Mascot(Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(20.dp))
        PText("TAMEXPENSES", size = 18, pixel = true, color = posColor(pal))
        Spacer(Modifier.height(6.dp))
        PText("YOUR MONEY QUEST STARTS HERE", size = 22, color = pal.sub)
        Spacer(Modifier.height(24.dp))
        PixelField(email, { email = it }, "Email or username", keyboard = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        PixelField(password, { password = it }, "Password", password = true)
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            PText(error, size = 21, color = Tam.Red)
        }
        Spacer(Modifier.height(20.dp))
        PixelButton("PRESS START", Modifier.fillMaxWidth(), size = 12) {
            if (email.isBlank() || password.length < 4) {
                error = "Enter an email and a password with 4+ characters."
            } else {
                Toast.makeText(ctx, "Welcome to Tamexpenses!", Toast.LENGTH_SHORT).show()
                s.login(email.substringBefore("@"))
            }
        }
        Spacer(Modifier.height(12.dp))
        PText("Demo login: any email + 4 character password.", size = 19, color = pal.sub, align = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------
// PLAN (goals + budgets)
// ---------------------------------------------------------------------------

@Composable
fun GoalDialog(s: AppState, onDismiss: () -> Unit) {
    val pal = LocalPal.current
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var months by remember { mutableStateOf(3) }
    var error by remember { mutableStateOf("") }
    PixelDialog("NEW GOAL", onDismiss) {
        PixelField(title, { title = it }, "Goal name")
        Spacer(Modifier.height(10.dp))
        PixelField(target, { target = it }, "Target amount (${s.symbol()})", keyboard = KeyboardType.Decimal)
        Spacer(Modifier.height(12.dp))
        PText("REACH IT IN", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 3, 6, 12).forEach { m ->
                ChoiceChip(if (m == 12) "1 YR" else "$m MO", months == m, Modifier.weight(1f)) { months = m }
            }
        }
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PText(error, size = 20, color = Tam.Red)
        }
        Spacer(Modifier.height(16.dp))
        PixelButton("CREATE", Modifier.fillMaxWidth()) {
            val v = target.replace(",", "").toDoubleOrNull()
            if (title.isBlank() || v == null || v <= 0) {
                error = "Add a name and a target above 0."
            } else {
                s.addGoal(title.trim(), v, months)
                onDismiss()
            }
        }
    }
}

@Composable
fun BudgetDialog(s: AppState, startCat: String?, startLimit: Double?, onDismiss: () -> Unit) {
    val pal = LocalPal.current
    var cat by remember { mutableStateOf(startCat ?: "Food") }
    var limit by remember { mutableStateOf(startLimit?.let { String.format(Locale.US, "%.0f", it) } ?: "") }
    var error by remember { mutableStateOf("") }
    PixelDialog(if (startCat == null) "NEW BUDGET" else "EDIT BUDGET", onDismiss) {
        PText("CATEGORY", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(8.dp))
        if (startCat == null) {
            CatPicker(EXPENSE_CATS, false, cat) { cat = it }
        } else {
            CatChip(startCat, false, true) {}
        }
        Spacer(Modifier.height(8.dp))
        PText("MONTHLY LIMIT (${s.symbol()})", size = 9, pixel = true, color = pal.sub)
        Spacer(Modifier.height(8.dp))
        PixelField(limit, { limit = it }, "0", keyboard = KeyboardType.Decimal)
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PText(error, size = 20, color = Tam.Red)
        }
        Spacer(Modifier.height(16.dp))
        PixelButton("SAVE", Modifier.fillMaxWidth()) {
            val v = limit.replace(",", "").toDoubleOrNull()
            if (v == null || v <= 0) {
                error = "Enter a limit above 0."
            } else {
                s.setBudget(cat, v)
                onDismiss()
            }
        }
    }
}

@Composable
fun GoalCard(s: AppState, g: Goal) {
    val pal = LocalPal.current
    val progress = (g.saved / g.target).toFloat()
    val total = (g.endDay - g.startDay).coerceAtLeast(1L)
    val elapsed = (today() - g.startDay).coerceIn(0L, total)
    val expected = elapsed.toFloat() / total
    val behind = ((expected - progress) * 100).roundToInt()
    val done = g.saved >= g.target
    val dueText = LocalDate.ofEpochDay(g.endDay).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)).uppercase()

    PixelCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CatTile("Gift", true)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                PText(g.title.uppercase(), size = 10, pixel = true, maxLines = 1)
                PText("DUE $dueText", size = 18, color = pal.sub)
            }
            PixelButton("X", fill = Tam.Red, textColor = Color.White, size = 8) { s.removeGoal(g.id) }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            PText(s.fmt(g.saved), size = 32, maxLines = 1)
            Spacer(Modifier.width(8.dp))
            PText("OF " + s.fmt0(g.target), size = 20, color = pal.sub)
        }
        Spacer(Modifier.height(8.dp))
        PixelBar(progress, Tam.Green, height = 18.dp)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            PText("YOUR PROGRESS ${(progress * 100).roundToInt()}%", Modifier.weight(1f), size = 19, color = pal.sub)
            PText(s.fmt0(g.target - g.saved) + " LEFT", size = 19)
        }
        Spacer(Modifier.height(8.dp))
        val msg: String
        val bannerColor: Color
        if (done) {
            msg = "GOAL COMPLETE! LEVEL UP!"; bannerColor = Tam.Green
        } else if (behind >= 5) {
            msg = "YOU'RE $behind% BEHIND SCHEDULE!"; bannerColor = Tam.Orange
        } else {
            msg = "ON TRACK. KEEP GOING!"; bannerColor = Tam.Green
        }
        Box(
            Modifier.fillMaxWidth().pixelBox(bannerColor, pal.border, null, 2.dp).padding(horizontal = 10.dp, vertical = 6.dp)
        ) { PText(msg, size = 20, color = Color.White) }
        if (!done) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100.0, 500.0, 1000.0).forEach { amt ->
                    PixelButton("+" + String.format(Locale.US, "%,.0f", amt), Modifier.weight(1f), size = 9) {
                        s.contribute(g.id, amt)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetCard(s: AppState, b: Budget, onEdit: () -> Unit) {
    val pal = LocalPal.current
    val spent = s.spentIn(b.category, s.month)
    val ratio = (spent / b.limit).toFloat()
    val badge = when {
        ratio >= 1f -> Tam.Red
        ratio >= 0.8f -> Tam.Gold
        else -> Tam.Green
    }
    PixelCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CatTile(b.category, false)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                PText(b.category.uppercase(), size = 10, pixel = true)
                PText(s.fmt0(spent) + " of " + s.fmt0(b.limit), size = 20, color = pal.sub)
            }
            Box(Modifier.pixelBox(badge, pal.border, null, 2.dp).padding(horizontal = 8.dp, vertical = 6.dp)) {
                PText("${(ratio * 100).roundToInt()}%", size = 9, pixel = true, color = if (badge == Tam.Gold) Tam.Ink else Color.White)
            }
        }
        Spacer(Modifier.height(10.dp))
        PixelBar(ratio, badge)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PixelButton("EDIT", size = 8, onClick = onEdit)
            PixelButton("DEL", fill = Tam.Red, textColor = Color.White, size = 8) { s.removeBudget(b.category) }
        }
    }
}

@Composable
fun PlanScreen(s: AppState) {
    var showGoal by remember { mutableStateOf(false) }
    var budgetDialog by remember { mutableStateOf(false) }
    var editCat by remember { mutableStateOf<String?>(null) }
    var editLimit by remember { mutableStateOf<Double?>(null) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PText("MY PLAN", Modifier.weight(1f), size = 14, pixel = true)
                MonthPicker(s.month) { s.month = it }
            }
        }
        item { SectionHeader("GOALS", "+ NEW") { showGoal = true } }
        if (s.goals.isEmpty()) item { EmptyBox("No goals yet", "Pick something to save for and start stacking coins.") }
        items(s.goals, key = { it.id }) { g -> GoalCard(s, g) }
        item { SectionHeader("BUDGETS", "+ NEW") { editCat = null; editLimit = null; budgetDialog = true } }
        if (s.budgets.isEmpty()) item { EmptyBox("No budgets yet", "Set a monthly limit for a category to keep spending in check.") }
        items(s.budgets, key = { it.category }) { b ->
            BudgetCard(s, b) { editCat = b.category; editLimit = b.limit; budgetDialog = true }
        }
    }
    if (showGoal) GoalDialog(s) { showGoal = false }
    if (budgetDialog) BudgetDialog(s, editCat, editLimit) { budgetDialog = false }
}

// ---------------------------------------------------------------------------
// CALENDAR
// ---------------------------------------------------------------------------

@Composable
fun CalendarScreen(s: AppState, back: () -> Unit) {
    val pal = LocalPal.current
    var selected by remember { mutableStateOf<Long?>(null) }
    val ym = s.month
    val offset = ym.atDay(1).dayOfWeek.value % 7
    val days = ym.lengthOfMonth()
    val spend = s.monthTxns(ym, false).groupBy { it.day }.mapValues { e -> e.value.sumOf { it.amount } }
    val maxSpend = spend.values.maxOrNull() ?: 1.0
    val rows = (offset + days + 6) / 7

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
        ScreenTitle("CALENDAR", back)
        MonthPicker(ym) { s.month = it; selected = null }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                PText(it, Modifier.weight(1f), size = 9, pixel = true, color = pal.sub, align = TextAlign.Center)
            }
        }
        Spacer(Modifier.height(8.dp))
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val dn = r * 7 + c - offset + 1
                    if (dn in 1..days) {
                        val epoch = ym.atDay(dn).toEpochDay()
                        val amt = spend[epoch]
                        val fill = if (amt == null) pal.surface
                        else lerp(pal.surface, Tam.Red, (0.25f + 0.6f * (amt / maxSpend).toFloat()))
                        val isSel = selected == epoch
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                .pixelBox(fill, if (isSel) Tam.Gold else pal.border, null, 2.dp)
                                .tap { selected = epoch },
                            contentAlignment = Alignment.Center
                        ) { PText("$dn", size = 20, color = if (amt != null && !pal.dark) Tam.Ink else pal.text) }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        val sel = selected
        if (sel == null) {
            PText("Tap a day to see what you spent.", size = 21, color = pal.sub)
        } else {
            val list = s.txns.filter { it.day == sel }
            PText(dayLabel(sel), size = 9, pixel = true, color = pal.sub)
            Spacer(Modifier.height(8.dp))
            if (list.isEmpty()) EmptyBox("Quiet day", "No transactions on this day.")
            list.forEach { t ->
                TxnRow(s, t)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SUBSCRIPTIONS
// ---------------------------------------------------------------------------

@Composable
fun SubsScreen(s: AppState, back: () -> Unit) {
    val pal = LocalPal.current
    var show by remember { mutableStateOf(false) }
    val monthly = s.subs.filter { it.active }.sumOf { it.amount }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("SUBSCRIPTIONS", back) }
        item {
            PixelCard(Modifier.fillMaxWidth(), fill = Tam.Hair) {
                PText("ACTIVE PER MONTH", size = 9, pixel = true, color = Tam.Ink)
                Spacer(Modifier.height(4.dp))
                PText(s.fmt(monthly), size = 34, color = Tam.Ink)
            }
        }
        item { SectionHeader("YOUR SUBS", "+ NEW") { show = true } }
        if (s.subs.isEmpty()) item { EmptyBox("No subscriptions", "Add recurring payments so renewals never surprise you.") }
        items(s.subs, key = { it.id }) { sub ->
            PixelCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CatTile("Fun", false)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        PText(sub.name, size = 24, maxLines = 1)
                        PText(s.fmt(sub.amount) + " / MONTH" + if (sub.active) "" else "  (PAUSED)", size = 18, color = pal.sub)
                    }
                    PixelToggle(sub.active) { s.toggleSub(sub.id) }
                    Spacer(Modifier.width(10.dp))
                    PixelButton("X", fill = Tam.Red, textColor = Color.White, size = 8) { s.removeSub(sub.id) }
                }
            }
        }
    }
    if (show) {
        var name by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var err by remember { mutableStateOf("") }
        PixelDialog("NEW SUBSCRIPTION", { show = false }) {
            PixelField(name, { name = it }, "Name")
            Spacer(Modifier.height(10.dp))
            PixelField(amount, { amount = it }, "Monthly cost (${s.symbol()})", keyboard = KeyboardType.Decimal)
            if (err.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                PText(err, size = 20, color = Tam.Red)
            }
            Spacer(Modifier.height(16.dp))
            PixelButton("ADD", Modifier.fillMaxWidth()) {
                val v = amount.replace(",", "").toDoubleOrNull()
                if (name.isBlank() || v == null || v <= 0) err = "Add a name and a cost above 0."
                else { s.addSub(name.trim(), v); show = false }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// SETTINGS
// ---------------------------------------------------------------------------

@Composable
fun SettingRow(title: String, sub: String, trailing: @Composable () -> Unit) {
    val pal = LocalPal.current
    PixelCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                PText(title, size = 10, pixel = true)
                PText(sub, size = 19, color = pal.sub)
            }
            trailing()
        }
    }
}

@Composable
fun SettingsScreen(s: AppState, go: (String) -> Unit) {
    val ctx = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenTitle("SETTINGS")
        SettingRow("PLAYER", s.userName.ifBlank { "Friend" }) {
            Box(Modifier.size(44.dp)) { Mascot(Modifier.fillMaxSize()) }
        }
        SettingRow("DARK MODE", if (s.dark) "Night shift on" else "Off") {
            PixelToggle(s.dark) { s.dark = !s.dark; s.save() }
        }
        SettingRow("CURRENCY", if (s.currency == "PHP") "Philippine peso" else "US dollar") {
            PixelButton(if (s.currency == "PHP") "PHP" else "USD", size = 9) {
                s.currency = if (s.currency == "PHP") "USD" else "PHP"; s.save()
            }
        }
        SettingRow("CALENDAR", "Spending heat map") { PixelButton(">", size = 9) { go("calendar") } }
        SettingRow("SUBSCRIPTIONS", "Recurring payments") { PixelButton(">", size = 9) { go("subs") } }
        SettingRow("EXPORT", "Share your data as CSV") {
            PixelButton("SEND", size = 9) {
                val csv = buildString {
                    append("date,type,category,title,amount\n")
                    s.txns.sortedBy { it.day }.forEach { t ->
                        val type = if (t.income) "income" else "expense"
                        val safe = t.title.replace("\"", "\"\"")
                        append("${LocalDate.ofEpochDay(t.day)},$type,${t.category},\"$safe\",${t.amount}\n")
                    }
                }
                val send = Intent(Intent.ACTION_SEND).apply {
                    this.type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Tamexpenses export")
                    putExtra(Intent.EXTRA_TEXT, csv)
                }
                ctx.startActivity(Intent.createChooser(send, "Export Tamexpenses"))
            }
        }
        SettingRow("DEMO DATA", "Fill the app with sample coins") {
            PixelButton("LOAD", size = 9) {
                s.loadDemo()
                Toast.makeText(ctx, "Demo data loaded", Toast.LENGTH_SHORT).show()
            }
        }
        SettingRow("RESET", "Erase everything") {
            PixelButton("WIPE", fill = Tam.Red, textColor = Color.White, size = 9) { confirmClear = true }
        }
        PixelButton("LOG OUT", Modifier.fillMaxWidth(), size = 11) { s.logout() }
    }

    if (confirmClear) {
        PixelDialog("ERASE ALL?", { confirmClear = false }) {
            PText("This deletes every transaction, budget, goal and subscription on this phone.", size = 22)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelButton("CANCEL", Modifier.weight(1f), size = 9) { confirmClear = false }
                PixelButton("ERASE", Modifier.weight(1f), fill = Tam.Red, textColor = Color.White, size = 9) {
                    s.clearAll(); confirmClear = false
                }
            }
        }
    }
}
