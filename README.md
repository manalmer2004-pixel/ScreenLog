# ScreenLog

**A local-first film and television discovery platform for Kenya, East Africa, and the broader Global South.**

ScreenLog unifies film and TV logging, behavior-driven personalized recommendations, and a dedicated regional discovery layer for Kenyan and East African cinema — features no single existing platform (Letterboxd, MUBI, IMDb, Showmax, Buni.tv, Viusasa) currently combines.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Firestore Data Model](#firestore-data-model)
- [Admin Console](#admin-console)
- [Testing](#testing)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

Existing discovery platforms are film-only (Letterboxd), popularity-biased rather than personalized (Letterboxd, IMDb), fully paywalled with editorial-only curation (MUBI), or streaming-only with no social/discovery layer (Showmax, Buni.tv, Viusasa). None provide a dedicated discovery layer for Kenyan and East African productions.

ScreenLog addresses this by combining:

1. **Unified film + TV logging** — one interface for both formats, with per-title ratings, reviews, and spoiler tagging.
2. **Behavior-driven recommendations** — a collaborative-filtering recommendation engine driven by actual viewing/rating history rather than global popularity.
3. **Regional discovery** — a dedicated "Regional Spotlight" surfacing locally and regionally produced content, seeded from a curated dataset informed by the Kenya Film Classification Board (KFCB) public catalogue and expanded through moderated community submissions.
4. **Multilingual community** — reviews and discussion supported in English and Swahili (`sw`), with more languages plannable.
5. **Personal analytics** — a dashboard of viewing habits: top genres, top countries, favorite directors, rating distribution, and monthly activity.

## Features

- 🔐 Email/password authentication (Firebase Auth)
- 🔎 Title search via TMDB, with a "Local Content" filter toggle
- 📝 Log watched movies/TV with star rating, review text, language, and spoiler flag
- ❤️ Watchlist management
- 🌍 Regional Discovery screen — "Locally Produced" and "Regionally Produced" carousels with a `KE / LOCAL` badge
- 🎯 Personalized "For You" recommendations, generated server-side via a Firebase Cloud Function
- 📊 Personal analytics dashboard (genres, countries, directors, rating distribution, monthly trend)
- 🚩 Community review moderation (flag → moderator review → keep/remove)
- 📴 Offline-first: all writes land in Room first and sync to Firestore in the background
- 🌓 Light/dark theme toggle
- 🛠️ Companion web admin console for moderation and local-registry curation

## Tech Stack

**Mobile app (this repo)**

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Unidirectional Data Flow |
| DI | Dagger Hilt |
| Local DB | Room |
| Networking | Retrofit + OkHttp + Gson |
| Images | Coil |
| Navigation | Navigation Compose |
| Backend | Firebase (Authentication, Cloud Firestore, Cloud Functions) |
| External metadata | [The Movie Database (TMDB) API](https://www.themoviedb.org/documentation/api) |
| Build | Gradle Kotlin DSL, KSP |

**Admin console** (companion repo/directory — see [Admin Console](#admin-console)): React, TypeScript, Vite, Tailwind CSS, Firebase Web SDK.

## Architecture

ScreenLog follows a three-tier architecture:

```
┌─────────────────────────────┐
│   Presentation Layer         │  Android app (Kotlin, Jetpack Compose, MVVM)
├─────────────────────────────┤
│   Application Layer          │  Firebase Cloud Functions
│                               │  (generateRecommendations, flagReview)
├─────────────────────────────┤
│   Data Layer                 │  Cloud Firestore · Firebase Auth · Room (offline cache)
└─────────────────────────────┘
```

Within the mobile app:

```
presentation/  → Composable screens + ViewModels (per feature: auth, home, search, detail, log, discover, profile)
domain/        → Models + repository interfaces (platform-agnostic contracts)
data/          → Repository implementations, Room DAOs/entities, Retrofit DTOs, Firebase data sources, mappers
core/          → DI modules, navigation graph, theme, shared utils/constants
```

**Offline-first write pattern:** every write (log, rating, review, watchlist item) is saved to Room immediately with a `PENDING_SYNC` status, then pushed to Firestore in the background. On success the local status flips to `SYNCED`; on failure it's marked `FAILED` and retried on the next manual or automatic sync pass — so logging never blocks on network connectivity.

## Project Structure

```
app/
├── src/main/java/com/screenlog/app/
│   ├── core/
│   │   ├── common/          # Constants, Resource<T>, DateUtils, UiText
│   │   ├── di/               # Hilt modules (Network, Database, Firebase, App)
│   │   ├── navigation/       # Screen routes, NavGraph, bottom nav
│   │   └── theme/            # Color, Type, Theme, dark-mode DataStore prefs
│   ├── data/
│   │   ├── local/            # Room entities, DAOs, database, type converters
│   │   ├── mapper/           # Entity <-> Domain <-> DTO mappers
│   │   ├── remote/
│   │   │   ├── tmdb/         # Retrofit API + DTOs
│   │   │   └── firebase/     # Auth, Firestore, Cloud Functions data sources
│   │   └── repository/       # Repository implementations
│   ├── domain/
│   │   ├── model/            # Title, LogEntry, Review, Recommendation, etc.
│   │   └── repository/       # Repository interfaces
│   └── presentation/
│       ├── auth/  home/  search/  detail/  log/  discover/  profile/
│       └── components/       # Reusable Composables (TitleCard, RatingBar, badges…)
├── src/main/assets/kfcb_local_titles.json   # Seed dataset for local content registry
└── src/main/res/                             # Strings (incl. Swahili labels), colors, themes
```

## Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- JDK 17
- A Firebase project (Authentication, Firestore, and Cloud Functions enabled)
- A [TMDB API](https://www.themoviedb.org/settings/api) read access token

### Setup

1. **Clone the repo**
   ```bash
   git clone <repo-url>
   cd screenlog
   ```

2. **Add Firebase config**
   Download `google-services.json` from your Firebase project (Project Settings → your Android app) and place it at:
   ```
   app/google-services.json
   ```

3. **Add local API keys**
   Create `local.properties` in the project root (this file is git-ignored) and add:
   ```properties
   TMDB_API_KEY=your_tmdb_api_key
   TMDB_READ_ACCESS_TOKEN=your_tmdb_read_access_token
   ```
   These are injected into `BuildConfig` at build time (see `app/build.gradle.kts`).

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   or open the project in Android Studio and run on an emulator/device (minSdk 26, targetSdk 34).

## Configuration

| Setting | Location |
|---|---|
| TMDB base URL / image base URL | `core/common/Constants.kt` |
| Firestore collection names | `core/common/Constants.kt` |
| Database name/version | `data/local/ScreenLogDatabase.kt` |
| Auth/network interceptors | `core/di/NetworkModule.kt` |

Firestore collections used: `users`, `titles`, `logs` (per-user subcollection), `watchlist` (per-user subcollection), `reviews` (per-title subcollection), `recommendations`, `regions`, `localContentRegistry`, `moderationQueue`.

## Firestore Data Model

- **`users/{uid}`** — profile, `homeCountry`, `isModerator` flag
- **`users/{uid}/logs/{logId}`** — private viewing logs (rating, review, watched date, sync status)
- **`titles/{titleId}/reviews/{reviewId}`** — public reviews, denormalized with reviewer name, `flagged`/`flagReason`
- **`users/{uid}/watchlist/{itemId}`** — saved-for-later titles
- **`recommendations/{userId}`** (cached, mirrored to Room `recommendations` table) — output of the collaborative-filtering Cloud Function
- **`localContentRegistry/{registryId}`** — curated local/regional catalogue (seeded from `kfcb_local_titles.json`, extended via the admin console)
- **`moderationQueue/{flagId}`** — flagged-review reports pending moderator action

Security rules restrict log/watchlist writes to the owning user, allow public read of titles/reviews, and grant moderators elevated read/write access via an `isModerator()` helper (see Chapter 6.5 of the project report for the full rule set).

## Admin Console

A companion React + TypeScript + Vite web app (ScreenLog Console) covers the Content Moderator role: resolving flagged reviews, curating the local content registry, and managing moderator access. It talks to the **same Firebase project** as the mobile app — no separate backend.

```bash
cd admin-console        # or wherever the console lives in your checkout
cp .env.example .env.local   # fill in Firebase web config
npm install
npm run dev              # http://localhost:5173
```

Access is gated on `users/{uid}.isModerator == true`; promote your first moderator manually via the Firebase console, then use the console's Users page to promote others.

## Testing

- **Unit tests**: JUnit + Kotlin Coroutines Test on ViewModels/repositories — `./gradlew test`
- **Instrumented/UI tests**: Espresso + Compose UI test — `./gradlew connectedAndroidTest`
- **Manual/UAT**: covered functional areas include unified logging, recommendation quality, regional discovery, and admin moderation workflows

## Roadmap

- Hybrid recommendations (collaborative + content-based) to reduce the cold-start problem for new users
- Cross-platform expansion (iOS/web) via Kotlin Multiplatform / Compose Multiplatform
- Social features: shared watchlists, festival lists, community discussion, viewing challenges
- Broader regional coverage beyond Kenya, Uganda, Tanzania, Rwanda

## License

No license has been specified for this project yet. Add a `LICENSE` file to define usage terms.
