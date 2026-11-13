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

import com.pgmmers.radar.model.query.EventQuery;
import com.pgmmers.radar.model.query.RuleHistoryQuery;
import com.pgmmers.radar.model.query.RuleQuery;
import com.pgmmers.radar.interceptor.ContextHolder;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.model.vo.model.ModelVO;
import com.pgmmers.radar.model.vo.model.RuleVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/services/v1/rule")
@Tag(name = "规则管理API", description = "规则管理接口操作")
public class RuleApiController {

    @Autowired
    private RuleService ruleService;
        
    @Autowired
    private ModelService modelService;

    @Autowired
    private ContextHolder contextHolder;

    @GetMapping("/{id}")
    public CommonResult get(@PathVariable Long id) {
        CommonResult result = new CommonResult();
        RuleVO ruleVO = ruleService.get(id);
        if (ruleVO != null) {
            result.setSuccess(true);
            result.getData().put("rule", ruleVO);
        }
        return result;
    }

    @PostMapping
    public CommonResult query(@RequestBody RuleQuery query) {
        return ruleService.query(query);
    }

    @PutMapping
    public CommonResult save(@RequestBody RuleVO rule) {
        return ruleService.save(rule, contextHolder.getContext().getUsername());
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return ruleService.delete(id);
    }

    @PostMapping("/hitsSort/{modelId}")
    public CommonResult hitsSort(@PathVariable Long modelId, @RequestBody EventQuery query) {
        CommonResult result = new CommonResult();
        ModelVO modelVO = modelService.getModelById(modelId);
        if (modelVO == null) {
            return result; 
        }
        return ruleService.getHitSorts(modelId, query.getBeginTime(), query.getEndTime());
    }

	@PostMapping("/ruleHistory")
	public CommonResult queryRuleHistory(@RequestBody RuleHistoryQuery query) {
		return ruleService.queryHistory(query);
	}

}
