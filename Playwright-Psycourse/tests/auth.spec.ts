import { test, expect } from '@playwright/test';
import { fillReg, submitReg, registerVerify, resetRate, uniq, otp, login, DEMO, text, sql } from './helpers';
test.beforeEach(() => resetRate());
const valid = async (p, o = {}) => { await fillReg(p, o); await submitReg(p); };
test('REG_001 registrasi valid ke OTP', async ({ page }) => {
  const email = uniq(); await valid(page, { email });
  await expect(page).toHaveURL(new RegExp('/verify-otp\\?mode=register&email=')); await text(page, 'Verifikasi email');
});
test('REG_002 semua kosong', async ({ page }) => {
  await page.goto('/register'); await submitReg(page); await expect(page).toHaveURL(/\/register$/);
  expect(await page.locator('input[name=name]').evaluate((e: any) => e.validationMessage)).not.toBe('');
});
test('REG_003 email tanpa @', async ({ page }) => {
  await fillReg(page, { email: 'oliviamail.com' }); await submitReg(page); await expect(page).toHaveURL(/\/register$/);
  expect(await page.locator('input[name=email]').evaluate((e: any) => e.validationMessage)).not.toBe('');
});
test('REG_004 email a@b', async ({ page }) => { await valid(page, { email: 'a@b' }); await text(page, 'Format email tidak valid.'); await expect(page).toHaveURL(/\/register$/); });
test('REG_005 konfirmasi beda', async ({ page }) => { await valid(page, { pass: 'Rahasia123', conf: 'Rahasia124' }); await text(page, 'Konfirmasi kata sandi tidak sama.'); await expect(page).toHaveURL(/\/register/); });
test('REG_006 tanpa persetujuan', async ({ page }) => { await fillReg(page, { consent: false }); await submitReg(page); await expect(page).toHaveURL(/\/register$/); });
test('REG_007 nama spasi', async ({ page }) => { await valid(page, { name: '   ' }); await text(page, 'Nama wajib diisi, maksimal 100 karakter.'); });
test('REG_008 nama 100', async ({ page }) => { await valid(page, { name: 'a'.repeat(100) }); await expect(page).toHaveURL(/verify-otp/); });
test('REG_009 nama 101', async ({ page }) => { await valid(page, { name: 'a'.repeat(101) }); await text(page, 'Nama wajib diisi, maksimal 100 karakter.'); });
test('REG_010 sandi 7', async ({ page }) => { await valid(page, { pass: 'Rahasi1' }); await text(page, 'Kata sandi minimal 8 karakter.'); });
test('REG_011 sandi 8', async ({ page }) => { await valid(page, { pass: 'Rahasia1' }); await expect(page).toHaveURL(/verify-otp/); });
test('REG_012 sandi 73', async ({ page }) => { await valid(page, { pass: 'a'.repeat(73) }); await text(page, 'Kata sandi terlalu panjang.'); });
test('REG_013 email sudah terdaftar tidak bocor', async ({ page }) => {
  await valid(page, { email: DEMO.email }); await expect(page).toHaveURL(/verify-otp/);
  await expect(page.locator('body')).not.toContainText(/sudah terdaftar/i);
});
test('REG_014 rate limit registrasi', async ({ page }) => {
  for (let i = 0; i < 5; i++) { await valid(page, { email: i % 2 ? 'a@b' : uniq() }); }
  await valid(page); await text(page, 'Terlalu banyak percobaan. Coba lagi dalam 60 menit.');
});
test('REG_015 link di form registrasi', async ({ page }) => {
  await page.goto('/register');
  await page.getByRole('link', { name: 'Ketentuan Layanan' }).click(); await expect(page).toHaveURL(/terms/);
  await page.goto('/register'); await page.getByRole('link', { name: 'Kebijakan Privasi' }).click(); await expect(page).toHaveURL(/privacy/);
  await page.goto('/register'); await page.locator('.auth-switch').getByRole('link', { name: 'Masuk' }).click(); await expect(page).toHaveURL(/login/);
});
// ---- OTP
async function toOtp(page) { const email = uniq('o'); await valid(page, { email }); await page.waitForURL(/verify-otp/); return email; }
const verify = (p) => p.getByRole('button', { name: 'Verifikasi kode' }).click();
test('OTP_001 kode benar', async ({ page }) => {
  const e = await toOtp(page); await page.fill('input[name=otp]', await otp(e)); await verify(page);
  await expect(page).toHaveURL(/welcome/); await text(page, 'Akun berhasil dibuat!');
});
test('OTP_002 kode salah', async ({ page }) => { await toOtp(page); await page.fill('input[name=otp]', '000000'); await verify(page); await text(page, 'Kode salah atau sudah kedaluwarsa.'); await expect(page).toHaveURL(/verify-otp/); });
test('OTP_003 5 digit', async ({ page }) => { await toOtp(page); await page.fill('input[name=otp]', '12345'); await verify(page); await expect(page).toHaveURL(/verify-otp/);
  expect(await page.locator('input[name=otp]').evaluate((e: any) => e.validity.patternMismatch)).toBe(true); });
