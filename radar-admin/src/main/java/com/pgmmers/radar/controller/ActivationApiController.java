/*
 * Copyright (c) 2019 WangFeiHu
 *  Radar is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *  http://license.coscl.org.cn/MulanPSL2
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 */

package com.pgmmers.radar.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.engine.vo.DataColumnInfo;
import com.pgmmers.radar.service.enums.DataType;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.model.*;
import com.pgmmers.radar.vo.model.*;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/services/v1/activation")
@Api(value = "ActivationApi", description = "策略集管理相关操作",  tags = {"策略集API"})
public class ActivationApiController {

    @Autowired
    private ActivationService activationService;

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
        ActivationVO activationVO = activationService.get(id);
        if (activationVO != null) {
            result.setSuccess(true);
            result.getData().put("activation", activationVO);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody ActivationQuery query) {
        return activationService.query(query);
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
            PluginServiceV2 pt = pluginManager.pluginServiceMap(preItem.getPlugin());
            if (StringUtils.isNotEmpty(pt.getType()) && pt.getType().equals("JSON")) {
                //load  http request data
                JsonNode json = preItem.getConfigJson();
                List<DataColumnInfo> children = new ArrayList<>();
                extractMetaColumn(ds, preItem, json.toString(), children);
            } else if (StringUtils.isNotEmpty(pt.getType())) {
                ds.addChildren(preItem.getLabel(), preItem.getDestField(), pt.getType());
            } else {
                List<DataColumnInfo> children = new ArrayList<>();
                extractMetaColumn(ds, preItem, pt.getMeta(), children);
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
		List<ActivationVO> listActivation=activationService.listActivation(modelId);
		if(listActivation!=null){
			for(ActivationVO activation:listActivation){
				DataColumnInfo ds=new DataColumnInfo(activation.getLabel(),activation.getActivationName());
				List<RuleVO> listRule=ruleService.listRule(activation.getId());
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
    public CommonResult save(@RequestBody ActivationVO activation) {
        return activationService.save(activation);
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return activationService.delete(id);
    }

	@PostMapping("/updateOrder")
	public CommonResult updateOrder(@RequestParam Long activationId, @RequestParam String ruleOrder) {
		return activationService.updateOrder(activationId,ruleOrder);
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

    @PostMapping("/disable/{activationId}")
    public CommonResult disable(@PathVariable Long activationId) {
        return activationService.updateStatus(activationId, 0);
    }

    @PostMapping("/enable/{activationId}")
    public CommonResult enable(@PathVariable Long activationId) {
        return activationService.updateStatus(activationId, 1);
    }

    @PostMapping("/copy")
    public CommonResult copy(@RequestBody Long id) {
        CommonResult result = activationService.copy(id);
        return result;
    }
}
