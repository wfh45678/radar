package com.radar.plugin.api;

/**
 * 输出列描述。多列结果插件通过 {@link Plugin#outputMeta()} 声明列结构，
 * 管理端据此自动生成响应列配置（替代手填 configJson）。
 *
 * @since 3.0.0
 */
public final class OutputColumn {

    private final String name;
    private final String title;
    private final String type;

    private OutputColumn(String name, String title, String type) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("column name must not be empty");
        }
        this.name = name;
        this.title = title == null ? name : title;
        this.type = type == null ? "STRING" : type;
    }

    /** @return 列名（写入响应结构的键）。 */
    public String getName() {
        return name;
    }

    /** @return 列标题（管理端展示）。 */
    public String getTitle() {
        return title;
    }

    /** @return 列类型标记（STRING/DOUBLE 等）。 */
    public String getType() {
        return type;
    }

    public static OutputColumn of(String name, String title) {
        return new OutputColumn(name, title, "STRING");
    }

    public static OutputColumn of(String name, String title, String type) {
        return new OutputColumn(name, title, type);
    }
}