test('OTP_004 maxlength 6', async ({ page }) => { await toOtp(page); await page.locator('input[name=otp]').pressSequentially('1234567'); await expect(page.locator('input[name=otp]')).toHaveValue('123456'); });
test('OTP_005 huruf', async ({ page }) => { await toOtp(page); await page.fill('input[name=otp]', 'abcdef'); await verify(page); await expect(page).toHaveURL(/verify-otp/);
  expect(await page.locator('input[name=otp]').evaluate((e: any) => e.validity.patternMismatch)).toBe(true); });
test('OTP_006 kosong', async ({ page }) => { await toOtp(page); await verify(page); expect(await page.locator('input[name=otp]').evaluate((e: any) => e.validity.valueMissing)).toBe(true); });
test('OTP_007 kirim ulang setelah 60 detik', async ({ page }) => {
  test.setTimeout(120000); const e = await toOtp(page); await page.waitForTimeout(61000);
  await page.getByRole('button', { name: 'Kirim ulang' }).click(); await text(page, 'Kode baru sudah dikirim. Periksa emailmu.');
  await page.waitForTimeout(1500); await page.fill('input[name=otp]', await otp(e)); await verify(page); await expect(page).toHaveURL(/welcome/);
});
test('OTP_008 kirim ulang sebelum 60 detik', async ({ page }) => {
  await toOtp(page); await page.getByRole('button', { name: 'Kirim ulang' }).click();
  await expect(page.locator('body')).not.toContainText('Kode baru sudah dikirim.', { timeout: 3000 }); // seharusnya diminta menunggu
  await page.getByRole('button', { name: 'Kirim ulang' }).click(); await text(page, 'Terlalu banyak percobaan. Coba lagi dalam 1 menit.');
});
test('OTP_009 tanpa email di URL', async ({ page }) => { await page.goto('/verify-otp'); await text(page, 'Alamat email tidak ditemukan.'); await expect(page.getByRole('button', { name: 'Verifikasi kode' })).toBeDisabled(); });
test('OTP_010 5 kali salah mengunci', async ({ page }) => {
  const e = await toOtp(page);
  for (let i = 0; i < 5; i++) { await page.fill('input[name=otp]', '000000'); await verify(page); await text(page, 'Kode salah atau sudah kedaluwarsa.'); await page.waitForTimeout(300); }
  await page.fill('input[name=otp]', await otp(e)); await verify(page); await expect(page).toHaveURL(/verify-otp/); await text(page, 'Kode salah');
});
// ---- WELCOME
test('WEL_001 welcome & ke dashboard', async ({ page }) => {
  await registerVerify(page); await text(page, 'BERHASIL'); await text(page, 'Akun berhasil dibuat!');
  await page.getByRole('link', { name: /Masuk ke dashboard/ }).click(); await expect(page).toHaveURL(/dashboard/); await text(page, 'Halo, Olivia Tester!');
});
test('WEL_002 kembali ke beranda', async ({ page }) => { await registerVerify(page); await page.getByRole('link', { name: /Kembali ke beranda/ }).click(); await expect(page).toHaveURL(/localhost:3000\/?$/); });
// ---- LOGIN
test('LGN_001 login valid', async ({ page }) => { await login(page); await expect(page).toHaveURL(/dashboard/); await text(page, 'Halo, Mei!'); });
test('LGN_002 sandi salah', async ({ page }) => { await login(page, DEMO.email, 'salahsandi1'); await text(page, 'Email atau kata sandi salah.'); await expect(page).toHaveURL(/login/); });
test('LGN_003 email tak terdaftar', async ({ page }) => { await login(page, 'tidakada@mail.com', 'Rahasia123'); await text(page, 'Email atau kata sandi salah.'); });
test('LGN_004 kosong', async ({ page }) => { await page.goto('/login'); await page.getByRole('button', { name: /^Masuk/ }).click(); await expect(page).toHaveURL(/login/);
  expect(await page.locator('input[name=email]').evaluate((e: any) => e.validity.valueMissing)).toBe(true); });
