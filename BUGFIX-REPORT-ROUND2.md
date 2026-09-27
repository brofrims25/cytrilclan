# CytrilClan — Laporan Ronde 2 (dari source code asli, bukan decompile)

Kali ini kamu upload source code aslinya (`_git.zip`), jadi saya bisa audit
langsung dari kode asli — lebih akurat daripada decompile jar kemarin. Semua
6 bug dari laporan sebelumnya sudah saya terapkan ke source asli ini (bukan
cuma rekonstruksi dari decompile), **plus ketemu 2 bug baru** yang persis
kayak yang kamu bilang: "fitur nya tampilan aja".

---

## 🔴 BARU — Bug #7: Statistik Kill/Death di menu member 100% kosmetik

Ini paling cocok sama keluhan kamu. `ClanMember.java` udah dari awal
punya method `addKill()` dan `addDeath()`, dan `MemberListGui.java`
udah nampilin "Kills: X" / "Deaths: X" di lore tiap kepala member —
**tapi nggak ada satupun kode di seluruh plugin yang pernah manggil
`addKill()`/`addDeath()`**. Jadi angka itu selamanya beku di 0 (atau
apapun yang kebetulan ada di file save lama), nggak peduli berapa kali
member itu ngebunuh/mati.

**Perbaikan:** nambahin listener baru `PlayerDeathEvent` di
`ClanPvpListener.java` yang beneran nge-increment kill korbannya
`+1 death` dan pembunuhnya `+1 kill` (kalau pembunuhnya juga punya
clan), terus di-save.

---

## 🔴 BARU — Bug #8: Menu `/clan base` CRASH kalau `max-bases` di config > 3

Ini bug serius. `BaseListGui.java` cuma punya **3 slot fisik hardcoded**
(`int[] slots = {11, 13, 15};`), padahal `general.max-bases` di config
kelihatannya bisa diset bebas. Command `/clan base set <name>` udah
BENER ngikutin config (boleh bikin base ke-4, ke-5, dst kalau
`max-bases` disetel lebih dari 3) — tapi begitu ada base ke-4 dan
pemain buka menu `/clan base`, plugin **crash**
(`ArrayIndexOutOfBoundsException`) karena `slots[3]` nggak ada.

Jadi kalau kamu (atau admin manapun) pernah nyoba naikin `max-bases`
lebih dari 3 di config, fitur base langsung berantakan.

**Perbaikan:** layout slot sekarang otomatis menyesuaikan jumlah base
(pakai 2 baris penuh = 18 slot kalau perlu), jadi nggak crash lagi
berapapun `max-bases` yang kamu set (sampai 18, yang harusnya lebih dari
cukup buat kebutuhan normal).

---

## 🟡 BARU — Bug #9: Lore di menu utama nge-hardcode "3 base(s)"

Di `MainMenuGui.java`, tombol "Bases" selalu nulis `X/3 base(s)` di
lore-nya, padahal batasnya seharusnya ambil dari config `max-bases`.
Kalau kamu ubah config jadi 5 misalnya, pemain tetap lihat "/3" yang
menyesatkan.

**Perbaikan:** sekarang ambil angka asli dari config, bukan hardcode.

---

## ✅ Konfirmasi: 6 bug dari laporan kemarin sudah diterapkan ke source asli

Kemarin saya kerja dari jar yang di-decompile (karena cuma jar yang
diupload). Sekarang dengan source asli, saya terapkan ulang semua fix
itu langsung ke kode aslinya (bukan rekonstruksi), jadi hasilnya lebih
rapi dan konsisten gaya kodingnya:

1. Item hilang di Give Item (`ClanGuiListener` — `onClose`, `handleGiveItemTarget`)
2. Chat ke-hijack permanen (`PendingActionManager` — timeout otomatis)
3. Save synchronous blocking main thread (`StorageManager` — `saveAsync()`)
4. Buku kick hilang kalau inventory penuh (`ClanGuiListener`)
5. `bank-rows` dead config (`ClanManager.createClan` sekarang beneran pakai)
6. Transfer leader gagal diam-diam kalau clan di-rename (`LeaderTransferTask`)

---

## Soal versi Paper 1.21–1.26.3 dan Java 21/25/26

Kabar baik: **project kamu sudah benar secara arsitektur untuk ini**,
nggak perlu diubah:

