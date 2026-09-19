# 🎮 Tic-Tac-Toe (Крестики-Нолики для Android)

Современная, стильная нативная игра **«Крестики-Нолики»** для двух игроков на одном мобильном устройстве (Hot-Seat), переписанная на **100% Jetpack Compose** и **Kotlin 2.0** по стандартам Clean MVI.

---

## 📱 Особенности и возможности
- **Режим на двоих**: Играйте по очереди на одном экране смартфона или планшета.
- **100% Jetpack Compose & Material 3**: Полностью декларативный UI с поддержкой **Edge-to-Edge (Android 15 / API 35)**.
- **MVI Архитектура**: Однонаправленный поток данных (UDF), `StateFlow` и иммутабельные состояния экрана.
- **Праздничные эффекты (Confetti Canvas)**: Анимированный салют из разноцветных частиц при победе.
- **Пружинные анимации**: Плавное появление символов X и O с физикой отскока (`spring`).
- **Процедурный синтез звука**: Звуки ходов, победы и сброса генерируются алгоритмически без лишних файлов в APK.
- **Тактильная отдача**: Адаптивный виброотклик через современный `VibratorManager` API.
- **Управление звуком и вибрацией**: Переключатели в шапке экрана (🔊/🔇 и 📳/📴).
- **Счётчик матчей**: Подсчёт побед X, O и ничьих со сбросом.

---

## 📥 Скачать APK
Готовый файл приложения `.apk` доступен во вкладке **[Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases)**.

1. Перейдите в [Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases).
2. Скачайте `tic-tac-toe.apk`.
3. Откройте файл на Android-устройстве и подтвердите установку.

---

## 🛠 Технологический стек
- **Язык**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose (BOM 2024.06.00) + Material 3
- **Архитектура**: Clean Architecture + MVI (Model-View-Intent)
- **Компилятор Compose**: Kotlin Compose Compiler Plugin 2.0.0
- **Android SDK**: minSdk 24 (Android 7.0+), targetSdk 35 (Android 15)
- **CI/CD**: GitHub Actions автоматическая сборка и публикация релизов

---

## 💻 Сборка из исходников
```bash
gradle assembleRelease
```
Собранный APK появится в папке `app/build/outputs/apk/release/`.
