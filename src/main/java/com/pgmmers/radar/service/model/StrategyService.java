package com.pgmmers.radar.service.model;


import com.pgmmers.radar.model.query.StrategyQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.model.StrategyVO;

import java.util.List;

public interface StrategyService {

	 List<StrategyVO> listStrategy(Long modelId);

	 StrategyVO get(Long id);

	 CommonResult query(StrategyQuery query);

	 CommonResult save(StrategyVO strategy);

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

}
