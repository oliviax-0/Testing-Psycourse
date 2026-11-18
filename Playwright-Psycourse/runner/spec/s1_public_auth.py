from dsl import *

H = "header"
TCS = []
A = TCS.append

# ---------------- NAV ----------------
A(TC("NAV_001", "NAV", "Beranda tampil dengan judul tab dan heading yang benar",
     [o("open", path="/"), *see("PsyCourse")],
     o("title", text="PsyCourse — Persiapan Tes IQ"),
     o("see", text="Kenali potensimu. Tumbuh percaya diri.")))

A(TC("NAV_002", "NAV", "Menu header mengarah ke tujuan yang benar",
     [o("open", path="/"), *see("Fitur", "Kategori", "Paket", "Masuk", "Mulai Sekarang")],
     [o("click", text="Fitur", within=H), at("/#features")],
     [o("click", text="Kategori", within=H), at("/#categories")],
     [o("click", text="Paket", within=H), at("/packages")],
     [o("open", path="/"), o("click", text="Masuk", within=H, exact=True), at("/login")],
     [o("open", path="/"), o("click", text="Mulai Sekarang", within=H), at("/register")]))

A(TC("NAV_003", "NAV", "Tombol CTA hero dan footer berfungsi",
     [o("open", path="/"), *see("Mulai Belajar Gratis")],
     [o("click", text="Mulai Belajar Gratis"), at("/register")],
     [o("open", path="/"), o("click", text="Kenali PsyCourse"), at("/#features")],
     [o("open", path="/"), o("click", text="Buat Akun Gratis"), at("/register")]))

A(TC("NAV_004", "NAV", "Kartu kategori di beranda membuka halaman kategori",
     [o("open", path="/#categories"), *see("Logika & Pola", "Penalaran Numerik", "Penalaran Verbal", "Visual & Spasial", "Memori & Konsentrasi")],
     [o("click", text="Logika & Pola"), at("/login")]))

A(TC("NAV_005", "NAV", "Link footer mengarah ke halaman yang benar",
     [o("open", path="/"), o("visible", css="footer")],
     [o("click", text="Ketentuan layanan", within="footer"), at("/terms"), o("see", text="Ketentuan Layanan")],
     [o("open", path="/"), o("click", text="Kebijakan privasi", within="footer"), at("/privacy"), o("see", text="Kebijakan Privasi")],
     [o("open", path="/"), o("click", text="Paket akses", within="footer"), at("/packages")]))

A(TC("NAV_006", "NAV", "Halaman yang tidak ada menampilkan 404",
     [o("open", path="/halamanngawur"), o("see", text="This page could not be found.")]))

A(TC("NAV_007", "NAV", "Tampilan mobile 375px tidak ada scroll horizontal",
     o("viewport", w=375, h=800),
     [o("open", path="/"), o("visible", css="button[aria-label='Buka navigasi']")],
     o("noHScroll", w=375)))

A(TC("NAV_008", "NAV", "Menu mobile terbuka dan berisi link navigasi",
     [o("viewport", w=375, h=800), o("open", path="/"), o("visible", css="button[aria-label='Buka navigasi']")],
     [o("clickCss", css="button[aria-label='Buka navigasi']"), *see("Fitur", "Kategori", "Paket", "Masuk", "Mulai Sekarang")],
     [o("click", text="Masuk", exact=True), at("/login")]))

# ---------------- REGISTRASI ----------------
def reg_tc(id, title, rows_fill, *rest):
    return TC(id, "REG", title, [RESET, o("newEmail"), *rows_fill], *rest)

A(TC("REG_001", "REG", "Registrasi dengan data valid lanjut ke verifikasi OTP",
     [RESET, o("newEmail"), o("open", path="/register"), *see("Buat akun")],
     [o("fill", name="name", value="Olivia Tester"), o("value", name="name", value="Olivia Tester")],
     [o("fill", name="email", value="{EMAIL}"), o("value", name="email", value="{EMAIL}")],
     [*fill(password="Rahasia123", confirmPassword="Rahasia123"), o("value", name="password", value="Rahasia123")],
     [o("check", name="privacyConsent"), o("checked", name="privacyConsent")],
     [submit(), at("/verify-otp?mode=register&email="), *see("Verifikasi email")]))

