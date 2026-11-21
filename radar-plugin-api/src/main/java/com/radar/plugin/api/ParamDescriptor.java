package com.radar.plugin.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private final List<ParamOption> options;

    private ParamDescriptor(Builder builder) {
        this.name = builder.name;
        this.label = builder.label;
        this.type = builder.type;
        this.required = builder.required;
        this.defaultValue = builder.defaultValue;
        this.placeholder = builder.placeholder;
        this.options = builder.options == null ? Collections.emptyList() : List.copyOf(builder.options);
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

    /**
     * 枚举选项；非空时管理端渲染为下拉框（label 展示、value 入库），
     * value 恒为字符串，类型解析逻辑不变。空列表表示自由输入。
     *
     * @since 3.1.0
     */
    public List<ParamOption> options() {
        return options;
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
        private List<ParamOption> options;

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

        /** 声明枚举选项（管理端渲染为下拉框）。@since 3.1.0 */
        public Builder options(List<ParamOption> options) {
            this.options = options == null || options.isEmpty() ? null : options;
            return this;
        }

        /** 声明枚举选项（便捷形式）。@since 3.1.0 */
        public Builder options(ParamOption... options) {
            return options(Arrays.asList(options));
        }

        public ParamDescriptor build() {
            return new ParamDescriptor(this);
        }
    }
}
