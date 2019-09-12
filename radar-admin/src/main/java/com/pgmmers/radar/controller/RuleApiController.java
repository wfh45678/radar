package com.pgmmers.radar.controller;

import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.vo.admin.UserVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
@RestController
@RequestMapping("/services/v1/rule")
public class RuleApiController {

    @Autowired
    private RuleService ruleService;
        
    @Autowired
    private ModelService modelService;

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
    	HttpSession session = request.getSession();
        UserVO user = (UserVO) session.getAttribute("user");
        return ruleService.save(rule, user.getUserName());
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
