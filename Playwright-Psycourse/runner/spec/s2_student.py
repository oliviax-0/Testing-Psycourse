from dsl import *

TCS = []
A = TCS.append

FREE = [RESET, o("registerVerify")]
PREM = [RESET, o("registerVerify"), o("makePremium"), o("open", path="/dashboard")]
OPT = "label.answer-choice"
NEXT = o("click", text="Berikutnya")
def pick(n=1): return o("clickCss", css=f"{OPT}:nth-of-type({n})")
def answer_all(n=5):
    ops = []
    for i in range(n):
        ops.append(pick(1))
        ops.append(o("sleep", sec=0.6))
        if i < n - 1: ops.append(NEXT)
    return ops
def take_test1():
    return [o("open", path="/tests/logika-test-1"), *answer_all(), o("click", text="Kirim jawaban"), at("/results/")]

# ---------------- DASHBOARD ----------------
A(TC("DSB_001", "DSB", "Dashboard akun baru menampilkan data awal",
     [*FREE, o("open", path="/dashboard"), *see("Halo, Olivia Tester! ✦")],
     [*see("Tes diselesaikan", "Kategori tersedia", "Akses akun", "Gratis", "Paket aktif", "Belum ada akses premium"), o("sqlIs", stmt="select 0", expect=0)],
     [*see("Belum ada latihan")]))

A(TC("DSB_002", "DSB", "Dashboard akun Premium",
     [*PREM, *see("Akses akun", "Premium", "Paket aktif")]))

A(TC("DSB_003", "DSB", "Kartu kategori membuka halaman kategori",
     [*FREE, o("open", path="/dashboard"), o("click", text="Logika & Pola"), at("/courses/logika"), *see("Logika & Pola")]))

A(TC("DSB_004", "DSB", "Akses cepat dan link riwayat",
     [*FREE, o("open", path="/dashboard"), o("click", text="Paket akses"), at("/packages")],
     [o("open", path="/dashboard"), o("click", text="Lihat riwayat"), at("/history")],
     [o("open", path="/dashboard"), o("click", text="Lihat transaksi"), at("/payment-history")]))

A(TC("DSB_005", "DSB", "Riwayat latihan bertambah setelah menyelesaikan tes",
     [*FREE, *take_test1(), o("open", path="/dashboard"), *see("Tes diselesaikan"), o("notsee", text="Belum ada latihan")]))

# ---------------- KATEGORI ----------------
A(TC("KAT_001", "KAT", "Halaman kategori akun Free",
     [*FREE, o("open", path="/courses/logika"), *see("Logika & Pola", "Test 1", "Test 2", "Practice Question Bank")],
     [*see("Gratis", "Mulai tes")],
     [*see("Premium", "Memerlukan paket premium")]))

A(TC("KAT_002", "KAT", "Akun Free klik Test 2 muncul notifikasi paywall",
     [*FREE, o("open", path="/courses/logika"), o("click", text="Buka akses"), *see("Lihat paket akses")],
     [o("click", text="Lihat paket akses"), at("/packages?testId=logika-test-2")]))

A(TC("KAT_003", "KAT", "Notifikasi paywall bisa ditutup",
     [*FREE, o("open", path="/courses/logika"), o("click", text="Buka akses"), *see("Lihat paket akses")],
     [o("clickCss", css="button[aria-label='Tutup notifikasi']"), o("notsee", text="Lihat paket akses"), at("/courses/logika")]))

A(TC("KAT_004", "KAT", "Halaman kategori akun Premium",
     [*PREM, o("open", path="/courses/logika"), *see("Aktif", "Mulai tes", "Mulai latihan")]))

A(TC("KAT_005", "KAT", "Slug kategori tidak ada",
     [*FREE, o("open", path="/courses/abc"), *see("This page could not be found.")]))

A(TC("KAT_006", "KAT", "Jumlah soal di kartu sesuai jumlah soal aktif",
     [*PREM, o("open", path="/courses/logika"), *see("5 soal")],
     [o("click", text="Mulai tes"), at("/tests/logika-test-1"), *see("dari 5")]))

