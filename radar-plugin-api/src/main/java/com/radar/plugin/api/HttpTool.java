package com.radar.plugin.api;

import java.net.URI;

/**
 * 引擎提供的 HTTP 工具（远程型插件请一律通过它外呼）：
 * <ul>
 *   <li>强制连接/读取超时（引擎统一配置），外部服务抖动不会拖垮 preItems 阶段；</li>
 *   <li>默认拒绝私网/回环/链路本地地址目标（{@code plugin.http.deny-private-network=true}），
 *       缓解管理端配置 url 带来的 SSRF 面；</li>
 *   <li>内部连接池复用，替代逐请求创建客户端。</li>
 * </ul>
 *
 * @since 3.0.0
 */
public interface HttpTool {

    /** GET 请求。URL 失败（非法/被私网防护拒绝/网络异常）返回 status=0 的结果。 */
    HttpResult get(String url);

    /** POST JSON 请求。失败语义同 {@link #get(String)}。 */
    HttpResult postJson(String url, String jsonBody);

    /** 校验 url 目标是否被当前安全策略放行（私网防护）。供插件在动态拼 url 前自检。 */
    default boolean isAllowed(String url) {
        try {
            return URI.create(url) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
