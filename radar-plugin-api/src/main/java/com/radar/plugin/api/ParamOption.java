package com.radar.plugin.api;

/**
 * 插件参数的枚举选项（label/value 对）。
 *
 * <p>{@link ParamDescriptor#options()} 非空时，管理端把该参数渲染为下拉框
 * （label 展示、value 入库），value 恒为字符串，引擎解析逻辑不变。</p>
 *
 * @since 3.1.0
 */
public final class ParamOption {

    private final String label;
    private final String value;

    private ParamOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /** @return 下拉展示文案。 */
    public String getLabel() {
        return label;
    }

    /** @return 入库值（字符串）。 */
    public String getValue() {
        return value;
    }

    /** 构建选项：label 与 value 相同的便捷形式。 */
    public static ParamOption of(String value) {
        return new ParamOption(value, value);
    }

    /** 构建选项。 */
    public static ParamOption of(String label, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("option value must not be empty");
        }
        return new ParamOption(label == null || label.isEmpty() ? value : label, value);
    }

    @Override
    public String toString() {
        return label + "=" + value;
    }
}
