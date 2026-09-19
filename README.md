# 🕹️ Tic-Tac-Toe 3D (Full 3D Kinetic Edition)

Настоящая трёхмерная игра **«Крестики-Нолики»** для Android на **Jetpack Compose** и **Kotlin 2.0**, совмещающая полигональный 3D-движок, интерактивную орбитальную камеру, эстетику **Anti-AI-Slop** и компоненты **Magic UI**.

---

## 🔮 3D Игровой движок и механики
- **True 3D Geometry**: Каждая фигура X и O представлена 3D полигональным мешем с расчётом нормалей граней и динамическим направленным освещением (Lambertian Shading).
- **Интерактивное вращение в 3D (Orbit Drag)**: Проводите пальцем в любой точке поля, чтобы свободно вращать 3D доску в трёхмерном пространстве с кинематической пружинной инерцией.
- **3D тактильные постаменты**: Каждая ячейка — это 3D блок с фасками, физически утапливающийся вглубь по оси Z при касании.
- **3D парящий лазер**: Объёмный неоновый луч парит в трёхмерном пространстве над победной тройкой.
- **Magic UI Компоненты**: Border Beam по периметру активного хода, Shimmer Button с кинетическим световым бликом, Bento Grid для статистики.
- **Процедурный звук**: Алгоритмическая генерация звуков без сторонних тяжелых файлов.

---

## 📥 Скачать APK
Готовый файл приложения `.apk` доступен во вкладке **[Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases)**.

1. Перейдите в [Releases](https://github.com/maksimcvetkov888-crypto/tic-tac-toe-android/releases).
2. Скачайте `tic-tac-toe.apk`.
3. Установите на ваше Android-устройство.

---

## 🛠 Технологический стек
- **Язык**: Kotlin 2.0.0
- **3D Engine**: Software Polygon Mesh Engine на Compose Canvas (Painter's Algorithm, Lambert Shading, 4x4 Perspective Matrix)
- **UI Toolkit**: Jetpack Compose + Material 3 (BOM 2024.06.00)
- **Компоненты**: Magic UI (Border Beam, Shimmer Button, Bento Grid)
- **MCP Сервер**: `@magicuidesign/mcp`
- **Архитектура**: Clean MVI (Model-View-Intent)
- **Android SDK**: minSdk 24 (Android 7.0+), targetSdk 35 (Android 15)
- **CI/CD**: GitHub Actions автоматическая сборка и релизы
