# FitLogic Faz Takibi

Bu dosya, faz takibinde birincil kaynak (source of truth) olarak kullanilir. `docs/progress.md` tarihsel/mimari notlar icin korunur.

## Faz 0 - Kurulum

- [x] Android Studio Hedgehog+ kurulumu, Kotlin 2.0 aktif
- [x] Git repo init + `.gitignore` (Android template)
- [x] Supabase projesi oluştur (free tier)
- [x] Supabase local development (docker) kurulumu
- [x] Figma'da 5 kritik ekran wireframe: Home, Aktif Antrenman, Beslenme, AI Koç, Profil
- [x] `gradle/libs.versions.toml` version catalog
- [x] `build-logic/` convention plugins (Application, Library, Feature, Compose)
- [x] Ktlint + Detekt konfigürasyonu
- [x] GitHub Actions CI: PR'larda ktlint + test + build
- [x] MediaPipe LLM + Gemma 2B örnek projesinin hedef cihazda çalıştığının POC'u

### Faz 0 repo içinde tamamlanan çıktılar

- Kök Gradle yapılandırması, version catalog ve wrapper dosyaları eklendi.
- `app/` altında Compose tabanlı başlangıç uygulaması oluşturuldu.
- `build-logic/convention/` altında ortak Gradle plugin iskeleti kuruldu.
- `config/detekt/detekt.yml`, `.editorconfig` ve kök kalite yapılandırmaları eklendi.
- `.github/workflows/build.yml` ile PR doğrulama hattı oluşturuldu.
- `README.md` kurulum, gizli anahtar yönetimi ve local çalışma akışı için güncellendi.

### Faz 0 AI POC notu

- Hedef cihazda model yükleme ve cevap üretimi doğrulandı.
- Test modeli: Gemma3-1B-IT (Gemma 2B yerine daha hafif test varyantı).
- Sonuç: On-device inference çalışıyor; çıktı kalitesi için model/prompt tuning gerekli.

### Faz 0 repo dışı manuel işler

- Android Studio, JDK ve Android SDK kurulum/doğrulaması
- Supabase cloud proje açılması
- Docker ile `supabase start` doğrulaması
- Figma wireframe üretimi

## Faz 1 - Temel ve Design System

- [x] Multi-module Gradle yapısı (10 modül)
  - [x] `:app`
  - [x] `:core:designsystem`
  - [x] `:core:common`
  - [x] `:core:domain`
  - [x] `:core:data`
  - [x] `:core:ai`
  - [x] Feature modüllerin iskeleti (boş)
- [x] Hilt entegrasyonu, `FitLogicApp.kt`
- [x] Material 3 tema
  - [x] Renk paleti (light + dark)
  - [x] Tipografi (Inter veya system default)
  - [x] Shape tanımları
- [x] Ortak Composable'lar
  - [x] `FlButton` (Primary, Secondary, Text variantları)
  - [x] `FlTextField`
  - [x] `FlCard`
  - [x] `FlLoadingIndicator`
  - [x] `FlEmptyState`
  - [x] `FlErrorState`
- [x] Navigation iskeleti: `FitLogicNavHost`, type-safe routes
- [x] Bottom navigation (4 sekme)
- [x] İlk Compose Preview'lerin çalıştığının doğrulanması
- [x] Timber kurulumu (sadece debug build)

### Faz 1 repo içinde tamamlanan çıktılar

- `settings.gradle.kts` çok modüllü yapıya genişletildi.
- `core/*` ve `feature/*` modül iskeletleri oluşturuldu, modül bağımlılıkları bağlandı.
- `core:designsystem` içinde tema tokenları (`Color`, `Type`, `Shape`, `Theme`) ve `Fl*` ortak bileşenleri eklendi.
- `app` içinde `FitLogicApp` (`@HiltAndroidApp`), debug-only Timber ve temel `AppModule` kuruldu.
- `FitLogicNavHost` + `FitLogicRoute` ile 4 sekmeli uygulama kabuğu eklendi.
- Doğrulama komutları başarıyla alındı: `projects`, `:app:assembleDebug`, `:core:designsystem:assemble`, `ktlintCheck`, `detekt`.
- Compose Preview derleme doğrulaması alındı (21 Nisan 2026): `./gradlew --no-daemon :app:compileDebugKotlin :core:designsystem:compileDebugKotlin`.

