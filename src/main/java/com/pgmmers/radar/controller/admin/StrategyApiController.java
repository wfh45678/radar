/*
 * Copyright (c) 2019 WangFeiHu
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
import com.pgmmers.radar.model.query.StrategyQuery;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.model.*;
import com.pgmmers.radar.model.vo.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/services/v1/strategy")
@Tag(name = "策略API", description = "策略管理相关操作（v4.0 起原「激活器/策略集」统一称「策略」）")
public class StrategyApiController {

    /**
     * service.
     */
    @Autowired
    private StrategyService strategyService;

    @Autowired
    private com.pgmmers.radar.interceptor.ContextHolder contextHolder;

    @Autowired
    private FieldService fieldService;
    @Autowired
    private FeatureService featureService;
    @Autowired
    private RuleService ruleService;
    @Autowired
    private PluginManager pluginManager;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        StrategyVO strategyVO = strategyService.get(id);
        if (strategyVO != null) {
            result.setSuccess(true);
            result.getData().put("strategy", strategyVO);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody StrategyQuery query) {
        return strategyService.query(query);
    }

    /**
     * 变量树（v4.1 统一）：fields + features 两组（原 preItems/abstractions 三组合并）。
     * @deprecated 见 {@link FeatureApiController#getColumns}（/feature/columns/{modelId}），前端随 v4.1 切换后移除。
     */
    @Deprecated
    @GetMapping("/feature/columns/{modelId}")
    public CommonResult getDataColumns(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", featureColumns(modelId));
        return result;
    }

    /** 聚合类特征标量节点（报表页历史图选择用，路径随前端 v4.1 切换）。 */
    @Deprecated
    @GetMapping("/abstraction/columns/{modelId}")
    public CommonResult getAbstractionColumns(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        List<com.pgmmers.radar.service.engine.vo.DataColumnInfo> features =
                featureColumns(modelId);
        com.pgmmers.radar.service.engine.vo.DataColumnInfo featureGroup = null;
        for (com.pgmmers.radar.service.engine.vo.DataColumnInfo ds : features) {
            if (com.pgmmers.radar.service.enums.DataType.FEATURES.getName().equals(ds.getValue())) {
                featureGroup = ds;
            }
        }
        result.getData().put("columns", featureGroup == null ? java.util.List.of() : featureGroup.getChildren());
        return result;
    }

    /** fields + features 统一变量树（与 /services/v1/feature/columns 同源）。 */
    private List<com.pgmmers.radar.service.engine.vo.DataColumnInfo> featureColumns(Long modelId) {
        List<com.pgmmers.radar.service.engine.vo.DataColumnInfo> list = new ArrayList<>();
        com.pgmmers.radar.service.engine.vo.DataColumnInfo ds =
                new com.pgmmers.radar.service.engine.vo.DataColumnInfo(
                        com.pgmmers.radar.service.enums.DataType.FIELDS.getDesc(),
                        com.pgmmers.radar.service.enums.DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(modelId);
        for (FieldVO field : listField) {
            ds.addChildren(field.getLabel(), field.getFieldName(), field.getFieldType());
        }
        list.add(ds);

        com.pgmmers.radar.service.engine.vo.DataColumnInfo featureGroup =
                new com.pgmmers.radar.service.engine.vo.DataColumnInfo(
                        com.pgmmers.radar.service.enums.DataType.FEATURES.getDesc(),
                        com.pgmmers.radar.service.enums.DataType.FEATURES.getName());
        for (FeatureVO feature : featureService.listFeature(modelId)) {
            com.fasterxml.jackson.databind.JsonNode output = feature.getOutput();
            if (output != null && output.isArray() && !output.isEmpty()) {
                List<com.pgmmers.radar.service.engine.vo.DataColumnInfo> children = new ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode col : output) {
                    String column = col.path("column").asText("");
                    if (!column.isEmpty()) {
                        children.add(new com.pgmmers.radar.service.engine.vo.DataColumnInfo(
                                col.path("title").asText(column), column,
                                col.path("type").asText("STRING")));
                    }
                }
                if (!children.isEmpty()) {
                    featureGroup.addChildren(feature.getLabel(), feature.getFeatureName(), children);
                    continue;
                }
            }
            String type = "ENRICH".equals(feature.getType()) ? "STRING" : FieldType.DOUBLE.name();
            featureGroup.addChildren(feature.getLabel(), feature.getFeatureName(), type);
        }
        list.add(featureGroup);
        return list;
    }

    @GetMapping("/rule/columns/{modelId}")
	public CommonResult getRuleColumns(@PathVariable Long modelId) {
		List<DataColumnInfo> list = new ArrayList<>();
		List<StrategyVO> listStrategy=strategyService.listStrategy(modelId);
		if(listStrategy!=null){
			for(StrategyVO strategy:listStrategy){
				DataColumnInfo ds=new DataColumnInfo(strategy.getLabel(),strategy.getStrategyName());
				List<RuleVO> listRule=ruleService.listRule(strategy.getId());
				for(RuleVO rule:listRule){
					ds.addChildren(rule.getLabel(), rule.getName(),rule.getId()+"");
				}
				list.add(ds);
			}
		}

		CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;
	}

	@PutMapping
    public CommonResult save(@RequestBody StrategyVO strategy) {
        return strategyService.save(strategy, contextHolder.getContext().getUsername());
    }

    /** 策略定义变更历史（v4.0：评分卡/决策树保存快照） */
    @GetMapping("/history/{strategyId}")
    public CommonResult history(@PathVariable Long strategyId) {
        CommonResult result = new CommonResult();
        List<StrategyHistoryVO> history = strategyService.listHistory(strategyId);
        result.setSuccess(true);
        result.getData().put("list", history);
        return result;
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return strategyService.delete(id);
    }

	@PostMapping("/updateOrder")
	public CommonResult updateOrder(@RequestParam Long strategyId, @RequestParam String ruleOrder) {
		return strategyService.updateOrder(strategyId,ruleOrder);
	}


    @PostMapping("/disable/{strategyId}")
    public CommonResult disable(@PathVariable Long strategyId) {
        return strategyService.updateStatus(strategyId, 0);
    }

    @PostMapping("/enable/{strategyId}")
    public CommonResult enable(@PathVariable Long strategyId) {
        return strategyService.updateStatus(strategyId, 1);
    }

    @PostMapping("/copy")
    public CommonResult copy(@RequestBody Long id) {
        CommonResult result = strategyService.copy(id);
        return result;
    }
}
