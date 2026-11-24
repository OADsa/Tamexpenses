package com.tamexpenses.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val state = AppState(getSharedPreferences("tamexpenses", MODE_PRIVATE))
        setContent { TamexpensesApp(state) }
    }
}

private val TABS = listOf("home", "report", "plan", "settings")

@Composable
fun TamexpensesApp(s: AppState) {
    val pal = if (s.dark) DarkPal else LightPal
    CompositionLocalProvider(LocalPal provides pal) {
        Box(Modifier.fillMaxSize().background(pal.bg).systemBarsPadding()) {
            if (!s.loggedIn) LoginScreen(s) else MainShell(s)
        }
    }
}

@Composable
fun MainShell(s: AppState) {
    var screen by remember { mutableStateOf("home") }
    var previous by remember { mutableStateOf("home") }
    fun go(to: String) {
        if (to != screen) { previous = screen; screen = to }
    }

    BackHandler(enabled = screen != "home") {
        screen = if (screen in TABS) "home" else previous
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (screen) {
                "home" -> HomeScreen(s) { go(it) }
                "report" -> ReportScreen(s)
                "plan" -> PlanScreen(s)
                "settings" -> SettingsScreen(s) { go(it) }
                "add" -> AddScreen(s, back = { screen = previous }, done = { screen = "home" })
                "txns" -> TxnsScreen(s) { screen = "home" }
                "calendar" -> CalendarScreen(s) { screen = "settings" }
                "subs" -> SubsScreen(s) { screen = "settings" }
            }
        }
        if (screen in TABS) BottomBar(screen) { go(it) }
    }
}

@Composable
fun NavItem(label: String, rows: List<String>, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val pal = LocalPal.current
    val color = if (selected) (if (pal.dark) Tam.Gold else Tam.GreenDark) else pal.sub
    Column(
        modifier.tap(onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavIcon(rows, color, pal.surface, Modifier.size(26.dp))
        PText(label, size = 18, color = color)
        Spacer(Modifier.height(2.dp))
        Box(Modifier.size(width = 22.dp, height = 3.dp).background(if (selected) Tam.Gold else Color.Transparent))
    }
}

@Composable
fun BottomBar(current: String, go: (String) -> Unit) {
    val pal = LocalPal.current
    Row(
        Modifier.fillMaxWidth()
            .background(pal.surface)
            .drawBehind { drawRect(pal.border, Offset.Zero, Size(size.width, 3.dp.toPx())) }
            .padding(top = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("Home", Sprites.home, current == "home", Modifier.weight(1f)) { go("home") }
        NavItem("Report", Sprites.chart, current == "report", Modifier.weight(1f)) { go("report") }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(54.dp)
                    .pixelBox(Tam.Gold, pal.border, pal.shadow, 3.dp)
                    .tap { go("add") }
                    .padding(11.dp)
            ) { NavIcon(Sprites.plus, Tam.Ink, Tam.Gold, Modifier.fillMaxSize()) }
        }
        NavItem("Plan", Sprites.flag, current == "plan", Modifier.weight(1f)) { go("plan") }
        NavItem("Settings", Sprites.gear, current == "settings", Modifier.weight(1f)) { go("settings") }
    }
}
