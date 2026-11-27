package com.pgmmers.radar.service.model;


import com.pgmmers.radar.model.query.StrategyQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.StrategyHistoryVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;

import java.util.List;

public interface StrategyService {

	 List<StrategyVO> listStrategy(Long modelId);

	 StrategyVO get(Long id);

	 CommonResult query(StrategyQuery query);

	 CommonResult save(StrategyVO strategy, String operator);

	 CommonResult delete(Long[] id);

	 CommonResult updateOrder(Long strategyId, String ruleOrder);

	 CommonResult copy(Long id);

	/**
	 * update status of strategy.
	 * @param strategyId
	 * @param status
	 * @return
	 *
	 * @author feihu.wang
	 */
	 CommonResult updateStatus(Long strategyId, Integer status);

	/**
	 * 策略定义变更历史（v4.0）：评分卡/决策树保存快照列表，按时间倒序。
	 */
	 List<StrategyHistoryVO> listHistory(Long strategyId);

}
