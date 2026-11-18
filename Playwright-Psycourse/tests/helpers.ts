import { Page, expect } from '@playwright/test';
import { execSync } from 'child_process';
export const DEMO = { email: 'siswa.demo@example.com', pass: 'demo-password-123' };
const DOCKER = process.env.HOME + '/.docker/bin/docker';
export function sql(q: string) {
  return execSync(`${DOCKER} exec psycourse-db psql -U psycourse -d psycourse -t -A -c '${q}'`).toString().trim();
}
export const resetRate = () => sql('DELETE FROM "RateLimit"');
export const uniq = (t = 'qa') => `olivia.${t}+${Date.now()}${Math.floor(Math.random()*1000)}@mail.com`;
export async function otp(email: string): Promise<string> {
  for (let i = 0; i < 30; i++) {
    const r = await (await fetch(`http://localhost:8025/api/v1/search?query=${encodeURIComponent('to:' + email)}`)).json() as any;
    if (r.messages?.length) {
      const m = await (await fetch(`http://localhost:8025/api/v1/message/${r.messages[0].ID}`)).json() as any;
      const c = ((m.Text || m.HTML || '') as string).match(/(?<!\d)(\d{6})(?!\d)/);
      if (c) return c[1];
    }
    await new Promise(r => setTimeout(r, 500));
  }
  throw new Error('OTP tidak ditemukan');
}
export async function fillReg(p: Page, o: Partial<{name:string;email:string;pass:string;conf:string;consent:boolean}> = {}) {
  await p.goto('/register');
  await p.fill('input[name=name]', o.name ?? 'Olivia Tester');
  await p.fill('input[name=email]', o.email ?? uniq());
  await p.fill('input[name=password]', o.pass ?? 'Rahasia123');
  await p.fill('input[name=confirmPassword]', o.conf ?? o.pass ?? 'Rahasia123');
  if (o.consent !== false) await p.check('input[name=privacyConsent]');
}
export const submitReg = (p: Page) => p.getByRole('button', { name: /Daftar dan lanjutkan/ }).click();
export async function registerVerify(p: Page) {
  const email = uniq('v');
  await fillReg(p, { email }); await submitReg(p);
  await p.waitForURL(/verify-otp/);
  await p.fill('input[name=otp]', await otp(email));
  await p.getByRole('button', { name: 'Verifikasi kode' }).click();
  await p.waitForURL(/welcome/);
  return email;
}
export async function login(p: Page, email = DEMO.email, pass = DEMO.pass) {
  await p.goto('/login');
  await p.fill('input[name=email]', email); await p.fill('input[name=password]', pass);
  await p.getByRole('button', { name: /^Masuk/ }).click();
}
export const text = (p: Page, t: string | RegExp) => expect(p.locator('body')).toContainText(t);
