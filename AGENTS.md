# AGENTS.md — unlock_awareness

App Android minimalista de conciencia de uso: cuenta desbloqueos diarios y
"vistazos" (desbloqueos de <60s) como señal de adicción. Gratis, sin cuenta,
sin permisos, todo local. Idea y decisiones: `docs/idea_v1.md`.

Heredado del repo `rule_extraction` (2026-09-30): solo las convenciones
transversales. Las skills de usuario (`debate`, `interceptor`) y el PAL MCP
(34 modelos vía OpenRouter) ya son de ámbito usuario y funcionan desde aquí.

## Convenciones

- **Estado ≠ conocimiento**: docs con fechas explícitas ("al 2026-09-30");
  nada de "actualmente" sin fecha.
- **Docs vivos** (actualizar en el mismo turno que cambian):
  - `docs/idea_v1.md` — idea, análisis de viabilidad y decisiones abiertas.
  - `docs/estado.md` — estado del desarrollo (cuando exista código).
- **Debate/red-team disponible**: antes de decisiones de diseño con riesgo,
  opción de revisión adversarial con otro modelo vía PAL MCP (skill `debate`).
  Veredictos: REAL / FALSO / PENDIENTE-SERGIO, adjudicados por el orquestador
  contra la fuente (docs oficiales de Android), no por consenso.
- **Guardrail de git**: `git add/commit/push` se propone, no se ejecuta sin
  confirmación de Sergio. Nada de `rm -rf`.
- **Privacidad por diseño**: la app no pide permisos ni red. Cualquier feature
  que rompa eso (analytics, crash reporting remoto, backup en nube) se discute
  explícitamente antes de implementarse.

## Stack (decisión provisional 2026-09-30)

Kotlin nativo + Jetpack Compose. Nada de Flutter/RN: la app es dos receivers,
una tabla SQLite y un widget; el APK debe ser mínimo.
