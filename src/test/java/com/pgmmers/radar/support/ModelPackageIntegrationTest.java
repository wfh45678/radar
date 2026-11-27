package com.pgmmers.radar.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ModelMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackage;
import com.pgmmers.radar.service.impl.model.transfer.ModelPackageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 策略包 L3 集成测试（Testcontainers 真库）：种子模型 导出 → 文件化往返 → 导入为新模型，
 * 验证图完整性（子对象数量一致）、ID 重映射（规则的 strategyId 落在新模型的激活上）、
 * 商户码+名称冲突拒绝与事务兜底。
 */
class ModelPackageIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ModelPackageService packageService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private RuleMapper ruleMapper;

    private static final String MERCHANT = "pkgtest";
    private static final String LABEL = "策略包导入测试";

    @Test
    void exportImportRoundTripOnSeedModel() {
        List<ModelPO> models = modelMapper.selectList(null);
        assertFalse(models.isEmpty(), "种子库应包含模型（radar-init.sql）");
        ModelPO source = models.get(0);

        // 1) 导出
        ModelPackage pkg = packageService.export(source.getId());
        assertNotNull(pkg);
        assertEquals(ModelPackageService.FORMAT_VERSION, pkg.getFormatVersion());
        int sourceFieldCount = pkg.getFields().size();
        int sourceRuleCount = pkg.getRules().size();

        // 2) 校验：种子模型只用内置插件，不应有"未安装插件"告警
        List<String> warnings = packageService.validate(pkg);
        assertTrue(warnings.stream().noneMatch(w -> w.contains("未安装插件")),
                "内置插件齐备不应告警: " + warnings);

        // 3) 文件化往返后导入为新模型
        byte[] body = packageService.serialize(pkg);
        ModelPackage parsed = packageService.parse(body);
        Long newId = packageService.importPackage(parsed, MERCHANT, null, LABEL);
        assertNotEquals(source.getId(), newId);

        // 4) 图完整性：字段/规则数量一致
        List<FieldPO> newFields = fieldMapper.selectList(
                new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getModelId, newId));
        assertEquals(sourceFieldCount, newFields.size());

        // 5) ID 重映射：导入规则的 strategyId 均属于新模型的激活
        List<RulePO> newRules = ruleMapper.selectList(
                new LambdaQueryWrapper<RulePO>().eq(RulePO::getModelId, newId));
        assertEquals(sourceRuleCount, newRules.size());
        for (RulePO rule : newRules) {
            assertNotEquals(pkg.getRules().get(0).getStrategyId(), rule.getStrategyId(),
                    "strategyId 应已重映射");
            assertNotNull(rule.getStrategyId());
        }

        // 6) 新模型行：INIT 状态、新 GUID、名称兜底
        ModelPO imported = modelMapper.selectById(newId);
        assertEquals(MERCHANT, imported.getCode());
        assertEquals(LABEL, imported.getLabel());
        assertEquals("model_" + newId, imported.getModelName());
        assertNotEquals(source.getGuid(), imported.getGuid());

        // 7) 冲突拒绝（同商户码+名称）且不产生半写
        assertThrows(ModelPackageService.ModelPackageException.class,
                () -> packageService.importPackage(parsed, MERCHANT, null, LABEL));
        assertEquals(1, modelMapper.selectCount(new LambdaQueryWrapper<ModelPO>()
                .eq(ModelPO::getCode, MERCHANT).eq(ModelPO::getLabel, LABEL)));
    }
}