A(TC("REG_002", "REG", "Semua field kosong tidak bisa submit",
     [RESET, o("open", path="/register"), submit(), o("valmsg", name="name"), stays("/register")]))

A(TC("REG_003", "REG", "Format email salah ditolak",
     [RESET, *reg(email="oliviamail.com"), o("value", name="email", value="oliviamail.com")],
     [submit(), o("valmsg", name="email"), stays("/register")]))

A(TC("REG_004", "REG", "Email tanpa domain lengkap ditolak server",
     [RESET, *reg(email="a@b"), o("value", name="email", value="a@b")],
     [submit(), *see("Format email tidak valid."), stays("/register")]))

A(TC("REG_005", "REG", "Konfirmasi kata sandi tidak sama",
     [RESET, o("newEmail"), *reg(cf="Rahasia124"), o("value", name="confirmPassword", value="Rahasia124")],
     [submit(), *see("Konfirmasi kata sandi tidak sama."), stays("/register")]))

A(TC("REG_006", "REG", "Tidak mencentang persetujuan privasi",
     [RESET, o("newEmail"), *reg(consent=False), o("checked", name="privacyConsent", value=False)],
     [submit(), o("valmsg", name="privacyConsent"), stays("/register")]))

A(TC("REG_007", "REG", "Nama hanya berisi spasi ditolak",
     [RESET, o("newEmail"), *reg(name="   "), o("value", name="name", value="   ")],
     [submit(), *see("Nama wajib diisi, maksimal 100 karakter.")]))

A(TC("REG_008", "REG", "Nama 100 karakter diterima",
     [RESET, o("newEmail"), *reg(name="a" * 100), o("valueLen", name="name", len=100)],
     [submit(), at("/verify-otp")]))

A(TC("REG_009", "REG", "Nama 101 karakter ditolak",
     [RESET, o("newEmail"), *reg(name="a" * 101), o("valueLen", name="name", len=101)],
     [submit(), *see("Nama wajib diisi, maksimal 100 karakter.")]))

A(TC("REG_010", "REG", "Kata sandi 7 karakter ditolak",
     [RESET, o("newEmail"), *reg(pw="Rahasi1"), o("value", name="password", value="Rahasi1")],
     [submit(), *see("Kata sandi minimal 8 karakter.")]))

A(TC("REG_011", "REG", "Kata sandi tepat 8 karakter diterima",
     [RESET, o("newEmail"), *reg(pw="Rahasia1"), o("value", name="password", value="Rahasia1")],
     [submit(), at("/verify-otp")]))

A(TC("REG_012", "REG", "Kata sandi 73 karakter ditolak",
     [RESET, o("newEmail"), *reg(pw="Aa1" * 24 + "b"), o("valueLen", name="password", len=73)],
     [submit(), *see("Kata sandi terlalu panjang.")]))

A(TC("REG_013", "REG", "Email terdaftar tidak membocorkan informasi akun",
     [RESET, *reg(email="{DEMO_EMAIL}"), o("value", name="email", value="{DEMO_EMAIL}")],
     [submit(), at("/verify-otp"), o("notsee", text="sudah terdaftar")]))

A(TC("REG_014", "REG", "Batas percobaan registrasi (rate limit)",
     [RESET, *sum([[*reg(email="a@b"), submit(), *see("Format email tidak valid.")] for _ in range(5)], [])],
     [o("newEmail"), *reg(), submit(), *see("Terlalu banyak percobaan. Coba lagi dalam 60 menit.")]))

