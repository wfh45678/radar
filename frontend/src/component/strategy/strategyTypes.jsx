// 策略类型（v4.0）——与后端 enums/StrategyType 对齐
export const STRATEGY_TYPES = [
    { value: 'RULE_SET', label: '规则集', color: 'blue' },
    { value: 'SCORECARD', label: '评分卡', color: 'gold' },
    { value: 'DECISION_TREE', label: '决策树', color: 'purple' }
];

export function strategyTypeOf(type) {
    return STRATEGY_TYPES.find(t => t.value === type) || STRATEGY_TYPES[0];
}
