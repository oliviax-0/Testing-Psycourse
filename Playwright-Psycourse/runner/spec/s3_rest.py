from dsl import *
TCS = []
A = TCS.append
FREE = [RESET, o("registerVerify")]
NAME = "main form input:not([type='email']):not([type='password'])"
EMAILF = "input[type='email']"
PWD = "input[type='password']"
SW = "button[aria-label='Switch language']"
ADMIN = "admin.demo@example.com"

# ---- PKT lanjutan ----
A(TC("PKT_006", "PKT", "Pilih Virtual Account menampilkan daftar bank",
     [*FREE, o("open", path="/checkout?package=premium"), o("clickLabel", text="Transfer Virtual Account"), *see("BCA VA", "BNI VA", "BRI VA", "Mandiri Bill", "Permata VA")]))
A(TC("PKT_007", "PKT", "Kartu kredit dengan nomor kosong",
     [*FREE, o("open", path="/checkout?package=premium"), o("clickLabel", text="Kartu Kredit / Debit Online"), *see("Nomor kartu")],
     [o("click", text="Bayar Sekarang"), *see("Nomor kartu kredit/debit tidak valid.")]))
A(TC("PKT_010", "PKT", "Halaman hasil pembayaran tanpa order valid",
     [*FREE, o("open", path="/payment-result?order_id=abc"), *see("ID pembayaran tidak ditemukan.")]))
A(TC("PKT_011", "PKT", "Riwayat pembayaran kosong",
     [*FREE, o("open", path="/payment-history"), *see("Riwayat pembayaran", "Belum ada transaksi", "Lihat paket akses")]))

# ---- SET ----
A(TC("SET_001", "SET", "Data akun terisi otomatis",
     [*FREE, o("open", path="/settings"), o("valueCss", css=NAME, value="Olivia Tester"), o("valueCss", css=EMAILF, value="{EMAIL}"), o("attrCss", css=EMAILF, attr="readonly")]))
A(TC("SET_002", "SET", "Ubah nama berhasil disimpan",
     [*FREE, o("open", path="/settings"), o("fillCss", css=NAME, value="Olivia QA Edit"), o("valueCss", css=NAME, value="Olivia QA Edit")],
     [o("click", text="Simpan perubahan"), *see("Perubahan tersimpan.")],
     [o("reload"), o("sleep", sec=1), o("valueCss", css=NAME, value="Olivia QA Edit")]))
A(TC("SET_003", "SET", "Nama dikosongkan",
     [*FREE, o("open", path="/settings"), o("fillCss", css=NAME, value=""), o("click", text="Simpan perubahan"), o("valmsgCss", css=NAME)]))
A(TC("SET_004", "SET", "Nama lebih dari 100 karakter",
     [*FREE, o("open", path="/settings"), o("fillCss", css=NAME, value=""), o("typeCss", css=NAME, value="a" * 101), o("valueLenCss", css=NAME, len=100)]))
A(TC("SET_005", "SET", "Ubah bahasa antarmuka",
     [*FREE, o("open", path="/settings"), o("selectCss", css="select", value="en"), *see("Preferensi bahasa tersimpan.")],
     [o("reload"), o("sleep", sec=1), o("valueCss", css="select", value="en")]))
A(TC("SET_006", "SET", "Hapus akun dengan kata sandi salah",
     [*FREE, o("open", path="/settings"), o("click", text="Minta hapus data"), *see("Tindakan ini tidak dapat dibatalkan.")],
     [o("fillCss", css=PWD, value="salahpassword"), o("click", text="Hapus akun dan data saya"), *see("Kata sandi salah.")]))
A(TC("SET_007", "SET", "Hapus akun dengan kata sandi benar",
     [*FREE, o("open", path="/settings"), o("click", text="Minta hapus data"), o("fillCss", css=PWD, value="Rahasia123"), o("valueCss", css=PWD, value="Rahasia123")],
     [o("click", text="Hapus akun dan data saya"), o("sleep", sec=2), o("cookie", name="psycourse_session", exists=False)],
     [*login_try("{EMAIL}", "Rahasia123"), *see("Email atau kata sandi salah.")]))
A(TC("SET_008", "SET", "Link kebijakan dari pengaturan",
     [*FREE, o("open", path="/settings"), o("click", text="Kebijakan privasi"), at("/privacy")],
     [o("open", path="/settings"), o("click", text="Ketentuan layanan"), at("/terms")]))

# ---- ADM ----
def admin_login(email=ADMIN, pw="{DEMO_PASS}"):
    return [RESET, o("open", path="/admin"), *fill(email=email, password=pw), o("click", text="Masuk ke admin")]
A(TC("ADM_001", "ADM", "Login admin berhasil",
     [RESET, o("open", path="/admin"), *see("Admin masuk")],
     [*fill(email=ADMIN, password="{DEMO_PASS}"), o("click", text="Masuk ke admin"), at("/admin/dashboard"), *see("Dashboard admin")]))
A(TC("ADM_002", "ADM", "Akun siswa login di halaman admin",
     [*admin_login("{DEMO_EMAIL}"), *see("Akun ini bukan administrator."), o("cookie", name="psycourse_session", exists=False)]))
A(TC("ADM_003", "ADM", "Kata sandi admin salah",
     [*admin_login(ADMIN, "salah12345"), *see("Email atau kata sandi salah.")]))
A(TC("ADM_004", "ADM", "Akun siswa membuka /admin/dashboard lewat URL",
     [*FREE, o("open", path="/admin/dashboard"), at("/admin", exact=True)]))
A(TC("ADM_005", "ADM", "Menu admin membuka halaman pengelolaan",
     [*admin_login(), at("/admin/dashboard"), o("click", text="Kelola kategori"), at("/admin/courses")],
     [o("open", path="/admin/dashboard"), o("click", text="Kelola pertanyaan"), at("/admin/questions"), *see("Kelola pertanyaan")],
     [o("click", text="Tambah"), *see("Mode demo: perubahan hanya berada di state halaman dan tidak tersimpan di database.")]))
A(TC("ADM_006", "ADM", "Section admin yang tidak ada",
     [*admin_login(), at("/admin/dashboard"), o("open", path="/admin/abc"), *see("This page could not be found.")]))
A(TC("ADM_007", "ADM", "Keluar dari admin",
     [*admin_login(), at("/admin/dashboard"), o("click", text="Keluar"), at("/admin", exact=True), o("cookie", name="psycourse_session", exists=False)]))

# ---- BHS ----
A(TC("BHS_001", "BHS", "Ganti bahasa ke English di beranda",
     [o("open", path="/"), o("clickCss", css=SW), *see("Discover your potential. Grow your confidence.", "EN")],
     [o("reload"), *see("Discover your potential. Grow your confidence.")],
     [o("clickCss", css=SW), *see("Kenali potensimu. Tumbuh percaya diri.")]))
A(TC("BHS_002", "BHS", "Konsistensi terjemahan di halaman lain",
     [o("open", path="/"), o("clickCss", css=SW), o("open", path="/login"), o("sleep", sec=1), o("notsee", text="Selamat datang kembali"), o("notsee", text="Masuk ke akun PsyCourse-mu."), o("notsee", text="Lupa kata sandi?"), o("notsee", text="Belum punya akun?"), o("notsee", text="Tetap masuk")]))
A(TC("BHS_003", "BHS", "Halaman Ketentuan Layanan",
     [o("open", path="/terms"), *see("Ketentuan Layanan")]))
A(TC("BHS_004", "BHS", "Halaman Kebijakan Privasi",
     [o("open", path="/privacy"), *see("Kebijakan Privasi")]))