A(TC("REG_015", "REG", "Link Ketentuan, Privasi, dan Masuk di form registrasi",
     [o("open", path="/register"), o("visible", css="form")],
     [o("click", text="Ketentuan Layanan", within="form"), at("/terms")],
     [o("open", path="/register"), o("click", text="Kebijakan Privasi", within="form"), at("/privacy")],
     [o("open", path="/register"), o("click", text="Masuk", within="main", exact=True), at("/login")]))

# ---------------- OTP ----------------
REGO = [RESET, o("registerOnly")]
BTN = "Verifikasi kode"

A(TC("OTP_001", "OTP", "Kode OTP benar mengaktifkan akun",
     [*REGO, o("fillOtp"), o("valueLen", name="otp", len=6)],
     [o("click", text=BTN), at("/welcome"), *see("Akun berhasil dibuat!"), o("cookie", name="psycourse_session")]))

A(TC("OTP_002", "OTP", "Kode OTP salah",
     [*REGO, o("fill", name="otp", value="000000"), o("value", name="otp", value="000000")],
     [o("click", text=BTN), *see("Kode salah atau sudah kedaluwarsa."), stays("/verify-otp")]))

A(TC("OTP_003", "OTP", "Kode OTP 5 digit ditolak",
     [*REGO, o("fill", name="otp", value="12345")],
     [o("click", text=BTN), o("valmsg", name="otp"), stays("/verify-otp")]))

A(TC("OTP_004", "OTP", "Kode OTP lebih dari 6 digit terpotong",
     [*REGO, o("type", name="otp", value="1234567"), o("value", name="otp", value="123456")]))

A(TC("OTP_005", "OTP", "Kode OTP berisi huruf ditolak",
     [*REGO, o("fill", name="otp", value="abcdef")],
     [o("click", text=BTN), o("valmsg", name="otp"), stays("/verify-otp")]))

A(TC("OTP_006", "OTP", "Kode OTP kosong",
     [*REGO, o("click", text=BTN), o("valmsg", name="otp"), stays("/verify-otp")]))

A(TC("OTP_007", "OTP", "Kirim ulang kode setelah jeda 60 detik",
     [*REGO, o("markMail"), o("sleep", sec=62), o("click", text="Kirim ulang"), *see("Kode baru sudah dikirim. Periksa emailmu.")],
     [o("fillOtpNew"), o("click", text=BTN), at("/welcome")]))

A(TC("OTP_008", "OTP", "Kirim ulang kode sebelum 60 detik",
     [*REGO, o("markMail"), o("click", text="Kirim ulang"), o("mailUnchanged")],
     [o("click", text="Kirim ulang"), *see("Terlalu banyak percobaan. Coba lagi dalam 1 menit.")]))

A(TC("OTP_009", "OTP", "Halaman OTP dibuka tanpa email di URL",
     [o("open", path="/verify-otp"), *see("Alamat email tidak ditemukan. Mulai lagi"), o("disabled", text=BTN)]))

wrong5 = sum([[o("fill", name="otp", value="000000"), o("click", text=BTN), o("sleep", sec=0.7), *see("Kode salah atau sudah kedaluwarsa.")] for _ in range(5)], [])
A(TC("OTP_010", "OTP", "Salah kode 5 kali mengunci kode",
     [*REGO, *wrong5],
     [o("fillOtp"), o("click", text=BTN), o("sleep", sec=0.7), stays("/verify-otp"), *see("Kode salah atau sudah kedaluwarsa.")]))

# ---------------- WELCOME ----------------
A(TC("WEL_001", "WEL", "Halaman welcome setelah verifikasi",
     [o("registerVerify"), *see("BERHASIL", "Akun berhasil dibuat!", "Masuk ke dashboard")],
     [o("click", text="Masuk ke dashboard"), at("/dashboard"), *see("Halo, Olivia Tester! ✦")]))

A(TC("WEL_002", "WEL", "Tombol Kembali ke beranda di welcome",
     [o("registerVerify"), o("click", text="Kembali ke beranda"), at("/", exact=True)]))

