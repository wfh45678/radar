import { expect, test } from '@playwright/test';

/**
 * Radar 五步冒烟（v3.3 开源基建 T3）：
 *   登录(UI) → 新建模型(UI) → 进入模型工作区(UI) → 事件发布(公开接口链) → 报表可见(UI)
 *
 * 前提：
 *   - 环境以 SYSCONF_CAPTCHAENABLED=false 启动（E2E 验证码免检，生产默认开）；
 *   - 种子数据（radar-init.sql，模板 103 + admin 账号）。
 * 口径说明：规则配置经模板继承（copy 模板 103 即完整规则链），
 * 事件发布走引擎公开接口 uploadInfo（与 Java SDK 同一通道），同为黑盒契约。
 */

const USER = process.env.E2E_USER || 'admin';
const PASS = process.env.E2E_PASS || '123456';
const CAPTCHA_PLACEHOLDER = process.env.E2E_CAPTCHA || '0000';
const UNIQUE = Date.now();
const CREATE_MODEL_LABEL = `e2e冒烟_${UNIQUE}`;
const EVENT_MODEL_LABEL = `IT冒烟_${UNIQUE}`;
const REQ_ID = `e2e_${UNIQUE}`;

let authToken = '';

/** 带 JWT 的 API 调用（断言 success，返回 data 已解析的整个 json）。 */
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

function demoEventJson() {
  return JSON.stringify({
    eventId: REQ_ID,
    mobile: '18516249909',
    userId: 'e2e',
    eventTime: Date.now(),
    userIP: '180.175.166.148',
    deviceId: 'E2E-DEVICE-001',
    os: 'ios',
    amount: 500.0,
    channel: 'alipay',
  });
}

test.describe.serial('Radar 五步冒烟', () => {
  test('step1 登录', async ({ page }) => {
    await page.goto('/#/login');
    await page.fill('input[name=username]', USER);
    await page.fill('input[name=password]', PASS);
    await page.fill('input[name=captcha]', CAPTCHA_PLACEHOLDER);
    await page.getByRole('button', { name: '登录' }).click();
    await page.waitForURL(/modelList/, { timeout: 20_000 });
    authToken = await page.evaluate(() => localStorage.getItem('x-auth-token') || '');
    expect(authToken, '登录应下发 x-auth-token').toBeTruthy();
  });

  test('step2 新建模型（模板复制）', async ({ page }) => {
    await page.addInitScript((t) => localStorage.setItem('x-auth-token', t), authToken);
    await page.goto('/#/modelList');
    await page.getByRole('button', { name: '新建模型' }).click();
    await page.fill('.ant-modal input[name=label]', CREATE_MODEL_LABEL);
    // 模板下拉：选第一个 [系统] 模板（antd5 选项渲染在 portal）
    await page.locator('.ant-modal .ant-select-selector').first().click();
    await page.locator('.ant-select-item-option').filter({ hasText: '[系统]' }).first().click();
    await page.locator('.ant-modal .ant-btn-primary').click();
    await expect(page.locator('#table')).toContainText(CREATE_MODEL_LABEL, { timeout: 15_000 });
  });

  test('step3 进入模型工作区', async ({ page }) => {
    await page.addInitScript((t) => localStorage.setItem('x-auth-token', t), authToken);
    await page.goto('/#/modelList');
    const row = page.locator('#table .ant-table-row').filter({ hasText: CREATE_MODEL_LABEL }).first();
    await expect(row).toBeVisible();
    await row.getByText('进入模型').click();
    await page.waitForURL(/field\//, { timeout: 20_000 });
    await expect(page.locator('.ant-table').first()).toBeVisible();
  });

  test('step4 事件发布（模板链路 + 引擎公开接口）', async ({ request }) => {
    // 模板 103 → copy → build → enable（频次类规则完整继承）
    const templates = await api(request, 'GET', '/services/v1/model/list/template');
    const templateList = templates.data.modelList as Array<{ id: number; label: string }>;
    const templateId = templateList.map((t) => Number(t.id)).find((id) => id === 103);
    expect(templateId, '种子模板 103 存在').toBeTruthy();

    await api(request, 'POST', '/services/v1/model/copy', {
      data: { id: templateId, modelName: `it_${UNIQUE}`, label: EVENT_MODEL_LABEL },
    });
    const list = await api(request, 'GET', '/services/v1/model/list');
    const rows = (list.data.modelList || list.data.list) as Array<Record<string, any>>;
    const copied = rows.find((r) => r.label === EVENT_MODEL_LABEL);
    expect(copied, 'copy 后可定位新模型').toBeTruthy();
    expect(copied!.guid, '新模型含 GUID').toBeTruthy();

    await api(request, 'POST', `/services/v1/model/build/${copied!.id}`);
    await api(request, 'POST', `/services/v1/model/enable/${copied!.id}`);

    // 事件上传（公开接口，表单参数；返回即含引擎打分结果）
    const upload = await api(request, 'POST', '/services/v1/uploadInfo', {
      form: { modelGuid: copied!.guid, reqId: REQ_ID, jsonInfo: demoEventJson() },
    });
    expect(upload.data, '引擎返回打分数据').toBeTruthy();
  });

  test('step5 报表可见', async ({ page }) => {
    await page.addInitScript((t) => localStorage.setItem('x-auth-token', t), authToken);
    await page.goto('/#/event');
    // Report 布局默认选第一个模型——显式切到事件所属模型再断言（选择会触发事件列表重载）
    await page.locator('.ant-select').first().click();
    await page.locator('.ant-select-item-option').filter({ hasText: EVENT_MODEL_LABEL }).first().click();
    await expect(page.locator('.ant-table').first()).toBeVisible({ timeout: 20_000 });
    await expect(
      page.locator('.ant-table-row').filter({ hasText: REQ_ID }).first()
    ).toBeVisible({ timeout: 20_000 });
  });
});
