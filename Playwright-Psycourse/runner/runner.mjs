// Runner Playwright untuk TC FrontEnd Psycourse.
// Membaca spec/tcs.json (hasil gen.py) dan menulis Reports/pw-results.json.
// Pakai:  node runner.mjs [TC_ID ...]   (tanpa argumen = semua TC)
import { chromium } from 'playwright-core';
import { execFileSync } from 'node:child_process';
import { readFileSync, writeFileSync, mkdirSync, rmSync } from 'node:fs';
import { homedir } from 'node:os';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = dirname(fileURLToPath(import.meta.url));
const BASE = process.env.PSY_BASE || 'http://localhost:3000';
const MAILPIT = 'http://localhost:8025';
const DOCKER = `${homedir()}/.docker/bin/docker`;
const DEMO = { email: 'siswa.demo@example.com', pass: 'demo-password-123' };
const TIMEOUT = 6000;

const tcs = JSON.parse(readFileSync(resolve(HERE, 'spec/tcs.json'), 'utf8'));
const only = process.argv.slice(2);
const selected = only.length ? tcs.filter((t) => only.includes(t.id) || only.includes(t.story)) : tcs;

const FIX = readFileSync(resolve(HERE, 'spec/fixtures.sql'), 'utf8').split('\n').filter((l) => !l.trim().startsWith('--')).join('\n');
const applyFixtures = () => { for (const part of FIX.split(/;\s*\n/).filter((x) => x.trim())) sql(part); };
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

function sql(stmt) {
  return execFileSync(DOCKER, ['exec', 'psycourse-db', 'psql', '-U', 'psycourse', '-d', 'psycourse', '-t', '-A', '-c', stmt], { encoding: 'utf8' }).trim();
}

async function latestOtp(email, timeout = 15000, afterCount = 0) {
  const end = Date.now() + timeout;
  while (Date.now() < end) {
    try {
      const list = await (await fetch(`${MAILPIT}/api/v1/search?query=${encodeURIComponent('to:' + email)}`)).json();
      if (list.messages && list.messages.length > afterCount) {
        const msg = await (await fetch(`${MAILPIT}/api/v1/message/${list.messages[0].ID}`)).json();
        const m = (msg.Text || msg.HTML || '').match(/(?<!\d)(\d{6})(?!\d)/);
        if (m) return m[1];
      }
    } catch {}
    await sleep(400);
  }
  return null;
}
async function mailCount(email) {
  try {
    const list = await (await fetch(`${MAILPIT}/api/v1/search?query=${encodeURIComponent('to:' + email)}`)).json();
    return list.messages ? list.messages.length : 0;
  } catch { return 0; }
}

const sub = (v, ctx) => (typeof v === 'string' ? v.replaceAll('{EMAIL}', ctx.email || '').replaceAll('{DEMO_EMAIL}', DEMO.email).replaceAll('{DEMO_PASS}', DEMO.pass) : v);

function clickLocator(page, s) {
  const scope = s.within ? page.locator(s.within) : page;
  const text = s.text;
  let loc = scope.locator('a, button, [role=button]').filter({ hasText: text });
  if (s.exact) {
    const norm = (t) => t.replace(/[→←✦]/g, '').replace(/\s+/g, ' ').trim();
    return { loc, exact: norm(text) };
  }
  return { loc };
}

