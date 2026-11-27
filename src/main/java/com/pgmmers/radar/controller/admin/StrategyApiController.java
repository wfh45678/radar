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
    private AbstractionService abstractionService;
    @Autowired
    private PreItemService preItemService;
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

    @GetMapping("/feature/columns/{modelId}")
    public CommonResult getDataColumns(@PathVariable Long modelId) {
        List<DataColumnInfo> list = new ArrayList<DataColumnInfo>();
        // 1、Data
        DataColumnInfo ds = new DataColumnInfo(DataType.FIELDS.getDesc(), DataType.FIELDS.getName());
        List<FieldVO> listField = fieldService.listField(modelId);
        for (FieldVO field : listField) {
            ds.addChildren(field.getLabel(), field.getFieldName(), field.getFieldType());
        }
        list.add(ds);

        // 2、PREPARE
        ds = new DataColumnInfo(DataType.PREITEMS.getDesc(), DataType.PREITEMS.getName());
        List<PreItemVO> listPreItem = preItemService.listPreItem(modelId);
        for (PreItemVO preItem : listPreItem) {
            PluginEntry pt = pluginManager.entry(preItem.getPlugin());
            if (StringUtils.isNotEmpty(pt.getType()) && pt.getType().equals("JSON")) {
                //load  http request data
                JsonNode json = preItem.getConfigJson();
                List<DataColumnInfo> children = new ArrayList<>();
                extractMetaColumn(ds, preItem, json.toString(), children);
            } else if (StringUtils.isNotEmpty(pt.getType())) {
                ds.addChildren(preItem.getLabel(), preItem.getDestField(), pt.getType());
            } else {
                List<DataColumnInfo> children = new ArrayList<>();
                extractMetaColumn(ds, preItem, pt.legacyMetaJson(), children);
            }
        }
        list.add(ds);

        // 3、ABSTRACTION
        List<AbstractionVO> listAbstract = abstractionService.listAbstraction(modelId);
        ds = new DataColumnInfo(DataType.ABSTRACTIONS.getDesc(), DataType.ABSTRACTIONS.getName());
        if (listAbstract != null) {
            for (AbstractionVO abs : listAbstract) {
                ds.addChildren(abs.getLabel(), abs.getName(), FieldType.DOUBLE.name());
            }
        }

        list.add(ds);

        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", list);
        return result;
    }

    @GetMapping("/abstraction/columns/{modelId}")
    public CommonResult getAbstractionColumns(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        List<AbstractionVO> listAbstract = abstractionService.listAbstraction(modelId);
        DataColumnInfo ds = new DataColumnInfo(DataType.ABSTRACTIONS.getDesc(), DataType.ABSTRACTIONS.getName());
        if (listAbstract != null) {
            for (AbstractionVO abs : listAbstract) {
                ds.addChildren(abs.getLabel(), abs.getName(), FieldType.DOUBLE.name());
            }
        }

        result.getData().put("columns", ds.getChildren());
        return result;
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

    private void extractMetaColumn(DataColumnInfo ds, PreItemVO preItem, String jsonStr, List<DataColumnInfo> children) {
        JSONArray array = JSONArray.parseArray(jsonStr);
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            children.add(new DataColumnInfo(obj.getString("title"), obj.getString("column"), obj
                    .getString("type")));
        }
        ds.addChildren(preItem.getLabel(), preItem.getDestField(), children);
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
