# Tamexpenses Handoff Notes

## Current status

- Kotlin + Jetpack Compose Android application
- 8-bit green and gold visual theme
- Pixel mascot and custom Press Start 2P / VT323 fonts
- Login screen with demo validation
- Home, Report, Plan, Settings, Calendar, Subscriptions, Add, and Transactions screens
- Persistent local data using SharedPreferences
- Demo data loader and reset controls
- PHP/USD currency options
- CSV export option
- Debug APK included separately in the project outputs folder

## Login limitations

The current login is a prototype login. It validates the entered fields but does not connect to a real authentication service.

For a production-ready login, add:

1. Registration
2. Password hashing
3. Forgot-password flow
4. Login session management
5. User-specific records
6. Secure authentication through Firebase Auth or a backend API

## Database upgrade recommendation

The current app stores data locally with SharedPreferences. For a stronger final project, migrate the data layer to Room Database.

Recommended Room entities:

- User: id, username, email, passwordHash
- Expense: id, userId, title, amount, category, date, note
- Income: id, userId, source, amount, date
- Budget: id, userId, category, limit
- SavingsGoal: id, userId, title, targetAmount, savedAmount, deadline
- Subscription: id, userId, name, amount, billingDate, isActive
- Category: id, name, icon, color

## Recommended final-project features

1. Add, edit, and delete expenses
2. Add and track income
3. Search and filter transactions
4. Category management
5. Monthly budgets
6. Savings goals
7. Subscription tracking
8. Calendar spending view
9. Reports and charts
10. CSV export
11. Dark mode
12. Multiple currencies
13. Login and user profiles
14. Local database persistence
15. Notifications for budget limits and subscription renewals

## Opening the project

Open the extracted `Tamexpenses` folder in Android Studio. Allow Gradle to sync, then select an emulator and press Run.
