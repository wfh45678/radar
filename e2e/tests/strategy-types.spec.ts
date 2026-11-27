import { expect, test } from '@playwright/test';
import { login, gotoWithAuth, E2E_USER } from './helpers';

/**
 * v4.0 策略类型 e2e：导入 transaction-risk 官方模板（规则集+评分卡+决策树三种类型）
 * → 模型工作区策略列表类型徽标（UI）→ 评分卡编辑器渲染与保存（UI）→ 决策树编辑器渲染（UI）。
 *
 * 前提与 smoke 一致：SYSCONF_CAPTCHAENABLED=false，种子数据内置模板库。
 * 策略执行语义的精确断言由 L3 EngineFullChainTest.step4 覆盖（Testcontainers）。
 */

const UNIQUE = Date.now();
const MODEL_NAME = `e2e_st_${UNIQUE}`;
let authToken = '';

async function api(request: any, method: string, url: string, opts: any = {}) {
  const res = await request.fetch(url, {
    method,
    headers: { 'x-auth-token': authToken },
    ...opts,
  });
  expect(res.ok(), `${method} ${url} HTTP ${res.status()}`).toBeTruthy();
  const json = await res.json();
  expect(json.success, `${method} ${url} 应成功: ${JSON.stringify(json).slice(0, 300)}`).toBeTruthy();
  return json;
}

test.describe.serial('v4.0 策略类型', () => {
  let modelId = 0;

  test('step0 登录并导入策略类型模板', async ({ page, request }) => {
    authToken = await login(page);
    const imported = await api(request, 'POST',
      `/services/v1/model/templates/transaction-risk/import?modelName=${MODEL_NAME}`);
    modelId = imported.data.id;
    expect(modelId, '导入应返回新模型 id').toBeTruthy();
    await api(request, 'POST', `/services/v1/model/build/${modelId}`);
    await api(request, 'POST', `/services/v1/model/enable/${modelId}`);
  });

  test('step1 策略列表展示三种类型徽标', async ({ page }) => {
    await gotoWithAuth(page, `#/strategy/${modelId}`, authToken);
    await expect(page.getByText('交易风险评分卡')).toBeVisible({ timeout: 20_000 });
    await expect(page.getByText('交易准入决策树')).toBeVisible();
    await expect(page.getByText('评分卡', { exact: true })).toBeVisible();
    await expect(page.getByText('决策树', { exact: true })).toBeVisible();
    await expect(page.getByText('规则集', { exact: true })).toBeVisible();
  });

  test('step2 评分卡编辑器渲染、编辑分箱并保存', async ({ page }) => {
    await gotoWithAuth(page, `#/strategy/${modelId}`, authToken);
    // 进入评分卡配置
    await page.getByText('交易风险评分卡').waitFor({ state: 'visible', timeout: 20_000 });
    const row = page.locator('tr', { hasText: '交易风险评分卡' });
    await row.getByRole('link', { name: '配置' }).click();
    // 详情分发到评分卡编辑器
    await expect(page.getByText('初始分')).toBeVisible({ timeout: 20_000 });
    await expect(page.getByText('IP交易频次')).toBeVisible();
    await expect(page.getByText('添加特征')).toBeVisible();
    // 编辑 IP交易频次 的 >=30 分箱分值：60 → 80，保存
    const featureCard = page.locator('.ant-card', { hasText: 'IP交易频次' });
    const lastBinScore = featureCard.locator('td', { hasText: '>=30' }).locator('..').locator('input[class*="ant-input-number-input"]').last();
    await lastBinScore.fill('80');
    await page.getByRole('button', { name: /保存/ }).click();
    await expect(page.getByText('评分卡已保存，保存即生效')).toBeVisible({ timeout: 15_000 });
  });

  test('step3 决策树编辑器渲染并可编辑叶节点', async ({ page }) => {
    await gotoWithAuth(page, `#/strategy/${modelId}`, authToken);
    await page.getByText('交易准入决策树').waitFor({ state: 'visible', timeout: 20_000 });
    const row = page.locator('tr', { hasText: '交易准入决策树' });
    await row.getByRole('link', { name: '配置' }).click();
    // 详情分发到决策树编辑器
    await expect(page.getByText('树结构')).toBeVisible({ timeout: 20_000 });
    await expect(page.getByText('节点编辑')).toBeVisible();
    // 树中应有根节点徽标与叶节点（白名单放行）
    await expect(page.getByText('根', { exact: true })).toBeVisible();
    await page.getByText('白名单放行').first().click();
    // 叶节点编辑面板出现决策下拉
    await expect(page.getByText('决策：')).toBeVisible();
    await expect(page.getByText('理由')).toBeVisible();
  });
});
