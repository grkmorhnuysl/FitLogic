# FitLogic AI - Progress & Project Guide

> **Bu dokuman canli calisma defteridir.** Hem projenin anayasasi (mimari, konvansiyon, komutlar) hem de ilerleme takibidir. Her gelistirme oturumundan once okunmali, oturum sonunda guncellenmeli.
>
> **Takip kaynagi:** Faz tamamlama ve dogruluk denetimi icin birincil kaynak `docs/phaze.md` dosyasidir. Bu dosya agirlikli olarak tarihsel/mimari notlar icin korunur.

**Son guncelleme:** 2026-04-22
**Mevcut faz:** Faz 5 - Beslenme MVP
**Mevcut sprint:** -

---

## ğŸ“‹ Ä°Ã§indekiler

1. [Proje Ã–zeti](#1-proje-Ã¶zeti)
2. [HÄ±zlÄ± Komutlar](#2-hÄ±zlÄ±-komutlar)
3. [Teknoloji YÄ±ÄŸÄ±nÄ±](#3-teknoloji-yÄ±ÄŸÄ±nÄ±)
4. [Mimari Kararlar (ADR)](#4-mimari-kararlar-adr)
5. [Kod KonvansiyonlarÄ±](#5-kod-konvansiyonlarÄ±)
6. [KlasÃ¶r YapÄ±sÄ±](#6-klasÃ¶r-yapÄ±sÄ±)
7. [Task Breakdown](#7-task-breakdown)
8. [Bilinen Sorunlar & Ã‡Ã¶zÃ¼mleri](#8-bilinen-sorunlar--Ã§Ã¶zÃ¼mleri)
9. [Oturum GÃ¼nlÃ¼ÄŸÃ¼](#9-oturum-gÃ¼nlÃ¼ÄŸÃ¼)

---

## 1. Proje Ã–zeti

**FitLogic AI**, yeni baÅŸlayan ve genel fitness kitlesine yÃ¶nelik, **gizlilik odaklÄ±** bir Android uygulamasÄ±dÄ±r.

**FarklÄ±laÅŸma noktasÄ±:** Yapay zeka analizleri bulut yerine cihaz Ã¼zerinde (on-device) Gemma 2B modeli ile Ã§alÄ±ÅŸÄ±r. Veri cihazdan Ã§Ä±kmaz, API maliyeti yoktur, internet gerektirmez.

**Hedef kitle:** Yeni baÅŸlayanlar + genel fitness severler (kilo verme, form koruma, kas yapma)

**MVP 4 ana modÃ¼l:**
- AI koÃ§luk ve progressive overload analizi
- DetaylÄ± beslenme takibi (barkod, makro)
- GeliÅŸmiÅŸ istatistik ve grafikler
- Egzersiz kÃ¼tÃ¼phanesi

**TasarÄ±m prensipleri:**
- **Offline-first** â€” her ÅŸey lokal Ã§alÄ±ÅŸÄ±r, sync ikincildir
- **SÃ¼rtÃ¼nmesiz UX** â€” set giriÅŸi <5 saniye, Ã¶ÄŸÃ¼n ekleme <15 saniye
- **Sade dil** â€” AI yeni baÅŸlayan diliyle konuÅŸur, teknik terim yok
- **Veri sahipliÄŸi** â€” kullanÄ±cÄ± verisini cihazda tutar, dÄ±ÅŸa aktarabilir

---

## 2. HÄ±zlÄ± Komutlar

### Build & Run
```bash
# Debug APK build
./gradlew assembleDebug

# Release APK build (imzalÄ±)
./gradlew assembleRelease

# UygulamayÄ± baÄŸlÄ± cihaza yÃ¼kle + Ã§alÄ±ÅŸtÄ±r
./gradlew installDebug
adb shell am start -n com.fitlogic.ai/.MainActivity

# Clean build
./gradlew clean build
```

### Test
```bash
# TÃ¼m unit testler
./gradlew test

# Belirli modÃ¼l
./gradlew :feature:workout:test

# Instrumented testler (cihaz/emÃ¼latÃ¶r gerekli)
./gradlew connectedAndroidTest

# Compose UI testleri
./gradlew :app:connectedDebugAndroidTest

# Test coverage raporu
./gradlew jacocoTestReport
```

### Kod Kalitesi
```bash
# Ktlint format
./gradlew ktlintFormat

# Ktlint check
./gradlew ktlintCheck

# Detekt (static analysis)
./gradlew detekt

# Hepsini bir arada
./gradlew check
```

### Room Database
```bash
# Åema JSON'larÄ±nÄ± Ã¼ret (migration referansÄ± iÃ§in)
./gradlew :core:data:kspDebugKotlin

# Migration test
./gradlew :core:data:test --tests "*Migration*"
```

### Supabase
```bash
# Supabase local baÅŸlat (docker)
supabase start

# Migration uygula
supabase db push

# Åema generate
supabase gen types kotlin --local > core/data/src/main/kotlin/SupabaseTypes.kt

# Local studio: http://localhost:54323
```

### MediaPipe / AI
```bash
# Gemma 2B modelini indir (geliÅŸtirme)
mkdir -p ~/fitlogic/models
wget https://storage.googleapis.com/mediapipe-models/llm_inference/gemma-2b-it-cpu-int4/float32/1/gemma-2b-it-cpu-int4.bin \
  -O ~/fitlogic/models/gemma-2b-it.bin

# Cihaza push (adb)
adb push ~/fitlogic/models/gemma-2b-it.bin /data/local/tmp/llm/
```

### Git Workflow
```bash
# Feature branch
git checkout -b feature/workout-timer

# Commit mesaj formatÄ±: type(scope): description
git commit -m "feat(workout): add rest timer"
git commit -m "fix(nutrition): fix barcode crash on empty result"

# Pre-push: ktlint + testler otomatik Ã§alÄ±ÅŸÄ±r
git push origin feature/workout-timer
```

---

## 3. Teknoloji YÄ±ÄŸÄ±nÄ±

| Alan | SeÃ§im | Versiyon |
|------|-------|----------|
| Dil | Kotlin | 2.0.0+ |
| Min SDK | Android | 26 (8.0) |
| Target SDK | Android | 34 |
| UI | Jetpack Compose + Material 3 | BOM 2024.10 |
| DI | Hilt | 2.51+ |
| Yerel DB | Room | 2.6+ |
| Backend | Supabase Kotlin | 2.6+ |
| AI | MediaPipe LLM Inference | 0.10+ |
| Model | Gemma 2B (4-bit quantized) | gemma-2b-it-cpu-int4 |
| Barkod | ML Kit Barcode | 17.3+ |
| Food API | OpenFoodFacts | v2 |
| Grafikler | Vico Charts | 2.0+ |
| Async | Coroutines + Flow | 1.8+ |
| Navigation | Compose Navigation | 2.8+ |
| WorkManager | AndroidX Work | 2.9+ |
| Test | JUnit5, MockK, Turbine, Compose UI Test | â€” |
| Lint | Ktlint + Detekt | â€” |

---

## 4. Mimari Kararlar (ADR)

Her karar: **BaÄŸlam â†’ Karar â†’ SonuÃ§lar** formatÄ±nda.

### ADR-001: Clean Architecture + MVVM
**BaÄŸlam:** Uygulama bÃ¼yÃ¼dÃ¼kÃ§e test edilebilirlik ve modÃ¼lerlik kritik olacak. AI katmanÄ±nÄ±n UI'dan tamamen izole olmasÄ± gerekiyor.

**Karar:** 3 katmanlÄ± yapÄ± kullan: **Presentation** (Compose + ViewModel) â†’ **Domain** (UseCase'ler, saf Kotlin) â†’ **Data** (Repository + Room/Supabase/AI).

**SonuÃ§lar:**
- âœ… Domain katmanÄ± Android'den baÄŸÄ±msÄ±z, JVM'de test edilebilir
- âœ… ViewModel â†’ UseCase â†’ Repository akÄ±ÅŸÄ± zorunlu
- âŒ Boilerplate artar (her feature iÃ§in 3-4 dosya)
- âš ï¸ Basit ekranlarda bile UseCase katmanÄ± atlamÄ±yoruz (tutarlÄ±lÄ±k iÃ§in)

---

### ADR-002: Multi-Module Gradle YapÄ±sÄ±
**BaÄŸlam:** Tek modÃ¼llÃ¼ yapÄ±da her deÄŸiÅŸiklik tÃ¼m projeyi rebuild ettiriyor. 16 haftalÄ±k geliÅŸtirmede bu bÃ¼yÃ¼k zaman kaybÄ±.

**Karar:** Feature-bazlÄ± multi-module yapÄ±. `:core:*` ortak, `:feature:*` baÄŸÄ±msÄ±z.

**SonuÃ§lar:**
- âœ… Paralel derleme, 2-3x daha hÄ±zlÄ± incremental build
- âœ… Feature'lar birbirini bilmiyor â†’ daha temiz baÄŸÄ±mlÄ±lÄ±klar
- âŒ Ä°lk kurulum karmaÅŸÄ±k, Gradle convention plugin'leri gerekli
- âš ï¸ Feature modÃ¼ller **sadece** `:core:*` modÃ¼llerine baÄŸlÄ± olabilir, birbirlerine **asla**

---

### ADR-003: Offline-First Senkronizasyon
**BaÄŸlam:** Spor salonlarÄ±nda internet genelde yok. KullanÄ±cÄ± set girdiÄŸinde "Sync bekleniyor" gÃ¶rmek istemiyor.

**Karar:** TÃ¼m yazma iÅŸlemleri Ã¶nce Room'a gider. UI Flow Ã¼zerinden Room'dan besleniyor. Supabase'e sync arka planda WorkManager ile yapÄ±lÄ±yor.

**SonuÃ§lar:**
- âœ… KullanÄ±cÄ± iÃ§in uygulama her zaman anlÄ±k tepki verir
- âœ… Offline tam iÅŸlevsellik
- âŒ Ã‡akÄ±ÅŸma yÃ¶netimi gerekiyor (last-write-wins stratejisi)
- âš ï¸ Her tabloda `sync_status` (PENDING/SYNCED/CONFLICT) ve `updated_at` (epoch ms) alanlarÄ± **zorunlu**

---

### ADR-004: AI Opsiyonel â€” Lite Mod DesteÄŸi
**BaÄŸlam:** Gemma 2B ~1.5 GB ve 4GB+ RAM istiyor. Hedef kitlenin %30-40'Ä±nÄ±n cihazÄ± bunu kaldÄ±ramayabilir.

**Karar:** AI Ã¶zelliÄŸi tamamen opsiyonel. <4GB RAM cihazlarda otomatik **Lite Mod**: kural-tabanlÄ± analiz (AI simÃ¼lasyonu, not AI).

**SonuÃ§lar:**
- âœ… Uygulama tÃ¼m kitleye eriÅŸilebilir
- âœ… Model indirme sÃ¼reci kullanÄ±cÄ±yÄ± kaÃ§Ä±rmÄ±yor (opsiyonel)
- âŒ Ä°ki farklÄ± analiz sistemi bakÄ±mÄ± (AI + rule-based)
- âš ï¸ `AiEngine` interface'i soyut â€” `GemmaAiEngine` ve `RuleBasedEngine` implementasyonlarÄ±

---

### ADR-005: Denormalize Edilen Alanlar
**BaÄŸlam:** Grafikler iÃ§in her seferinde JOIN + SUM yapmak yavaÅŸ. Workout listesinde toplam hacim gÃ¶stermek isteniyor.

**Karar:** BazÄ± alanlarÄ± denormalize et:
- `workouts.total_volume` (bu oturumda kaldÄ±rÄ±lan toplam aÄŸÄ±rlÄ±k)
- `food_entries.kcal/protein/carb/fat` (yemek eklendiÄŸi anki deÄŸerler)
- `sets.is_pr` (PR kÄ±rÄ±ldÄ±ysa iÅŸaretli)

**SonuÃ§lar:**
- âœ… Liste ekranlarÄ± anlÄ±k aÃ§Ä±lÄ±r
- âœ… GeÃ§miÅŸe dÃ¶nÃ¼k veri bÃ¼tÃ¼nlÃ¼ÄŸÃ¼ (yemek DB'sindeki deÄŸer deÄŸiÅŸse bile entry sabit kalÄ±r)
- âŒ Yazma tarafÄ±nda denormalizasyon mantÄ±ÄŸÄ± gerekir
- âš ï¸ `WorkoutRepository.saveSet()` otomatik olarak `workouts.total_volume`'u gÃ¼ncellemeli

---

### ADR-006: Compose Navigation, Tek NavHost
**BaÄŸlam:** Fragment-based navigation eski. Multi-module yapÄ±da her feature kendi route'unu tanÄ±mlamalÄ±.

**Karar:** Tek bir `NavHost` (app modÃ¼lde). Her feature `NavGraphBuilder.featureXGraph(navController)` extension'Ä± sunar.

**SonuÃ§lar:**
- âœ… Type-safe navigation (Kotlin serialization ile)
- âœ… Feature'lar route'larÄ±nÄ± kendi iÃ§inde tanÄ±mlar
- âš ï¸ Deep link desteÄŸi iÃ§in route'larÄ±n stringify edilebilir olmasÄ± gerekir

---

### ADR-007: Dependency Injection â€” Hilt
**BaÄŸlam:** DI olmadan test edilebilirlik yok. Koin daha hafif ama compile-time gÃ¼venliÄŸi yok.

**Karar:** Hilt kullan. Her modÃ¼lde kendi `@Module` sÄ±nÄ±flarÄ± (`DataModule`, `AiModule` vb).

**SonuÃ§lar:**
- âœ… Compile-time DI kontrolÃ¼
- âœ… Google Ã¶nerisi, Android yaÅŸam dÃ¶ngÃ¼sÃ¼ entegrasyonu
- âŒ Build sÃ¼resi ~%15 artar (KSP ile %5)
- âš ï¸ KSP (Kotlin Symbol Processing) kullan, KAPT deÄŸil

---

## 5. Kod KonvansiyonlarÄ±

### Genel Kurallar
- **Ktlint** resmi kurallara baÄŸlÄ±, pre-push'ta Ã§alÄ±ÅŸÄ±r. Format problemi = push reddedilir.
- **Detekt** complexity ve kod kokusu iÃ§in. `detekt-baseline.xml` ile mevcut uyarÄ±lar gÃ¶rmezden gelinir, yeni uyarÄ± eklenemez.
- Her public API iÃ§in **KDoc** (iÃ§ kullanÄ±m iÃ§in gerek yok).
- **1 dosya = 1 sÄ±nÄ±f**. Ä°stisna: kÃ¼Ã§Ã¼k data class'lar (<10 satÄ±r) ve sealed class hierarchy'leri.

### Naming

| Ã–ÄŸe | Kural | Ã–rnek |
|------|-------|-------|
| Class | PascalCase | `WorkoutRepository` |
| Function | camelCase, fiil baÅŸlar | `saveSet()`, `loadWorkout()` |
| Composable | PascalCase | `WorkoutScreen()`, `SetInputCard()` |
| Variable | camelCase | `currentWeight`, `totalVolume` |
| Constant | UPPER_SNAKE_CASE | `MAX_SETS_PER_EXERCISE` |
| Package | lowercase | `com.fitlogic.ai.feature.workout` |
| Kaynak (XML) | snake_case | `ic_barbell`, `bg_gradient` |
| Test | `` `should do X when Y`() `` | `` `should save set when weight is valid`() `` |

### Composable KurallarÄ±
- Composable fonksiyonlar **yan etkisiz** olmalÄ±. Yan etki â†’ `LaunchedEffect`, `DisposableEffect`.
- Parametreler sÄ±rasÄ±: **required â†’ optional â†’ modifier â†’ callback'ler**.
- Modifier default `Modifier` olmalÄ±, zincirin baÅŸÄ±nda uygulanmalÄ±.
- Preview her ekran iÃ§in: `@Preview(showBackground = true, widthDp = 380)`.
- **State hoisting**: Composable state tutmuyorsa, caller'dan al. `remember` sadece en Ã¼st dÃ¼zeyde.

```kotlin
// âœ… Ä°yi
@Composable
fun SetInputCard(
    set: SetUiModel,
    modifier: Modifier = Modifier,
    onWeightChange: (Float) -> Unit,
    onRepsChange: (Int) -> Unit,
) { /* ... */ }

// âŒ KÃ¶tÃ¼ â€” modifier eksik, callback ortada
@Composable
fun SetInputCard(
    set: SetUiModel,
    onWeightChange: (Float) -> Unit,
    editable: Boolean,
) { /* ... */ }
```

### ViewModel KurallarÄ±
- State **tek bir `UiState` sealed class** olmalÄ±: `Loading | Success(data) | Error(message)`.
- `StateFlow<UiState>` kullan, `LiveData` yasak.
- Events iÃ§in `Channel<UiEvent>` (one-shot) veya `SharedFlow` (multiple observer).
- `viewModelScope.launch { }` iÃ§inde try-catch zorunlu, repository Result wrapper dÃ¶ndÃ¼rmeli.
- ViewModel **asla** Android Context tutmaz. `@ApplicationContext` bile yok.

```kotlin
// Standart yapÄ±
sealed interface WorkoutUiState {
    data object Loading : WorkoutUiState
    data class Success(val workout: Workout) : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val getWorkoutUseCase: GetWorkoutUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Loading)
    val uiState = _uiState.asStateFlow()
}
```

### Repository KurallarÄ±
- Repository **interface'i** Domain'de, **implementation** Data'da.
- DÃ¶nÃ¼ÅŸ tipi `Flow<Result<T>>` veya `Result<T>`. Exception **asla** fÄ±rlatÄ±lmaz, Result'a wrap edilir.
- Room DAO ve Supabase client doÄŸrudan UseCase/ViewModel'e gitmez â€” **sadece Repository Ã¼zerinden**.

### Room Entity KurallarÄ±
- Tablo adlarÄ± **Ã§oÄŸul, snake_case**: `users`, `workout_exercises`.
- Primary key: `@PrimaryKey val id: String = UUID.randomUUID().toString()` (String UUID, Long deÄŸil â€” Supabase ile uyum iÃ§in).
- Her tabloda zorunlu: `created_at: Long`, `updated_at: Long`, `sync_status: SyncStatus`.
- Entity â†” Domain model dÃ¶nÃ¼ÅŸÃ¼mÃ¼ iÃ§in **mapper fonksiyonlarÄ±**: `WorkoutEntity.toDomain()`, `Workout.toEntity()`.

### Hata YÃ¶netimi
- Domain'de Ã¶zel hata hierarchy'si:
```kotlin
sealed class AppError : Exception() {
    data object NetworkUnavailable : AppError()
    data class ValidationError(val field: String, val reason: String) : AppError()
    data class DatabaseError(override val cause: Throwable) : AppError()
    data class AiEngineError(override val cause: Throwable) : AppError()
}
```
- UI'da hata mesajlarÄ± **TÃ¼rkÃ§e, kullanÄ±cÄ± dostu**. Teknik hata mesajÄ± kullanÄ±cÄ±ya gÃ¶sterilmez.
- Logging: `Timber` kullan, `Log.d` yasak. Production build'de Timber.DebugTree kapalÄ±.

### Test KurallarÄ±
- Her UseCase iÃ§in unit test (mock'lanmÄ±ÅŸ repository ile).
- ViewModel testleri: `Turbine` ile Flow collection.
- Repository testleri: fake DAO ve fake Supabase client ile integration test.
- UI testleri sadece **kritik akÄ±ÅŸlar** iÃ§in: onboarding, antrenman kaydetme, yemek ekleme.
- Minimum coverage hedefi: `core:domain` **%80**, `core:data` **%60**, feature modÃ¼ller **%40**.

---

## 6. KlasÃ¶r YapÄ±sÄ±

```
fitlogic-ai/
â”œâ”€â”€ app/                                    # Uygulama giriÅŸ noktasÄ±
â”‚   â”œâ”€â”€ src/main/
â”‚   â”‚   â”œâ”€â”€ kotlin/com/fitlogic/ai/
â”‚   â”‚   â”‚   â”œâ”€â”€ FitLogicApp.kt              # @HiltAndroidApp
â”‚   â”‚   â”‚   â”œâ”€â”€ MainActivity.kt             # Tek activity
â”‚   â”‚   â”‚   â””â”€â”€ navigation/
â”‚   â”‚   â”‚       â””â”€â”€ FitLogicNavHost.kt
â”‚   â”‚   â””â”€â”€ AndroidManifest.xml
â”‚   â””â”€â”€ build.gradle.kts
â”‚
â”œâ”€â”€ core/
â”‚   â”œâ”€â”€ designsystem/                       # Tema, renkler, ortak Composable'lar
â”‚   â”‚   â””â”€â”€ src/main/kotlin/com/fitlogic/ai/core/designsystem/
â”‚   â”‚       â”œâ”€â”€ theme/
â”‚   â”‚       â”‚   â”œâ”€â”€ Color.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ Type.kt
â”‚   â”‚       â”‚   â””â”€â”€ Theme.kt
â”‚   â”‚       â””â”€â”€ component/
â”‚   â”‚           â”œâ”€â”€ FlButton.kt
â”‚   â”‚           â”œâ”€â”€ FlTextField.kt
â”‚   â”‚           â”œâ”€â”€ FlCard.kt
â”‚   â”‚           â””â”€â”€ FlLoadingIndicator.kt
â”‚   â”‚
â”‚   â”œâ”€â”€ common/                             # Ortak yardÄ±mcÄ±lar, extensions
â”‚   â”‚   â””â”€â”€ src/main/kotlin/com/fitlogic/ai/core/common/
â”‚   â”‚       â”œâ”€â”€ Result.kt
â”‚   â”‚       â”œâ”€â”€ AppError.kt
â”‚   â”‚       â”œâ”€â”€ DateExt.kt
â”‚   â”‚       â””â”€â”€ FormulaCalculator.kt        # Mifflin-St Jeor, 1RM vs
â”‚   â”‚
â”‚   â”œâ”€â”€ domain/                             # UseCase'ler, domain modelleri
â”‚   â”‚   â””â”€â”€ src/main/kotlin/com/fitlogic/ai/core/domain/
â”‚   â”‚       â”œâ”€â”€ model/
â”‚   â”‚       â”‚   â”œâ”€â”€ User.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ Workout.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ Exercise.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ Set.kt
â”‚   â”‚       â”‚   â””â”€â”€ FoodEntry.kt
â”‚   â”‚       â”œâ”€â”€ repository/                 # Interface'ler
â”‚   â”‚       â”‚   â”œâ”€â”€ UserRepository.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ WorkoutRepository.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ NutritionRepository.kt
â”‚   â”‚       â”‚   â””â”€â”€ AiInsightRepository.kt
â”‚   â”‚       â””â”€â”€ usecase/
â”‚   â”‚           â”œâ”€â”€ workout/
â”‚   â”‚           â”œâ”€â”€ nutrition/
â”‚   â”‚           â”œâ”€â”€ stats/
â”‚   â”‚           â””â”€â”€ ai/
â”‚   â”‚
â”‚   â”œâ”€â”€ data/                               # Room, Supabase, Repository impl
â”‚   â”‚   â””â”€â”€ src/main/kotlin/com/fitlogic/ai/core/data/
â”‚   â”‚       â”œâ”€â”€ local/
â”‚   â”‚       â”‚   â”œâ”€â”€ FitLogicDatabase.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ entity/
â”‚   â”‚       â”‚   â”œâ”€â”€ dao/
â”‚   â”‚       â”‚   â””â”€â”€ mapper/
â”‚   â”‚       â”œâ”€â”€ remote/
â”‚   â”‚       â”‚   â”œâ”€â”€ SupabaseClient.kt
â”‚   â”‚       â”‚   â””â”€â”€ dto/
â”‚   â”‚       â”œâ”€â”€ repository/                 # Impl
â”‚   â”‚       â”œâ”€â”€ sync/
â”‚   â”‚       â”‚   â””â”€â”€ SyncWorker.kt
â”‚   â”‚       â””â”€â”€ di/
â”‚   â”‚           â””â”€â”€ DataModule.kt
â”‚   â”‚
â”‚   â””â”€â”€ ai/                                 # AI motoru
â”‚       â””â”€â”€ src/main/kotlin/com/fitlogic/ai/core/ai/
â”‚           â”œâ”€â”€ AiEngine.kt                 # Interface
â”‚           â”œâ”€â”€ GemmaAiEngine.kt            # MediaPipe impl
â”‚           â”œâ”€â”€ RuleBasedEngine.kt          # Lite mod fallback
â”‚           â”œâ”€â”€ prompt/
â”‚           â”‚   â”œâ”€â”€ PromptBuilder.kt
â”‚           â”‚   â””â”€â”€ templates/
â”‚           â”‚       â”œâ”€â”€ WeeklyReportTemplate.kt
â”‚           â”‚       â”œâ”€â”€ PlateauTemplate.kt
â”‚           â”‚       â””â”€â”€ PostWorkoutTemplate.kt
â”‚           â”œâ”€â”€ ModelDownloader.kt
â”‚           â””â”€â”€ di/
â”‚               â””â”€â”€ AiModule.kt
â”‚
â”œâ”€â”€ feature/
â”‚   â”œâ”€â”€ onboarding/
â”‚   â”œâ”€â”€ auth/
â”‚   â”œâ”€â”€ home/
â”‚   â”œâ”€â”€ workout/
â”‚   â”‚   â””â”€â”€ src/main/kotlin/com/fitlogic/ai/feature/workout/
â”‚   â”‚       â”œâ”€â”€ list/
â”‚   â”‚       â”‚   â”œâ”€â”€ WorkoutListScreen.kt
â”‚   â”‚       â”‚   â”œâ”€â”€ WorkoutListViewModel.kt
â”‚   â”‚       â”‚   â””â”€â”€ WorkoutListUiState.kt
â”‚   â”‚       â”œâ”€â”€ active/                     # Aktif antrenman
â”‚   â”‚       â”œâ”€â”€ detail/
â”‚   â”‚       â””â”€â”€ navigation/
â”‚   â”‚           â””â”€â”€ WorkoutNavigation.kt
â”‚   â”œâ”€â”€ nutrition/
â”‚   â”œâ”€â”€ exercises/                          # Egzersiz kÃ¼tÃ¼phanesi
â”‚   â”œâ”€â”€ stats/
â”‚   â”œâ”€â”€ coach/                              # AI koÃ§ ekranlarÄ±
â”‚   â””â”€â”€ profile/
â”‚
â”œâ”€â”€ build-logic/                            # Gradle convention plugins
â”‚   â””â”€â”€ convention/
â”‚       â””â”€â”€ src/main/kotlin/
â”‚           â”œâ”€â”€ AndroidApplicationConventionPlugin.kt
â”‚           â”œâ”€â”€ AndroidLibraryConventionPlugin.kt
â”‚           â”œâ”€â”€ AndroidFeatureConventionPlugin.kt
â”‚           â””â”€â”€ AndroidComposeConventionPlugin.kt
â”‚
â”œâ”€â”€ gradle/
â”‚   â””â”€â”€ libs.versions.toml                  # Version catalog
â”‚
â”œâ”€â”€ config/
â”‚   â”œâ”€â”€ detekt/detekt.yml
â”‚   â””â”€â”€ ktlint/.editorconfig
â”‚
â”œâ”€â”€ docs/
â”‚   â”œâ”€â”€ progress.md                         # Bu dosya
â”‚   â”œâ”€â”€ phaze.md                            # Faz Takip
â”‚   â””â”€â”€ prompts.md                          # AI prompt katalogu
â”‚
â”œâ”€â”€ .github/workflows/
â”‚   â”œâ”€â”€ build.yml
â”‚   â””â”€â”€ release.yml
â”‚
â”œâ”€â”€ build.gradle.kts
â”œâ”€â”€ settings.gradle.kts
â””â”€â”€ README.md
```

**Kural:** Yeni bir feature eklenirken bu yapÄ± bozulmaz. Bir dosyanÄ±n yeri belirsizse bu dokÃ¼manda karar verilir, sonra kod yazÄ±lÄ±r.

---

## 7. Task Breakdown

> **Format:** Her gÃ¶rev bir checkbox. Alt gÃ¶revler nested. TamamlanÄ±nca `[x]` yapÄ±lÄ±r ve yanÄ±na tarih eklenir: `[x] 2026-04-25`.
>
> **Durum simgeleri:** ğŸŸ¢ devam ediyor Â· ğŸŸ¡ bloke Â· ğŸ”´ risk Â· â¸ï¸ duraklatÄ±ldÄ±

### Faz 0 â€” Kurulum (Hafta 0, hazÄ±rlÄ±k)

- [ ] Android Studio Hedgehog+ kurulumu, Kotlin 2.0 aktif
- [ ] Git repo init + `.gitignore` (Android template)
- [ ] Supabase projesi oluÅŸtur (free tier)
- [ ] Supabase local development (docker) kurulumu
- [ ] Figma'da 5 kritik ekran wireframe: Home, Aktif Antrenman, Beslenme, AI KoÃ§, Profil
- [ ] `gradle/libs.versions.toml` version catalog
- [ ] `build-logic/` convention plugins (Application, Library, Feature, Compose)
- [ ] Ktlint + Detekt konfigÃ¼rasyonu
- [ ] GitHub Actions CI: PR'larda ktlint + test + build
- [ ] MediaPipe LLM + Gemma 2B Ã¶rnek projesinin hedef cihazda Ã§alÄ±ÅŸtÄ±ÄŸÄ±nÄ±n POC'u

---

### Faz 1 â€” Temel & Design System (Hafta 1-2)

**AmaÃ§:** Projenin iskeleti, tema, ortak bileÅŸenler. BoÅŸ ama derleniyor.

- [ ] Multi-module Gradle yapÄ±sÄ± (10 modÃ¼l)
  - [ ] `:app`
  - [ ] `:core:designsystem`
  - [ ] `:core:common`
  - [ ] `:core:domain`
  - [ ] `:core:data`
  - [ ] `:core:ai`
  - [ ] Feature modÃ¼llerin iskeleti (boÅŸ)
- [ ] Hilt entegrasyonu, `FitLogicApp.kt`
- [ ] Material 3 tema
  - [ ] Renk paleti (light + dark)
  - [ ] Tipografi (Inter veya system default)
  - [ ] Shape tanÄ±mlarÄ±
- [ ] Ortak Composable'lar
  - [ ] `FlButton` (Primary, Secondary, Text variantlarÄ±)
  - [ ] `FlTextField`
  - [ ] `FlCard`
  - [ ] `FlLoadingIndicator`
  - [ ] `FlEmptyState`
  - [ ] `FlErrorState`
- [ ] Navigation iskeleti: `FitLogicNavHost`, type-safe routes
- [ ] Bottom navigation (4 sekme)
- [ ] Ä°lk Compose Preview'lerin Ã§alÄ±ÅŸtÄ±ÄŸÄ±nÄ±n doÄŸrulanmasÄ±
- [ ] Timber kurulumu (sadece debug build)

---

### Faz 2 â€” Kimlik & Veri KatmanÄ± (Hafta 3-4)

**AmaÃ§:** KullanÄ±cÄ± kayÄ±t olabiliyor, profil bilgileri kaydediliyor.

- [ ] Room DB kurulumu: `FitLogicDatabase`
  - [ ] `UserEntity` + `UserDao`
  - [ ] Migration altyapÄ±sÄ±
  - [ ] Åema JSON export
- [ ] Supabase SDK entegrasyonu
  - [ ] `SupabaseClient` singleton
  - [ ] Auth provider: Email/Password + Google Sign-In
- [ ] Onboarding akÄ±ÅŸÄ± (4 adÄ±m)
  - [ ] HoÅŸ geldin ekranÄ±
  - [ ] Temel bilgiler (cinsiyet, yaÅŸ, boy, kilo, aktivite)
  - [ ] Hedef seÃ§imi (kilo ver / form koru / kas yap)
  - [ ] Ä°lk Ã¶neri sunumu
- [ ] Hedef kalori & makro hesaplayÄ±cÄ± (Mifflin-St Jeor)
- [ ] Auth ekranlarÄ±: GiriÅŸ, KayÄ±t, Åifre sÄ±fÄ±rlama
- [ ] Misafir modu (local-only, auth opsiyonel)
- [ ] `UserRepository` impl
- [ ] Profil ekranÄ± (gÃ¶rÃ¼ntÃ¼leme + edit)
- [ ] Ayarlar ekranÄ± (tema, dil, birim, veri dÄ±ÅŸa aktar, hesap sil)

---

### Faz 3 â€” Antrenman MVP (Hafta 5-6)

**AmaÃ§:** KullanÄ±cÄ± boÅŸ bir antrenman baÅŸlatÄ±p set girebilsin.

- [ ] DB ÅŸemasÄ±
  - [ ] `WorkoutEntity` + `WorkoutDao`
  - [ ] `WorkoutExerciseEntity` + dao
  - [ ] `SetEntity` + dao
  - [ ] `ExercisesCatalogEntity` + dao (read-only, pre-populated)
- [ ] Egzersiz kÃ¼tÃ¼phanesi dataset (JSON) â€” ilk 50 egzersiz, tam set Faz 4'te
- [ ] `WorkoutRepository` impl
- [ ] UseCase'ler
  - [ ] `StartWorkoutUseCase`
  - [ ] `FinishWorkoutUseCase`
  - [ ] `AddExerciseToWorkoutUseCase`
  - [ ] `SaveSetUseCase`
  - [ ] `GetWorkoutHistoryUseCase`
- [ ] Ekranlar
  - [ ] Antrenman BaÅŸlat (boÅŸ / ÅŸablon / geÃ§miÅŸi tekrarla)
  - [ ] Aktif Antrenman ekranÄ±
    - [ ] Egzersiz ekleme
    - [ ] Set input (aÄŸÄ±rlÄ±k + rep)
    - [ ] Bir Ã¶nceki oturumdan referans deÄŸer
    - [ ] Set kopyalama butonu
  - [ ] Dinlenme timer (modal bottom sheet)
  - [ ] Antrenman bitir akÄ±ÅŸÄ± + Ã¶zet
  - [ ] Antrenman GeÃ§miÅŸi (takvim + liste)
  - [ ] Antrenman Detay
- [ ] PR tespiti mantÄ±ÄŸÄ± (`sets.is_pr` flag)
- [ ] Denormalize `workouts.total_volume` gÃ¼ncelleme

---

### Faz 4 â€” Egzersiz KÃ¼tÃ¼phanesi (Hafta 7)

**AmaÃ§:** Yeni baÅŸlayan iÃ§in kapsamlÄ± offline egzersiz rehberi.

- [ ] Egzersiz dataset'ini 150-200'e tamamla
  - [ ] Kas grubu etiketleri
  - [ ] Ekipman etiketleri
  - [ ] Zorluk seviyesi
  - [ ] AdÄ±m adÄ±m uygulama (yeni baÅŸlayan dili)
  - [ ] YaygÄ±n hatalar bÃ¶lÃ¼mÃ¼
  - [ ] GIF dosyalarÄ± (hafif, ~200KB/egzersiz)
  - [ ] Alternatif egzersiz eÅŸlemeleri
- [ ] Egzersiz KÃ¼tÃ¼phanesi ekranÄ±
  - [ ] Arama
  - [ ] Kas grubu filtreleri
  - [ ] Ekipman filtreleri
  - [ ] Zorluk filtresi
- [ ] Egzersiz Detay ekranÄ±
  - [ ] GIF oynatÄ±cÄ± (Coil ile)
  - [ ] AÃ§Ä±klama sekmesi
  - [ ] YaygÄ±n hatalar sekmesi
  - [ ] Alternatifler sekmesi
  - [ ] "Bu egzersizi ekle" CTA

---

### Faz 5 â€” Beslenme MVP (Hafta 8-9)

**AmaÃ§:** KullanÄ±cÄ± yemek ekleyebilsin, barkod tarayabilsin, gÃ¼nlÃ¼k makrolarÄ± gÃ¶rebilsin.

- [ ] DB ÅŸemasÄ±
  - [ ] `FoodsCatalogEntity` + dao
  - [ ] `FoodEntryEntity` + dao
  - [ ] `WaterEntryEntity` + dao
- [ ] Temel yemek dataset'i (~500 yerel yemek, TÃ¼rk mutfaÄŸÄ± odaklÄ±)
- [ ] OpenFoodFacts API client
- [ ] ML Kit Barcode entegrasyonu
- [ ] `NutritionRepository` impl
- [ ] UseCase'ler
  - [ ] `AddFoodEntryUseCase`
  - [ ] `SearchFoodUseCase`
  - [ ] `ScanBarcodeUseCase`
  - [ ] `GetDailyMacrosUseCase`
  - [ ] `AddWaterUseCase`
- [ ] Ekranlar
  - [ ] GÃ¼nlÃ¼k Beslenme (makro halkalarÄ±, Ã¶ÄŸÃ¼n listeleri)
  - [ ] Yemek Ekle (arama/barkod/favori/son sekmeleri)
  - [ ] Yemek Detay (porsiyon ayarla)
  - [ ] Barkod TarayÄ±cÄ± (kamera + overlay)
  - [ ] Manuel Yemek Ekleme (barkod bulunamazsa)
  - [ ] Su Takibi widget'Ä±
- [ ] Favoriler sistemi

---

### Faz 6 â€” Ä°statistik & Grafikler (Hafta 10)

**AmaÃ§:** KullanÄ±cÄ± geliÅŸimini grafiklerle gÃ¶rebilsin.

- [ ] Vico Charts entegrasyonu
- [ ] `StatsRepository` (query-only)
- [ ] UseCase'ler
  - [ ] `GetExerciseProgressUseCase`
  - [ ] `GetWeeklyVolumeUseCase`
  - [ ] `GetMuscleGroupDistributionUseCase`
  - [ ] `GetPRHistoryUseCase`
  - [ ] `GetWeightTrendUseCase`
- [ ] Ekranlar
  - [ ] Ä°lerleme Ana (haftalÄ±k Ã¶zet kartlarÄ±)
  - [ ] Egzersiz Ä°lerlemesi (hacim + en iyi aÄŸÄ±rlÄ±k grafiÄŸi)
  - [ ] VÃ¼cut AÄŸÄ±rlÄ±ÄŸÄ± Takibi (gÃ¼nlÃ¼k ekle + trend)
  - [ ] Kas Grubu DaÄŸÄ±lÄ±mÄ± (pie/donut)
  - [ ] PR Listesi
- [ ] HaftalÄ±k Ã¶zet kartÄ± (paylaÅŸÄ±labilir, gelecekte Instagram story formatÄ±)

---

### Faz 7 â€” AI Entegrasyonu (Hafta 11-12)

**AmaÃ§:** Gemma 2B cihazda Ã§alÄ±ÅŸÄ±yor, haftalÄ±k rapor Ã¼retiyor.

- [ ] `AiEngine` interface
- [ ] `GemmaAiEngine` (MediaPipe impl)
  - [ ] Model yÃ¼kleme
  - [ ] Tokenization
  - [ ] Streaming response
- [ ] `RuleBasedEngine` (Lite mod fallback)
- [ ] `ModelDownloader`
  - [ ] Progress UI
  - [ ] Pause/resume
  - [ ] WiFi-only opsiyonu
- [ ] RAM tespiti â†’ mod seÃ§imi
- [ ] `PromptBuilder` + ÅŸablonlar
  - [ ] HaftalÄ±k rapor ÅŸablonu
  - [ ] Plato tespiti ÅŸablonu
  - [ ] Post-workout yorumu ÅŸablonu
  - [ ] Beslenme analizi ÅŸablonu
- [ ] UseCase'ler
  - [ ] `GenerateWeeklyReportUseCase`
  - [ ] `DetectPlateauUseCase`
  - [ ] `GetPostWorkoutInsightUseCase`
- [ ] `AiInsightRepository` + DB tablosu
- [ ] AI KoÃ§ ekranÄ±
  - [ ] Aktif insight listesi
  - [ ] Insight detay
  - [ ] Okundu iÅŸaretleme
  - [ ] HaftalÄ±k rapor gÃ¶rÃ¼ntÃ¼leme
- [ ] Cihaz performans profiling (hedef: <3 sn inference)

---

### Faz 8 â€” Gamification & Bildirimler (Hafta 13)

**AmaÃ§:** KullanÄ±cÄ± geri gelmek iÃ§in motive olsun.

- [ ] Streak sistemi
  - [ ] `StreakCalculator`
  - [ ] Home widget
- [ ] Rozetler
  - [ ] `AchievementEntity` + dao
  - [ ] 20 rozet tanÄ±mlanmasÄ±
  - [ ] `CheckAchievementsUseCase` (her iÅŸlem sonrasÄ± Ã§aÄŸrÄ±lÄ±r)
  - [ ] Rozet aÃ§Ä±lma animasyonu
  - [ ] Rozetler ekranÄ±
- [ ] HaftalÄ±k hedefler
  - [ ] Hedef belirleme
  - [ ] Progress tracker
- [ ] Push bildirim altyapÄ±sÄ± (FCM)
- [ ] Bildirim tipleri
  - [ ] Antrenman hatÄ±rlatÄ±cÄ±sÄ±
  - [ ] Su hatÄ±rlatÄ±cÄ±sÄ±
  - [ ] HaftalÄ±k rapor
  - [ ] PR kutlamasÄ±
  - [ ] Streak koruma uyarÄ±sÄ±
- [ ] Bildirim ayarlarÄ± ekranÄ± (her tipi aÃ§/kapa)

---

### Faz 9 â€” Senkronizasyon (Hafta 14)

**AmaÃ§:** Online olduÄŸunda veri gÃ¼venle Supabase'e yedekleniyor.

- [ ] Supabase Postgres ÅŸemasÄ± (Kotlin entity'leriyle aynÄ±)
- [ ] RLS (Row-Level Security) policy'leri
- [ ] `SyncWorker` (WorkManager)
- [ ] Sync stratejisi
  - [ ] PENDING kayÄ±tlarÄ± batch'le gÃ¶nder
  - [ ] Conflict resolution (last-write-wins)
  - [ ] Retry with exponential backoff
- [ ] Offline davranÄ±ÅŸ doÄŸrulamasÄ± (airplane mode testi)
- [ ] Ã‡oklu cihaz senaryosu testi
- [ ] Sync durum gÃ¶stergesi (profil ekranÄ±nda)
- [ ] Ä°lk giriÅŸ: sunucudan veri Ã§ekme akÄ±ÅŸÄ±

---

### Faz 10 â€” Polish, Test, Beta (Hafta 15-16)

**AmaÃ§:** YayÄ±na hazÄ±r, stabil, gÃ¼zel.

- [ ] Animasyonlar
  - [ ] Ekran geÃ§iÅŸleri
  - [ ] Rozet aÃ§Ä±lma
  - [ ] Makro halka dolumu
  - [ ] PR kutlama
- [ ] EriÅŸilebilirlik
  - [ ] Content descriptions
  - [ ] Minimum dokunma alanÄ± 48dp
  - [ ] YazÄ± boyutu ayarÄ± desteÄŸi
- [ ] Performance profiling
  - [ ] BaÅŸlangÄ±Ã§ sÃ¼resi <2 sn
  - [ ] Set kaydetme <500 ms
  - [ ] AI inference <3 sn
  - [ ] APK boyutu <80 MB (model hariÃ§)
- [ ] Kritik akÄ±ÅŸlar iÃ§in Compose UI testi
  - [ ] Onboarding tamamlama
  - [ ] Antrenman kaydetme
  - [ ] Yemek ekleme (arama + barkod)
  - [ ] AI insight Ã¼retme
- [ ] Crash reporting (Firebase Crashlytics)
- [ ] Play Store listesi hazÄ±rlanmasÄ±
  - [ ] Ekran gÃ¶rÃ¼ntÃ¼leri
  - [ ] AÃ§Ä±klama metni
  - [ ] Gizlilik politikasÄ± (on-device AI vurgusu)
- [ ] Internal test release
- [ ] 20-30 beta kullanÄ±cÄ± geri bildirimi
- [ ] Kritik bug fix round
- [ ] Closed beta â†’ Open beta

---

## 8. Bilinen Sorunlar & Ã‡Ã¶zÃ¼mleri

> Bu bÃ¶lÃ¼m geliÅŸtirme sÄ±rasÄ±nda bÃ¼yÃ¼yecek. Her sorun: **Belirti â†’ Sebep â†’ Ã‡Ã¶zÃ¼m** formatÄ±nda.

### Åablon
```
### [KOD] KÄ±sa baÅŸlÄ±k
**Belirti:** Ne gÃ¶zlemleniyor?
**Sebep:** KÃ¶k neden.
**Ã‡Ã¶zÃ¼m:** NasÄ±l Ã§Ã¶zÃ¼ldÃ¼ / workaround.
**Tarih:** 2026-XX-XX
```

### Muhtemel Sorunlar (Ã–nden Notlar)

#### [AI-01] MediaPipe LLM emÃ¼latÃ¶rde Ã§alÄ±ÅŸmÄ±yor
**Belirti:** Gemma 2B emÃ¼latÃ¶rde yÃ¼klenmiyor veya Ã§ok yavaÅŸ.
**Sebep:** EmÃ¼latÃ¶r GPU delegation desteÄŸi kÄ±sÄ±tlÄ±.
**Ã‡Ã¶zÃ¼m:** GeliÅŸtirme boyunca fiziksel cihaz kullan (en az Snapdragon 7 Gen 1). EmÃ¼latÃ¶rde `RuleBasedEngine` (Lite mod) test et.

#### [DB-01] Room migration sÄ±rasÄ±nda veri kaybÄ± riski
**Belirti:** Schema deÄŸiÅŸince eski DB ile Ã§akÄ±ÅŸma.
**Sebep:** `fallbackToDestructiveMigration()` aktif kalÄ±rsa.
**Ã‡Ã¶zÃ¼m:** Production build'de **asla** `fallbackToDestructiveMigration` kullanma. Her ÅŸema deÄŸiÅŸikliÄŸi iÃ§in `Migration` yaz ve `MigrationTestHelper` ile test et.

#### [SYNC-01] AynÄ± kullanÄ±cÄ±nÄ±n iki cihazda farklÄ± timestamp'leri
**Belirti:** AynÄ± kullanÄ±cÄ± iki cihazdan giriÅŸ yapÄ±nca veri karÄ±ÅŸÄ±yor.
**Sebep:** Cihaz saatleri farklÄ± olabilir.
**Ã‡Ã¶zÃ¼m:** `updated_at` iÃ§in cihaz saati deÄŸil, sunucu saati (Supabase `now()`) referans alÄ±nmalÄ± â€” yazma baÅŸarÄ±lÄ± olunca sunucudan dÃ¶nen timestamp'i lokal DB'ye yaz.

#### [UX-01] Set input'ta klavye ekranÄ± kapatÄ±yor
**Belirti:** AÄŸÄ±rlÄ±k girerken input alanÄ± klavyenin altÄ±nda kalÄ±yor.
**Sebep:** `imePadding` unutulmuÅŸ.
**Ã‡Ã¶zÃ¼m:** Aktif antrenman ekranÄ±nda `Modifier.imePadding()` + `Scaffold.contentWindowInsets = WindowInsets(0)`.

#### [AI-02] Gemma halÃ¼sinasyonu
**Belirti:** Model saÄŸlÄ±k aÃ§Ä±sÄ±ndan tehlikeli tavsiye verebilir.
**Sebep:** TÃ¼m LLM'lerin doÄŸasÄ±.
**Ã‡Ã¶zÃ¼m:** (1) System prompt'ta katÄ± sÄ±nÄ±rlar. (2) SakatlÄ±k/aÄŸrÄ± geÃ§en promptlar iÃ§in fallback: "AÄŸrÄ± hissediyorsan bir saÄŸlÄ±k profesyoneline danÄ±ÅŸ". (3) AI ekranlarÄ±nda alt bilgi: "Bu tavsiyeler genel niteliklidir, tÄ±bbi tavsiye yerine geÃ§mez".

---

## 9. Oturum GÃ¼nlÃ¼ÄŸÃ¼

> Her geliÅŸtirme oturumu sonunda kÄ±sa not. Format:
>
> ```
> ### 2026-MM-DD â€” [BaÅŸlÄ±k]
> **SÃ¼re:** 2 saat
> **YapÄ±lanlar:**
> - Madde 1
> - Madde 2
>
> **Kararlar:**
> - ADR-XXX'e eklendi / yeni karar alÄ±ndÄ±
>
> **Sonraki oturum:**
> - Ne yapÄ±lacak
>
> **Blokerler:**
> - Varsa
> ```

### 2026-04-21 â€” Proje kurulumu ve planlama
**SÃ¼re:** â€”
**YapÄ±lanlar:**
- Teknik taslak Ã¼zerine detaylÄ± uygulama planÄ± dÃ¶kÃ¼manÄ± hazÄ±rlandÄ±
- `progress.md` oluÅŸturuldu (bu dosya)
- 7 ADR belgelendi
- 16 haftalÄ±k task breakdown oluÅŸturuldu

**Kararlar:**
- Clean Architecture + Multi-module yapÄ± (ADR-001, ADR-002)
- Offline-first senkronizasyon (ADR-003)
- AI opsiyonel + Lite mod (ADR-004)

**Sonraki oturum:**
- Faz 0 kurulum gÃ¶revlerine baÅŸla
- Figma wireframe'ler
- Supabase projesini oluÅŸtur
- MediaPipe LLM POC'u hedef cihazda test et

**Blokerler:**
- Yok

---

<!--
### YYYY-MM-DD â€” [BaÅŸlÄ±k]
**SÃ¼re:**
**YapÄ±lanlar:**
-

**Kararlar:**
-

**Sonraki oturum:**
-

**Blokerler:**
-

---
-->

## ğŸ“Œ HatÄ±rlatÄ±cÄ±lar

- [ ] Her oturumda baÅŸlangÄ±Ã§ta bu dÃ¶kÃ¼manÄ± aÃ§
- [ ] Bir task tamamlanÄ±nca `[x]` + tarih iÅŸaretle
- [ ] Mimari karar alÄ±rken yeni ADR ekle
- [ ] Sorun Ã§Ã¶zÃ¼ldÃ¼ÄŸÃ¼nde Bilinen Sorunlar'a ekle
- [ ] HaftalÄ±k olarak "Mevcut faz" ve "Son gÃ¼ncelleme" alanlarÄ±nÄ± revize et

