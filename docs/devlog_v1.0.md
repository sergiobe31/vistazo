# Devlog v1.0 — de idea a app publicada (2026-09-30)

Todo en un día, con Sergio a los mandos y Kimi Code como copiloto.

## Lo que se hizo

1. **Idea y viabilidad** (`docs/idea_v1.md`): contador de desbloqueos +
   "vistazos" (<60s) como métrica diferencial. Primera corrección importante:
   `USER_PRESENT`/`SCREEN_OFF` **no** se pueden escuchar desde el manifiesto
   (solo runtime). Review de repos reales (unlock-master, screenlookcount)
   antes de escribir una línea.
2. **Decisiones**: Room (escala sin migraciones), M3 + Calm Technology como
   base de diseño, GPLv3, solo Android (iOS no tiene API de desbloqueos),
   nombre "Vistazo".
3. **Toolchain desde cero** en el Mac: Homebrew → JDK 17 → Android SDK
   cmdline-tools → Gradle wrapper.
4. **Scaffold → build verde** → prueba en móvil real vía WhatsApp.
5. **Iteración de diseño** con feedback de Sergio: v1 genérica → v2
   crema/JetBrains Mono/histórico aparte → selector de hora inline → icono
   "Vistazo de Reojo" (concepto debatido con DeepSeek v4.1 Flash vía PAL MCP).
6. **v1.1**: historial tras 7 días, reporte diario configurable, "¿cuántas
   crees?", umbral de vistazo configurable.
7. **Publicación**: repo público, release firmado v1.0, GitHub Pages.

## Lo aprendido (lo que no está en el código)

- **La arquitectura "cero servicios" era un mito**: los broadcasts de pantalla
  exigen proceso vivo. La respuesta honesta fue el modo híbrido (default sin
  servicio + FGS opcional), no insistir en la premisa original.
- **Gradle 9.8 (Homebrew) no aplica AGP 8.13.2** (API interna eliminada en
  9.6): todo build va por `./gradlew` (pin 8.13). El wrapper se generó en un
  dir temporal porque la tarea `wrapper` evalúa el build y fallaba.
- **openjdk@17 es keg-only**: sdkmanager no encuentra Java sin PATH explícito
  (`/opt/homebrew/opt/openjdk@17/bin`).
- **Glance**: `TextStyle.color` quiere `ColorProvider`, no `Color`; no admite
  fuentes custom (el widget lleva estilo por jerarquía y color, no por Mono).
- **lifecycle-viewmodel 2.9.1**: no existe `createApplication()` en la
  factory DSL; patrón correcto: `this[AndroidViewModelFactory.APPLICATION_KEY]`.
- **Kotlin DSL**: dentro del bloque `android {}`, `java` no es el paquete —
  importar `java.util.Properties` arriba.
- **Room AutoMigration** necesita esquemas exportados; para una tabla nueva,
  migración manual de 3 líneas es mejor.
- **Canales de notificación son inmutables** tras crearse: decidir bien la
  importancia antes de publicar.
- **Adaptive icon**: la zona segura es Ø66dp; el ojo quedó con 0.25dp de
  margen — verificar en launchers squircle.
- **Play Store** exige AAB + $25 + 12 testers/14 días en cuentas nuevas;
  GitHub Releases + F-Droid es el camino para una app así.
- **El keystore de firma es el activo más crítico**: fuera del repo, con
  backup. Sin él no hay actualizaciones.

## Proceso que funcionó

- Review adversarial de repos antes de codificar (descubrió el mito del
  manifiesto).
- Skill de diseño (ui-ux-pro-max) como *materia prima curada*, no como
  veredicto: su salida automática era web-céntrica con rojos (anti-calm).
- Debate de icono con segundo modelo (PAL MCP) antes de decidir.
- Docs vivos actualizados en el mismo turno que cada decisión.
