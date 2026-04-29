# FitLogic Kullanici Test Senaryolari

Bu liste 2026-04-29 tarihinde otomatik ve manuel akislarla dogrulama icin hazirlandi.

## 1) Ilk acilis + onboarding
- Uygulamayi sifir veriyle ac.
- `onboarding_next` ile adimlari tamamla (isim, yas, boy, kilo, cinsiyet, aktivite, hedef).
- Beklenen: onboarding tamamlanir, auth veya ana ekran gorulur, crash olmaz.

## 2) Misafir girisi
- Auth ekraninda `auth_guest` sec.
- Beklenen: tab bar yuklenir (`tab_home`, `tab_workout`, `tab_nutrition`, `tab_coach`).

## 3) Antrenman baslat / bitir
- `tab_workout` ac.
- `workout_start_empty` butonuna bas.
- Antrenman ekraninda `workout_finish` gorunmeli.
- Beklenen: akis boyunca hata diyaloðu/cokme olmaz.

## 4) Beslenme barkod akisi
- `tab_nutrition` ac.
- `nutrition_open_barcode_breakfast` ile barkod ekranina git.
- Beklenen: "Barkod Tara" ekrani gorulur.

## 5) Koç/AI akis kontrolu
- `tab_coach` ac.
- `coach_generate_weekly` ve `coach_detect_plateau` aksiyonlarini tetikle.
- Beklenen: ekran donmeden kalmaz, hata mesaji verirse uygulama bozulmadan toparlar.

## 6) Profil - hesap silme guvenlik akisi
- Profilde hesap sil diyalogunu ac.
- Yanlis ifade gir (`INVALID`) ve onayla.
- Beklenen: silme yapilmaz, "Silme onayi icin HESABIMI SIL yazin." mesaji gorulur.
- Dogru ifade gir (`HESABIMI SIL`) ve onayla.
- Beklenen: silme aksiyonu tetiklenir ve diyalog kapanir.

## 7) Profil - bildirim/senkronizasyon
- Bildirim ac/kapat butonlarini degistir.
- "Simdi senkronize et" benzeri aksiyon varsa tetikle.
- Beklenen: UI donmez, aksiyon basarisiz olsa bile kontrollu mesaj verir.

## 8) Arka plan/dayaniklilik
- Uygulamayi arka plana alip geri getir.
- Ekran donusleri yap (Home -> Workout -> Nutrition -> Coach -> Profile).
- Beklenen: state kaybi, bos beyaz ekran veya crash olmaz.

## Notlar
- Bu senaryolar, `app/src/androidTest/kotlin/com/fitlogic/ai/Faz10CriticalFlowsTest.kt` ile paralel tutuldu.
- Gercek cihaz testinde bir akis (`aiInsightFlow_smoke`) flaky durumda skip edilebiliyor; bu durum geri kalan testleri bozmaz.
