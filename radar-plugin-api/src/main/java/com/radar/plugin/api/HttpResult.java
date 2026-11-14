package com.radar.plugin.api;

/**
 * HTTP 调用结果。非 2xx 不抛异常，由插件按 status 自行判断业务语义。
 *
 * @since 3.0.0
 */
public final class HttpResult {

    private final int status;
    private final String body;

    public HttpResult(int status, String body) {
        this.status = status;
        this.body = body == null ? "" : body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    /** @return 2xx 时 true。 */
    public boolean isOk() {
        return status >= 200 && status < 300;
    }
}