# ---------------- LOGIN ----------------
D = "{DEMO_EMAIL}"; DP = "{DEMO_PASS}"
A(TC("LGN_001", "LGN", "Login dengan akun valid",
     [RESET, o("open", path="/login"), *see("Selamat datang kembali")],
     [*fill(email=D, password=DP), o("value", name="email", value=D)],
     [submit(), at("/dashboard"), *see("Halo, Mei! ✦")]))

A(TC("LGN_002", "LGN", "Kata sandi salah",
     [RESET, *login_try(D, "salahsandi1")[:-1], o("value", name="password", value="salahsandi1")],
     [submit(), *see("Email atau kata sandi salah."), stays("/login")]))

A(TC("LGN_003", "LGN", "Email tidak terdaftar",
     [RESET, *login_try("tidakada@mail.com", "Rahasia123")[:-1], o("value", name="email", value="tidakada@mail.com")],
     [submit(), *see("Email atau kata sandi salah.")]))

A(TC("LGN_004", "LGN", "Field kosong",
     [RESET, o("open", path="/login"), submit(), o("valmsg", name="email"), stays("/login")]))

A(TC("LGN_005", "LGN", "Format email salah",
     [RESET, *login_try("siswa.demo", "apasaja")[:-1], o("value", name="email", value="siswa.demo")],
     [submit(), o("valmsg", name="email"), stays("/login")]))

A(TC("LGN_006", "LGN", "Email huruf besar tetap bisa login",
     [RESET, *login_try("SISWA.DEMO@EXAMPLE.COM", DP)[:-1], o("value", name="email", value="SISWA.DEMO@EXAMPLE.COM")],
     [submit(), at("/dashboard")]))

A(TC("LGN_007", "LGN", "Login menekan tombol Enter",
     [RESET, *login_try(D, DP)[:-1], o("value", name="email", value=D)],
     [o("press", name="password", key="Enter"), at("/dashboard")]))

A(TC("LGN_008", "LGN", "Akun belum verifikasi email",
     [RESET, o("registerOnly"), o("clearCookies"), *login_try("{EMAIL}", "Rahasia123")[:-1], o("value", name="email", value="{EMAIL}")],
     [submit(), at("/verify-otp?mode=register&email=")]))

A(TC("LGN_009", "LGN", "Terkunci setelah 5 kali gagal",
     [RESET, *sum([[*login_try(D, "salahsandi1"), *see("Email atau kata sandi salah."), o("sleep", sec=0.3)] for _ in range(5)], [])],
     [*login_try(D, DP), *see("Terlalu banyak percobaan. Coba lagi dalam 15 menit."), stays("/login")]))

A(TC("LGN_010", "LGN", "Tetap masuk (remember me) 30 hari",
     [RESET, o("open", path="/login"), *fill(email=D, password=DP), o("check", name="rememberMe"), o("checked", name="rememberMe")],
     [submit(), at("/dashboard")],
     [o("cookie", name="psycourse_session", days=30)]))

A(TC("LGN_011", "LGN", "Keluar (logout) dari dashboard",
     [RESET, o("login"), o("click", text="Keluar"), at("/login"), o("cookie", name="psycourse_session", exists=False)],
     [o("open", path="/dashboard"), at("/login")],
     [o("back"), at("/login"), o("notsee", text="Halo,")]))

A(TC("LGN_012", "LGN", "Link Lupa kata sandi dan Daftar sekarang",
     [o("open", path="/login"), o("click", text="Lupa kata sandi?"), at("/forgot-password")],
     [o("open", path="/login"), o("click", text="Daftar sekarang"), at("/register")]))

