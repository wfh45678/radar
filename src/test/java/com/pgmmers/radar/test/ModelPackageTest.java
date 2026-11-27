package com.pgmmers.radar.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackage;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackageService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 策略包 L1 单测：信封 round-trip、格式版本拒绝、校验报告（缺插件/Groovy/名单阈值）。
 * 纯逻辑验证，无 DB / 无 Spring 上下文。
 */
class ModelPackageTest {

    private ModelPackageService service;
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        service = new ModelPackageService();
        pluginManager = new PluginManager();
        ReflectionTestUtils.setField(service, "pluginManager", pluginManager);
        ReflectionTestUtils.setField(service, "recordWarnThreshold", 2L);
        ReflectionTestUtils.setField(service, "maxUploadSize", 10 * 1024 * 1024L);
    }

    /** 注册种子模型用到的内置插件（排除"缺插件"告警干扰）。 */
    private void registerBuiltinPlugins() {
        pluginManager.register(new PluginEntry("IP2LOCATION", "IP2LOCATION", "IP 归属地", "JSON", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(), (item, jsonInfo, sf) -> null));
    }

    private ModelPO sourceModel() {
        ModelPO model = new ModelPO();
        model.setId(99L);
        model.setGuid("GUID-99");
        model.setModelName("model_99");
        model.setLabel("电商开户");
        model.setCode("demo");
        model.setEntityName("Event");
        model.setEntryName("requestId");
        model.setStatus(1);
        model.setCreateTime(new Date());
        model.setUpdateTime(new Date());
        return model;
    }

    private ModelPackage fullPackage() {
        ModelPackage pkg = new ModelPackage();
        pkg.setFormatVersion(ModelPackageService.FORMAT_VERSION);
        pkg.setExportedAt("2026-11-21T00:00:00Z");
        pkg.setRadarVersion("3.2.0");
        pkg.setModel(sourceModel());

        FieldPO field = new FieldPO();
        field.setId(1L);
        field.setModelId(99L);
        field.setFieldName("userIP");
        pkg.setFields(List.of(field));

        PreItemPO preItem = new PreItemPO();
        preItem.setId(2L);
        preItem.setModelId(99L);
        preItem.setDestField("ipInfo");
        preItem.setPlugin("IP2LOCATION");
        pkg.setPreItems(List.of(preItem));

        StrategyPO strategy = new StrategyPO();
        strategy.setId(3L);
        strategy.setModelId(99L);
        strategy.setStrategyName("strategy_1");
        pkg.setStrategies(List.of(strategy));

        RulePO rule = new RulePO();
        rule.setId(4L);
        rule.setModelId(99L);
        rule.setStrategyId(3L);
        rule.setName("rule_ip");
        rule.setScripts("def x = 1\nreturn x");
        pkg.setRules(List.of(rule));
        return pkg;
    }

    @Test
    void roundTripPreservesGraph() {
        byte[] body = service.serialize(fullPackage());
        ModelPackage parsed = service.parse(body);

        assertEquals(ModelPackageService.FORMAT_VERSION, parsed.getFormatVersion());
        assertEquals("电商开户", parsed.getModel().getLabel());
        assertEquals("Event", parsed.getModel().getEntityName());
        assertEquals(1, parsed.getFields().size());
        assertEquals("userIP", parsed.getFields().get(0).getFieldName());
        assertEquals("IP2LOCATION", parsed.getPreItems().get(0).getPlugin());
        // 图内引用键保留（导入时才重映射）
        assertEquals(Long.valueOf(99L), parsed.getFields().get(0).getModelId());
        assertEquals(Long.valueOf(3L), parsed.getRules().get(0).getStrategyId());
        assertTrue(parsed.getRules().get(0).getScripts().contains("def x = 1"));
    }

    @Test
    void parseRejectsUnsupportedFormatVersion() {
        ModelPackage pkg = fullPackage();
        pkg.setFormatVersion("9.9");
        byte[] body = service.serialize(pkg);
        ModelPackageService.ModelPackageException e = assertThrows(
                ModelPackageService.ModelPackageException.class, () -> service.parse(body));
        assertTrue(e.getMessage().contains("格式版本不支持"));
    }

    @Test
    void parseRejectsGarbageAndMissingModel() {
        assertThrows(ModelPackageService.ModelPackageException.class,
                () -> service.parse("this is not json".getBytes()));
        byte[] body = service.serialize(fullPackage());
        ModelPackage pkg = service.parse(body);
        pkg.setModel(null);
        assertThrows(ModelPackageService.ModelPackageException.class,
                () -> service.parse(service.serialize(pkg)));
    }

    @Test
    void validateWarnsOnMissingPlugin() {
        // 注册表为空：IP2LOCATION 未安装
        List<String> warnings = service.validate(fullPackage());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("IP2LOCATION"));
        assertTrue(warnings.get(0).contains("ipInfo"));

        // 注册后告警消失
        pluginManager.register(new PluginEntry("IP2LOCATION", "IP2LOCATION", "IP 归属地", "JSON", false,
                PluginEntry.Source.BUILTIN_V3, List.of(), List.of(), (item, jsonInfo, sf) -> null));
        assertTrue(service.validate(fullPackage()).isEmpty());
    }

    @Test
    void validateWarnsOnBrokenGroovyOnly() {
        registerBuiltinPlugins();
        ModelPackage pkg = fullPackage();
        RulePO broken = new RulePO();
        broken.setId(5L);
        broken.setStrategyId(3L);
        broken.setName("rule_broken");
        broken.setScripts("def x = === definitely broken");
        pkg.setRules(List.of(pkg.getRules().get(0), broken));

        List<String> warnings = service.validate(pkg);
        assertEquals(1, warnings.size(), "合法脚本不应告警，坏脚本应告警");
        assertTrue(warnings.get(0).contains("rule_broken"));
    }

    @Test
    void validateWarnsOnLargeDataListRecords() {
        registerBuiltinPlugins();
        ModelPackage pkg = fullPackage();
        DataListRecordPO r1 = new DataListRecordPO();
        r1.setDataListId(10L);
        DataListRecordPO r2 = new DataListRecordPO();
        r2.setDataListId(10L);
        DataListRecordPO r3 = new DataListRecordPO();
        r3.setDataListId(10L);
        pkg.setDataListRecords(List.of(r1, r2, r3));

        List<String> warnings = service.validate(pkg);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("3 行"));
    }
}
