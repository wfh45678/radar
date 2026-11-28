/*
 * Copyright (c) 2019-2026 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller.admin;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.model.query.FeatureQuery;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.model.FeatureService;
import com.pgmmers.radar.service.model.FieldService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 特征 API（v4.1 特征工程重构）：取代原 /preitem 与 /abstraction 两套接口，
 * 统一管理 ENRICH/AGGREGATE/DERIVED/RATIO 四类特征。
 */
@RestController
@RequestMapping("/services/v1/feature")
@Tag(name = "特征API", description = "特征工程相关操作（v4.1 起统一特征体系：插件加工/衍生表达式/统计聚合/组合比率）")
public class FeatureApiController {

    @Autowired
    private FeatureService featureService;

    @Autowired
    private FieldService fieldService;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        FeatureVO feature = featureService.get(id);
        if (feature != null) {
            result.setSuccess(true);
            result.getData().put("feature", feature);
        }
        return result;
    }

    @GetMapping("/list/{scenarioId}")
    public CommonResult list(@PathVariable Long scenarioId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", featureService.listFeature(scenarioId));
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody FeatureQuery query) {
        return featureService.query(query);
    }

    @PutMapping
    public CommonResult save(@RequestBody FeatureVO feature) {
        return featureService.save(feature);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return featureService.delete(id);
    }

    @PostMapping("/enable/{id}")
    public CommonResult enable(@PathVariable Long id) {
        return featureService.updateStatus(id, 1);
    }

    @PostMapping("/disable/{id}")
    public CommonResult disable(@PathVariable Long id) {
        return featureService.updateStatus(id, 2);
    }

    /**
     * 统一变量树（v4.1）：fields + features 两组，供规则/评分卡/决策树/特征过滤编辑器级联选择。
     * ENRICH 多列特征（OUTPUT 声明）展开子列 features.&lt;name&gt;.&lt;column&gt;，
     * 单值特征与 AGGREGATE/DERIVED/RATIO 为标量节点 features.&lt;name&gt;。
     */
    @GetMapping("/columns/{scenarioId}")
    public CommonResult getColumns(@PathVariable Long scenarioId) {
        List<DataColumnInfo> list = new ArrayList<>();
        // 1、fields
        DataColumnInfo ds = new DataColumnInfo(DataType.FIELDS.getDesc(), DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(scenarioId);
        if (listField != null && !listField.isEmpty()) {
            for (FieldVO field : listField) {
                ds.addChildren(field.getLabel(), field.getFieldName(), field.getFieldType());
            }
            list.add(ds);
        }

        // 2、features
        ds = new DataColumnInfo(DataType.FEATURES.getDesc(), DataType.FEATURES.getName());
        List<FeatureVO> features = featureService.listFeature(scenarioId);
        if (features != null && !features.isEmpty()) {
            for (FeatureVO feature : features) {
                addFeatureNode(ds, feature);
            }
            list.add(ds);
        }

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;
    }

    private void addFeatureNode(DataColumnInfo ds, FeatureVO feature) {
        JsonNode output = feature.getOutput();
        List<DataColumnInfo> children = new ArrayList<>();
        if (output != null && output.isArray() && !output.isEmpty()) {
            // 多列特征：features.<name>.<column>
            for (JsonNode col : output) {
                String name = col.path("column").asText("");
                String title = col.path("title").asText(name);
                String type = col.path("type").asText("");
                if (!name.isEmpty()) {
                    children.add(new DataColumnInfo(title, name, type));
                }
            }
            if (!children.isEmpty()) {
                ds.addChildren(feature.getLabel(), feature.getFeatureName(), children);
                return;
            }
        }
        // 单值特征：聚合/衍生/比率恒为数值，加工类看输出声明（缺省 STRING）
        String type = FeatureType.AGGREGATE.name().equals(feature.getType())
                || FeatureType.DERIVED.name().equals(feature.getType())
                || FeatureType.RATIO.name().equals(feature.getType())
                ? "DOUBLE" : "STRING";
        ds.addChildren(feature.getLabel(), feature.getFeatureName(), type);
    }
}
