package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;

import com.pgmmers.radar.dal.bean.AbstractionQuery;
import com.pgmmers.radar.dal.model.AbstractionDal;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.AbstractionService;
import com.pgmmers.radar.util.GroovyScriptUtil;
import com.pgmmers.radar.vo.model.AbstractionVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AbstractionServiceImpl implements AbstractionService, SubscribeHandle {
    public static Logger logger = LoggerFactory.getLogger(AbstractionServiceImpl.class);

    @Autowired
    private ModelDal modelDal;
    @Autowired
    private AbstractionDal abstractionDal;
    @Autowired
    private CacheService cacheService;

    public Map<Long, List<AbstractionVO>> contextMap = new HashMap<Long, List<AbstractionVO>>();

    @Override
    public List<AbstractionVO> listAbstraction(Long modelId) {
        List<AbstractionVO> list = contextMap.get(modelId);
        if (list == null || list.size() == 0) {
            list = modelDal.listAbstraction(modelId, null);
            contextMap.put(modelId, list);
        }
        return list;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("abstraction sub:{}", message);
        AbstractionVO abstraction = JSON.parseObject(message, AbstractionVO.class);
        if (abstraction != null) {
            List<AbstractionVO> list = modelDal.listAbstraction(abstraction.getModelId(), null);
            contextMap.put(abstraction.getModelId(), list);
        }

    }

    @Override
    public AbstractionVO get(Long id) {
        return abstractionDal.get(id);
    }

    @Override
    public CommonResult list(Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", abstractionDal.list(modelId));
        return result;
    }

    @Override
    public CommonResult query(AbstractionQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", abstractionDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(AbstractionVO abstraction) {
        CommonResult result = new CommonResult();
        if (abstraction.getId() != null) {
            AbstractionVO oldAbs = abstractionDal.get(abstraction.getId());
            // 如果规则有更新，以前的编译好的groovy 对象用不到了，应该删除。
            if (!oldAbs.getRuleScript().equals(abstraction.getRuleScript())) {
                GroovyScriptUtil.removeInactiveScript(oldAbs.getRuleScript());
            }
        }
        int count = abstractionDal.save(abstraction);
        if (count > 0) {
        	if(StringUtils.isEmpty(abstraction.getName())){
        		abstraction.setName("abstraction_" + abstraction.getId());
        		abstractionDal.save(abstraction);
        	}
        	
            result.getData().put("id", abstraction.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishAbstraction(abstraction);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        AbstractionVO abs = abstractionDal.get(id[0]);
        int count = abstractionDal.delete(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishAbstraction(abs);
        }
        return result;
    }
    
    @PostConstruct
    public void init() {
        cacheService.subscribeAbstraction(this);
    }
}
