import { defineConfig } from '@playwright/test';

/**
 * E2E 冒烟配置：默认打本地 compose 环境（localhost:6580），
 * CI 经 E2E_BASE_URL 覆盖；失败截图 + trace 便于排查白屏类回归。
 */
export default defineConfig({
  testDir: './tests',
  timeout: 120_000,
  expect: { timeout: 15_000 },
  retries: 1,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: process.env.E2E_BASE_URL || 'http://localhost:6580',
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    actionTimeout: 15_000,
    navigationTimeout: 20_000,
    ignoreHTTPSErrors: true,
  },
  projects: [{ name: 'chromium', use: { browserName: 'chromium' } }],
});
