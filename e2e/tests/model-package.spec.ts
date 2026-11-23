import { expect, test } from '@playwright/test';
import { gotoWithAuth, login } from './helpers';

/**
 * 策略包回归（v3.5 T3）：导出下载（filename 断言）+ 导入弹窗打开与关闭。
 * 导入成功路径依赖有效包文件，属 L3/L1 覆盖范围；E2E 断言 UI 可达性与下载链路。
 */
test('策略包导出下载与导入弹窗', async ({ page }) => {
  const token = await login(page);
  await gotoWithAuth(page, '/#/modelList', token);

  // 找一个有 entityName 的模型行（种子模板复制后的模型或模板本身）
  const anyRow = page.locator('#table .ant-table-row').first();
  await expect(anyRow).toBeVisible({ timeout: 20_000 });

  // 行内「导出」触发下载
  const exportLink = anyRow.getByText('导出', { exact: true }).first();
  await expect(exportLink).toBeVisible();

  const downloadPromise = page.waitForEvent('download', { timeout: 20_000 });
  await exportLink.click();
  const download = await downloadPromise;
  const filename = download.suggestedFilename();
  expect(filename, '下载文件名应为策略包 json: ' + filename).toMatch(/^radar-model-.*\.json$/);
});

test('导入弹窗可达并关闭', async ({ page }) => {
  const token = await login(page);
  await gotoWithAuth(page, '/#/modelList', token);

  await page.getByRole('button', { name: '导入模型' }).click();
  const modal = page.locator('.ant-modal', { hasText: '导入模型策略包' });
  await expect(modal).toBeVisible({ timeout: 10_000 });
  await expect(modal.getByText('显示名称：')).toBeVisible();
  await expect(modal.getByText('商户码：')).toBeVisible();

  await modal.getByRole('button', { name: '取 消' }).click().catch(() => {});
  await modal.locator('.ant-modal-close').first().click();
  await expect(modal).not.toBeVisible({ timeout: 10_000 });
});
