package com.pgmmers.radar.service.model;


import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.model.ActivationVO;

import java.util.List;

public interface ActivationService {

	 List<ActivationVO> listActivation(Long modelId);

	 ActivationVO get(Long id);

	 CommonResult query(ActivationQuery query);

	 CommonResult save(ActivationVO activation);

	 CommonResult delete(Long[] id);
	
	 CommonResult updateOrder(Long activationId, String ruleOrder);

	/**
	 * update status of activation.
	 * @param activationId
	 * @param status
	 * @return
	 *
	 * @author feihu.wang
	 */
	 CommonResult updateStatus(Long activationId, Integer status);

}