- **Java 21/25/26** — `pom.xml` udah compile pakai `--release 21`
  (`<maven.compiler.release>21</maven.compiler.release>` +
  `<maven.compiler.source/target>21</...>`). Bytecode hasil compile
  release 21 itu **otomatis jalan di JVM manapun yang lebih baru**
  (21, 25, 26, dst) — ini prinsip dasar Java (backward-compatible ke
  bawah dari sisi JVM). Kalau saya naikin ke `--release 25`, itu malah
  akan **mematahkan** dukungan ke server yang masih pakai Java 21.
  Jadi biarkan seperti sekarang, ini sudah paling benar.

- **Paper 1.21 s/d 1.26.3** — plugin Paper/Spigot itu nggak di-compile
  per-versi; kamu compile SEKALI terhadap satu Paper API (di sini:
  `1.21.4-R0.1-SNAPSHOT`), dan itu jalan di rentang versi server yang
  luas selama nggak makai API yang baru ditambah/dihapus di versi lebih
  baru. `plugin.yml` kamu juga udah pakai `api-version: '1.21'` (angka
  RENDAH ini justru yang bikin plugin bisa dimuat di 1.21 sampai versi
  berapapun di atasnya — kalau dinaikin ke "1.26" malah plugin GAGAL
  dimuat di server di bawah 1.26). Jadi ini juga sudah konfigurasi yang
  benar, jangan diubah ke versi tinggi.

  Catatan jujur: saya nggak bisa cek dari sandbox ini apakah Paper API
  versi `1.21.4-R0.1-SNAPSHOT` itu masih yang terbaru/tersedia
  (repo.papermc.io nggak bisa saya akses dari sini) — itu satu hal yang
  perlu kamu cek sendiri di CI/GitHub Actions kamu. Kalau nanti Paper
  merilis versi API yang lebih baru dan kamu mau ikut update artifact-nya
  di `pom.xml`, silakan, itu aman dan nggak akan mematahkan dukungan
  versi lama selama kamu nggak pakai API yang cuma ada di versi baru.

---

## File yang diubah ronde ini

```
src/main/java/com/cytril/cytrilclan/CytrilClan.java
src/main/java/com/cytril/cytrilclan/listeners/ClanGuiListener.java
src/main/java/com/cytril/cytrilclan/listeners/ClanPvpListener.java      (BARU: kill/death tracking)
src/main/java/com/cytril/cytrilclan/manager/ClanManager.java
src/main/java/com/cytril/cytrilclan/manager/ConfigManager.java
src/main/java/com/cytril/cytrilclan/manager/PendingActionManager.java
src/main/java/com/cytril/cytrilclan/manager/StorageManager.java
src/main/java/com/cytril/cytrilclan/tasks/LeaderTransferTask.java
src/main/java/com/cytril/cytrilclan/gui/BaseListGui.java               (BARU: fix crash)
src/main/java/com/cytril/cytrilclan/gui/MainMenuGui.java               (BARU: fix hardcode)
src/main/resources/config.yml                                          (+pending-action-timeout-seconds)
pom.xml                                                                 (versi -> 1.3.1)
```

## Yang belum sempat diaudit habis

Saya sudah baca seluruh 46 file di project ini, tapi untuk file-file GUI
yang lebih kecil (BankDepositGui, BankWithdrawConfirmGui,
ConfirmDisbandGui, MemberManageGui, GiveItemGui/GiveItemTargetGui,
SettingsGui) saya cross-check slot vs listener-nya dan semuanya cocok
(nggak ada tombol yang "nyasar"/nggak ke-handle). `FloodgateHook`
(deteksi Bedrock) terpasang dan berfungsi (log "Hooked into Floodgate")
tapi memang belum dipakai buat menyesuaikan tampilan apapun secara khusus
untuk Bedrock — ini bukan bug (Geyser/Floodgate sendiri sudah
menerjemahkan GUI/chat Java ke Bedrock secara transparan), tapi kalau
kamu mau saya bikin sesuatu yang eksplisit pakai deteksi itu (misal
placeholder `%cytrilclan_platform%`), tinggal bilang.

Kalau kamu punya bug spesifik lain yang ketemu pas main langsung di
server (bukan cuma baca kode), kasih tau skenario reproduksinya — itu
akan jauh lebih efektif daripada saya nebak-nebak dari kode.
