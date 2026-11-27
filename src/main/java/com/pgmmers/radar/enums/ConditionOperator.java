package com.pgmmers.radar.enums;

/**
 * 条件操作符（v4.0 后端条件求值器）。
 *
 * <p>名称与语义对齐前端规则编辑器操作符注册表（frontend/src/component/utils/operatorUtil.jsx），
 * 保证同一份条件 JSON 在前端（Groovy 编译）与后端（{@code ConditionEvaluator} 解释）两侧求值一致。
 * Field_ 前缀表示左右操作数均为取值表达式（字段对字段比较），求值语义与无常缀版本相同。</p>
 */
public enum ConditionOperator {

    StartsWith("以...开始"),
    Contains("包含"),
    Equal("等于"),
    Less("小于"),
    Less_Equal("小于等于"),
    Greater("大于"),
    Greater_Equal("大于等于"),
    InList("在列表...中"),
    NotStartsWith("不以...开始"),
    NotContains("不包含"),
    NotEqual("不等于"),
    NotInList("不在列表...中"),
    IsNull("为空"),
    IsNotNull("不为空"),
    Field_Greater("大于（字段）"),
    Field_Less("小于（字段）"),
    Field_Greater_Equal("大于等于（字段）"),
    Field_Less_Equal("小于等于（字段）"),
    Field_Equal("等于（字段）"),
    Field_Not_Equal("不等于（字段）");

    private final String label;

    ConditionOperator(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 解析存储值；未知操作符返回 null（调用方按条件不成立处理并告警）。
     */
    public static ConditionOperator of(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
