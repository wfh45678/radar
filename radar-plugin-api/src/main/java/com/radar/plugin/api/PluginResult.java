package com.radar.plugin.api;

/**
 * 插件执行结果载荷。
 *
 * <p>约定：</p>
 * <ul>
 *   <li>单值结果：{@link #of(Object)} 传任意标量（写回 destField 单字段）；</li>
 *   <li>多列结果：{@link #of(Object)} 传 {@code Map<String, Object>}（键与
 *       {@link #outputMeta()} 列名对应，abstraction/rules 以
 *       {@code preItems.destField.列名} 消费）。</li>
 * </ul>
 *
 * @since 3.0.0
 
 *
 * @deprecated v3.x 插件契约（v4.1 起由新契约取代，v5 移除）。存量外置 jar 仍可加载。*/
@Deprecated
public final class PluginResult {

    private final Object payload;

    private PluginResult(Object payload) {
        this.payload = payload;
    }

    public static PluginResult of(Object payload) {
        return new PluginResult(payload);
    }

    /** @return 结果载荷。 */
    public Object getPayload() {
        return payload;
    }
}
