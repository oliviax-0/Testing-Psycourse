import { defineConfig } from '@playwright/test';
export default defineConfig({
  testDir: './tests', workers: 1, timeout: 60000, retries: 0,
  reporter: [['list'], ['json', { outputFile: 'results.json' }]],
  use: { baseURL: 'http://localhost:3000', channel: 'chrome', headless: true, viewport: { width: 1280, height: 900 } },
});
