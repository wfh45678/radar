package com.radar.plugin.api;

/**
 * 插件参数描述。{@link Plugin#paramSchema()} 返回的每一项描述一个可配置参数，
 * 管理端据此动态渲染配置表单；引擎据此把原始配置字符串解析为类型化值。
 *
 * @since 3.0.0
 */
public final class ParamDescriptor {

    private final String name;
    private final String label;
    private final ParamType type;
    private final boolean required;
    private final String defaultValue;
    private final String placeholder;

    private ParamDescriptor(Builder builder) {
        this.name = builder.name;
        this.label = builder.label;
        this.type = builder.type;
        this.required = builder.required;
        this.defaultValue = builder.defaultValue;
        this.placeholder = builder.placeholder;
    }

    /** @return 参数名（配置存储键，英文）。 */
    public String getName() {
        return name;
    }

    /** @return 表单展示标签（中文）。 */
    public String getLabel() {
        return label;
    }

    /** @return 参数类型。 */
    public ParamType getType() {
        return type;
    }

    /** @return 是否必填。 */
    public boolean isRequired() {
        return required;
    }

    /** @return 默认值（原始字符串形式，可为 null）。 */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** @return 输入框占位提示，可为 null。 */
    public String getPlaceholder() {
        return placeholder;
    }

    public static Builder name(String name, String label) {
        return new Builder(name, label);
    }

    public static final class Builder {
        private final String name;
        private final String label;
        private ParamType type = ParamType.STRING;
        private boolean required = false;
        private String defaultValue;
        private String placeholder;

        private Builder(String name, String label) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("param name must not be empty");
            }
            this.name = name;
            this.label = label == null ? name : label;
        }

        public Builder type(ParamType type) {
            this.type = type == null ? ParamType.STRING : type;
            return this;
        }

        public Builder required() {
            this.required = true;
            return this;
        }

        public Builder defaultValue(String value) {
            this.defaultValue = value;
            return this;
        }

        public Builder placeholder(String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public ParamDescriptor build() {
            return new ParamDescriptor(this);
        }
    }
}