A(TC("KAT_007", "KAT", "Link kembali ke dashboard",
     [*FREE, o("open", path="/courses/logika"), o("click", text="Dashboard"), at("/dashboard")]))

# ---------------- TES ----------------
TIMER = "div.timer"
A(TC("TES_001", "TES", "Test 1 tampil tanpa timer",
     [*FREE, o("open", path="/tests/logika-test-1"), *see("TEST 1 · TANPA TIMER", "Soal 1 dari 5", "0 dijawab")],
     [o("visible", css=TIMER, count=0)],
     [o("visible", css=OPT, count=4)],
     [o("disabled", text="Sebelumnya")]))

A(TC("TES_002", "TES", "Memilih jawaban dan navigasi soal",
     [*FREE, o("open", path="/tests/logika-test-1"), pick(1), *see("1 dijawab"), o("visible", css=f"{OPT}.selected", count=1)],
     [NEXT, *see("Soal 2 dari 5")],
     [o("click", text="Sebelumnya"), *see("Soal 1 dari 5"), o("visible", css=f"{OPT}.selected", count=1)]))

A(TC("TES_003", "TES", "Jawaban tersimpan otomatis setelah refresh",
     [*FREE, o("open", path="/tests/logika-test-1"), pick(1), *see("1 dijawab")],
     [o("sleep", sec=2), o("reload"), *see("1 dijawab"), o("visible", css=f"{OPT}.selected", count=1)]))

A(TC("TES_004", "TES", "Kirim jawaban setelah semua dijawab",
     [*FREE, o("open", path="/tests/logika-test-1"), *answer_all(), *see("Kirim jawaban")],
     [o("click", text="Kirim jawaban"), at("/results/"), *see("Persentase")]))

A(TC("TES_005", "TES", "Kirim tes tanpa menjawab satu soal pun",
     [*FREE, o("open", path="/tests/logika-test-1"), *[NEXT for _ in range(4)], *see("Kirim jawaban")],
     [o("click", text="Kirim jawaban"), o("sleep", sec=1.5), o("url", path="/tests/logika-test-1", timeout=2000)]))

A(TC("TES_006", "TES", "Akun Free membuka Test 2 langsung lewat URL",
     [*FREE, o("open", path="/tests/logika-test-2"), *see("Kamu belum memiliki akses ke tes ini."), o("visible", css=OPT, count=0)]))

A(TC("TES_007", "TES", "Akun Free membuka Practice lewat URL",
     [*FREE, o("open", path="/tests/logika-practice"), *see("Kamu belum memiliki akses ke tes ini.")]))

A(TC("TES_008", "TES", "Test 2 menampilkan timer berjalan",
     [*PREM, o("open", path="/tests/logika-test-2"), *see("TEST 2 · BERTIMER", "Tes akan dikirim otomatis saat waktu habis."), o("visible", css=TIMER, count=1)],
     [o("sleep", sec=5), o("timerBelow", css=TIMER, seconds=598)]))

A(TC("TES_009", "TES", "Timer tidak reset saat halaman di-refresh",
     [*PREM, o("open", path="/tests/logika-test-2"), o("visible", css=TIMER, count=1), o("sleep", sec=4)],
     [o("reload"), o("sleep", sec=1), o("timerBelow", css=TIMER, seconds=599)]))

A(TC("TES_010", "TES", "Waktu habis mengirim tes otomatis",
     [*PREM, o("sql", stmt="""UPDATE "Test" SET "timerDetik"=8 WHERE slug='logika-test-2'"""), o("open", path="/tests/logika-test-2"), pick(1), o("sleep", sec=1), o("url", path="/results/", timeout=20000)],
     [*see("Waktu tes sudah habis. Hasil dan jawaban yang tersimpan telah dihitung."),
      {**o("sql", stmt="""UPDATE "Test" SET "timerDetik"=600 WHERE slug='logika-test-2'"""), "cleanup": True}]))

