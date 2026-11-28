package com.pgmmers.radar.config;

import com.pgmmers.radar.common.util.GroovyScriptUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 引擎业务指标（Micrometer/Prometheus）。
 *
 * <p>标签纪律：不引入 scenarioGuid 等高基数标签（防指标爆炸），模型维度分析由 hitsSort 报表承担。
 * 埋点位置：事件入口 {@code RiskAnalysisEngineServiceImpl.uploadInfo}、
 * 各阶段 {@code AntiFraudServiceImpl.process}（features/adaptations/strategies）/ entity 落库、
 * 决策分布 {@code AntiFraudEngineImpl.executeStrategy}、Groovy 编译 {@code GroovyScriptUtil}。</p>
 *
 * <p>暴露端点：/actuator/prometheus（见 application.yml management 配置）。</p>
 *
 * @author feihu.wang
 */
@Component
public class EngineMetrics {

    private static final String EVENTS = "radar_events_total";
    private static final String STAGE = "radar_engine_stage_seconds";
    private static final String DECISION = "radar_strategy_decision_total";
    private static final String STRATEGY_EXEC = "radar_strategy_executions_total";
    private static final String FEATURE_EXEC = "radar_feature_executions_total";
    private static final String RULE_HITS = "radar_rule_hits_total";
    private static final String GROOVY_COMPILES = "radar_groovy_compiles_total";
    private static final String PLUGIN_EXEC = "radar_plugin_executions_total";
    private static final String PLUGIN_SECONDS = "radar_plugin_seconds";

    private final MeterRegistry registry;

    private Counter eventsSuccess;
    private Counter eventsFail;
    private Counter ruleHits;

    public EngineMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        eventsSuccess = Counter.builder(EVENTS).tag("result", "success")
                .description("事件处理成功数").register(registry);
        eventsFail = Counter.builder(EVENTS).tag("result", "fail")
                .description("事件处理失败数").register(registry);
        ruleHits = Counter.builder(RULE_HITS)
                .description("规则命中累计").register(registry);
        // GroovyScriptUtil 为静态工具类，通过 holder 注入 registry（缓存未命中即一次编译）
        GroovyScriptUtil.setMetricsRegistry(registry);
    }

    /** 事件处理结果计数（uploadInfo 单一漏斗处调用） */
    public void recordEvent(boolean success) {
        (success ? eventsSuccess : eventsFail).increment();
    }

    /** 记录引擎阶段耗时（毫秒）。stage: entity / features / adaptations / strategies */
    public void recordStage(String stage, long elapsedMillis) {
        Timer.builder(STAGE).tag("stage", stage)
                .description("引擎各阶段耗时")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(elapsedMillis, TimeUnit.MILLISECONDS);
    }

    /** 策略决策分布（pass/review/reject） */
    public void recordDecision(String risk) {
        Counter.builder(DECISION).tag("risk", risk)
                .description("策略决策分布").register(registry)
                .increment();
    }

    /**
     * 特征执行计数（v4.1 特征 SPI）。
     *
     * <p>type: ENRICH/DERIVED/AGGREGATE/RATIO；result: success / error / not_found。</p>
     */
    public void recordFeatureExecution(String type, String result) {
        Counter.builder(FEATURE_EXEC).tag("type", type).tag("result", result)
                .description("特征执行计数").register(registry)
                .increment();
    }

    /**
     * 策略执行计数（v4.0 策略 SPI）。
     *
     * <p>type: RULE_SET/SCORECARD/DECISION_TREE/unknown（解析失败兜底）；
     * result: success / no_executor / error（单策略异常隔离时记录）。</p>
     */
    public void recordStrategyExecution(String type, String result) {
        Counter.builder(STRATEGY_EXEC).tag("type", type).tag("result", result)
                .description("策略执行计数").register(registry)
                .increment();
    }

    /** 规则命中计数 */
    public void recordRuleHit() {
        ruleHits.increment();
    }

    /** Groovy 编译计数 + 耗时（由 GroovyScriptUtil 在缓存未命中时调用） */
    public static void recordGroovyCompile(MeterRegistry registry, long elapsedNanos) {
        Counter.builder(GROOVY_COMPILES).description("Groovy 脚本编译次数").register(registry).increment();
        Timer.builder("radar_groovy_compile_seconds").description("Groovy 脚本编译耗时")
                .publishPercentiles(0.95, 0.99)
                .register(registry)
                .record(elapsedNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * ENRICH 特征插件执行计数 + 耗时（v3.0 插件加固，v4.1 归入特征管线）。
     *
     * <p>plugin 标签取注册表插件 key；插件缺失时统一记为 {@code unknown}（防标签基数爆炸）。
     * result: success / fail / not_found。</p>
     */
    public void recordPluginExecution(String plugin, String result, long elapsedMillis) {
        String boundedPlugin = plugin == null || plugin.isEmpty() ? "unknown" : plugin;
        Counter.builder(PLUGIN_EXEC).tag("plugin", boundedPlugin).tag("result", result)
                .description("特征插件执行计数").register(registry).increment();
        Timer.builder(PLUGIN_SECONDS).tag("plugin", boundedPlugin)
                .description("特征插件执行耗时")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry)
                .record(elapsedMillis, TimeUnit.MILLISECONDS);
    }
}
