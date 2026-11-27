package com.pgmmers.radar.service.engine.strategy;

import com.pgmmers.radar.service.engine.vo.HitObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单个策略的执行结果（v4.0 策略 SPI）。
 *
 * <p>由 {@link StrategyExecutor} 产出，编排层（AntiFraudEngineImpl）统一汇总为
 * 引擎响应的 activations / hitsDetail 结构（键名保持 v3.x 兼容）。</p>
 */
public class StrategyOutcome {

    /** 策略得分（决策树叶节点直出、规则集/评分卡为累加值） */
    private BigDecimal score = BigDecimal.ZERO;

    /** 决策结果：pass / review / reject（阈值型策略按阈值推导，决策树由叶节点直出） */
    private String risk = "pass";

    /** 命中明细（响应 hitsDetail：策略名 → List&lt;HitObject&gt;） */
    private List<HitObject> hits = new ArrayList<>();

    /** 命中明细 ES 检索形态（策略名 → {rule_xxx/node_xxx → HitObject}） */
    private Map<String, HitObject> hits2 = new HashMap<>();

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public List<HitObject> getHits() {
        return hits;
    }

    public void setHits(List<HitObject> hits) {
        this.hits = hits;
    }

    public Map<String, HitObject> getHits2() {
        return hits2;
    }

    public void setHits2(Map<String, HitObject> hits2) {
        this.hits2 = hits2;
    }
}
