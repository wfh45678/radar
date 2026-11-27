package com.pgmmers.radar.service.impl.engine.strategy;

import com.pgmmers.radar.config.EngineMetrics;
import com.pgmmers.radar.common.util.GroovyScriptUtil;
import com.pgmmers.radar.enums.Operator;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.enums.StrategyType;
import com.pgmmers.radar.model.vo.model.RuleVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.service.engine.strategy.StrategyExecutor;
import com.pgmmers.radar.service.engine.strategy.StrategyOutcome;
import com.pgmmers.radar.service.engine.vo.HitObject;
import com.pgmmers.radar.service.model.RuleService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 规则集策略执行器（v4.0）：原 v3.x 激活器执行逻辑原样抽取——
 * 遍历 ACTIVE 规则，Groovy 脚本判命中，按公式计分累加，阈值出决策。
 * 规则明细存 engine_rule，条件仍由前端 ruleDefinition 编译的 Groovy 执行。
 */
@Component
public class RuleSetExecutor implements StrategyExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RuleSetExecutor.class);

    @Autowired
    private RuleService ruleService;

    @Autowired
    private EngineMetrics engineMetrics;

    @Override
    public StrategyType type() {
        return StrategyType.RULE_SET;
    }

    @Override
    public StrategyOutcome execute(StrategyVO strategy, Map<String, Map<String, ?>> data, Map<String, ?> lists) {
        StrategyOutcome outcome = new StrategyOutcome();
        List<RuleVO> rules = ruleService.listRule(strategy.getId());
        BigDecimal sum = BigDecimal.ZERO;
        for (RuleVO rule : rules) {
            if (!rule.getStatus().equals(StatusType.ACTIVE.getKey())) {
                continue;
            }
            String scripts = rule.getScripts();
            boolean matched = checkRuleScript(scripts, data, lists);
            //规则匹配计算score
            if (matched) {
                String opt = rule.getOperator();
                Operator operator = Enum.valueOf(Operator.class, opt);
                BigDecimal initScore = new BigDecimal(rule.getInitScore());
                // 后续应该需要增加比例值
                BigDecimal rate = new BigDecimal(rule.getRate() * 0.01);
                BigDecimal base = new BigDecimal(rule.getBaseNum());
                BigDecimal extra = BigDecimal.ZERO;
                String by = rule.getAbstractionName();
                BigDecimal maxScore = new BigDecimal(rule.getMax());
                if (!StringUtils.isEmpty(by)) {
                    if (by.indexOf(".") != -1) {
                        //目前 被操作数 支持 基础字段和抽象字段。
                        String[] varNames = by.split("\\.");
                        Object val = null;
                        if (varNames[0].equals("fields")) {
                            val = data.get("fields").get(varNames[1]);
                        } else if (varNames[0].equals("abstractions")) {
                            val = data.get("abstractions").get(varNames[1]);
                        }
                        if (val instanceof Number) {
                            extra = new BigDecimal(val.toString());
                        }
                    } else {
                        //兼容以前的处理。
                        Object abs = data.get("abstractions").get(rule.getAbstractionName());
                        if (abs != null) {
                            extra = new BigDecimal(abs.toString());
                        }
                    }
                }
                extra = extra.multiply(rate);
                switch (operator) {
                case ADD:
                    extra = base.add(extra);
                    break;
                case SUB:
                    extra = base.subtract(extra);
                    break;
                case MUL:
                    extra = base.multiply(extra);
                    break;
                case DIV:
                    extra = base.divide(extra, 2, 4);
                    break;
                default:
                }
                BigDecimal amount = initScore.add(extra);
                // 规则得分设置最大值.
                if (maxScore.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxScore) > 0) {
                    amount = maxScore;
                }
                sum = sum.add(amount);

                // hit detail
                HitObject hit = new HitObject();
                hit.setKey(rule.getId().toString());
                hit.setDesc(rule.getLabel());
                hit.setValue(amount.setScale(2, 4).doubleValue());
                outcome.getHits().add(hit);
                outcome.getHits2().put("rule_" + hit.getKey(), hit);
                engineMetrics.recordRuleHit();
            }
        }
        sum = sum.setScale(0, 4);
        outcome.setScore(sum);
        outcome.setRisk(riskLevel(sum, strategy.getHigh(), strategy.getMedian()));
        return outcome;
    }

    /**
     * 风险级别决策：sum ≥ high → reject（拒绝），sum ≥ median → review（人工审核），否则 pass（通过）。
     * 阈值型策略（规则集/评分卡）通用；决策树由叶节点直出决策不走本方法。
     * bottom（底部阈值）为历史遗留字段，不参与决策。
     *
     * @param sum    命中得分总和
     * @param high   顶部阈值
     * @param median 中间阈值
     * @return pass / review / reject
     */
    public static String riskLevel(BigDecimal sum, Integer high, Integer median) {
        if (sum.compareTo(new BigDecimal(high)) >= 0) {
            return "reject";
        }
        if (sum.compareTo(new BigDecimal(median)) >= 0) {
            return "review";
        }
        return "pass";
    }

    /**
     * 检查数据是否符合规则条件（Groovy check 脚本，lists 为预加载名单）。
     */
    private Boolean checkRuleScript(String ruleScript, Map data, Map<String, ?> lists) {
        Object[] args = { data, lists };
        Boolean ret = false;
        try {
            ret = (Boolean) GroovyScriptUtil.invokeMethod(ruleScript, "check", args);
        } catch (Exception e) {
            logger.info("rule:{}", ruleScript);
            logger.error("", e);
        }
        return ret;
    }
}
