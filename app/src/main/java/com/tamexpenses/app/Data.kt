package com.tamexpenses.app

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.abs

data class Txn(
    val id: Long, val title: String, val amount: Double,
    val category: String, val income: Boolean, val day: Long
)

data class Budget(val category: String, val limit: Double)

data class Goal(
    val id: Long, val title: String, val target: Double, val saved: Double,
    val startDay: Long, val endDay: Long
)

data class Sub(val id: Long, val name: String, val amount: Double, val active: Boolean)

fun today(): Long = LocalDate.now().toEpochDay()
fun ymOf(day: Long): YearMonth = YearMonth.from(LocalDate.ofEpochDay(day))

class AppState(private val prefs: SharedPreferences) {
    var loggedIn by mutableStateOf(false)
    var userName by mutableStateOf("")
    var dark by mutableStateOf(false)
    var currency by mutableStateOf("PHP")
    var month by mutableStateOf(YearMonth.now())

    val txns = mutableStateListOf<Txn>()
    val budgets = mutableStateListOf<Budget>()
    val goals = mutableStateListOf<Goal>()
    val subs = mutableStateListOf<Sub>()

    private var seed = System.currentTimeMillis()
    private fun nid(): Long = seed++

    init { load() }

    // ---------- money helpers ----------
    fun symbol(): String = if (currency == "PHP") "\u20B1" else "\$"

    fun fmt(v: Double): String =
        (if (v < 0) "-" else "") + symbol() + String.format(Locale.US, "%,.2f", abs(v))

    fun fmt0(v: Double): String =
        (if (v < 0) "-" else "") + symbol() + String.format(Locale.US, "%,.0f", abs(v))

    // ---------- queries ----------
    fun monthTxns(ym: YearMonth, income: Boolean? = null): List<Txn> =
        txns.filter { ymOf(it.day) == ym && (income == null || it.income == income) }

    fun sumMonth(ym: YearMonth, income: Boolean): Double =
        monthTxns(ym, income).sumOf { it.amount }

    fun balance(): Double =
        txns.sumOf { if (it.income) it.amount else -it.amount }

    fun weekNet(): Double {
        val from = today() - 6
        return txns.filter { it.day >= from }.sumOf { if (it.income) it.amount else -it.amount }
    }

    fun spentIn(category: String, ym: YearMonth): Double =
        monthTxns(ym, false).filter { it.category == category }.sumOf { it.amount }

    // ---------- actions ----------
    fun addTxn(title: String, amount: Double, cat: String, income: Boolean, day: Long) {
        txns.add(Txn(nid(), title, amount, cat, income, day)); save()
    }

    fun removeTxn(id: Long) { txns.removeAll { it.id == id }; save() }

    fun setBudget(cat: String, limit: Double) {
        val i = budgets.indexOfFirst { it.category == cat }
        if (i >= 0) budgets[i] = Budget(cat, limit) else budgets.add(Budget(cat, limit))
        save()
    }

    fun removeBudget(cat: String) { budgets.removeAll { it.category == cat }; save() }

    fun addGoal(title: String, target: Double, months: Int) {
        val t = today()
        goals.add(Goal(nid(), title, target, 0.0, t, t + months * 30L)); save()
    }

    fun contribute(id: Long, amount: Double) {
        val i = goals.indexOfFirst { it.id == id }
        if (i >= 0) {
            val g = goals[i]
            goals[i] = g.copy(saved = (g.saved + amount).coerceAtMost(g.target))
            save()
        }
    }

    fun removeGoal(id: Long) { goals.removeAll { it.id == id }; save() }

    fun addSub(name: String, amount: Double) { subs.add(Sub(nid(), name, amount, true)); save() }

    fun toggleSub(id: Long) {
        val i = subs.indexOfFirst { it.id == id }
        if (i >= 0) { subs[i] = subs[i].copy(active = !subs[i].active); save() }
    }

    fun removeSub(id: Long) { subs.removeAll { it.id == id }; save() }

    fun login(name: String) { userName = name; loggedIn = true; save() }
    fun logout() { loggedIn = false; save() }

    fun clearAll() {
        txns.clear(); budgets.clear(); goals.clear(); subs.clear(); save()
    }

