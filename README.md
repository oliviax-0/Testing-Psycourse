# Testing-Psycourse

Pengujian frontend (UI) aplikasi Psycourse menggunakan **Playwright**.

Test case dan hasil eksekusi ada di Google Sheets:
https://docs.google.com/spreadsheets/d/1mWeeWEc2Scao622LJhXPvYtCfT5sZ1w4eljOGGIH3Lc/edit?gid=1997438894

## Struktur

| Folder | Isi |
|---|---|
| `psycourse/` | Kode aplikasi yang diuji |
| `Playwright-Psycourse/runner/` | Runner Playwright dan definisi test case (`spec/`) |
| `Playwright-Psycourse/evidence/` | Bukti pengujian: `screenshots/`, `evidence.html`, `pw-results.json`, `sheet-values.json` |

## Prasyarat

- Node.js
- Docker Desktop
- Google Chrome

## Tata cara menjalankan

1. **Nyalakan database dan Mailpit** (Mailpit dipakai untuk membaca OTP email):
   ```
   cd psycourse/psycourse
   docker compose up -d
   ```

2. **Buat `.env.local`** di `psycourse/psycourse` agar aplikasi memakai DB dan SMTP lokal (file ini tidak di-commit):
   ```
   DATABASE_URL="postgresql://psycourse:psycourse_dev@localhost:5432/psycourse"
   SMTP_HOST=localhost
   SMTP_PORT=1025
   SMTP_USER=
   SMTP_PASS=
   ```

3. **Jalankan aplikasi** dan biarkan terbuka. Pastikan log menampilkan `Environments: .env.local, .env`, lalu cek `http://localhost:3000`:
   ```
   npm run dev
   ```

4. **Jalankan tes** di terminal lain:
   ```
   cd Playwright-Psycourse/runner
   npm install
   node runner.mjs              # semua test case
   node runner.mjs TES_014      # satu test case
   node runner.mjs NAV AKS      # satu atau beberapa story
   ```

5. **Lihat hasil** di `Playwright-Psycourse/evidence/`:
   - `pw-results.json`: status tiap test case
   - `screenshots/`: screenshot per langkah (`<TC_ID>_baris<N>.png`)
   - `evidence.html`: ringkasan visual

## Catatan

- Jangan menjalankan tes saat aplikasi memakai `.env` (DB cloud). Pastikan `.env.local` terbaca.
- Test case yang butuh Midtrans Sandbox (PKT_008, PKT_009) tidak diotomasi dan diuji manual.
