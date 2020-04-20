package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.PreItemQuery;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.dal.model.PreItemDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreItemServiceImpl extends BaseLocalCacheService implements PreItemService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(FieldServiceImpl.class);


    @Override
    public Object query(Long modelId) {
        return  modelDal.listPreItem(modelId, null);
    }

    @Autowired
    private ModelDal modelDal;
    @Autowired
    private PreItemDal preItemDal;
    @Autowired
    private CacheService cacheService;

    @Override
    public List<PreItemVO> listPreItem(Long modelId) {
        //noinspection unchecked
        return (List<PreItemVO>) getByCache(modelId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("prefield sub:{}", message);
        PreItemVO preItem = JSON.parseObject(message, PreItemVO.class);
        if (preItem != null) {
          invalidateCache(preItem.getModelId());
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

        int count = preItemDal.save(preItem);
        if (count > 0) {
        	if(StringUtils.isEmpty(preItem.getDestField())){
        		preItem.setDestField("preItem_" + preItem.getId());
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