async function runStep(page, context, s, ctx) {
  const T = { timeout: TIMEOUT };
  switch (s.op) {
    case 'open': await page.goto(BASE + sub(s.path, ctx), { waitUntil: 'domcontentloaded' }); await page.waitForLoadState('networkidle').catch(() => {}); return;
    case 'fill': await page.locator(`input[name='${s.name}']`).fill(sub(s.value, ctx), T); return;
    case 'type': await page.locator(`input[name='${s.name}']`).pressSequentially(sub(s.value, ctx), T); return;
    case 'check': await page.locator(`input[name='${s.name}']`).check(T); return;
    case 'press': await page.locator(`input[name='${s.name}']`).press(s.key, T); return;
    case 'click': {
      const { loc, exact } = clickLocator(page, s);
      let target = loc;
      if (exact !== undefined) {
        const n = await loc.count();
        let idx = -1;
        for (let i = 0; i < n; i++) {
          const t = ((await loc.nth(i).innerText()) || '').replace(/[→←✦]/g, '').replace(/\s+/g, ' ').trim();
          if (t === exact && (await loc.nth(i).isVisible())) { idx = i; break; }
        }
        if (idx < 0) throw new Error(`Tombol/link dengan teks persis '${s.text}' tidak ditemukan`);
        target = loc.nth(idx);
      } else target = loc.nth(s.nth || 0);
      await target.click(T);
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      return;
    }
    case 'clickCss': await (s.css.includes('form-submit') ? page.locator(s.css).last() : page.locator(s.css).first()).click(T); await page.waitForLoadState('domcontentloaded').catch(() => {}); return;
    case 'back': await page.goBack({ waitUntil: 'domcontentloaded' }); return;
    case 'reload': await page.reload({ waitUntil: 'domcontentloaded' }); return;
    case 'sleep': await sleep(s.sec * 1000); return;
    case 'viewport': await page.setViewportSize({ width: s.w, height: s.h }); return;
    case 'clearCookies': await context.clearCookies(); return;
    case 'setCookie': await context.addCookies([{ name: s.name, value: s.value, url: BASE }]); return;
    case 'sql': sql(s.stmt); return;
    case 'resetRate': sql('DELETE FROM "RateLimit"'); return;
    case 'clearMail': await fetch(`${MAILPIT}/api/v1/messages`, { method: 'DELETE' }); return;
    case 'newEmail': ctx.email = `olivia.qa+${Date.now()}${Math.floor(Math.random() * 1000)}@mail.com`; return;
    case 'fillOtp': {
      const code = await latestOtp(ctx.email, 15000, s.after ?? 0);
      if (!code) throw new Error(`OTP untuk ${ctx.email} tidak ditemukan di Mailpit`);
      await page.locator("input[name='otp']").fill(code, T); return;
    }
    case 'markMail': ctx.mailMark = await mailCount(ctx.email); return;
    case 'fillOtpNew': {
      const code = await latestOtp(ctx.email, 15000, ctx.mailMark ?? 0);
      if (!code) throw new Error('Kode OTP baru tidak ditemukan di Mailpit');
      await page.locator("input[name='otp']").fill(code, T); return;
    }
    case 'login': {
      await page.goto(BASE + '/login', { waitUntil: 'domcontentloaded' });
      await page.locator("input[name='email']").fill(sub(s.email ?? DEMO.email, ctx), T);
      await page.locator("input[name='password']").fill(sub(s.pass ?? DEMO.pass, ctx), T);
      if (s.remember) await page.locator("input[name='rememberMe']").check(T);
      await page.getByRole('button', { name: /Masuk/ }).click(T);
      await page.waitForURL('**/dashboard', { timeout: 10000 }).catch(() => {});
      return;
    }
    case 'registerVerify': {
      ctx.email = `olivia.qa+${Date.now()}${Math.floor(Math.random() * 1000)}@mail.com`;
      await page.goto(BASE + '/register', { waitUntil: 'domcontentloaded' });
      await page.locator("input[name='name']").fill(s.name || 'Olivia Tester');
      await page.locator("input[name='email']").fill(ctx.email);
      await page.locator("input[name='password']").fill('Rahasia123');
      await page.locator("input[name='confirmPassword']").fill('Rahasia123');
      await page.locator("input[name='privacyConsent']").check();
      await page.getByRole('button', { name: /Daftar dan lanjutkan/ }).click(T);
      await page.waitForURL('**/verify-otp**', { timeout: 10000 });
      const code = await latestOtp(ctx.email);
      if (!code) throw new Error('OTP tidak ditemukan');
      await page.locator("input[name='otp']").fill(code);
      await page.getByRole('button', { name: /Verifikasi kode/ }).click(T);
      await page.waitForURL('**/welcome', { timeout: 10000 });
      return;
    }
    case 'registerOnly': {
      ctx.email = `olivia.qa+${Date.now()}${Math.floor(Math.random() * 1000)}@mail.com`;
      await page.goto(BASE + '/register', { waitUntil: 'domcontentloaded' });
      await page.locator("input[name='name']").fill('Olivia Tester');
      await page.locator("input[name='email']").fill(ctx.email);
      await page.locator("input[name='password']").fill('Rahasia123');
      await page.locator("input[name='confirmPassword']").fill('Rahasia123');
      await page.locator("input[name='privacyConsent']").check();
      await page.getByRole('button', { name: /Daftar dan lanjutkan/ }).click(T);
      await page.waitForURL('**/verify-otp**', { timeout: 10000 });
      return;
    }
    // ---- verifikasi ----
    case 'see': {
      const text = sub(s.text, ctx);
      try { await page.waitForFunction((t) => document.body.innerText.replace(/\s+/g, ' ').includes(t.replace(/\s+/g, ' ')), text, { timeout: s.timeout || TIMEOUT }); }
      catch { const body = (await page.locator('body').innerText()).replace(/\s+/g, ' ').slice(0, 160); throw new Error(`Teks '${text}' tidak tampil. Halaman menampilkan: "${body}" (URL ${new URL(page.url()).pathname})`); }
      return;
    }
    case 'notsee': {
      const body = await page.locator('body').innerText();
      if (body.replace(/\s+/g, ' ').includes(s.text)) throw new Error(`Teks '${s.text}' ternyata tampil`);
      return;
    }
    case 'url': {
      const want = sub(s.path, ctx);
      try {
        await page.waitForFunction(([w, exact]) => { const p = location.pathname + location.search + location.hash; return exact ? p === w : p.startsWith(w); }, [want, !!s.exact], { timeout: s.timeout || TIMEOUT });
      } catch { const u = new URL(page.url()); throw new Error(`URL aktual ${u.pathname + u.search + u.hash}, diharapkan ${s.exact ? '' : 'diawali '}${want}`); }
      return;
    }
    case 'urlStays': {
      await sleep(600);
      const u = new URL(page.url());
      if (u.pathname !== s.path) throw new Error(`URL berpindah ke ${u.pathname + u.search}, diharapkan tetap ${s.path}`);
      return;
    }
    case 'title': {
      const t = await page.title();
      if (t !== s.text) throw new Error(`Judul tab aktual '${t}', diharapkan '${s.text}'`);
      return;
    }
    case 'valmsg': {
      const msg = await page.locator(`input[name='${s.name}']`).evaluate((el) => el.validationMessage);
      if (!msg) throw new Error(`Tidak ada pesan validasi browser pada field ${s.name}`);
      return;
    }
    case 'value': {
      const v = await page.locator(`input[name='${s.name}']`).inputValue();
      if (v !== sub(s.value, ctx)) throw new Error(`Nilai field ${s.name} aktual '${v}', diharapkan '${s.value}'`);
      return;
    }
    case 'checked': {
      const c = await page.locator(`input[name='${s.name}']`).isChecked();
      if (c !== (s.value !== false)) throw new Error(`Checkbox ${s.name} aktual ${c ? 'tercentang' : 'tidak tercentang'}`);
      return;
    }
    case 'disabled': {
      const { loc } = clickLocator(page, s);
      const d = await loc.first().isDisabled();
      if (d !== (s.value !== false)) throw new Error(`Tombol '${s.text}' aktual ${d ? 'nonaktif' : 'aktif'}`);
      return;
    }
    case 'visible': {
      const n = await page.locator(s.css).count();
      if ((s.count !== undefined && n !== s.count) || (s.count === undefined && n === 0)) throw new Error(`Elemen '${s.css}' ditemukan ${n}x${s.count !== undefined ? ', diharapkan ' + s.count : ''}`);
      return;
    }
    case 'noHScroll': {
      const sw = await page.evaluate(() => document.documentElement.scrollWidth);
      if (sw > s.w) throw new Error(`scrollWidth ${sw} lebih besar dari ${s.w} (ada scroll horizontal)`);
      return;
    }
    case 'cookie': {
      const cs = await context.cookies(BASE);
      const c = cs.find((x) => x.name === s.name);
      if (s.exists === false) { if (c) throw new Error(`Cookie ${s.name} masih ada`); return; }
      if (!c) throw new Error(`Cookie ${s.name} tidak ditemukan`);
      if (s.days) {
        const days = (c.expires - Date.now() / 1000) / 86400;
        if (Math.abs(days - s.days) > Math.max(1, s.days * 0.1)) throw new Error(`Masa berlaku cookie ${days.toFixed(1)} hari, diharapkan sekitar ${s.days} hari`);
      }
      return;
    }
    case 'valueLen': {
      const v = await page.locator(`input[name='${s.name}']`).inputValue();
      if (v.length !== s.len) throw new Error(`Panjang field ${s.name} aktual ${v.length}, diharapkan ${s.len}`);
      return;
    }
    case 'mailUnchanged': {
      await sleep(2000);
      const n = await mailCount(ctx.email);
      if (n !== (ctx.mailMark ?? 0)) throw new Error(`Ada ${n - (ctx.mailMark ?? 0)} email baru terkirim, diharapkan tidak ada`);
      return;
    }
    case 'mailCountIs': {
      await sleep(1500);
      const n = await mailCount(sub(s.email, ctx));
      if (n !== s.n) throw new Error(`Jumlah email untuk ${s.email}: ${n}, diharapkan ${s.n}`);
      return;
    }
    case 'toResetPage': {
      ctx.mailMark = await mailCount(ctx.email);
      await page.goto(BASE + '/forgot-password', { waitUntil: 'domcontentloaded' });
      await page.locator("input[name='email']").fill(ctx.email);
      await page.getByRole('button', { name: /Kirim kode verifikasi/ }).click(T);
      await page.waitForURL('**/verify-otp**', { timeout: 10000 });
      const code = await latestOtp(ctx.email, 15000, ctx.mailMark);
      if (!code) throw new Error('OTP pemulihan tidak ditemukan');
      await page.locator("input[name='otp']").fill(code);
      await page.getByRole('button', { name: /Verifikasi kode/ }).click(T);
      await page.waitForURL('**/reset-password', { timeout: 10000 });
      return;
    }
    case 'makePremium': {
      sql(`INSERT INTO "Entitlement"(id,"userId","packageId",mulai,berakhir) SELECT md5(random()::text),id,'premium',now(),now()+interval '30 days' FROM "User" WHERE email='${ctx.email}'`);
      return;
    }
    case 'timerBelow': {
      const t = (await page.locator(s.css).first().innerText()).trim();
      const m = t.match(/(\d+):(\d+)/);
      const sec = m ? Number(m[1]) * 60 + Number(m[2]) : NaN;
      if (!(sec <= s.seconds)) throw new Error(`Timer aktual '${t}' (${sec} dtk), diharapkan <= ${s.seconds} dtk`);
      return;
    }
    case 'seeAny': {
      await sleep(500);
      const body = (await page.locator('body').innerText()).replace(/\s+/g, ' ').toLowerCase();
      if (!s.texts.some((t) => body.includes(t.toLowerCase()))) throw new Error(`Tidak ada pemberitahuan percobaan resmi/sudah mengerjakan. Halaman menampilkan: "${body.slice(0, 140)}" (URL ${new URL(page.url()).pathname})`);
      return;
    }
    case 'saveUrl': ctx.saved = new URL(page.url()).pathname; return;
    case 'openSaved': await page.goto(BASE + ctx.saved, { waitUntil: 'domcontentloaded' }); await page.waitForLoadState('networkidle').catch(() => {}); return;
    case 'fillCss': await page.locator(s.css).first().fill(sub(s.value, ctx), T); return;
    case 'typeCss': await page.locator(s.css).first().pressSequentially(sub(s.value, ctx), T); return;
    case 'selectCss': await page.locator(s.css).first().selectOption(s.value, T); return;
    case 'clickLabel': await page.getByText(s.text).first().click(T); return;
    case 'valueCss': {
      const v = await page.locator(s.css).first().inputValue();
      if (v !== sub(s.value, ctx)) throw new Error(`Nilai '${s.css}' aktual '${v}', diharapkan '${sub(s.value, ctx)}'`);
      return;
    }
    case 'valueLenCss': {
      const v = await page.locator(s.css).first().inputValue();
      if (v.length !== s.len) throw new Error(`Panjang isi aktual ${v.length}, diharapkan ${s.len}`);
      return;
    }
    case 'valmsgCss': {
      const msg = await page.locator(s.css).first().evaluate((el) => el.validationMessage);
      if (!msg) throw new Error('Tidak ada pesan validasi browser pada field');
      return;
    }
    case 'attrCss': {
      const has = await page.locator(s.css).first().evaluate((el, a) => el.hasAttribute(a), s.attr);
      if (has !== (s.present !== false)) throw new Error(`Atribut ${s.attr} ${has ? 'ada' : 'tidak ada'} pada ${s.css}`);
      return;
    }
    case 'sqlIs': {
      const v = sql(s.stmt);
      if (v !== String(s.expect)) throw new Error(`Hasil query '${v}', diharapkan '${s.expect}'`);
      return;
    }
    default: throw new Error('op tidak dikenal: ' + s.op);
  }
}

