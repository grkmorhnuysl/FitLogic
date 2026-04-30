# FitLogic Kullanici Niyeti vs Gercek Deneyim Matrisi

Bu dokuman `2026-04-29` tarihinde AI Koç deneyimi iyilestirmesiyle birlikte guncellendi.

## Senaryo Matrisi

| Niyet | Aksiyon | Beklenti | Gercek Sonuc | Kirilma Etkisi | Duzeltme |
|---|---|---|---|---|---|
| Hemen uygulamayi denemek | Onboarding + `auth_guest` | Hatasiz ve hizli ana ekrana gecis | Ana sekmeler yuklenir, akisa girilir | Dusuk | Mevcut E2E akislari korunur |
| Antrenman takibi baslatmak | `workout_start_empty`, set kaydi, `workout_finish` | Kayitlarin korunmasi | Set/hacim gecmisi gorunur | Dusuk | Mevcut E2E akislari korunur |
| Hizli besin eklemek | Arama veya barkod | Yemek girisini hizli tamamlama | Besin kaydi olusur, ogune duser | Dusuk-Orta | Barkod izin reddinde manuel fallback akisi |
| AI kocluk almak | `coach_chat_send` ile mesaj gondermek | Anlamli, guvenli ve kisa yanit | Chat icin ayri prompt kullanilir; prompt echo engellenir; demo modda yerel anlamli yanit doner | Yuksekten Dusuge | Prompt echo guardrail, kalite kontrol, hata normalizasyonu, retry |
| Hesap guvenligi | Profilde silme/senkronizasyon | Yanlis islemleri engelleme, kontrol kaybi olmamasi | Silme onay metni olmadan silme yapilmaz; sync hatasi mesajlanir | Orta | Acik hata metni + tekrar dene |

## AI Koç Kirilma Analizi ve Yeni Durum

| Alan | Once | Simdi |
|---|---|---|
| Prompt kullanimi | Sohbet mesaji `postWorkout` prompt'una bagliydi | Chat icin `coachChat` prompt API'si kullaniliyor |
| Engine davranisi | Stub cevap prompt metnini geri yansitabiliyordu | Engine `mode` ayrimi var; `DEMO_STUB` modunda guvenli yerel yanit donuyor |
| Hata gorunurlugu | `json/token/auth` gibi teknik metinler kullaniciya sizabiliyordu | Hatalar kullanici dostu mesaja normalize ediliyor |
| Cevap kalitesi | Bos/prompt-echo/repetitive yanitlar filtrelenmiyordu | Guardrail ile kalite kontrolu uygulanıyor |

## Kabul Kriteri Eslesmesi

- Kullanici `"Bugun ne yapayim?"` gonderdiginde prompt metni geri donmez.
- `token/auth/json/network` kaynakli hata durumlarinda teknik dump yerine sade yonlendirici mesaj gosterilir.
- Bos veya dusuk kaliteli cevap durumunda guvenli fallback metni uygulanir.
- `isSending=true` iken ikinci gonderim engellenir (double-send yok).
