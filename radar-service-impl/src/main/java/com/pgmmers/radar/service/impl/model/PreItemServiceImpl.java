package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.PreItemQuery;
import com.pgmmers.radar.dal.model.FieldDal;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.dal.model.PreItemDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.vo.model.PreItemVO;
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
public class PreItemServiceImpl implements PreItemService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(FieldServiceImpl.class);

    public Map<Long, List<PreItemVO>> contextMap = new HashMap<Long, List<PreItemVO>>();

    @Autowired
    private ModelDal modelDal;
    @Autowired
    private PreItemDal preItemDal;
    @Autowired
    private FieldDal fieldDal;
    @Autowired
    private CacheService cacheService;

    @Override
    public List<PreItemVO> listPreItem(Long modelId) {
        List<PreItemVO> list = contextMap.get(modelId);
        if (list == null) {
            list = modelDal.listPreItem(modelId, null);
            contextMap.put(modelId, list);
        }
        return list;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("prefield sub:{}", message);
        PreItemVO preItem = JSON.parseObject(message, PreItemVO.class);
        if (preItem != null) {
            List<PreItemVO> list = modelDal.listPreItem(preItem.getModelId(), null);
            contextMap.put(preItem.getModelId(), list);
        }

    }

    @Override
    public PreItemVO get(Long id) {
        return preItemDal.get(id);
    }

    @Override
    public CommonResult query(PreItemQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", preItemDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(PreItemVO preItem) {
        CommonResult result = new CommonResult();
        if (preItem.getId() == null) {
            PreItemQuery query = new PreItemQuery();
            query.setModelId(preItem.getModelId());
            query.setLabel(preItem.getLabel());
            PageResult<PreItemVO> page = preItemDal.query(query);
            if (page != null && page.getRowCount() > 0) {
                result.setMsg("字段名已存在");
                return result;
            }
        }
//        if (preItem.getId() == null) {
//            FieldQuery query = new FieldQuery();
//            query.setModelId(preItem.getModelId());
//            query.setFieldName(fieldName);
//            PageResult<FieldVO> page = fieldDal.query(query);
//            if (page != null && page.getRowCount() > 0) {
//                result.setMsg("字段名已定义");
//                return result;
//            }
//        }
        int count = preItemDal.save(preItem);
        if (count > 0) {
        	if(StringUtils.isEmpty(preItem.getDestField())){
        		preItem.setDestField("preItem_"+preItem.getId());
        		preItemDal.save(preItem);
        	}
        	
            result.getData().put("id", preItem.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishPreItem(preItem);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        PreItemVO item = preItemDal.get(id[0]);
        int count = preItemDal.delete(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishPreItem(item);
        }
        return result;
    }
    
    @PostConstruct
    public void init() {
        cacheService.subscribePreItem(this);
    }
}
