# 🎮 Tic-Tac-Toe (Anti-AI-Slop & Magic UI Edition)

Премиальная нативная игра **«Крестики-Нолики»** для Android на **Jetpack Compose** и **Kotlin 2.0**, созданная по строгим стандартам **Anti-AI-Slop Design**, компонентов **Magic UI** и кинематики **Creative Motion**.

---

## 🎨 Дизайн и UI/UX решения
- **Anti-AI-Slop Palette**: Никаких шаблонных неоновых градиентов или безликих размытых карточек. Цветовая палитра Obsidian Zinc (`#09090B`, `#141417`, `#27272a`) с прецизионными 1px границами (`border-white/10`).
- **Border Beam (Magic UI)**: Кинетический световой луч, непрерывно циркулирующий по периметру карточки текущего хода и победных ячеек (60fps GPU Canvas).
- **Shimmer Button (Magic UI)**: Тактильная кнопка с кинетическим световым бликом и пружинной физикой нажатия.
- **Bento Grid**: Информационная сетка статистики раундов с акцидентной типографикой.
- **Tactile Kinematics (Creative Motion)**: Пружинное вдавливание ячеек (`scale: 0.94f`, `stiffness: 380`) и упругий овершут появления символов X и O.
- **Процедурный звук и виброотклик**: Звуковой движок синтезирует щелчки и победные фанфары на лету через `ToneGenerator` (0 байт лишнего веса APK).

---

## 📥 Скачать APK
Готовый файл приложения `.apk` доступен во вкладке **[Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases)**.

1. Перейдите в [Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases).
2. Скачайте `tic-tac-toe.apk`.
3. Установите на ваше Android-устройство.

---

## 🛠 Технологический стек
- **Язык**: Kotlin 2.0.0
- **UI Toolkit**: Jetpack Compose (BOM 2024.06.00) + Material 3
- **Компоненты**: Magic UI (Border Beam, Shimmer Button, Bento Grid)
- **Архитектура**: Clean Architecture + MVI (Model-View-Intent)
- **Android SDK**: minSdk 24 (Android 7.0+), targetSdk 35 (Android 15) Edge-to-Edge
- **CI/CD**: GitHub Actions автоматическая сборка и публикация релизов
