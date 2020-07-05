package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.dal.model.ActivationDal;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ActivationService;
import com.pgmmers.radar.vo.model.ActivationVO;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivationServiceImpl extends BaseLocalCacheService implements ActivationService, SubscribeHandle {

    @Override
    public Object query(Long modelId) {
        return  modelDal.listActivation(modelId, null);
    }

    public static Logger logger = LoggerFactory.getLogger(ActivationServiceImpl.class);

    @Autowired
    private ModelDal modelDal;

    @Autowired
    private ActivationDal activationDal;

    @Autowired
    private CacheService cacheService;

    @Override
    public List<ActivationVO> listActivation(Long modelId) {
        //noinspection unchecked
        return (List<ActivationVO>) getByCache(modelId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("activation sub:{}", message);
        ActivationVO act = JSON.parseObject(message, ActivationVO.class);
        if (act != null) {
            invalidateCache(act.getModelId());
        }
    }

    @Override
    public ActivationVO get(Long id) {
        return activationDal.get(id);
    }

    @Override
    public CommonResult query(ActivationQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", activationDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(ActivationVO activation) {
        CommonResult result = new CommonResult();
        int count = activationDal.save(activation);
        if (count > 0) {
        	if(StringUtils.isEmpty(activation.getActivationName())){
        		activation.setActivationName("activation_"+activation.getId());
        		activationDal.save(activation);
        	}
            result.getData().put("id", activation.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishActivation(activation);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        ActivationVO act = activationDal.get(id[0]);
        int count = activationDal.delete(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishActivation(act);
        }
        return result;
    }

    @Override
	public CommonResult updateOrder(Long activationId, String ruleOrder) {
    	CommonResult result = new CommonResult();
		ActivationVO activation=activationDal.get(activationId);
		activation.setRuleOrder(ruleOrder);
		int count = activationDal.save(activation);
		if(count > 0){
			result.setSuccess(true);
			// 通知更新
            cacheService.publishActivation(activation);
		}
		return result;
	}

	@PostConstruct
    public void init() {
        cacheService.subscribeActivation(this);
    }

}
