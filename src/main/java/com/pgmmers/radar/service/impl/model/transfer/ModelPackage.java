package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RulePO;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型策略包信封（v3.2）：模型配置全图的文件形态，格式版本见
 * {@link ModelPackageService#FORMAT_VERSION}。
 *
 * <p>范围对齐 copyModel 全图：字段/preItem/数据名单(+meta+record)/抽象/激活/规则。
 * 文件内保留原 ID 作为图内引用键（strategyId/dataListId 等），导入时整图重映射分配新 ID。
 * engine_model_conf(+param) 为 ML 休眠特征配置（Estimator 零实现），不入包——
 * 待 P1-5 ML 重做时随新方案扩展子集合（formatVersion 已留版本门）。</p>
 *
 * @since 3.2.0
 */
public class ModelPackage {

    private String formatVersion;
    private String exportedAt;
    private String radarVersion;

    private ModelPO model;
    private List<FieldPO> fields = new ArrayList<>();
    private List<PreItemPO> preItems = new ArrayList<>();
    private List<DataListsPO> dataLists = new ArrayList<>();
    private List<DataListMetaPO> dataListMeta = new ArrayList<>();
    private List<DataListRecordPO> dataListRecords = new ArrayList<>();
    private List<AbstractionPO> abstractions = new ArrayList<>();
    /**
     * 策略列表（v4.0 起原"激活器"更名"策略"；JSON 键经 @JsonAlias 兼容 v3.x 包的 "activations"，
     * 新导出仍写旧键以保双向可导入，见 docs/model-package.md）。
     */
    @JsonProperty("activations")
    private List<StrategyPO> strategies = new ArrayList<>();
    private List<RulePO> rules = new ArrayList<>();

    public String getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(String formatVersion) {
        this.formatVersion = formatVersion;
    }

    public String getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(String exportedAt) {
        this.exportedAt = exportedAt;
    }

    public String getRadarVersion() {
        return radarVersion;
    }

    public void setRadarVersion(String radarVersion) {
        this.radarVersion = radarVersion;
    }

    public ModelPO getModel() {
        return model;
    }

    public void setModel(ModelPO model) {
        this.model = model;
    }

    public List<FieldPO> getFields() {
        return fields;
    }

    public void setFields(List<FieldPO> fields) {
        this.fields = fields;
    }

    public List<PreItemPO> getPreItems() {
        return preItems;
    }

    public void setPreItems(List<PreItemPO> preItems) {
        this.preItems = preItems;
    }

    public List<DataListsPO> getDataLists() {
        return dataLists;
    }

    public void setDataLists(List<DataListsPO> dataLists) {
        this.dataLists = dataLists;
    }

    public List<DataListMetaPO> getDataListMeta() {
        return dataListMeta;
    }

    public void setDataListMeta(List<DataListMetaPO> dataListMeta) {
        this.dataListMeta = dataListMeta;
    }

    public List<DataListRecordPO> getDataListRecords() {
        return dataListRecords;
    }

    public void setDataListRecords(List<DataListRecordPO> dataListRecords) {
        this.dataListRecords = dataListRecords;
    }

    public List<AbstractionPO> getAbstractions() {
        return abstractions;
    }

    public void setAbstractions(List<AbstractionPO> abstractions) {
        this.abstractions = abstractions;
    }

    public List<StrategyPO> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<StrategyPO> strategies) {
        this.strategies = strategies;
    }

    public List<RulePO> getRules() {
        return rules;
    }

    public void setRules(List<RulePO> rules) {
        this.rules = rules;
    }
}
