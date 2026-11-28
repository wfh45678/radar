package com.radar.plugin.api;

/**
 * 插件业务失败异常。引擎默认隔离该次失败：
 * 计入 {@code radar_plugin_executions_total{result=fail}} 指标、
 * destField 写入错误标记，事件链路继续。
 *
 * @since 3.0.0
 
 *
 * @deprecated v3.x 插件契约（v4.1 起由新契约取代，v5 移除）。存量外置 jar 仍可加载。*/
@Deprecated
public class PluginException extends Exception {

    private final String userMessage;

    public PluginException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public PluginException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    /** @return 用户可读的失败原因（会进引擎日志与错误标记）。 */
    public String getUserMessage() {
        return userMessage;
    }
}
