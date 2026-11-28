package com.pgmmers.radar.service.impl.model.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pgmmers.radar.model.entity.FeaturePO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.RulePO;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型策略包信封（v4.1 起 formatVersion 2.0）：模型配置全图的文件形态，格式版本见
 * {@link ModelPackageService#FORMAT_VERSION}。
 *
 * <p>范围对齐 copyModel 全图：字段/特征（features，v4.1 统一原 preItem+抽象）/数据名单
 * (+meta+record)/策略/规则。文件内保留原 ID 作为图内引用键（strategyId/dataListId 等），
 * 导入时整图重映射分配新 ID。v4.0 包（preItems/abstractions 键）导入时自动转换为特征形态，
 * 见 {@link PackageV1Converter}。engine_model_conf(+param) 为 ML 休眠特征配置
 * （Estimator 零实现），不入包——待 P1-5 ML 重做时随新方案扩展子集合。</p>
 *
 * @since 3.2.0
 */
public class ModelPackage {

    private String formatVersion;
    private String exportedAt;
    private String radarVersion;

    private ModelPO model;
    private List<FieldPO> fields = new ArrayList<>();
    private List<DataListsPO> dataLists = new ArrayList<>();
    private List<DataListMetaPO> dataListMeta = new ArrayList<>();
    private List<DataListRecordPO> dataListRecords = new ArrayList<>();
    /**
     * 特征列表（v4.1 特征工程：ENRICH/AGGREGATE/DERIVED/RATIO 统一；取代 v4.0 包的 preItems+abstractions）。
     */
    @JsonProperty("features")
    private List<FeaturePO> features = new ArrayList<>();
    @JsonProperty("strategies")
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

    public List<FeaturePO> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeaturePO> features) {
        this.features = features;
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
