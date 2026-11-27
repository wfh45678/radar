package com.pgmmers.radar.service.impl.model.transfer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ModelMapper;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import groovy.lang.GroovyClassLoader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模型策略包服务（v3.2）：导出（模型全图 → JSON 信封）、解析/校验（含导入报告）、
 * 导入（新模型 + 整图事务装配）。
 *
 * <p>校验目标是"带着已知问题决策"：缺插件 key / Groovy 编译告警 / 名单行数只进报告，
 * 不阻断导入；结构错误（格式版本/反序列化）才拒绝。</p>
 *
 * @since 3.2.0
 */
@Service
public class ModelPackageService {

    private static final Logger logger = LoggerFactory.getLogger(ModelPackageService.class);

    /** 策略包格式版本（import 只接受本版本，演进时随 formatVersion 协商）。 */
    public static final String FORMAT_VERSION = "1.0";

    /** 引擎版本标记（随发布更新，仅供包接收方参考）。 */
    public static final String RADAR_VERSION = "3.2.0";

    @Value("${model.package.max-upload-size:10485760}")
    private long maxUploadSize;

    /** 名单记录行数告警阈值（超限仅提示，不截断）。 */
    @Value("${model.package.record-warn-threshold:100000}")
    private long recordWarnThreshold;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private PreItemMapper preItemMapper;
    @Autowired
    private DataListsMapper dataListMapper;
    @Autowired
    private DataListMetaMapper dataListMetaMapper;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;
    @Autowired
    private AbstractionMapper abstractionMapper;
    @Autowired
    private StrategyMapper strategyMapper;
    @Autowired
    private RuleMapper ruleMapper;
    @Autowired
    private ModelGraphAssembler assembler;
    @Autowired
    private PluginManager pluginManager;

    /** 专用 mapper：日期 ISO-8601 可读、未知字段忽略（向前兼容新版导出文件）。 */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    /** 模型策略包结构/校验异常（msg 可直接展示给管理员）。 */
    public static class ModelPackageException extends RuntimeException {
        public ModelPackageException(String message) {
            super(message);
        }

