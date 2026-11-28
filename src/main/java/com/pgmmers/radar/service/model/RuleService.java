package com.pgmmers.radar.service.model;

import com.pgmmers.radar.model.query.RuleHistoryQuery;
import com.pgmmers.radar.model.query.RuleQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.RuleVO;

import java.util.List;

public interface RuleService {

    List<RuleVO> listRule(Long strategyId);

    RuleVO get(Long id);

    CommonResult query(RuleQuery query);

    CommonResult save(RuleVO rule, String userCode);

    CommonResult delete(Long[] id);

    CommonResult getHitSorts(Long scenarioId);

    CommonResult getHitSorts(Long scenarioId, Long beginTime, Long endTime);

    CommonResult getHitSorts(Long scenarioId, String beginTime, String endTime);

    CommonResult queryHistory(RuleHistoryQuery query);
    
    List<RuleVO> listRuleByModelId(Long scenarioId);
}
