# FitLogic — Detaylı UI/UX Planı

## 1. Genel Felsefe ve Tasarım İlkeleri

Uygulama şu an **işlevsel ama soğuk** hissettiriyor. Material3 default'larıyla kurulmuş, fonksiyonel ama "kişiliksiz." Hedef: **bir fitness coach'unun stüdyosuna girmek gibi** hissettirmek — enerjik, motive edici, ama karmaşık değil.

**3 temel ilke:**
1. **Momentum** — Her ekran bir sonraki aksiyona yönlendirmeli
2. **Reward loop** — Küçük başarılar büyük kutlamalara dönüşmeli
3. **Clarity first** — Bir ekranda tek bir ana aksiyon

---

## 2. Design System — Güncellenecekler

### 2.1 Renk Paleti

Şu an Material3 default renkleri var. Fitness uygulaması için önerilen palet:

```
Primary:      #E63946  — Enerji kırmızısı (antrenman, aksiyon)
OnPrimary:    #FFFFFF
Secondary:    #457B9D  — Sakin mavi (beslenme, dinlenme)
Tertiary:     #2A9D8F  — Başarı yeşili (PR, rozet, tamamlama)
Background:   #0D0D0D  — Koyu (dark mode varsayılan)
Surface:      #1A1A1A  — Kart yüzeyi
SurfaceVar:   #262626  — Elevated kart
Neutral:      #8A8A8A  — İkincil metin
Error:        #FF6B6B
```

**Neden dark-first:** Spor salonunda, yoğun ışık altında dark mode çok daha okunabilir.

### 2.2 Tipografi

```
Display:   Bebas Neue veya Barlow Condensed (PR kutlamalar, büyük rakamlar)
Headline:  Inter Semi-Bold 700
Body:      Inter Regular 400
Label:     Inter Medium 500
Mono:      Manrope (ağırlık/rep rakamları)
```

**Neden Mono font rakamlar için:** 90 kg → 100 kg geçişinde rakamlar "zıplamamalı."

### 2.3 Shape — Köşe Yarıçapları

```kotlin
ExtraSmall = 4.dp    // Tag, chip
Small      = 8.dp    // TextField
Medium     = 16.dp   // Card
Large      = 24.dp   // Bottom sheet, modal
ExtraLarge = 32.dp   // FAB, ana buton
```

---

## 3. Onboarding — Yeniden Tasarım

### Şu anki sorun
4 adım düz form. Kullanıcı "neden bunları veriyorum?" diye hissetmiyor.

### Yeni akış (5 adım, story-telling format)

**Adım 0 — Karşılama (Splash → Onboarding)**
```
[Büyük animasyonlu logo]
"FitLogic, sana özel çalışıyor."
[Devam et butonu — bottom]
```

**Adım 1 — "Sen kimsin?"**
```
[Avatar seçici - emoji veya renkli harf]
İsim girişi — tek alan
"Merhaba [İsim]! 👋"
```

**Adım 2 — Vücut ölçüleri**
```
[Scrollable drum-picker: yaş, boy, kilo]
Metric/Imperial toggle
— Sayılar büyük ve net görünmeli
```

**Adım 3 — Hedef seçimi (tek büyük seçim)**
```
3 büyük kart, tüm ekranı kaplayan:
┌─────────────────────┐
│  🔥 KİLO VER        │
│  Yağ yakımı optimize│
└─────────────────────┘
┌─────────────────────┐
│  💪 KAS YAP         │
│  Güç ve hacim       │
└─────────────────────┘
┌─────────────────────┐
│  ⚡ FORMA KOR       │
│  Dengeli yaşam      │
└─────────────────────┘
```

**Adım 4 — Aktivite seviyesi**
```
İkon + metin + açıklama üçlüsü:
🪑 Az aktif → 🚶 Hafif → 🏃 Orta → ⚡ Aktif → 🔥 Çok Aktif
```

**Adım 5 — Makro özeti (Reward!)**
```
Animasyonla gelen sayılar:
┌──────────────────────────────────┐
│  Günlük hedefin:                 │
│                                  │
│    2.340 kcal  ← büyük counter  │
│  🥩 180g protein                │
│  🍚 260g karbonhidrat           │
│  🥑 78g yağ                     │
│                                  │
│  [Hadi Başlayalım!]              │
└──────────────────────────────────┘
```

---

## 4. Ana Ekran (Home) — Motivasyon Motoru