        public ModelPackageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** 导出：模型全图 → 策略包（模型不存在返回 null）。 */
    public ModelPackage export(Long modelId) {
        ModelPO model = modelMapper.selectById(modelId);
        if (model == null) {
            return null;
        }
        ModelPackage pkg = new ModelPackage();
        pkg.setFormatVersion(FORMAT_VERSION);
        pkg.setExportedAt(Instant.now().toString());
        pkg.setRadarVersion(RADAR_VERSION);
        pkg.setModel(model);
        pkg.setFields(fieldMapper.selectList(
                new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getModelId, modelId)));
        pkg.setPreItems(preItemMapper.selectList(
                new LambdaQueryWrapper<PreItemPO>().eq(PreItemPO::getModelId, modelId)));
        List<DataListsPO> dataLists = dataListMapper.selectList(
                new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getModelId, modelId));
        pkg.setDataLists(dataLists);
        if (!dataLists.isEmpty()) {
            List<Long> dataListIds = dataLists.stream().map(DataListsPO::getId).collect(Collectors.toList());
            pkg.setDataListMeta(dataListMetaMapper.selectList(
                    new LambdaQueryWrapper<DataListMetaPO>()
                            .in(DataListMetaPO::getDataListId, dataListIds)));
            pkg.setDataListRecords(dataListRecordMapper.selectList(
                    new LambdaQueryWrapper<DataListRecordPO>()
                            .in(DataListRecordPO::getDataListId, dataListIds)));
        }
        pkg.setAbstractions(abstractionMapper.selectList(
                new LambdaQueryWrapper<AbstractionPO>().eq(AbstractionPO::getModelId, modelId)));
        List<StrategyPO> strategies = strategyMapper.selectList(
                new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getModelId, modelId));
        pkg.setStrategies(strategies);
        if (!strategies.isEmpty()) {
            List<Long> strategyIds = strategies.stream().map(StrategyPO::getId).collect(Collectors.toList());
            pkg.setRules(ruleMapper.selectList(
                    new LambdaQueryWrapper<RulePO>().in(RulePO::getStrategyId, strategyIds)));
        }
        return pkg;
    }

    /** 序列化为 JSON 字节（导出下载用）。 */
    public byte[] serialize(ModelPackage pkg) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(pkg);
        } catch (IOException e) {
            throw new ModelPackageException("策略包序列化失败: " + e.getMessage(), e);
        }
    }

    /** 解析上传的字节 → 策略包（大小上限 + 反序列化 + 格式版本校验）。 */
    public ModelPackage parse(byte[] body) {
        if (body == null || body.length == 0) {
            throw new ModelPackageException("上传内容为空");
        }
        if (body.length > maxUploadSize) {
            throw new ModelPackageException("策略包超过大小上限 " + (maxUploadSize / 1024 / 1024) + "MB");
        }
        ModelPackage pkg;
        try {
            pkg = objectMapper.readValue(body, ModelPackage.class);
        } catch (IOException e) {
            throw new ModelPackageException("不是合法的策略包 JSON: " + e.getMessage(), e);
        }
        if (pkg.getModel() == null) {
            throw new ModelPackageException("策略包缺少 model 节点");
        }
        if (!FORMAT_VERSION.equals(pkg.getFormatVersion())) {
            throw new ModelPackageException("策略包格式版本不支持: " + pkg.getFormatVersion()
                    + "（当前引擎支持 " + FORMAT_VERSION + "）");
        }
        return pkg;
    }

    /**
     * 导入前校验：返回告警列表（不阻断导入）。检查项：
     * 缺失插件 key（preItem 按引用）/ Groovy 预编译失败 / 名单记录行数超阈值。
     */
    public List<String> validate(ModelPackage pkg) {
        List<String> warnings = new ArrayList<>();
        Set<String> installedKeys = pluginManager.entries().stream()
                .map(e -> e.getKey()).collect(Collectors.toSet());
        for (PreItemPO preItem : pkg.getPreItems()) {
            if (preItem.getPlugin() != null && !installedKeys.contains(preItem.getPlugin())) {
                warnings.add("目标实例未安装插件: " + preItem.getPlugin()
                        + "（preItem=" + preItem.getDestField() + "，事件执行时该插件将被隔离跳过）");
            }
        }
        for (RulePO rule : pkg.getRules()) {
            String script = rule.getScripts();
            if (script == null || script.isBlank()) {
                continue;
            }
            GroovyClassLoader loader = new GroovyClassLoader();
            try {
                loader.parseClass(script);
            } catch (Exception e) {
                warnings.add("规则脚本编译告警（rule=" + rule.getName() + "）: " + e.getMessage());
            } finally {
                try {
                    loader.close();
                } catch (IOException ignore) {
                    // 关闭噪音不参与告警判定
                }
            }
        }
        long recordCount = pkg.getDataListRecords().size();
        if (recordCount > recordWarnThreshold) {
            warnings.add("名单记录共 " + recordCount + " 行（超过告警阈值 " + recordWarnThreshold + "），导入耗时可能较长");
        }
        return warnings;
    }

    /** 导出文件名（Content-Disposition 侧做 RFC 5987 编码）。 */
    public String exportFilename(ModelPackage pkg) {
        String label = pkg.getModel().getLabel() == null ? "model" : pkg.getModel().getLabel();
        String date = LocalDate.now().toString().replace("-", "");
        return "radar-model-" + label + "-" + date + ".json";
    }

    /**
     * 导入为新模型：新 GUID / INIT 状态 / 指定商户码与名称，整图装配单事务。
     * 商户码+名称冲突此处兜底再查一次（调用方可提前校验给更好的提示）。
     *
     * @return 新模型 id
     */
    @Transactional
    public Long importPackage(ModelPackage pkg, String merchantCode, String modelName, String label) {
        ModelPO source = pkg.getModel();
        Long conflict = modelMapper.selectCount(new LambdaQueryWrapper<ModelPO>()
                .eq(ModelPO::getCode, merchantCode).eq(ModelPO::getLabel, label));
        if (conflict != null && conflict > 0) {
            throw new ModelPackageException("模型已存在（商户码+名称冲突）: " + merchantCode + " / " + label);
        }
        // 列宽前置校验（engine_model.LABEL varchar(32) / MODEL_NAME varchar(30)）：
        // 明确拒绝优于 SQL Data truncation 报错
        if (label != null && label.length() > 32) {
            throw new ModelPackageException("模型显示名称过长（<=32 字符）: " + label);
        }
        if (modelName != null && modelName.length() > 30) {
            throw new ModelPackageException("模型名过长（<=30 字符）: " + modelName);
        }
        ModelPO model = new ModelPO();
        model.setGuid(UUID.randomUUID().toString().toUpperCase());
        // MODEL_NAME 列 NOT NULL 无默认值：留空时先以临时名插入，拿到 id 后归一为 model_<id>
        // （与 copy/save 的最终命名语义一致）
        model.setModelName(modelName == null || modelName.isBlank()
                ? "model_tmp_" + Long.toHexString(System.currentTimeMillis())
                : modelName);
        model.setLabel(label);
        model.setCode(merchantCode);
        model.setEntityName(source.getEntityName());
        model.setEntryName(source.getEntryName());
        model.setReferenceDate(source.getReferenceDate());
        model.setFieldValidate(source.getFieldValidate());
        model.setDashboardUrl(source.getDashboardUrl());
        model.setTemplate(false);
        model.setStatus(StatusType.INIT.getKey());
        Date now = new Date();
        model.setCreateTime(now);
        model.setUpdateTime(now);
        modelMapper.insert(model);
        Long newId = model.getId();
        if (model.getModelName().startsWith("model_tmp_")) {
            model.setModelName("model_" + newId);
            modelMapper.updateById(model);
        }
        int count = assembler.assemble(newId,
                pkg.getFields(), pkg.getPreItems(),
                pkg.getDataLists(), pkg.getDataListMeta(), pkg.getDataListRecords(),
                pkg.getAbstractions(), pkg.getStrategies(), pkg.getRules());
        logger.info("model package imported: source label={}, new modelId={}, {} objects",
                source.getLabel(), newId, count);
        return newId;
    }
}
