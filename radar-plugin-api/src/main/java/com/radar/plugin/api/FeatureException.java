package com.radar.plugin.api;

/**
 * 插件业务失败异常。引擎默认隔离该次失败：
 * 计入 {@code radar_plugin_executions_total{result=fail}} 指标、
 * destField 写入错误标记，事件链路继续。
 *
 * @since 3.0.0
 */
public class FeatureException extends Exception {

    private final String userMessage;

    public FeatureException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public FeatureException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    /** @return 用户可读的失败原因（会进引擎日志与错误标记）。 */
    public String getUserMessage() {
        return userMessage;
    }
}
