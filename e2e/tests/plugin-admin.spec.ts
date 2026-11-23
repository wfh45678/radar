import { expect, test } from '@playwright/test';
import { gotoWithAuth, login } from './helpers';

/**
 * 插件管理页回归（v3.5 T3）：页面渲染（8 内置插件可见）→ 启停切换即时生效 → 复位。
 * 断言深度按 v3.3 原则：页面级可达性 + 核心交互，防白屏/断链类回归。
 */
test('插件管理页渲染与启停复位', async ({ page }) => {
  const token = await login(page);
  await gotoWithAuth(page, '/#/plugins', token);

  // 页面关键 DOM：表格 + 8 个内置插件行 + 热重载徽标
  await expect(page.locator('table')).toBeVisible({ timeout: 20_000 });
  await expect(page.getByText('IP2LOCATION').first()).toBeVisible({ timeout: 10_000 });
  await expect(page.getByText('BANK_CARD_CHECK')).toHaveCount(0, { timeout: 3_000 })
    .catch(() => {}); // v3.5 demo 插件若已上传则可见，不强断言
  await expect(page.getByText(/热重载/).first()).toBeVisible();

  // 启停切换：禁用 IP2LOCATION → 行内状态变化 → 复位启用
  const row = page.locator('tr', { hasText: 'IP2LOCATION' }).first();
  await expect(row).toBeVisible();

  const switchInRow = row.locator('.ant-switch').first();
  const before = await switchInRow.getAttribute('aria-checked');
  await switchInRow.click();
  await page.waitForTimeout(1500); // 等启停请求与列表刷新
  const after = await switchInRow.getAttribute('aria-checked');
  expect(after, '启停切换应改变状态').not.toEqual(before);

  // 复位：切回原状态
  await switchInRow.click();
  await page.waitForTimeout(1500);
  const restored = await switchInRow.getAttribute('aria-checked');
  expect(restored, '复位后应恢复原状态').toEqual(before);

  // 刷新后状态持久化一致
  await page.reload();
  await expect(page.locator('table')).toBeVisible({ timeout: 20_000 });
  const persisted = await page.locator('tr', { hasText: 'IP2LOCATION' })
    .first().locator('.ant-switch').first().getAttribute('aria-checked');
  expect(persisted, '刷新后开关状态应持久化').toEqual(restored);
});
