# Estado del desarrollo

Al 2026-09-30: **v1.0 publicada** (incluye todo lo de la v1.1). Repo público
en github.com/sergiobe31/vistazo, APK firmado en GitHub Releases (tag v1.0),
web en GitHub Pages (sergiobe31.github.io/vistazo, servida desde /docs).
Release firmado con keystore local (`~/vistazo-release.keystore`,
credenciales en `~/.vistazo-signing.properties`, fuera del repo — **hacer
backup del keystore**: sin él no se pueden publicar actualizaciones
instalables encima).

## Qué existe ya

- Proyecto Gradle completo: version catalog, AGP 8.13.2, Kotlin 2.2.10,
  Gradle 8.13 (wrapper; el Gradle 9.8 de Homebrew NO sirve: AGP 8.13.2 es
  incompatible con Gradle ≥9.6), minSdk 26, target/compileSdk 36.
  `./gradlew assembleDebug` compila limpio.
- Manifiesto sin `INTERNET`; permisos: `POST_NOTIFICATIONS`,
  `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE[_SPECIAL_USE]`.
- Capa de datos Room **v2** (migración 1→2 manual): `Session`, nueva tabla
  `DailyEstimate` (estimación del día), repositorio con Flows de hoy, últimos
  7 días y estimación de hoy.
- Receivers dinámicos + `BootReceiver`; umbral de vistazo con cache en memoria
  (`VistazoClassifier.thresholdMs`, se refresca al mover el slider).
- **Features v1.1 (2026-09-30)**:
  - Historial visible solo tras 7 días de uso (`firstUseAt` en
    `VistazoSettings`; antes, texto calm en la home).
  - **Ajustes** (tercera pantalla): reporte diario con hora configurable
    (TimePicker M3, default 22:00), slider de umbral 15–300 s (pasos de 15,
    default 60), toggle de barra de estado (movido desde Historial).
  - Reporte diario con **WorkManager** 2.12.0: PeriodicWorkRequest de 24 h
    anclada a la hora elegida (initialDelay, sin alarmas exactas),
    `ExistingPeriodicWorkPolicy.UPDATE` al cambiarla. Notificación en canal
    propio IMPORTANCE_DEFAULT, silenciable en ajustes del sistema. La hora se
    edita con dos campos numéricos inline HH/MM (el TimePicker M3 se descartó
    por tosco; editor inline compacto con validación en ámbar).
  - **"¿Cuántas crees?"**: pantalla de estimación (input numérico mono grande)
    → revelado calm ("Creíste 40. / Fueron 87. / 34 fueron vistazos."),
    estimación persistida en Room. Accesible desde la home (entrada discreta
    cuando no hay estimación hoy y es de noche o hay datos suficientes) y
    desde la notificación del reporte (deep link por extra en el
    PendingIntent).
  - `VistazoSettings`: un único SharedPreferences (fichero "settings" de v1.0,
    preserva el valor del toggle existente).
- Diseño v2: paleta crema/ámbar fija, JetBrains Mono, widget Glance a juego.
- `README.md`, `LICENSE` (GPLv3), `.gitignore`.
- **Icono v2 "Vistazo de reojo" (2026-09-30, diseño validado por Sergio)**:
  adaptive icon con fondo crema `#FAF3D9` (path full-canvas por bugs OEM),
  almendra en tinta `#1F2937` + pupila descentrada en ámbar `#B45309`
  (encaja en la zona segura Ø66: máx. ~32.8dp del centro), versión
  monochrome para themed icons (Android 13+) e `ic_notification` con el
  mismo motivo en alpha plano. Fuente del diseño:
  `design/icon_preview/vistazo_reojo.svg`.

## Pendiente / siguiente

- [ ] Probar en dispositivo real: WorkManager tras reinicio y con Doze,
      notificación del reporte → pantalla de estimación, migración 1→2 en un
      install real, slider de umbral, entrada "¿cuántas crees?" en home.
- [ ] Onboarding contextual en MIUI/EMUI (instrucciones de autostart/batería).
- [ ] Decidir distribución: APK en GitHub Releases / F-Droid / Play.
- [ ] Exportar APK firmado de release (todavía solo debug).
