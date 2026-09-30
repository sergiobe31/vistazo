# Vistazo

## Qué hace

- **Cuenta tus desbloqueos del día** y tus **vistazos** (desbloqueos que duran
  menos del umbral configurado, 60 s por defecto, ajustable en Ajustes).
- **Historial** de los últimos 7 días, disponible tras 7 días de uso.
- **Barra de estado** (opcional): contador persistente "Hoy: N desbloqueos ·
  M vistazos" para ver el número sin abrir la app; además mejora la precisión
  del conteo en Xiaomi, Huawei y similares.
- **Reporte diario** (opcional, hora configurable): una notificación al día
  con el conteo, que abre el juego **"¿Cuántas crees?"**: estimas cuántas
  veces desbloqueaste el móvil y la app te muestra la cifra real, sin juicios.

Sin cuenta, sin registro, gratis. **Sin permisos de red**: la app no declara
`INTERNET` ni pide ningún permiso de conexión. Los datos nunca salen del
teléfono; se guardan en una base de datos local (Room/SQLite). Sin analíticas,
sin publicidad, sin rastreo.

## Cómo funciona

Android emite dos broadcasts del sistema que la app escucha:

- `ACTION_USER_PRESENT` → acabas de desbloquear el móvil (se abre una "sesión").
- `ACTION_SCREEN_OFF` → apagas la pantalla (se cierra la sesión; si duró menos
  de 60 s, esa sesión se cuenta como **vistazo**).

Estos broadcasts son *protegidos*: no pueden recibirse con un receiver declarado
en el manifiesto, solo registrados en runtime mientras el proceso vive. Por eso
la arquitectura es híbrida:

1. **Por defecto**: la app registra los receivers en runtime y usa un receiver
   de manifiesto para `BOOT_COMPLETED` que los vuelve a registrar tras cada
   reinicio. En Android tipo Pixel el conteo es casi perfecto; en Xiaomi,
   Huawei u Oppo el sistema puede matar el proceso y perderse algunos eventos.
2. **"Mejorar precisión"** (opcional, en ajustes): un servicio en primer plano
   de baja prioridad que mantiene el proceso vivo. Es la única vez que la app
   pide un permiso (notificaciones, en Android 13+).

## Compilar

Requisitos: JDK 17+, Android SDK (compileSdk 36) y Gradle 8.13 (el wrapper se
genera en el repo; hasta entonces, usa un Gradle instalado localmente).

- **Android Studio**: *Open* en la carpeta del repo → *Run* ▶. O
- **Línea de comandos** (una vez generado el wrapper):

  ```sh
  ./gradlew assembleDebug
  ```

  El APK queda en `app/build/outputs/apk/debug/`.

## Mejorar la app

El código está pensado para ser legible por una persona con conocimientos de
Android que quiera toquetearlo. Algunos puntos de entrada:

- Umbral del vistazo (60 s por defecto): `data/VistazoClassifier.kt` y el
  slider en `ui/SettingsScreen.kt`.
- Lógica de eventos de pantalla: `receiver/ScreenEventsReceiver.kt`.
- Persistencia y agregados por día: `data/`.
- Pantallas: `ui/HomeScreen.kt` (hoy), `ui/HistoryScreen.kt` (histórico),
  `ui/SettingsScreen.kt` (ajustes), `ui/EstimateScreen.kt` ("¿cuántas crees?").
- Reporte diario: `worker/DailyReportWorker.kt` + `DailyReportScheduler.kt`
  (WorkManager, sin alarmas exactas).
- Widget: `widget/CounterWidget.kt` (Jetpack Glance).

Si mejoras algo, abre un PR o un issue en GitHub.

## Licencia

[GPLv3](LICENSE) © 2026 Sergio (sergiobe31). Software libre: puedes usarlo,
estudiarlo, modificarlo y redistribuirlo bajo los términos de la licencia.

## Créditos

- Tipografía **JetBrains Mono**, de JetBrains, empaquetada tal cual del
  repositorio oficial (github.com/JetBrains/JetBrainsMono). Publicada bajo la
  [SIL Open Font License 1.1](https://openfontlicense.org): ver
  `docs/licenses/OFL-jetbrainsmono.txt`.