test('LGN_005 format email salah', async ({ page }) => { await login(page, 'siswa.demo', 'x'); await expect(page).toHaveURL(/login/);
  expect(await page.locator('input[name=email]').evaluate((e: any) => e.validity.typeMismatch)).toBe(true); });
test('LGN_006 email huruf besar', async ({ page }) => { await login(page, 'SISWA.DEMO@EXAMPLE.COM'); await expect(page).toHaveURL(/dashboard/); });
test('LGN_007 Enter', async ({ page }) => { await page.goto('/login'); await page.fill('input[name=email]', DEMO.email); await page.fill('input[name=password]', DEMO.pass); await page.press('input[name=password]', 'Enter'); await expect(page).toHaveURL(/dashboard/); });
test('LGN_008 akun belum verifikasi', async ({ page }) => {
  const email = await toOtp(page); await login(page, email, 'Rahasia123'); await expect(page).toHaveURL(/verify-otp\?mode=register/);
});
test('LGN_009 terkunci setelah 5 gagal', async ({ page }) => {
  for (let i = 0; i < 5; i++) { await login(page, DEMO.email, 'salahsandi1'); await text(page, 'Email atau kata sandi salah.'); }
  await login(page); await text(page, 'Terlalu banyak percobaan. Coba lagi dalam 15 menit.');
});
test('LGN_010 remember me 30 hari', async ({ page, context }) => {
  await page.goto('/login'); await page.fill('input[name=email]', DEMO.email); await page.fill('input[name=password]', DEMO.pass); await page.check('input[name=rememberMe]');
  await page.getByRole('button', { name: /^Masuk/ }).click(); await expect(page).toHaveURL(/dashboard/);
  const c = (await context.cookies()).find(c => c.name === 'psycourse_session')!; const days = (c.expires - Date.now() / 1000) / 86400;
  expect(days).toBeGreaterThan(28); expect(days).toBeLessThan(31);
});
test('LGN_011 logout', async ({ page, context }) => {
  await login(page); await expect(page).toHaveURL(/dashboard/); await page.getByText('Keluar').first().click(); await expect(page).toHaveURL(/login/);
  expect((await context.cookies()).find(c => c.name === 'psycourse_session')).toBeUndefined();
  await page.goto('/dashboard'); await expect(page).toHaveURL(/login/); await page.goBack(); await expect(page).not.toHaveURL(/dashboard/);
});
test('LGN_012 link lupa sandi & daftar', async ({ page }) => {
  await page.goto('/login'); await page.getByRole('link', { name: 'Lupa kata sandi?' }).click(); await expect(page).toHaveURL(/forgot-password/);
  await page.goto('/login'); await page.getByRole('link', { name: 'Daftar sekarang' }).click(); await expect(page).toHaveURL(/register/);
});
// ---- RESET
test('RST_001 reset end-to-end', async ({ page }) => {
  const e = await registerVerify(page); await page.context().clearCookies();
  await page.goto('/forgot-password'); await page.fill('input[name=email]', e); await page.getByRole('button', { name: 'Kirim kode verifikasi' }).click();
  await expect(page).toHaveURL(/verify-otp\?mode=recovery/);
  await page.waitForTimeout(1500);
  // ambil kode terbaru (pesan terbaru pertama di hasil search)
  await page.fill('input[name=otp]', await otp(e)); await verify(page); await expect(page).toHaveURL(/reset-password/);
  await page.fill('input[name=password]', 'BaruRahasia1'); await page.fill('input[name=confirmPassword]', 'BaruRahasia1');
  await page.getByRole('button', { name: 'Simpan kata sandi' }).click(); await expect(page).toHaveURL(/login/);
  await login(page, e, 'Rahasia123'); await text(page, 'Email atau kata sandi salah.');
  await login(page, e, 'BaruRahasia1'); await expect(page).toHaveURL(/dashboard/);
});
test('RST_002 email tak terdaftar', async ({ page }) => { await page.goto('/forgot-password'); await page.fill('input[name=email]', 'tidakada@mail.com'); await page.getByRole('button', { name: 'Kirim kode verifikasi' }).click(); await expect(page).toHaveURL(/verify-otp\?mode=recovery/); });
test('RST_003 kosong / format salah', async ({ page }) => {
  await page.goto('/forgot-password'); await page.getByRole('button', { name: 'Kirim kode verifikasi' }).click(); await expect(page).toHaveURL(/forgot-password/);
  await page.fill('input[name=email]', 'olivia@'); await page.getByRole('button', { name: 'Kirim kode verifikasi' }).click(); await expect(page).toHaveURL(/forgot-password/);
});
async function toReset(page) {
  const e = await registerVerify(page); await page.context().clearCookies();
  await page.goto('/forgot-password'); await page.fill('input[name=email]', e); await page.getByRole('button', { name: 'Kirim kode verifikasi' }).click();
  await page.waitForURL(/verify-otp/); await page.fill('input[name=otp]', await otp(e)); await verify(page); await page.waitForURL(/reset-password/);
}
test('RST_004 konfirmasi beda', async ({ page }) => { await toReset(page); await page.fill('input[name=password]', 'BaruRahasia1'); await page.fill('input[name=confirmPassword]', 'BaruRahasia2'); await page.getByRole('button', { name: 'Simpan kata sandi' }).click(); await text(page, 'Konfirmasi kata sandi tidak sama.'); });
test('RST_005 sandi 7 karakter', async ({ page }) => { await toReset(page); await page.fill('input[name=password]', 'Baru123'); await page.fill('input[name=confirmPassword]', 'Baru123'); await page.getByRole('button', { name: 'Simpan kata sandi' }).click(); await text(page, 'Kata sandi minimal 8 karakter.'); });
test('RST_006 reset tanpa sesi', async ({ page }) => { await page.goto('/reset-password'); await page.fill('input[name=password]', 'BaruRahasia1'); await page.fill('input[name=confirmPassword]', 'BaruRahasia1'); await page.getByRole('button', { name: 'Simpan kata sandi' }).click(); await text(page, 'Sesi reset kata sandi tidak ditemukan. Mulai lagi dari halaman Lupa kata sandi.'); });
test('RST_007 kembali ke masuk', async ({ page }) => { await page.goto('/forgot-password'); await page.getByRole('link', { name: /Kembali ke masuk/ }).click(); await expect(page).toHaveURL(/login/); });
