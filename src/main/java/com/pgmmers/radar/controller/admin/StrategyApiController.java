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
     * 变量树统一入口已迁移：{@link com.pgmmers.radar.controller.admin.FeatureApiController#getColumns}
     * （/services/v1/feature/columns/{scenarioId}，fields + features 两组，原 preItems/abstractions
     * 三组合并）——v4.1.1 移除本控制器内的旧路径。
     */

    @GetMapping("/rule/columns/{scenarioId}")
	public CommonResult getRuleColumns(@PathVariable Long scenarioId) {
		List<DataColumnInfo> list = new ArrayList<>();
		List<StrategyVO> listStrategy=strategyService.listStrategy(scenarioId);
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
