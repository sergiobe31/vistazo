# Vistazo

**¿Cuántas veces desbloqueas el móvil al día? ¿Y cuántas son solo un vistazo?**

Vistazo es una app Android minimalista de conciencia de uso. Cuenta tus
desbloqueos diarios y detecta los "vistazos": esas veces que desbloqueas el
móvil y lo vuelves a bloquear en menos de 60 segundos. El síntoma más claro
de uso compulsivo — y ninguna otra app lo mide.

## Qué hace

- **Un número grande**: desbloqueos de hoy. Debajo, cuántos fueron vistazos.
- **¿Cuántas crees?**: cada noche, estima cuántos llevas antes de ver el
  número real. La brecha entre percepción y realidad es el mensaje.
- **Reporte diario**: notificación a la hora que tú elijas (por defecto,
  22:00).
- **Widget de home** y, opcionalmente, contador permanente en la barra de
  estado.
- **Historial** a partir de los 7 días de uso.

## Privacidad radical

- **Sin permisos de red**: la app no puede enviar nada a ningún sitio. Ni
  analytics, ni crash reports, nada.
- **Sin cuenta, sin registro, gratis.**
- **Todo local**: tus datos viven en tu teléfono y nunca salen de él.
- Código abierto (GPLv3): audita cada línea.

## Cómo funciona (lo técnico)

Vistazo escucha dos broadcasts del sistema Android:

- `ACTION_USER_PRESENT` → se ha desbloqueado el móvil (huella, PIN, cara…).
- `ACTION_SCREEN_OFF` → se ha bloqueado. Si la sesión duró menos de 60
  segundos (configurable), fue un vistazo.

Estos broadcasts no se pueden escuchar desde el manifiesto, así que la app
los registra en runtime. Por defecto no hay servicios permanentes; si quieres
precisión total (y el contador en la barra de estado), puedes activar un
servicio en primer plano discreto desde los ajustes.

Stack: Kotlin nativo, Jetpack Compose (Material 3), Room, Glance (widget),
WorkManager (reporte diario). Sin Firebase, sin librerías de terceros de
tracking.

## Descargar

APK firmado en
[GitHub Releases](https://github.com/sergiobe31/vistazo/releases).
Descarga `Vistazo.apk` en tu móvil e instálalo (Android te pedirá permitir
"orígenes desconocidos" una vez).

## Mejórala a tu gusto

¿Eres usuario avanzado? El proyecto está pensado para ser hackeable:

1. Clona el repo: `git clone https://github.com/sergiobe31/vistazo`
2. Ábrelo en Android Studio (o compila con `./gradlew assembleDebug`).
3. La arquitectura cabe en una servilleta: receivers en `receiver/`, datos
   en `data/`, UI en `ui/`, widget en `widget/`.

Las decisiones de diseño están documentadas en
[`docs/idea_v1.md`](https://github.com/sergiobe31/vistazo/blob/main/docs/idea_v1.md)
y el estado del desarrollo en
[`docs/estado.md`](https://github.com/sergiobe31/vistazo/blob/main/docs/estado.md).

Ideas, issues y PRs bienvenidos. Licencia GPLv3: cualquier derivado debe
seguir siendo libre.
