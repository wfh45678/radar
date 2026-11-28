package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.converter.FieldMapping;
import com.pgmmers.radar.converter.ScenarioMapping;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ScenarioMapper;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ScenarioPO;
import com.pgmmers.radar.model.query.FieldQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.FieldService;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class FieldServiceImpl extends BaseLocalCacheService implements FieldService, SubscribeHandle  {
    public static Logger logger = LoggerFactory.getLogger(FieldServiceImpl.class);

    @Autowired
    private ScenarioMapper scenarioMapper;
    @Autowired
    private FieldMapper fieldMapper;
    @Resource
    private ScenarioMapping modelMapping;
    @Resource
    private FieldMapping fieldMapping;
    private final CacheService cacheService;
    public FieldServiceImpl(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Object query(Long scenarioId) {
        List<FieldPO> list = fieldMapper.selectList(
                new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getModelId, scenarioId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return fieldMapping.sourceToTarget(list);
    }

    @Override
    public List<FieldVO> listField(Long scenarioId) {
        //noinspection unchecked
        return (List<FieldVO>) getByCache(scenarioId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("field sub: {},{}", channel, message);
        FieldVO field = JSON.parseObject(message, FieldVO.class);
        invalidateCache(field.getModelId());
    }

    @Override
    public FieldVO get(Long id) {
        FieldPO field = fieldMapper.selectById(id);
        if (field != null) {
            return fieldMapping.sourceToTarget(field);
        }
        return null;
    }

    /**
     * 原 FieldDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<FieldVO> queryPage(FieldQuery query) {
        Page<FieldPO> p = fieldMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<FieldPO>()
                        .eq(FieldPO::getModelId, query.getModelId())
                        .like(!StringUtils.isEmpty(query.getFieldName()),
                                FieldPO::getFieldName, query.getFieldName())
                        .eq(!StringUtils.isEmpty(query.getFieldType()),
                                FieldPO::getFieldType, query.getFieldType()));
        List<FieldVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : fieldMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult query(FieldQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    @Override
    public CommonResult save(FieldVO field) {
        CommonResult result = new CommonResult();
        if (field.getId() == null) {
            FieldQuery query = new FieldQuery();
            query.setModelId(field.getModelId());
            query.setFieldName(field.getFieldName());
            PageResult<FieldVO> page = queryPage(query);
            if (page != null && page.getRowCount() > 0) {
                result.setMsg("字段名已存在");
                return result;
            }
        }
        int count = saveField(field);
        if (count > 0) {
        	if(StringUtils.isEmpty(field.getFieldName())){
        		field.setFieldName("field_"+field.getId());
        		saveField(field);
        	}
            result.getData().put("id", field.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishField(field);
        }
        return result;
    }

    /**
     * 原 FieldDalImpl.save 逻辑下沉。
     */
    private int saveField(FieldVO field) {
        FieldPO fieldPO = fieldMapping.targetToSource(field);
        Date sysDate = new Date();
        int count = 0;
        if (field.getId() == null) {
            fieldPO.setCreateTime(sysDate);
            fieldPO.setUpdateTime(sysDate);
            count = fieldMapper.insert(fieldPO);
            field.setId(fieldPO.getId());// 返回id
        } else {
            fieldPO.setUpdateTime(sysDate);
            count = fieldMapper.updateById(fieldPO);
        }
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        FieldVO field = get(id[0]);
        ScenarioPO modelPO = scenarioMapper.selectById(field.getModelId());
        ScenarioVO model = modelMapping.sourceToTarget(modelPO);
        if(model.getEntityName().equals(field.getFieldName())
        		||model.getEntryName().equals(field.getFieldName())
        		||model.getReferenceDate().equals(field.getFieldName())){
        	result.setMsg("模型必备字段不能删除！");
        	return result;
        }
        int count = fieldMapper.delete(
                new LambdaQueryWrapper<FieldPO>().in(FieldPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
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
