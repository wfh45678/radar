import { expect, type Page } from '@playwright/test';

/**
 * 公共登录 helper（v3.5 T3）：验证码免检路径（SYSCONF_CAPTCHAENABLED=false），
 * 登录成功后返回 x-auth-token 供 API 调用与 addInitScript 注入。
 */
export const E2E_USER = process.env.E2E_USER || 'admin';
export const E2E_PASS = process.env.E2E_PASS || '123456';
export const E2E_CAPTCHA = process.env.E2E_CAPTCHA || '0000';

export async function login(page: Page): Promise<string> {
  await page.goto('/#/login');
  await page.fill('input[name=username]', E2E_USER);
  await page.fill('input[name=password]', E2E_PASS);
  await page.fill('input[name=captcha]', E2E_CAPTCHA);
  await page.getByRole('button', { name: '登录' }).click();
  await page.waitForURL(/modelList/, { timeout: 20_000 });
  const token = await page.evaluate(() => localStorage.getItem('x-auth-token') || '');
  expect(token, '登录应下发 x-auth-token').toBeTruthy();
  return token;
}

/** 注入登录态后跳转到指定 hash 路由。 */
export async function gotoWithAuth(page: Page, hashPath: string, token: string): Promise<void> {
  await page.addInitScript((t) => localStorage.setItem('x-auth-token', t), token);
  await page.goto(hashPath);
}
