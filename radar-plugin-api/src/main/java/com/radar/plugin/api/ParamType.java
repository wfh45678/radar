package com.radar.plugin.api;

/**
 * 插件参数类型。引擎按类型把管理端配置的字符串解析为 Java 类型后传入
 * {@link PluginRequest#args()}。
 *
 * @since 3.0.0
 */
public enum ParamType {
    STRING,
    INTEGER,
    DOUBLE,
    BOOLEAN;

    /**
     * 按本类型把原始字符串解析为 Java 对象；空串返回 null。
     *
     * @throws IllegalArgumentException 解析失败（引擎会按参数校验失败隔离该次执行）
     */
    public Object parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        switch (this) {
            case STRING:
                return raw;
            case INTEGER:
                return Integer.valueOf(raw.trim());
            case DOUBLE:
                return Double.valueOf(raw.trim());
            case BOOLEAN:
                return Boolean.valueOf(raw.trim());
            default:
                throw new IllegalArgumentException("unknown param type: " + this);
        }
    }
}
