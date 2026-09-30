# Idea v1 — contador de desbloqueos y "vistazos"

Origen: idea de Sergio, 2026-09-30. App Android ultra-simple, gratis, sin
creación de usuario, puramente informativa.

## Concepto

Mostrar al usuario cuánto usa el móvil de forma compulsiva:

- "Hoy has desbloqueado el móvil 120 veces."
- "En 65 ocasiones lo has vuelto a bloquear en menos de 60 segundos."

La segunda métrica ("vistazos") es el diferenciador: desbloquear y soltar el
móvil en <60s es un síntoma claro de uso compulsivo, y Digital Wellbeing no lo
muestra.

## Análisis de viabilidad (corregido 2026-09-30 tras review de repos)

Mecanismo: broadcasts del sistema `ACTION_USER_PRESENT` (un desbloqueo) y
`ACTION_SCREEN_OFF` (cierra sesión; si duración <60s → "vistazo").

**Corrección importante**: estos broadcasts **no se pueden recibir con
receiver en manifiesto** — solo con `registerReceiver()` en runtime mientras
el proceso vive (doc oficial de `Intent` + confirmado en los repos reales
[unlock-master](https://github.com/sweakpl/unlock-master, ~72★) y
[screenlookcount](https://github.com/lomza/screenlookcount)). La premisa
inicial "cero servicios, cero batería, funciona tras reinicio" era incorrecta.

**Decisión: arquitectura híbrida (2026-09-30).**

- **Por defecto, sin servicio**: receivers dinámicos registrados mientras el
  proceso vive + receiver de manifiesto para `BOOT_COMPLETED` que relanza el
  proceso. Conteo casi perfecto en Pixel/stock (<1% eventos perdidos), con
  huecos en Xiaomi/Huawei/Oppo (matanza agresiva de procesos). Copy honesto:
  cifras aproximadas.
- **Opción en ajustes "mejorar precisión"**: foreground service `specialUse`
  con notificación persistente de baja prioridad (patrón de unlock-master) +
  exención de optimización de batería. Fiable en todos los OEM.
- Onboarding contextual: en MIUI/EMUI mostrar instrucciones de
  Autostart/batería (referencia: dontkillmyapp.com).
- Alternativa descartada: `UsageStatsManager` (`KEYGUARD_HIDDEN`) — exige el
  permiso especial "Acceso al uso". Solo para backfill histórico, si acaso.

## Competencia / posicionamiento

- **Digital Wellbeing** (Pixel, Samsung, etc.) ya cuenta desbloqueos diarios.
  No replicarlo: el valor está en la métrica de vistazos, el minimalismo
  radical (un número grande en un widget) y el framing de conciencia.
- Argumento de marketing: "sin permisos, sin cuenta, sin red, tus datos nunca
  salen del teléfono".

## Arquitectura mínima prevista

1. Receivers **dinámicos** (runtime) `USER_PRESENT`/`SCREEN_OFF` + receiver de
   manifiesto para `BOOT_COMPLETED`. Escritura en Room con `goAsync()` (no en
   `onReceive` directo, riesgo de ANR).
2. Modo precisión opcional: foreground service `specialUse` + notificación
   low-priority (requiere permiso `POST_NOTIFICATIONS` en Android 13+; es la
   única excepción al "cero permisos", solo si el usuario lo activa).
3. **Persistencia con Room/SQLite** (decidido 2026-09-30): local en el
   teléfono, cero coste, cero servidores. Se elige sobre SharedPreferences/
   fichero plano porque escala mejor: consultas ricas (tendencias, medias
   por franja horaria, exportación) sin migrar la capa de datos después.
4. Una pantalla (hoy + histórico simple) + **widget Glance 1.2.0**: Room como
   fuente de verdad, `provideGlance()` lee el contador del día, tras cada
   evento `updateAll()` desde el receiver, y `updatePeriodMillis=1800000`
   (30 min, el mínimo del sistema) como red de seguridad si se pierden
   broadcasts.

### Repos de referencia (review 2026-09-30)

- [sweakpl/unlock-master](https://github.com/sweakpl/unlock-master) (~72★) —
  la más cercana: cuenta desbloqueos, Kotlin + Room + widget Glance + FGS.
- [lomza/screenlookcount](https://github.com/lomza/screenlookcount) (~12★) —
  SCREEN_ON/OFF/USER_PRESENT por día en Room, directBootAware.
- Widgets Glance: [platform-samples/appwidgets](https://github.com/android/platform-samples/tree/main/samples/user-interface/appwidgets)
  y widget de [nowinandroid](https://github.com/android/nowinandroid).

## Diseño (decidido 2026-09-30)

Minimalista, bonito, no invasivo. No se inventa desde cero:

- **Base de implementación**: Material Design 3 (color dinámico) + guías
  oficiales de widgets Glance (legible en ≤2s, sin interacción obligatoria).
- **Criterio de revisión**: principios de Calm Technology (Amber Case) —
  comunicar sin interrumpir, sin rojos alarmistas ni badges de ansiedad para
  los vistazos.
- **Opcional**: skill `ui-ux-pro-max` (soporta Jetpack Compose) para acelerar
  dirección estética inicial (paletas/fuentes).

### Dirección estética v1 (decidido 2026-09-30, curada de ui-ux-pro-max)

- **Estilo**: Minimalism/Swiss — espacio en blanco, jerarquía tipográfica,
  sin sombras ni decoración, transiciones sutiles (~200ms).
- **Tipografía**: Varela Round (número grande/titulares) + Nunito Sans
  (cuerpo), OFL, empaquetadas en el APK (la app no tiene permiso de red).
- **Color**: dinámico Material You (Android 12+) con fallback fijo calm:
  fondo hueso `#F5F5F0`, texto gris `#6B7280`, acento teal `#0891B2`.
  Jamás rojos/alarma para los vistazos.
- **Microcopy calm**: neutro, sin rachas, sin badges, sin comparativas que
  juzguen. Umbral de 60s como info, no advertencia.

### Dirección estética v2 (decidido 2026-09-30, tras feedback de Sergio)

Iteración sobre la v1; la v1 queda descartada (referencia histórica).

- **Estilo**: Minimalism/Swiss con guiño a terminal retro (tipografía mono,
  paleta papel+tinta). Mismo criterio Calm Technology.
- **Paleta fija en todos los Androids**: se elimina el color dinámico
  Material You. Fondo crema papel `#FAF3D9` (light) / marrón muy oscuro
  `#241D12` (dark), tinta `#3F3222`/`#7A6A4F`, acento ámbar `#B45309`
  (light) / `#E8A33D` (dark). Sin rojos.
- **Tipografía**: JetBrains Mono (OFL, empaquetada) para todo: número grande
  en bold, cuerpo en regular. Sustituye a Varela Round + Nunito Sans (v1).
- **Home mínima**: solo el número grande de desbloqueos de hoy + línea de
  vistazos + acceso discreto al histórico. El histórico (barras 7 días, por
  ahora) vive en una segunda pantalla junto al ajuste de barra de estado.
- **Ver sin entrar**: la notificación persistente del servicio (antes "modo
  precisión") muestra el conteo del día en la barra de estado:
  "Hoy: N desbloqueos · M vistazos". Es la feature estrella: el usuario ve
  el número sin abrir la app. El toggle se renombra a "Mostrar en la barra
  de estado" y el copy explica que además mejora la precisión del conteo.
- **Navegación**: manual (estado en ViewModel + `when` en MainActivity); dos
  pantallas no justifican Navigation Compose.

## Plataformas (decidido 2026-09-30)

- **v1 solo Android.** El mecanismo (broadcasts `USER_PRESENT`/`SCREEN_OFF`
  sin permisos) no tiene equivalente en iOS: no hay API pública de eventos de
  desbloqueo; Screen Time/DeviceActivity no cuenta desbloqueos y exige
  entitlement aprobada a mano por Apple. iOS descartado de momento por
  imposibilidad de API, no por coste.
- Nada de Kotlin/Compose Multiplatform: Room y los receivers son Android-only.

## Distribución y comunidad (decidido 2026-09-30)

- Código abierto, repo público en GitHub (github.com/sergiobe31), package
  `io.github.sergiobe31.vistazo`, **licencia GPLv3** (copyleft: derivados
  deben seguir siendo open source; decidido 2026-09-30).
- Web en GitHub Pages explicando la idea y el cómo (los dos receivers, por
  qué no hacen falta permisos) + guía para que un usuario avanzado pueda
  mejorar/adaptar la app.
- APK en GitHub Releases. F-Droid/Play quedan como decisión abierta.

## Nombre (decidido 2026-09-30)

**Vistazo** — la app toma el nombre de su métrica diferencial. Público
principal: hispanohablante. Package: `io.github.sergiobe31.vistazo`.

## Decisiones abiertas

- [ ] Distribución: APK directo / F-Droid / Google Play ($25 cuenta dev,
      privacy policy, review — fácil con cero permisos).
- [ ] Backfill histórico vía UsageStats: ¿sí/no para v1?
- [ ] "Coger el móvil sin desbloquear" (raise-to-wake, mirar la hora):
      exigiría servicio con acelerómetro (batería + complejidad). Fuera de v1.

## Backlog de ideas (brainstorming 2026-09-30, sin compromiso)

**Actualización 2026-09-30: los puntos 1–4 se implementaron como v1.1** (ver
`docs/estado.md`). El punto 4 usa SharedPreferences (no DataStore) por
coherencia con lo existente; el umbral aplica a sesiones nuevas, sin
reclasificar histórico.

Orden sugerido de valor/esfuerzo:

1. **Historial visible solo tras 7 días de uso** — la app nace limpia y
   "madura" contigo. Trivial.
2. **"¿Cuántas crees?" vs realidad** — en el reporte diario, el usuario
   estima antes de ver el número ("Creíste 40. Fueron 87, 34 vistazos").
   La brecha percepción/realidad ES el mensaje. Solo UI + un campo en Room.
   Candidata a segundo diferenciador.
3. **Reporte diario a hora configurable** (default 22:00): "Hoy desbloqueaste
   X veces, Y fueron vistazos". WorkManager inexacto (sin permisos extra de
   alarma) + `POST_NOTIFICATIONS` una vez. Coexiste con la barra permanente
   como opción independiente: barra = info continua, reporte = ritual diario.
4. **Umbral de vistazo configurable** en ajustes (slider, DataStore).
5. **Conteo por app concreta (TikTok, Instagram)** — DESCARTADA para v1:
   exige "Acceso al uso" (UsageStats) o Accessibility Service; rompe el
   argumento "sin permisos". Documentar como extensión opt-in para forks.