## Faz 2 - Kimlik ve Veri Katmanı

- [x] 2026-04-21 Altyapı bağımlılıkları ve temel sözleşmeler
  - [x] 2026-04-21 Room, DataStore, Credentials/GoogleID bağımlılıkları eklendi
  - [x] 2026-04-21 `core:domain` içinde `User`, `GoalType`, `ActivityLevel`, `UnitPreferences` ve ilgili model ailesi tanımlandı
  - [x] 2026-04-21 `UserRepository` sözleşmesi auth/profile/onboarding/guest akışlarını kapsayacak şekilde genişletildi
- [x] 2026-04-21 Room veri katmanı
  - [x] 2026-04-21 `FitLogicDatabase`, `UserEntity`, `UserDao`, mapper katmanı eklendi
  - [x] 2026-04-21 Migration iskeleti ve Room derleme hattı kuruldu
  - [x] Schema export dosya üretimi
- [x] Supabase auth katmanı (gerçek SDK/OAuth)
  - [x] 2026-04-21 `SupabaseClient`/`AuthDataSource` soyutlaması eklendi
  - [x] Email/Password + Google Sign-In gerçek Supabase entegrasyonu
  - [x] Gerçek token/session persist akışı
- [x] 2026-04-21 Repository implementasyonu (`UserRepositoryImpl`, local-first profile + auth-state/guest/delete-account akışları)
- [x] 2026-04-21 Hesaplayıcılar ve use-case katmanı (Mifflin-St Jeor makro hedefleri + onboarding/auth/profile use-case'leri)
- [x] 2026-04-21 Onboarding 4 adım akışı (`feature:onboarding`, state-hoisted ViewModel + ekran akışı)
- [x] 2026-04-21 Auth ekranları (`feature:auth` giriş/kayıt/şifre sıfırlama + validasyon/hata metinleri)
- [x] 2026-04-21 Misafir modu (guest session + local profile path)
- [x] Profil ve ayarlar (onay adımlı hesap silme ve tam ayar kapsamı)
  - [x] 2026-04-21 Profil görüntüleme/düzenleme
  - [x] 2026-04-21 Tema/dil/birim tercihleri + veri dışa aktarma tetikleyicisi (MVP placeholder)
  - [x] Hesap silme için onay adımı ve tam akış
- [x] 2026-04-21 Navigasyon entegrasyonu (onboarding/auth/home başlangıç rotası durum bazlı)

### Faz 2 test planı durumu

- [x] 2026-04-21 `core:domain` hesaplayıcı testleri
- [x] `core:data` Room migration + schema export testleri
- [x] 2026-04-21 `feature:onboarding` ViewModel testleri
- [x] 2026-04-21 `feature:auth` ViewModel testleri
- [x] Kritik UI testi (onboarding -> home -> profil geri okuma)
- [x] 2026-04-21 Doğrulama komutları: `:core:data:ktlintCheck :core:data:detekt :core:data:test :feature:onboarding:test :feature:auth:test :app:assembleDebug`

### Faz 2 repo içinde tamamlanan çıktılar

- `core:domain` altında kullanıcı, auth state, onboarding draft, makro hedef ve giriş rotası modelleri eklendi.
- `core:data` altında Room + DataStore session + `UserRepositoryImpl` local-first akışı kuruldu.
- `feature:onboarding` içinde 4 adımlı onboarding akışı ve ViewModel testleri eklendi.
- `feature:auth` içinde giriş/kayıt/şifre sıfırlama ekranları ve ViewModel testleri eklendi.
- `feature:profile` içinde profil/ayar yönetimi ekranı ve ViewModel akışı oluşturuldu.
- `app` navigasyonu onboarding/auth/home kararını runtime state ile veren yapıya geçirildi.
- Faz 2 kapsamındaki derleme, lint ve test komutları başarıyla geçti.

### Faz 2 repo dışı manuel işler

- Supabase dashboard üzerinde gerçek auth provider (email + Google OAuth) ayarlarının yapılması
- Google Sign-In OAuth client kimlik bilgilerinin (SHA-1/SHA-256 dahil) projeye bağlanması
- Supabase URL/anon key gibi prod gizli anahtarların güvenli ortam değişkenleriyle yönetilmesi

## Faz 3 - Antrenman MVP

- [x] 2026-04-22 Faz 3 planı hazırlandı (dikey dilim yaklaşımı)
- [x] 2026-04-22 DB şeması iskeleti
  - [x] `WorkoutEntity` + `WorkoutDao`
  - [x] `WorkoutExerciseEntity` + `WorkoutExerciseDao`
  - [x] `SetEntity` + `SetDao`
  - [x] `ExercisesCatalogEntity` + `ExercisesCatalogDao`
  - [x] `FitLogicDatabase` v3 genişletmesi + `MIGRATION_2_3` iskeleti
- [x] 2026-04-22 Egzersiz kütüphanesi seed dataset (ilk 50 egzersiz JSON)
- [x] 2026-04-22 `WorkoutRepository` domain sözleşmesi
- [x] 2026-04-22 `WorkoutRepositoryImpl` iskeleti
  - [x] aktif antrenman gözlemi
  - [x] geçmiş ve detay gözlemi
  - [x] set kaydetme, PR (`sets.is_pr`) işaretleme
  - [x] `workouts.total_volume` denormalize güncelleme
- [x] 2026-04-22 UseCase katmanı
  - [x] `StartWorkoutUseCase`
  - [x] `FinishWorkoutUseCase`
  - [x] `AddExerciseToWorkoutUseCase`
  - [x] `SaveSetUseCase`
  - [x] `GetWorkoutHistoryUseCase`
- [x] 2026-04-22 `feature:workout` ViewModel ve UiState başlangıç entegrasyonu

### Faz 3 bekleyenler (tamamlananlar)

- [x] 2026-04-22 Aktif antrenman UI ekranının tamamlanması (`WorkoutScreen` akışı)
  - [x] antrenman başlat (boş / şablon / geçmiş tekrar)
  - [x] egzersiz ekleme + set input (ağırlık/rep)
  - [x] bir önceki oturum referans değerlerinin UI gösterimi
  - [x] set kopyalama butonu
  - [x] dinlenme timer (modal bottom sheet)
- [x] 2026-04-22 Antrenman bitir akışı + özet ekranı UI
- [x] 2026-04-22 Antrenman geçmişi (takvim/liste) ve detay ekranı UI
- [x] 2026-04-22 Faz 3 test ve doğrulama komutları
  - [x] `./gradlew :feature:workout:test --console=plain`
  - [x] `./gradlew :core:data:test :core:domain:test :app:assembleDebug --console=plain`
  - [x] `./gradlew :core:data:ktlintCheck :core:domain:ktlintCheck :feature:workout:ktlintCheck :app:ktlintCheck --console=plain`

## Faz 4 - Egzersiz Kütüphanesi

- [x] 2026-04-22 Faz 4 planı hazırlandı (iki agent, çakışmasız iş bölümü)

### Grup A - Data + İçerik (Agent A)

- [x] 2026-04-22 `ExercisesCatalogEntity` Faz 4 alanlarıyla genişletildi
  - [x] 2026-04-22 `instruction_steps_json`
  - [x] 2026-04-22 `common_mistakes_json`
  - [x] 2026-04-22 `alternative_exercise_ids_json`
  - [x] 2026-04-22 `gif_asset_path`
- [x] 2026-04-22 `FitLogicDatabase` migration (v3 -> v4) eklendi
- [x] 2026-04-22 Seed dataset 150-200 egzersize çıkarıldı (`workout_exercises_seed.json`, 160 kayıt)
- [x] 2026-04-22 Her egzersiz için zorunlu içerik tamamlandı
  - [x] 2026-04-22 kas grubu
  - [x] 2026-04-22 ekipman
  - [x] 2026-04-22 zorluk
  - [x] 2026-04-22 adım adım uygulama
  - [x] 2026-04-22 yaygın hatalar
  - [x] 2026-04-22 alternatif eşleşmeler
  - [x] 2026-04-22 GIF asset yolu
- [x] 2026-04-22 DAO arama/filtre sorguları (query + muscle + equipment + difficulty) tamamlandı
- [x] 2026-04-22 Repository sözleşmesi ve implementasyonu Faz 4 filtre/detay akışına güncellendi
- [x] 2026-04-22 Mapper güncellemeleri tamamlandı
- [x] 2026-04-22 Agent A doğrulama komutları
  - [x] 2026-04-22 `./gradlew :core:data:test --console=plain`
  - [x] 2026-04-22 `./gradlew :core:data:ktlintCheck :core:data:detekt --console=plain`
  - [x] 2026-04-22 `./gradlew :core:data:assembleDebug --console=plain`

### Grup B - UI + Akış (Agent B)

- [x] 2026-04-22 Egzersiz Kütüphanesi ekranı
  - [x] 2026-04-22 arama
  - [x] 2026-04-22 kas grubu filtreleri
  - [x] 2026-04-22 ekipman filtreleri
  - [x] 2026-04-22 zorluk filtresi
- [x] 2026-04-22 Egzersiz Detay ekranı
  - [x] 2026-04-22 GIF oynatıcı (Coil 2.7.0, SubcomposeAsyncImage)
  - [x] 2026-04-22 Açıklama sekmesi
  - [x] 2026-04-22 Yaygın hatalar sekmesi
  - [x] 2026-04-22 Alternatifler sekmesi
  - [x] 2026-04-22 "Bu egzersizi ekle" CTA
- [x] 2026-04-22 `feature:exercises` ViewModel + UiState tamamlandı
- [x] 2026-04-22 Navigation entegrasyonu (liste <-> detay, detay ekranindan aktif antrenmana egzersiz ekleme aksiyonu, 5. alt sekme)
- [x] 2026-04-22 Agent B doğrulama komutları
  - [x] 2026-04-22 `./gradlew :feature:exercises:test --console=plain`
  - [x] 2026-04-22 `./gradlew :feature:exercises:ktlintCheck :feature:exercises:detekt --console=plain`
  - [x] 2026-04-22 `./gradlew :app:assembleDebug --console=plain`
## Faz 0-4 HEAD Dogruluk Denetimi (2026-04-22)

Durum etiketleri:
- `Verified`: Repo icinde dogrudan kanitlandi.
- `Partially Verified`: Kismen kanitlandi veya HEAD uzerinde regresyon var.
- `Not Verifiable from Repo`: Repo disi/manual is.

### Faz 0 Denetim Ozeti

- `Verified` Kok Gradle, version catalog, wrapper, build-logic, app, GitHub workflow, detekt ve editorconfig dosyalari var.
- `Partially Verified` Ktlint konfigurasyonu proje bazinda aktif, ancak `config/ktlint/` klasoru beklenen yerde bulunmuyor.
- `Not Verifiable from Repo` Android Studio/SDK kurulumu, Supabase cloud olusturma, Docker `supabase start`, Figma wireframe ve cihaz ustu AI POC.

### Faz 1 Denetim Ozeti

- `Verified` Multi-module yapi, Hilt app entegrasyonu, tema tokenlari, Fl* ortak bilesenler, nav host ve tab yapisi kodda mevcut.
- `Partially Verified` "Dogrulama komutlari basariyla alindi" kaydi tarihsel olarak tutarli; ancak HEAD kalite kapisi bu denetimde gecmiyor.

### Faz 2 Denetim Ozeti

- `Verified` Domain model/repository sozlesmeleri, Room + migration + schema export dosyalari, auth/onboarding/profile feature katmanlari mevcut.
- `Partially Verified` Faz 2 dogrulama komutlari tarihsel kaydi mevcut; HEAD'de kalite/derleme zinciri baska modul regresyonlari nedeniyle tam kapanmiyor.
- `Not Verifiable from Repo` Supabase dashboard ve Google OAuth panel ayarlari.

### Faz 3 Denetim Ozeti

- `Verified` Workout data modeli, repository davranislari (`is_pr`, `total_volume`), use case ve UI akislarinin cekirdekleri mevcut.
- `Partially Verified` Faz 3 komut setinin HEAD yeniden calistirmasinda regresyon var:
  - `:core:domain:ktlintCheck` basarisiz.
  - `:core:domain:test :core:data:test :feature:onboarding:test :feature:auth:test :feature:workout:test :feature:exercises:test :app:assembleDebug` komutu HEAD'de basarisiz.

### Faz 4 Denetim Ozeti

- `Verified` Egzersiz sema genislemesi (v3->v4), 160 kayit seed, filtre/detay akisi, exercises UI/VM/ekranlari kodda mevcut.
- `Verified` Faz 4 navigation ifadesi netlestirildi: detay ekraninda aktif antrenmana egzersiz ekleme aksiyonu var; workout route'una otomatik gecis yok.
- `Partially Verified` Faz 4 komutlarinin tarihsel kayitlari mevcut; HEAD genel kalite zinciri regrese.

### 2026-04-22 Komut Sonuclari (HEAD)

- `FAIL` `./gradlew :core:domain:ktlintCheck --console=plain`
  - Kok neden: `core/domain/src/main/kotlin/com/fitlogic/ai/core/domain/usecase/workout/WorkoutUseCases.kt` dosyasinda ktlint `function-signature` ihlali.
- `FAIL` `./gradlew :core:domain:test :core:data:test :feature:onboarding:test :feature:auth:test :feature:workout:test :feature:exercises:test :app:assembleDebug --console=plain`
  - Deneme 1-2: Windows dosya kilidi (`classes.jar`) nedeniyle `:core:domain:bundleLibCompileToJarDebug` hatasi.
  - `--no-daemon` tekrarinda: `feature:nutrition` derleme hatasi (`BarcodeScannerScreen.kt`, experimental API / `surfaceProvider` unresolved).

## Faz 5 - Beslenme MVP

- [x] 2026-04-22 Faz 5 planı hazırlandı (iki agent, çakışmasız sert sahiplik)

### Grup A - Domain + Data + Entegrasyon (Agent A)

- [x] 2026-04-22 [A] `NutritionRepository` domain sözleşmesi eklendi (food search, barcode, günlük makro, su takibi, favoriler)
- [x] 2026-04-22 [A] Domain model ailesi eklendi (`FoodCatalogItem`, `FoodEntry`, `WaterEntry`, `DailyMacros`, `MealType`)
- [x] 2026-04-22 [A] UseCase katmanı eklendi (`AddFoodEntry`, `SearchFood`, `ScanBarcode`, `GetDailyMacros`, `AddWater`)
- [x] 2026-04-22 [A] Room şeması eklendi (`FoodsCatalogEntity`, `FoodEntryEntity`, `WaterEntryEntity`) + DAO'lar
- [x] 2026-04-22 [A] `FitLogicDatabase` migration (v4 -> v5) ve schema export güncellendi
- [x] 2026-04-22 [A] Türk mutfağı odaklı seed dataset (~500) eklendi ve seed mekanizması tamamlandı
- [x] 2026-04-22 [A] OpenFoodFacts client + DTO/mapper + hata yönetimi tamamlandı
- [x] 2026-04-22 [A] ML Kit barkod data katmanı entegrasyonu (scanner sonucu -> repository akışı)
- [x] 2026-04-22 [A] `NutritionRepositoryImpl` ve Hilt binding/provider güncellemeleri tamamlandı
- [x] 2026-04-22 [A] Favoriler veri modeli ve local persistence tamamlandı
- [x] 2026-04-22 [A] Seed mekanizması eklendi (`nutrition_foods_seed.json`, 500 kayıt)
- [x] 2026-04-22 [A] Barkod scanner -> repository akışı için ML Kit bridge eklendi (`Barcode -> payload -> NutritionRepository.scanBarcode`)
- [x] 2026-04-22 [A] Agent A doğrulama komutları çalıştırıldı:
  - [x] 2026-04-22 [A] `./gradlew :core:domain:test :core:data:test --console=plain`
  - [x] 2026-04-22 [A] `./gradlew :core:data:ktlintCheck :core:data:detekt --console=plain`
  - [x] 2026-04-22 [A] `./gradlew :core:data:assembleDebug --console=plain`

### Grup B - Feature Nutrition + UI/UX + Navigation (Agent B)

- [x] 2026-04-22 [B] `feature:nutrition` ekran mimarisi kuruldu (UiState, UiEvent, ViewModel)
- [x] 2026-04-22 [B] Günlük Beslenme ekranı tamamlandı (makro halkaları, öğün listeleri, hızlı aksiyonlar)
- [x] 2026-04-22 [B] Yemek Ekle ekranı tamamlandı (arama / barkod / favori / son sekmeleri)
- [x] 2026-04-22 [B] Yemek Detay ekranı tamamlandı (porsiyon ayarı, makro yeniden hesaplama, kaydet)
- [x] 2026-04-22 [B] Barkod Tarayıcı ekranı tamamlandı (kamera preview + overlay + sonuç yönlendirmesi)
- [x] 2026-04-22 [B] Barkod bulunamazsa Manuel Yemek Ekle akışı tamamlandı
- [x] 2026-04-22 [B] Su Takibi widget/komponenti tamamlandı (hızlı ekleme + günlük toplam)
- [x] 2026-04-22 [B] Favoriler UX akışı tamamlandı (favoriye ekle/çıkar, favoriden hızlı ekle)
- [x] 2026-04-22 [B] `app/navigation` beslenme alt-akış route'ları tamamlandı
- [x] 2026-04-22 [B] Agent B doğrulama komutları çalıştırıldı:
  - [x] 2026-04-22 [B] `./gradlew :feature:nutrition:test --console=plain`
  - [x] 2026-04-22 [B] `./gradlew :feature:nutrition:ktlintCheck :feature:nutrition:detekt --console=plain`
  - [x] 2026-04-22 [B] `./gradlew :app:assembleDebug --console=plain`

### Faz 5 çakışma kuralları (Sert Sahiplik)

- [ ] Agent A sahiplik alanı: `core/domain`, `core/data`, `core/data/assets`, data DI
- [ ] Agent B sahiplik alanı: `feature/nutrition`, `app/navigation`, beslenme UI akışları
- [ ] Agent B, `core/domain` ve `core/data` dosyalarını değiştirmez
- [ ] Agent A, `feature/nutrition` dosyalarını değiştirmez
- [ ] Ortak bağımlılık sözleşmesi: B, A'nın belirlenen repository/usecase imzalarını baz alır; imza değişikliği gerekiyorsa önce A finalize eder
- [ ] Merge sırası: A branch'i önce merge edilir, B branch'i A üstüne rebase edilip finalize edilir
- [ ] Tek entegrasyon noktası: B sadece feature tarafında gerçek usecase enjeksiyonuna geçer; data tarafına dokunmaz

### Public API / Interface değişiklikleri

- [ ] `core/domain` içine `NutritionRepository` eklenir ve tüm Faz 5 kullanım senaryolarını kapsar.
- [ ] `core/domain` içine nutrition model seti eklenir: günlük makro özeti, öğün bazlı giriş, su girişi, barkod arama sonucu, favori özetleri.
- [ ] `core/domain/usecase/nutrition` altında 5 temel use case yayınlanır.
- [ ] `app/navigation` içinde beslenme alt-akış route'ları tanımlanır (liste, ekle, detay, barkod, manuel ekleme).

### Test planı ve kabul senaryoları

- [ ] Kabul senaryosu 1: Kullanıcı arama ile yemek ekler, günlük makro halkaları anında güncellenir.
- [ ] Kabul senaryosu 2: Kullanıcı barkod tarar, ürün bulunursa porsiyon ekranına gider; bulunamazsa manuel ekleme ekranına düşer.
- [ ] Kabul senaryosu 3: Kullanıcı su ekler, günlük su toplamı anında artar ve ekran yeniden açıldığında korunur.
- [ ] Kabul senaryosu 4: Favoriye eklenen öğe, Yemek Ekle ekranındaki Favoriler sekmesinde listelenir ve tek dokunuşla eklenebilir.
- [ ] Kalite kapısı: Grup A ve Grup B doğrulama komutlarının tamamı geçmeden Faz 5 tamamlandı işaretlenmez.

### Varsayımlar ve seçilen varsayılanlar

- [ ] OpenFoodFacts hata/boş sonuç durumlarında kullanıcıya Türkçe, teknik olmayan mesaj gösterilecek.
- [ ] Offline-first korunacak: yemek/su kayıtları önce local DB'ye yazılacak, uzak kaynak sadece katalog zenginleştirme için kullanılacak.

