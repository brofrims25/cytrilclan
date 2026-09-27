# CytrilClan v1.3.2 — Fix: Tidak bisa taruh item di menu Deposit Bank

## Penyebab

Di `handleBankDeposit()` (`ClanGuiListener.java`), pengecekan slot cuma
begini:

```java
int slot = event.getRawSlot();
if (slot >= 0 && slot < DEPOSIT_AREA_SIZE) {   // 0-35, area drop di GUI
    return; // izinkan
}
event.setCancelled(true);   // <-- SEMUANYA selain 0-35 langsung dibatalkan
```

Masalahnya: `event.getRawSlot()` itu bukan cuma buat slot di GUI atas.
Kalau kamu **shift-click** item dari inventory kamu SENDIRI (bagian
bawah layar) buat ngirim cepat ke GUI, Bukkit kasih raw slot number di
rentang inventory BAWAH (45 ke atas untuk GUI 45-slot ini) — itu BUKAN
0-35, dan juga bukan salah satu slot tombol (36/40/44). Jadi kode di
atas langsung `setCancelled(true)` buat klik itu, padahal seharusnya
dibiarin biar Bukkit yang otomatis mindahin ke slot deposit yang kosong.

Efeknya persis kayak yang kamu alamin:
- **Shift-click buat deposit** → dibatalkan → item keliatan "balik
  sendiri" (padahal sebenernya nggak pernah beneran pindah)
- **Tekan Q buat drop** dari inventory sendiri saat GUI ini kebuka →
  juga dibatalkan → nggak ke-drop

Bug yang PERSIS SAMA juga ada di menu **Give Item** (`handleGiveItem`),
jadi sekalian saya perbaiki di situ juga.

## Perbaikan

Ditambah 1 pengecekan: kalau raw slot yang diklik itu >= ukuran
inventory ATAS (`event.getView().getTopInventory().getSize()`), berarti
klik itu terjadi di inventory PEMAIN SENDIRI (bukan di GUI) — biarin
aja, jangan dibatalkan. Ini aman karena nggak mungkin nyentuh
tombol-tombol kontrol di GUI atas (yang emang cuma ada di slot 36-44
untuk Bank Deposit, dan itu semua < topSize).

## Klik normal (bukan shift) tetap sama seperti sebelumnya

Klik biasa (tanpa shift) buat naruh item ke slot deposit **sudah
berfungsi dari awal** — itu udah masuk hitungan `slot 0-35` di GUI atas.
Kalau sebelumnya itu juga kerasa nggak jalan, kemungkinan besar
sebenarnya yang kamu coba itu shift-click (paling umum dipakai orang
buat "cepetan naruh barang").

## File yang diubah

```
src/main/java/com/cytril/cytrilclan/listeners/ClanGuiListener.java
pom.xml (versi -> 1.3.2)
```