### Şu anki sorun
Streak card, weekly goal card, achievements card → hepsi eşit ağırlıkta. Kullanıcı neye bakacağını bilemiyor.

### Yeni layout (scroll edilebilir, hiyerarşik)

```
┌────────────────────────────────────┐
│  Günaydın, Ahmet 🌅                │  ← Saat bazlı selamlama
│  Bugün: 29 Nisan                   │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│  🔥 12 günlük seri!               │  ← HERO CARD (büyük)
│  ████████████░ %92 hedefe          │
│  "Haftanın son antrenmanı kaldı"   │
│                                    │
│  [Antrenmanı Başlat →]             │  ← CTA
└────────────────────────────────────┘

┌──────────────┐ ┌──────────────────┐
│ Bugünkü      │ │ Kalan Makro      │
│ Kalori       │ │ ● ● ●            │
│ 1.840/2.340  │ │ P  C  Y          │
│ kcal         │ │ %78 %65 %82      │
└──────────────┘ └──────────────────┘

┌────────────────────────────────────┐
│ 🤖 AI Koç Önerisi                 │  ← Günlük insight
│ "Dün bacak gününde hacim PR kırdın │
│  Bugün üst vücut ideal olur →"    │
└────────────────────────────────────┘

┌────────────────────────────────────┐
│ Son Antrenman (Dün)               │
│ Göğüs · 45 dk · 8.450 kg         │
│ [Tekrar Et] [Detay]               │
└────────────────────────────────────┘

┌──────────────┐ ┌──────────────────┐
│ 🏅 3 Rozet   │ │ Su: 1.2L/2L     │
│ bu hafta!    │ │ ████░░░░ %60    │
└──────────────┘ └──────────────────┘
```

---

## 5. Aktif Antrenman — Odak Modu

### Şu anki sorun
Set input, timer, egzersiz listesi hepsi aynı anda ekranda. Distraction fazla.

### Yeni yapı: 3 katmanlı odak akışı

**Katman 1: Egzersiz odak kartı (full-bleed)**
```
┌────────────────────────────────────┐
│ ← Bench Press              45:23  │
│    [Hamle: 3 / 5]                  │
│                                    │
│        ┌───┐   ┌───┐              │
│  +5kg  │ 80│kg │ 8 │ rep  ✓      │
│        └───┘   └───┘              │
│                                    │
│  ← Önceki: 80kg × 8  ✓           │  ← referans
│                                    │
│       [Set Kaydet]                 │  ← büyük buton
│                                    │
│  Dinlenme: [60s] [90s] [120s]     │
└────────────────────────────────────┘
```

**Katman 2: Rest Timer (overlay, yarı saydam)**
```
┌────────────────────────────────────┐
│                                    │
│          90                        │  ← büyük countdown
│    ██████████░░░░░░                │  ← circular progress
│                                    │
│     Sonraki: 80 kg × 8            │
│                                    │
│    [Geç] [+30s]                   │
│                                    │
└────────────────────────────────────┘
```

**Katman 3: Egzersiz listesi (bottom sheet, kaldırılabilir)**
```
— ─── —  (swipe up indicator)
Bench Press ✓ 3 set
Squat       › 0 set
Shoulder P. › 0 set
[+ Egzersiz Ekle]
```

**Set input tasarımı:**
- Ağırlık için **drum picker** veya büyük sayı — küçük TextField değil
- +2.5, +5, +10 kg hızlı butonları
- Rep için benzer hızlı butonlar: -1, +1
- Haptik feedback her set kaydında

---

## 6. Antrenman Özeti — Kutlama Ekranı

```
┌────────────────────────────────────┐
│                                    │
│         🎉 TAMAMLANDI!            │  ← konfeti animasyonu
│         Harika iş çıkardın        │
│                                    │
│    ⏱ 47 dk   💪 9.200 kg  🔥 12  │
│    Süre      Hacim         Set     │
│                                    │
│    ┌──────────────────────────┐   │
│    │ 🏆 PR Kırdın!           │   │  ← eğer PR varsa
│    │ Bench Press: 95 kg       │   │
│    └──────────────────────────┘   │
│                                    │
│    [Paylaş]  [Kaydet & Çık]        │
│                                    │
└────────────────────────────────────┘
```

---

## 7. Beslenme — Görsel Makro Dashboard

### Şu anki sorun
CircularProgressIndicator 4 tane yan yana — hangisi ne olduğu zor anlaşılıyor.

### Yeni tasarım

