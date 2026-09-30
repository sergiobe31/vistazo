---
name: vistazo-ops
description: Operativa de build, firma y publicación de Vistazo (toolchain local, quirks de Gradle/AGP, keystore, releases). Usar cuando se compile, publique o toque el build del proyecto.
---

# vistazo-ops — operativa del proyecto Vistazo

## Toolchain local (Mac de Sergio, instalado 2026-09-30)

Todo vía Homebrew. En shells nuevos:

```bash
export PATH="/opt/homebrew/bin:/opt/homebrew/opt/openjdk@17/bin:$PATH"
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
```

- JDK: `openjdk@17` (keg-only: NO está en PATH por defecto, exportar siempre).
- SDK: cmdline-tools + platform-tools + android-36 + build-tools.
- Gradle: usar **siempre `./gradlew`** (pin 8.13). El `gradle` de Homebrew
  (9.8) NO sirve: AGP 8.13.2 usa APIs internas eliminadas en Gradle ≥9.6.

## Build

```bash
./gradlew assembleDebug --no-daemon      # debug
./gradlew assembleRelease --no-daemon    # release firmado (si existe signing props)
```

## Firma de release

- Keystore: `~/vistazo-release.keystore` (alias `vistazo`).
- Credenciales: `~/.vistazo-signing.properties` (chmod 600, FUERA del repo).
- `app/build.gradle.kts` lo detecta si existe; sin el archivo, release sale
  sin firmar.
- **CRÍTICO**: backup del keystore. Perderlo = imposible actualizar la app
  instalada. Nunca commitear ni subir.

## Publicación

- Repo: github.com/sergiobe31/vistazo (gh CLI autenticado).
- Release: subir APK como `Vistazo.apk` (el `#label` de `gh release create`
  no renombra el asset: copiar/renombrar el fichero antes de subir y borrar
  el asset antiguo).
- Pages: rama main, carpeta /docs → sergiobe31.github.io/vistazo.
- Subir `versionCode`/`versionName` en `app/build.gradle.kts` por release.

## Quirks conocidos (no reintroducir)

- Broadcasts de pantalla (USER_PRESENT/SCREEN_OFF/SCREEN_ON): SOLO runtime,
  nunca manifiesto. Escritura Room con goAsync().
- Glance: color como ColorProvider (no Color); sin fuentes custom.
- Factory de AndroidViewModel: `this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!`.
- Canales de notificación inmutables una vez creados (importancia se decide
  bien la primera vez).
- Room: migraciones manuales (exportSchema=false, AutoMigration no aplica).

## Pendientes conocidos al 2026-09-30

- F-Droid: metadatos fastlane + RFP.
- Google Play: AAB en app/build/outputs/bundle/release/; requiere cuenta
  $25 + 12 testers 14 días (cuentas nuevas).
- Verificar margen del icono en launchers squircle (0.25dp).
- Historia completa: docs/devlog_v1.0.md. Decisiones: docs/idea_v1.md.
  Estado vivo: docs/estado.md.