A(TC("TES_011", "TES", "Practice bisa dikerjakan akun Premium",
     [*PREM, o("open", path="/tests/logika-practice"), *see("PRACTICE QUESTION BANK", "Soal 1 dari 5")]))

A(TC("TES_012", "TES", "Tes dengan id tidak ada",
     [*FREE, o("open", path="/tests/abc"), *see("Tes tidak ditemukan.")]))

A(TC("TES_013", "TES", "Tes tanpa soal aktif",
     [*FREE, o("open", path="/tests/numerik-test-1"), *see("Tes belum memiliki soal aktif.")]))

A(TC("TES_014", "TES", "Test 1 dibuka lagi setelah selesai (satu percobaan resmi)",
     [*FREE, *take_test1(), o("open", path="/tests/logika-test-1"), o("sleep", sec=1.5),
      o("seeAny", texts=["sudah mengerjakan", "percobaan resmi", "sudah selesai", "bukan resmi"])]))

A(TC("TES_015", "TES", "Link kembali ke kategori",
     [*FREE, o("open", path="/tests/logika-test-1"), o("click", text="Kembali ke kategori"), at("/courses/logika")]))

# ---------------- HASIL & RIWAYAT ----------------
A(TC("HSL_001", "HSL", "Halaman hasil menampilkan skor",
     [*FREE, *take_test1(), *see("Hasil latihanmu", "JAWABAN BENAR", "Persentase")],
     [o("click", text="Lihat riwayat tes"), at("/history")],
     [o("back"), o("click", text="Kembali ke dashboard"), at("/dashboard")]))

A(TC("HSL_002", "HSL", "Riwayat tes menampilkan tes yang selesai",
     [*FREE, *take_test1(), o("open", path="/history"), *see("Kategori", "Tes", "Skor", "Status", "Logika & Pola", "TEST_1", "SELESAI")]))

A(TC("HSL_003", "HSL", "Riwayat kosong akun baru",
     [*FREE, o("open", path="/history"), *see("Belum ada riwayat tes", "Mulai latihan")]))

A(TC("HSL_004", "HSL", "Id hasil tidak ada",
     [*FREE, o("open", path="/results/abc"), *see("Percobaan tidak ditemukan.")]))

A(TC("HSL_005", "HSL", "Membuka hasil milik akun lain",
     [*FREE, *take_test1(), o("saveUrl"), o("clearCookies"), o("registerVerify"), o("openSaved"), *see("Percobaan tidak ditemukan."), o("notsee", text="Persentase")]))

# ---------------- PAKET & CHECKOUT ----------------
A(TC("PKT_001", "PKT", "Halaman paket menampilkan 3 paket",
     [o("open", path="/packages"), *see("Free Access", "Gratis", "Single Test", "Rp19.000", "Premium Access", "Rp49.000", "REKOMENDASI UTAMA")]))

A(TC("PKT_002", "PKT", "Pilih Premium membuka checkout",
     [*FREE, o("open", path="/packages"), o("click", text="Pilih Premium"), at("/checkout?package=premium"), *see("Premium Access", "Rp49.000")],
     [*see("QRIS", "Transfer Virtual Account", "Kartu Kredit / Debit")]))

A(TC("PKT_003", "PKT", "Single Test tanpa memilih tes",
     [*FREE, o("open", path="/packages"), *see("Pilih tes terlebih dahulu")],
     [o("click", text="Pilih tes terlebih dahulu"), at("/dashboard")]))

A(TC("PKT_004", "PKT", "Single Test dari paywall Test 2",
     [*FREE, o("open", path="/packages?testId=logika-test-2"), o("click", text="Pilih Single Test"), at("/checkout?package=single&testId=logika-test-2"), *see("Single Test", "Rp19.000")]))

A(TC("PKT_005", "PKT", "Paket tidak valid di URL checkout",
     [*FREE, o("open", path="/checkout?package=ngawur"), o("notsee", text="Premium Access")]))