    fun loadDemo() {
        clearAll()
        val t = today()
        fun e(title: String, amt: Double, cat: String, ago: Long) =
            txns.add(Txn(nid(), title, amt, cat, false, t - ago))
        fun i(title: String, amt: Double, cat: String, ago: Long) =
            txns.add(Txn(nid(), title, amt, cat, true, t - ago))
        i("Weekly allowance", 3500.0, "Allowance", 6)
        i("Part-time shift", 1800.0, "Job", 3)
        i("Birthday money", 1000.0, "Gift", 20)
        e("Canteen lunch", 120.0, "Food", 0)
        e("Milk tea", 95.0, "Food", 1)
        e("Jeepney fare", 40.0, "Transport", 1)
        e("Notebook + pens", 260.0, "School", 2)
        e("Movie night", 450.0, "Fun", 4)
        e("Dinner with blockmates", 380.0, "Food", 5)
        e("Grab ride", 220.0, "Transport", 7)
        e("Printing project", 150.0, "School", 9)
        e("Game top-up", 300.0, "Fun", 12)
        e("Groceries", 640.0, "Food", 14)
        e("Canteen lunch", 110.0, "Food", 33)
        e("Jeepney fare", 35.0, "Transport", 34)
        e("Textbook", 900.0, "School", 36)
        e("Arcade", 200.0, "Fun", 40)
        e("Merienda", 85.0, "Food", 45)
        i("Weekly allowance", 3500.0, "Allowance", 38)
        budgets.add(Budget("Food", 3000.0)); budgets.add(Budget("Transport", 1000.0))
        budgets.add(Budget("School", 1500.0)); budgets.add(Budget("Fun", 500.0))
        goals.add(Goal(nid(), "New Laptop", 40000.0, 12500.0, t - 60, t + 120))
        goals.add(Goal(nid(), "Trip with friends", 6000.0, 4200.0, t - 30, t + 60))
        subs.add(Sub(nid(), "Music streaming", 149.0, true))
        subs.add(Sub(nid(), "Cloud storage", 59.0, true))
        save()
    }

    // ---------- persistence ----------
    fun save() {
        val o = JSONObject()
        o.put("loggedIn", loggedIn)
        o.put("user", userName)
        o.put("dark", dark)
        o.put("currency", currency)
        val ta = JSONArray()
        txns.forEach {
            ta.put(JSONObject().put("id", it.id).put("title", it.title).put("amount", it.amount)
                .put("cat", it.category).put("inc", it.income).put("day", it.day))
        }
        o.put("txns", ta)
        val ba = JSONArray()
        budgets.forEach { ba.put(JSONObject().put("cat", it.category).put("limit", it.limit)) }
        o.put("budgets", ba)
        val ga = JSONArray()
        goals.forEach {
            ga.put(JSONObject().put("id", it.id).put("title", it.title).put("target", it.target)
                .put("saved", it.saved).put("start", it.startDay).put("end", it.endDay))
        }
        o.put("goals", ga)
        val sa = JSONArray()
        subs.forEach {
            sa.put(JSONObject().put("id", it.id).put("name", it.name).put("amount", it.amount).put("active", it.active))
        }
        o.put("subs", sa)
        prefs.edit().putString("state", o.toString()).apply()
    }

    private fun load() {
        val raw = prefs.getString("state", null) ?: return
        try {
            val o = JSONObject(raw)
            loggedIn = o.optBoolean("loggedIn", false)
            userName = o.optString("user", "")
            dark = o.optBoolean("dark", false)
            currency = o.optString("currency", "PHP")
            o.optJSONArray("txns")?.let { a ->
                for (k in 0 until a.length()) {
                    val j = a.getJSONObject(k)
                    txns.add(Txn(j.getLong("id"), j.getString("title"), j.getDouble("amount"),
                        j.getString("cat"), j.getBoolean("inc"), j.getLong("day")))
                }
            }
            o.optJSONArray("budgets")?.let { a ->
                for (k in 0 until a.length()) {
                    val j = a.getJSONObject(k)
                    budgets.add(Budget(j.getString("cat"), j.getDouble("limit")))
                }
            }
            o.optJSONArray("goals")?.let { a ->
                for (k in 0 until a.length()) {
                    val j = a.getJSONObject(k)
                    goals.add(Goal(j.getLong("id"), j.getString("title"), j.getDouble("target"),
                        j.getDouble("saved"), j.getLong("start"), j.getLong("end")))
                }
            }
            o.optJSONArray("subs")?.let { a ->
                for (k in 0 until a.length()) {
                    val j = a.getJSONObject(k)
                    subs.add(Sub(j.getLong("id"), j.getString("name"), j.getDouble("amount"), j.getBoolean("active")))
                }
            }
        } catch (e: Exception) {
            // corrupt save: start fresh
        }
    }
}
