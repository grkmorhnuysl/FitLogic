# FitLogic

FitLogic is an Android fitness app foundation focused on offline-first usage, privacy, and a modular architecture.

This repository now includes the Phase 0 bootstrap needed to start Phase 1 development safely:

- Android app skeleton with Jetpack Compose
- Gradle version catalog
- `build-logic` convention plugins
- ktlint + Detekt configuration
- GitHub Actions PR validation
- documented local setup and secret management

## Requirements

- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Docker Desktop for local Supabase

## Quick Start

1. Install Android Studio and confirm JDK 17 is available.
2. Copy `secrets.properties.example` to `secrets.properties`.
3. Fill Supabase values in `secrets.properties` when they are available.
4. Open the project in Android Studio and sync Gradle.
5. Run:

```bash
./gradlew assembleDebug
./gradlew check
```

## Web Preview (Optional)

For fast browser-side UI testing, a Vite React app is included under `web/`.

```bash
cd web
pnpm install
pnpm dev
```

From repository root, you can also run:

```bash
pnpm dev
```

## Project Layout

- `app/`: minimal Android entry point
- `build-logic/convention/`: shared Gradle convention plugins
- `config/detekt/`: static analysis configuration
- `docs/progress.md`: architecture, conventions, roadmap
- `docs/phaze.md`: phase tracking checklist

## Secrets

Do not commit local credentials.

- `secrets.properties` is ignored by git
- `local.properties` is ignored by git
- `secrets.properties.example` is the tracked template

Expected keys:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_PROJECT_ID=your-project-id
GOOGLE_WEB_CLIENT_ID=your-google-web-client-id.apps.googleusercontent.com
```

`SUPABASE_ACCESS_TOKEN` should not be used by the Android runtime.
Use it only for Supabase CLI/manual operations, and keep it only in local environment variables or local secret files.

## Local Supabase

Install Docker Desktop and the Supabase CLI, then use:

```bash
supabase start
supabase status
```

The actual Supabase project creation and local Docker verification are still manual tasks in Phase 0.

## Current Status

Implemented in repo:

- Android/Gradle bootstrap
- quality tooling
- CI workflow
- Phase 0 documentation updates

Still manual or external:

- Android Studio installation
- Supabase project creation
- local Docker verification
- Figma wireframes
- MediaPipe Gemma device POC