# ---------------- RESET PASSWORD ----------------
PWN = "BaruRahasia1"
A(TC("RST_001", "RST", "Reset kata sandi berhasil end-to-end",
     [RESET, o("registerVerify"), o("clearCookies"), o("open", path="/forgot-password"), o("fill", name="email", value="{EMAIL}"), o("value", name="email", value="{EMAIL}")],
     [o("markMail"), submit(), at("/verify-otp?mode=recovery&email=")],
     [o("fillOtpNew"), o("click", text=BTN), at("/reset-password")],
     [*fill(password=PWN, confirmPassword=PWN), o("click", text="Simpan kata sandi"), at("/login")],
     [*login_try("{EMAIL}", "Rahasia123"), *see("Email atau kata sandi salah.")],
     [o("open", path="/login"), *fill(email="{EMAIL}", password=PWN), submit(), at("/dashboard")]))

A(TC("RST_002", "RST", "Lupa sandi dengan email tidak terdaftar",
     [o("open", path="/forgot-password"), o("fill", name="email", value="tidakada@mail.com"), o("clearMail"), submit(),
      at("/verify-otp?mode=recovery"), o("mailCountIs", email="tidakada@mail.com", n=0)]))

A(TC("RST_003", "RST", "Lupa sandi email kosong / format salah",
     [o("open", path="/forgot-password"), submit(), o("valmsg", name="email"), stays("/forgot-password")],
     [o("fill", name="email", value="olivia@"), submit(), o("valmsg", name="email"), stays("/forgot-password")]))

RST_PRE = [RESET, o("registerVerify"), o("clearCookies"), o("toResetPage")]
A(TC("RST_004", "RST", "Konfirmasi kata sandi baru tidak sama",
     [*RST_PRE, *fill(password=PWN, confirmPassword="BaruRahasia2")],
     [o("click", text="Simpan kata sandi"), *see("Konfirmasi kata sandi tidak sama.")]))

A(TC("RST_005", "RST", "Kata sandi baru kurang dari 8 karakter",
     [*RST_PRE, *fill(password="Baru123", confirmPassword="Baru123"), o("click", text="Simpan kata sandi"), *see("Kata sandi minimal 8 karakter.")]))

A(TC("RST_006", "RST", "Halaman reset dibuka langsung tanpa sesi reset",
     [o("open", path="/reset-password"), *fill(password=PWN, confirmPassword=PWN), o("click", text="Simpan kata sandi"),
      *see("Sesi reset kata sandi tidak ditemukan. Mulai lagi dari halaman Lupa kata sandi.")]))

A(TC("RST_007", "RST", "Link kembali ke masuk",
     [o("open", path="/forgot-password"), o("click", text="Kembali ke masuk"), at("/login")]))

# ---------------- AKSES TANPA LOGIN ----------------
for n, (path, tid) in enumerate([
    ("/dashboard", "AKS_001"), ("/welcome", "AKS_002"), ("/courses/logika", "AKS_003"), ("/tests/logika-test-1", "AKS_004"),
    ("/results/abc", "AKS_005"), ("/history", "AKS_006"), ("/settings", "AKS_007"), ("/checkout?package=premium", "AKS_008"),
    ("/payment-result", "AKS_009"), ("/payment-history", "AKS_010")]):
    A(TC(tid, "AKS", f"Buka {path} tanpa login", [o("open", path=path), at("/login")]))

A(TC("AKS_011", "AKS", "Buka halaman admin tanpa login",
     [o("open", path="/admin/dashboard"), at("/admin", exact=True), *see("Admin masuk")],
     [o("open", path="/admin/questions"), at("/admin", exact=True)]))

A(TC("AKS_012", "AKS", "Halaman publik bisa dibuka tanpa login",
     [o("open", path="/packages"), at("/packages"), *see("Free Access")],
     [o("open", path="/terms"), at("/terms"), o("open", path="/privacy"), at("/privacy")]))

A(TC("AKS_013", "AKS", "Cookie sesi palsu tidak memberi akses",
     [o("open", path="/login"), o("setCookie", name="psycourse_session", value="palsu123"), o("cookie", name="psycourse_session")],
     [o("open", path="/dashboard"), at("/login")]))