applyFixtures();
const browser = await chromium.launch({ channel: 'chrome', headless: process.env.HEADED ? false : true });
const SHOTS = resolve(HERE, '../evidence/screenshots'); mkdirSync(SHOTS, { recursive: true });
const results = [];
for (const tc of selected) {
  const ctx = { email: '' };
  const context = await browser.newContext({ viewport: { width: 1280, height: 900 } });
  const page = await context.newPage();
  page.setDefaultTimeout(TIMEOUT);
  const fails = [];
  const dialogs = [];
  const shots = [];
  const shot = async (row) => {
    const f = `${tc.id}_baris${row}.png`;
    try { await page.screenshot({ path: resolve(SHOTS, f) }); shots.push({ row, file: f }); } catch {}
  };
  for (const [i, s] of tc.steps.filter((x) => !x.cleanup).entries()) {
    try { await runStep(page, context, s, ctx); }
    catch (e) { fails.push({ step: i + 1, row: s.r, exp: s.exp || s.op, actual: String(e.message).split('\n')[0].slice(0, 300) }); if (s.stop) break; }
    const next = tc.steps.filter((x) => !x.cleanup)[i + 1];
    if (!next || next.r !== s.r) await shot(s.r);
  }
  for (const s of tc.steps.filter((x) => x.cleanup)) { try { await runStep(page, context, s, ctx); } catch {} }
  await context.close();
  const status = fails.length ? 'Failed' : 'Passed';
  results.push({ id: tc.id, status, fails, shots });
  console.log(`${status === 'Passed' ? '✓' : '✗'} ${tc.id} ${status}${fails.length ? '\n    ' + fails.map((f) => `[langkah ${f.step}] ${f.actual}`).join('\n    ') : ''}`);
}
await browser.close();
mkdirSync(resolve(HERE, '../evidence'), { recursive: true });
const RES = resolve(HERE, '../evidence/pw-results.json');
let merged = results;
try {
  const old = JSON.parse(readFileSync(RES, 'utf8'));
  const fresh = new Map(results.map((r) => [r.id, r]));
  merged = old.map((r) => fresh.get(r.id) ?? r);
  for (const r of results) if (!old.some((o) => o.id === r.id)) merged.push(r);
} catch {}
writeFileSync(RES, JSON.stringify(merged, null, 1));
const p = results.filter((r) => r.status === 'Passed').length;
console.log(`\nTotal ${results.length}: ${p} Passed, ${results.length - p} Failed`);
