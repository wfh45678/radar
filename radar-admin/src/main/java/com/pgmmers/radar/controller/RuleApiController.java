package com.pgmmers.radar.controller;

import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.intercpt.ContextHolder;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.vo.admin.UserVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.RuleVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/services/v1/rule")
@Api(value = "RuleApi", description = "规则管理接口操作",  tags = {"规则管理API"})
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
    public CommonResult save(@RequestBody RuleVO rule, HttpServletRequest request) {
        return ruleService.save(rule, contextHolder.getContext().getUsername());
    }

    @DeleteMapping
    public CommonResult delete(@RequestBody Long[] id) {
        return ruleService.delete(id);
    }

    @GetMapping("/hitsSort/{modelId}")
    public CommonResult hitsSort(@PathVariable Long modelId) {
        CommonResult result = new CommonResult();
        ModelVO modelVO = modelService.getModelById(modelId);
        if (modelVO == null) {
            return result; 
        }
        return ruleService.getHitSorts(modelId);
    }

	@PostMapping("/ruleHistory")
	public CommonResult queryRuleHistory(@RequestBody RuleHistoryQuery query) {
		return ruleService.queryHistory(query);
	}

}
