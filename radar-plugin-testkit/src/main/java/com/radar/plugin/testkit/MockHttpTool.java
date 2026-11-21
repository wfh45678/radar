package com.radar.plugin.testkit;

import com.radar.plugin.api.HttpResult;
import com.radar.plugin.api.HttpTool;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 测试用 {@link HttpTool} 桩：按 URL 前缀注册响应，录制全部请求供断言。
 *
 * <pre>{@code
 * MockHttpTool http = new MockHttpTool()
 *         .stub("https://api.example.com/rate", 200, "{\"USD\":7.24}");
 * PluginContext ctx = TestContexts.context(http);
 * // ... 执行插件 ...
 * assertEquals(1, http.calls().size());
 * }</pre>
 *
 * <p>也可注册动态响应函数（按 URL 计算 body）。</p>
 *
 * @since 3.1.0
 */
public final class MockHttpTool implements HttpTool {

    /** 一次 HTTP 调用的录制。 */
    public record Call(String method, String url, String body) {
    }

    private final Map<String, Function<String, HttpResult>> stubs = new LinkedHashMap<>();
    private final List<Call> calls = new ArrayList<>();

    /** 注册固定响应（按 URL 前缀匹配，先注册者优先）。 */
    public MockHttpTool stub(String urlPrefix, int status, String body) {
        return stub(urlPrefix, url -> new HttpResult(status, body));
    }

    /** 注册动态响应（按 URL 前缀匹配，函数入参为完整 URL）。 */
    public MockHttpTool stub(String urlPrefix, Function<String, HttpResult> responder) {
        stubs.put(urlPrefix, responder);
        return this;
    }

    /** @return 全部调用录制（方法/URL/body）。 */
    public List<Call> calls() {
        return List.copyOf(calls);
    }

    /** 清空调用录制（保留桩）。 */
    public void reset() {
        calls.clear();
    }

    private HttpResult record(String method, String url, String body) {
        calls.add(new Call(method, url, body));
        for (Map.Entry<String, Function<String, HttpResult>> stub : stubs.entrySet()) {
            if (url.startsWith(stub.getKey())) {
                return stub.getValue().apply(url);
            }
        }
        return new HttpResult(0, "no stub for url: " + url);
    }

    @Override
    public HttpResult get(String url) {
        return record("GET", url, null);
    }

    @Override
    public HttpResult postJson(String url, String jsonBody) {
        return record("POST", url, jsonBody);
    }

    @Override
    public boolean isAllowed(String url) {
        return true;
    }
}
