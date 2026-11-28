// 特征类型（v4.1）——与后端 enums/FeatureType 对齐
export const FEATURE_TYPES = [
    { value: 'ENRICH', label: '插件加工', color: 'blue' },
    { value: 'DERIVED', label: '衍生表达式', color: 'gold' },
    { value: 'AGGREGATE', label: '统计聚合', color: 'purple' },
    { value: 'RATIO', label: '组合比率', color: 'green' }
];

export function featureTypeOf(type) {
    return FEATURE_TYPES.find(t => t.value === type) || { value: type, label: type, color: 'default' };
}

/** 特征所属阶段（用于展示） */
export const FEATURE_STAGES = {
    ENRICH: '入库前', DERIVED: '入库前', AGGREGATE: '分析期', RATIO: '分析期'
};

// 衍生表达式可用函数（与后端 StandardDerivedFunctions 对齐）
export const DERIVED_FUNCTIONS = [
    { value: 'ADD', label: '加', arity: [2, 2] },
    { value: 'SUB', label: '减', arity: [2, 2] },
    { value: 'MUL', label: '乘', arity: [2, 2] },
    { value: 'DIV', label: '除（除零无产出）', arity: [2, 2] },
    { value: 'ABS', label: '绝对值', arity: [1, 1] },
    { value: 'ROUND', label: '四舍五入', arity: [1, 2] },
    { value: 'CONCAT', label: '拼接', arity: [1, 9] },
    { value: 'SUBSTRING', label: '子串', arity: [2, 3] },
    { value: 'UPPER', label: '转大写', arity: [1, 1] },
    { value: 'LOWER', label: '转小写', arity: [1, 1] },
    { value: 'REGEX_EXTRACT', label: '正则提取', arity: [2, 2] },
    { value: 'MAPPING', label: '映射表', arity: [2, 9] },
    { value: 'COALESCE', label: '空值兜底', arity: [1, 9] }
];
