# EIS - Engineering Inventory Sistem (Android)

Aplikasi Android (Kotlin + Jetpack Compose) untuk inventory Engineering yang **terhubung ke web & database yang sama**
(REST API FastAPI + Vercel Postgres): `https://inventory-eng.vercel.app`.

## Fitur
- **User (teknisi)**: daftar akun sendiri (Username, Nama, Password), login username + password,
  melihat daftar stok, filter/pencarian, dan **hanya dapat mengambil barang (Barang Keluar)** - tercatat otomatis di web + database.
- **Admin**: login memakai akun admin web (username `admin`), akses penuh - tambah/ubah/hapus barang,
  tambah/kurangi stok (masuk/keluar), kelola kategori, kelola pengguna, lihat semua transaksi.

## Endpoint yang dipakai
`/api/login`, `/api/register`, `/api/items`, `/api/categories`, `/api/take`, `/api/items/{id}/stock`,
`/api/transactions`, `/api/my-transactions`, `/api/users`.

## Build APK
Otomatis lewat GitHub Actions (`.github/workflows/build-apk.yml`) pada setiap push ke `main`.