**Üst alan — Hero makro göstergesi:**
```
┌────────────────────────────────────┐
│                                    │
│     1.840 / 2.340 kcal            │
│  ████████████████░░░░ %78         │
│                                    │
│  🥩 Protein  🍚 Karbonhidrat  🥑  │
│  142/180g    195/260g    65/78g   │
│  ████░       ████████░   ███████░ │
│                                    │
└────────────────────────────────────┘
```

**Öğün kartları (collapsible):**
```
▼ KAHVALTI                7:30 · 620 kcal
  Yulaf ezmesi   250g  380 kcal
  Muz            100g   89 kcal
  Protein Shake  300ml 151 kcal
  [+ Ekle]

▶ ÖĞLE YEMEĞİ            boş
  [+ Ekle]
```

**Su takibi — sticky bottom:**
```
┌────────────────────────────────────┐
│  💧 1.200 ml / 2.000 ml          │
│  ████████░░░░░░░░░░               │
│  [+200ml]  [+250ml]  [+500ml]    │
└────────────────────────────────────┘
```

---

## 8. Egzersiz Kütüphanesi — Keşif Deneyimi

### Şu anki sorun
Düz liste, filtreler küçük chip'ler.

### Yeni tasarım

**Üst alan: Hızlı kategori seçimi (yatay scroll'lu büyük ikonlu kartlar)**
```
[💪 Göğüs] [🦵 Bacak] [🏋 Sırt] [🤸 Karın] ...
```

**Egzersiz kartı (grid 2 sütun):**
```
┌──────────┐  ┌──────────┐
│ [GIF/IMG]│  │ [GIF/IMG]│
│ Bench    │  │ Squat    │
│ Press    │  │          │
│ ●●● Orta │  │ ●●●● Zor │
└──────────┘  └──────────┘
```

**Detay ekranı — hero header:**
```
┌────────────────────────────────────┐
│  [GIF/Animasyon — büyük]          │
│                                    │
│  Bench Press                       │
│  ●●● Orta · Göğüs · Barbell       │
│                                    │
│  [Talimatlar] [Hatalar] [Alternat.]│  ← tab
│                                    │
│  [Aktif Antrenmana Ekle]          │
└────────────────────────────────────┘
```

---

## 9. İstatistikler — Görsel Hikaye Anlatımı

### Şu anki sorun
OutlinedTextField ile kilo girişi + düz liste. Chart yok (Vico dependency var ama kullanılmıyor).

### Yeni tasarım

**Haftalık özet kartı (hero):**
```
┌────────────────────────────────────┐
│  Bu Hafta                          │
│                                    │
│  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐       │
│  │██│ │██│ │  │ │██│ │  │        │  ← bar chart
│  │██│ │██│ │  │ │██│ │  │        │
│  Pzt  Sal  Çar  Per  Cum          │
│                                    │
│  4/5 antrenman ✓                  │
└────────────────────────────────────┘
```

**Kilo trend grafiği (Vico LineChart):**
```
┌────────────────────────────────────┐
│ Kilo Trendi                        │
│  ·                                 │
│    ·  ·                            │
│         ·   ·                      │  ← line chart
│               ·  ·                 │
│                    ·               │
│  Mar      Nis      May             │
│                                    │
│  [+ Kilo Ekle: ___ kg]            │
└────────────────────────────────────┘
```

**Kas grubu dağılımı (Vico PieChart veya radar):**
```
        Göğüs 32%
    ⬡─────────
   ⬡ Omuz 18% ⬡ Sırt 28%
    ⬡─────────
        Bacak 22%
```

---

## 10. AI Koç — Sohbet Deneyimi

### Şu anki sorun
Düz kart listesi + 2 buton. Koçluk hissi vermiyor.

### Yeni tasarım

**Ana görünüm — chat benzeri:**
```
┌────────────────────────────────────┐
│ 🤖 FitLogic Koç                   │
├────────────────────────────────────┤
│                                    │
│  ┌──────────────────────────────┐ │
│  │ 📊 Haftalık Analiz Hazır    │ │
│  │ Dün bacak günüydü ve...      │ │
│  │ [Devamını Oku →]            │ │
│  └──────────────────────────────┘ │
│                                    │
│  ┌──────────────────────────────┐ │
│  │ ⚠️ Plato Uyarısı            │ │
│  │ Bench Press 3 haftadır...    │ │
│  │ [Devamını Oku →]            │ │
│  └──────────────────────────────┘ │
│                                    │
├────────────────────────────────────┤
│  [📊 Rapor Üret] [🔍 Plato Analiz]│
└────────────────────────────────────┘
```

**Detay görünümü — tam ekran kart:**
```
← Geri

┌────────────────────────────────────┐
│ 📊 Haftalık Analiz                 │
│ 28 Nisan 2026                      │
├────────────────────────────────────┤
│                                    │
│  [Insight metni — tam okunabilir] │
│                                    │
│  Önerilen aksiyonlar:              │
│  • Üst vücut antrenmanı ekle       │
│  • Protein alımını %10 artır       │
│                                    │
│       [✓ Okundu İşaretle]         │
│                                    │
└────────────────────────────────────┘
```

---

## 11. Profil — Temiz Ayarlar

### Şu anki sorun
Her şey düz liste, görsel hiyerarşi yok.

### Yeni düzen

```
┌────────────────────────────────────┐
│  [Avatar]  Ahmet Yılmaz            │
│            ahmet@gmail.com         │
│            🔥 12 gün seri · 47 AT │
└────────────────────────────────────┘

─── Hedefler ───
┌────────────────────────────────────┐
│  Günlük Kalori         2.340 kcal  │
│  Protein Hedefi         180 g      │
│  Antrenman/Hafta        4 gün      │
└────────────────────────────────────┘

─── Tercihler ───
  🌙 Tema                 Koyu  >
  🌍 Dil               Türkçe  >
  ⚖️  Birim               kg/cm  >

─── Bildirimler ───
  [toggle switch'ler — sade liste]

─── Senkronizasyon ───
  Son sync: 2 dk önce ✓
  [Şimdi Senkronize Et]

─── Tehlikeli Bölge ───
  [Hesabı Sil]  ← kırmızı, küçük
```

---

## 12. Navigasyon — Alt Bar Optimizasyonu

### Şu anki sorun
7 sekme çok fazla. Alt bar 7 item ile çok kalabalık.

### Çözüm: 5 ana sekme + overflow

```
🏠 Ana   💪 Antrenman   🥗 Beslenme   📊 İstatistik   👤 Profil
```

- **Egzersiz Kütüphanesi** → Antrenman sekmesi içinden erişilir
- **AI Koç** → Ana ekran kartından veya Profil sekmesinden

---

## 13. Micro-interactions ve Animasyonlar

| Aksiyon | Animasyon |
|---|---|
| Set kaydet | Haptik + yeşil checkmark pulse |
| PR kır | Konfeti + büyük sayı bounce |
| Rozet kazan | Full-screen overlay + sfx |
| Streak güncelle | Sayı counter animasyonu |
| Makro tamamla | Halka dolum + pulse |
| Ekran geçişi | Slide + fade |
| Tab seç | Icon scale + renk geçişi |
| Kilo kaydet | Grafik animasyonlu güncelleme |
| Antrenman bitir | Hero animasyon |

---

## 14. Boş Durumlar ve Onboarding İpuçları

Her boş state için özel illüstrasyon + action CTA:

```
🏋️ "Henüz antrenmanın yok"
   "İlk antrenmanını başlatmak için hazır mısın?"
   [Antrenmanı Başlat]

🥗 "Bugün hiçbir şey kaydetmedin"
   "Kahvaltıyı ekleyerek güne başla"
   [Yemek Ekle]

📊 "Veri birikmedi"
   "Bir hafta sonra istatistiklerin burada görünecek"
```

---

## 15. Öncelik Sırası (Implementation Roadmap)

| Öncelik | Alan | Etki |
|---|---|---|
| 🔴 P0 | Dark theme + renk paleti | Tüm uygulama |
| 🔴 P0 | Aktif antrenman set input (drum picker / büyük input) | Günlük kullanım |
| 🔴 P0 | Home ekranı hero card + AI önerisi | İlk izlenim |
| 🟡 P1 | Beslenme makro görselleştirmesi | Günlük kullanım |
| 🟡 P1 | Onboarding yeniden tasarım | Dönüşüm |
| 🟡 P1 | İstatistikler Vico chart entegrasyonu | Motivasyon |
| 🟢 P2 | 7→5 sekme navigasyon | Kullanılabilirlik |
| 🟢 P2 | Egzersiz grid layout | Keşif |
| 🟢 P2 | AI Koç sohbet stili | Bağlılık |
| 🟢 P3 | Mikro-interactionlar ve haptic | Polish |
| 🟢 P3 | Boş state illüstrasyonları | İzlenim |
