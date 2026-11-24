# Tamexpenses (8-bit edition)

Expense tracker in Kotlin + Jetpack Compose with a pixel-art look themed on the green bull mascot
(green + gold palette, Press Start 2P / VT323 fonts, hand-built pixel sprites, no image assets).

## Screens
- Login ("PRESS START")
- Home: balance hero card with skyline, income / expenses, mascot tip bar, grouped transactions
- Report: pixel-mosaic donut chart, Expenses / Income tabs, % of total and change vs last month
- Plan: savings goals (behind-schedule banner, quick +amount buttons) and monthly budgets linked to real spending
- Add: expense or income, category, date
- Transactions list (delete), Calendar heat map, Subscriptions, Settings (dark mode, PHP/USD, CSV export, demo data, reset)

Data is saved on the phone (SharedPreferences). Use Settings > DEMO DATA > LOAD to fill it with sample entries.

## Open in Android Studio
Open this folder, let Gradle sync (needs JDK 17, Android SDK 35). Edit sdk.dir in local.properties if your SDK path differs.

Fonts: Press Start 2P and VT323 are SIL Open Font License (see FONT-LICENSE-PressStart2P.txt).
