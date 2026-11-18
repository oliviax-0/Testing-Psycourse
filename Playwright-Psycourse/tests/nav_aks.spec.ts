import { test, expect } from '@playwright/test';
import { text } from './helpers';
test('NAV_001 beranda judul & heading', async ({ page }) => {
  await page.goto('/'); await expect(page).toHaveTitle('PsyCourse — Persiapan Tes IQ');
  await text(page, 'Kenali potensimu. Tumbuh percaya diri.');
});
test('NAV_002 menu header', async ({ page }) => {
  await page.goto('/');
  const h = page.locator('header'); for (const t of ['Fitur','Kategori','Paket','Masuk','Mulai Sekarang']) await expect(h).toContainText(t);
  await h.getByRole('link', { name: 'Fitur' }).click(); await expect(page).toHaveURL(/\/#features/);
  await h.getByRole('link', { name: 'Kategori' }).click(); await expect(page).toHaveURL(/\/#categories/);
  await h.getByRole('link', { name: 'Paket' }).click(); await expect(page).toHaveURL(/\/packages/);
  await page.goto('/'); await h.getByRole('link', { name: 'Masuk' }).click(); await expect(page).toHaveURL(/\/login/);
  await page.goto('/'); await h.getByRole('link', { name: 'Mulai Sekarang' }).click(); await expect(page).toHaveURL(/\/register/);
});
test('NAV_003 CTA hero & footer', async ({ page }) => {
  await page.goto('/'); await page.getByRole('link', { name: /Mulai Belajar Gratis/ }).first().click(); await expect(page).toHaveURL(/\/register/);
  await page.goto('/'); await page.getByRole('link', { name: /Kenali PsyCourse/ }).first().click(); await expect(page).toHaveURL(/#features/);
  await page.goto('/'); await page.getByRole('link', { name: /Buat Akun Gratis/ }).first().click(); await expect(page).toHaveURL(/\/register/);
});
test('NAV_004 kartu kategori', async ({ page }) => {
  await page.goto('/#categories');
  for (const t of ['Logika & Pola','Penalaran Numerik','Penalaran Verbal','Visual & Spasial','Memori & Konsentrasi']) await text(page, t);
  await page.getByRole('link', { name: /Logika & Pola/ }).first().click(); await expect(page).toHaveURL(/\/login/);
});
test('NAV_005 link footer', async ({ page }) => {
  await page.goto('/'); const f = page.locator('footer');
  await f.getByRole('link', { name: 'Ketentuan layanan' }).click(); await expect(page).toHaveURL(/\/terms/); await text(page, 'Ketentuan Layanan');
  await page.goto('/'); await f.getByRole('link', { name: 'Kebijakan privasi' }).click(); await expect(page).toHaveURL(/\/privacy/); await text(page, 'Kebijakan Privasi');
  await page.goto('/'); await f.getByRole('link', { name: 'Paket akses' }).click(); await expect(page).toHaveURL(/\/packages/);
});
test('NAV_006 404', async ({ page }) => {
  const r = await page.goto('/halamanngawur'); await text(page, 'This page could not be found.');
});
test('NAV_007 mobile 375 tanpa scroll horizontal', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 800 }); await page.goto('/');
  await expect(page.getByRole('button', { name: 'Buka navigasi' })).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth)).toBeLessThanOrEqual(375);
});
test('NAV_008 menu mobile', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 800 }); await page.goto('/');
  await page.getByRole('button', { name: 'Buka navigasi' }).click();
  for (const t of ['Fitur','Kategori','Paket','Masuk','Mulai Sekarang']) await expect(page.locator('header')).toContainText(t);
  await page.locator('header').getByRole('link', { name: 'Masuk' }).click(); await expect(page).toHaveURL(/\/login/);
});
const prot = ['dashboard','welcome','courses/logika','tests/logika-test-1','results/abc','history','settings','checkout?package=premium','payment-result','payment-history'];
prot.forEach((p, i) => test(`AKS_${String(i+1).padStart(3,'0')} /${p} tanpa login`, async ({ page }) => {
  await page.goto('/' + p); await expect(page).toHaveURL(/\/login/);
}));
test('AKS_011 admin tanpa login', async ({ page }) => {
  await page.goto('/admin/dashboard'); await expect(page).toHaveURL(/\/admin$/);
  await page.goto('/admin/questions'); await expect(page).toHaveURL(/\/admin$/);
});
test('AKS_012 halaman publik', async ({ page }) => {
  await page.goto('/packages'); await expect(page).toHaveURL(/\/packages/);
  await page.goto('/terms'); await expect(page).toHaveURL(/\/terms/);
  await page.goto('/privacy'); await expect(page).toHaveURL(/\/privacy/);
});
test('AKS_013 cookie sesi palsu', async ({ page, context }) => {
  await context.addCookies([{ name: 'psycourse_session', value: 'palsu123', url: 'http://localhost:3000' }]);
  await page.goto('/dashboard'); await expect(page).toHaveURL(/\/login/);
});
