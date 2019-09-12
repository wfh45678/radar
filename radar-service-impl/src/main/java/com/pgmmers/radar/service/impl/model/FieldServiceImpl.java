package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;

import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.FieldDal;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
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
public class FieldServiceImpl implements FieldService, SubscribeHandle {
    public static Logger logger = LoggerFactory.getLogger(FieldServiceImpl.class);

    @Autowired
    private ModelDal modelDal;
    @Autowired
    private FieldDal fieldDal;
    @Autowired
    private CacheService cacheService;

    public Map<Long, List<FieldVO>> contextMap = new HashMap<Long, List<FieldVO>>();

    @Override
    public List<FieldVO> listField(Long modelId) {
        List<FieldVO> list = null;// cacheService.listFields(modelId);
        list = contextMap.get(modelId);
        if (list == null || list.size() == 0) {
            list = modelDal.listField(modelId);
            contextMap.put(modelId, list);
            // for (FieldVO vo: list)
            // cacheService.saveField(vo);
        }
        return list;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("field sub: {},{}", channel, message);
        FieldVO field = JSON.parseObject(message, FieldVO.class);
        List<FieldVO> list = modelDal.listField(field.getModelId());
        contextMap.put(field.getModelId(), list);
    }

    @Override
    public FieldVO get(Long id) {
        return fieldDal.get(id);
    }

    @Override
    public CommonResult query(FieldQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", fieldDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(FieldVO field) {
        CommonResult result = new CommonResult();
        if (field.getId() == null) {
            FieldQuery query = new FieldQuery();
            query.setModelId(field.getModelId());
            query.setFieldName(field.getFieldName());
            PageResult<FieldVO> page = fieldDal.query(query);
            if (page != null && page.getRowCount() > 0) {
                result.setMsg("字段名已存在");
                return result;
            }
        }
        int count = fieldDal.save(field);
        if (count > 0) {
        	if(StringUtils.isEmpty(field.getFieldName())){
        		field.setFieldName("field_"+field.getId());
        		fieldDal.save(field);
        	}
        	
            result.getData().put("id", field.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishField(field);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        FieldVO field = fieldDal.get(id[0]);
        ModelVO model = modelDal.getModelById(field.getModelId());
        if(model.getEntityName().equals(field.getFieldName())
        		||model.getEntryName().equals(field.getFieldName())
        		||model.getReferenceDate().equals(field.getFieldName())){
        	result.setMsg("模型必备字段不能删除！");
        	return result;
        }
        int count = fieldDal.delete(id);
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishField(field);
        }
        return result;
    }
    
    @PostConstruct
    public void init() {
        cacheService.subscribeField(this);
    }
}
