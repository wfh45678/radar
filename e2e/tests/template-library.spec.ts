import { expect, test } from '@playwright/test';
import { gotoWithAuth, login } from './helpers';

/**
 * 官方模板库回归（v3.5 T3/S4，v3.4 合入后生效）：弹窗打开 → 三官方模板卡片可见 →
 * 一键创建 → 新模型入列表。
 */
const TEMPLATE_NAMES = ['账户盗用防护', '营销反作弊', '交易风控'];

test('模板库浏览与一键创建', async ({ page }) => {
  const token = await login(page);
  await gotoWithAuth(page, '/#/scenarioList', token);

  // 打开模板库弹窗
  await page.getByRole('button', { name: '模板库' }).click();
  const modal = page.locator('.ant-modal', { hasText: '官方模板库' });
  await expect(modal).toBeVisible({ timeout: 20_000 });

  // 三官方模板卡片可见（在线源不可达时降级为仅内置，卡片仍在）
  for (const name of TEMPLATE_NAMES) {
    await expect(modal.getByText(name).first()).toBeVisible({ timeout: 10_000 });
  }

  // 一键创建：预填显示名 + 时间戳保证唯一
  const unique = Date.now();
  const label = 'TPL-E2E_交易风控_' + unique;
  const card = modal.locator('div').filter({ hasText: '交易风控' }).filter({ has: page.getByRole('button', { name: '一键创建' }) }).first();
  const labelInput = card.locator('input.ant-input');
  await labelInput.fill(label);
  await card.getByRole('button', { name: '一键创建' }).click();

  // 创建成功 → 告警/成功回显 → 关闭后列表 +1
  const resultModal = page.locator('.ant-modal', { hasText: '创建成功' });
  await expect(resultModal.or(page.getByText('模板创建成功'))).toBeVisible({ timeout: 20_000 });
  await resultModal.getByRole('button', { name: '知道了' }).click().catch(() => {});

  await gotoWithAuth(page, '/#/scenarioList', token);
  await page.reload();
  await expect(page.locator('#table')).toContainText(label, { timeout: 20_000 });
});
