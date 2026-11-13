package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.model.query.PreItemQuery;
import com.pgmmers.radar.converter.PreItemMapping;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.PreItemService;
import com.pgmmers.radar.model.vo.model.PreItemVO;
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
public class PreItemServiceImpl extends BaseLocalCacheService implements PreItemService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(FieldServiceImpl.class);


    @Override
    public Object query(Long modelId) {
        // 原 ModelDal.listPreItem(modelId, null) 逻辑下沉
        List<PreItemPO> list = preItemMapper.selectList(
                new LambdaQueryWrapper<PreItemPO>().eq(PreItemPO::getModelId, modelId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<PreItemVO>();
        }
        return preItemMapping.sourceToTarget(list);
    }

    @Autowired
    private PreItemMapper preItemMapper;
    @Resource
    private PreItemMapping preItemMapping;
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
        PreItemPO preItem = preItemMapper.selectById(id);
        if (preItem != null) {
            return preItemMapping.sourceToTarget(preItem);
        }
        return null;
    }

    @Override
    public CommonResult query(PreItemQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 PreItemDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<PreItemVO> queryPage(PreItemQuery query) {
        Page<PreItemPO> p = preItemMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<PreItemPO>()
                        .eq(PreItemPO::getModelId, query.getModelId())
                        .eq(!StringUtils.isEmpty(query.getPlugin()),
                                PreItemPO::getPlugin, query.getPlugin())
                        .eq(query.getStatus() != null,
                                PreItemPO::getStatus, query.getStatus())
                        .eq(!StringUtils.isEmpty(query.getLabel()),
                                PreItemPO::getLabel, query.getLabel()));
        List<PreItemVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : preItemMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(PreItemVO preItem) {
        CommonResult result = new CommonResult();
        if (preItem.getId() == null) {
            PreItemQuery query = new PreItemQuery();
            query.setModelId(preItem.getModelId());
            query.setLabel(preItem.getLabel());
            PageResult<PreItemVO> page = queryPage(query);
            if (page != null && page.getRowCount() > 0) {
                result.setMsg("字段名已存在");
                return result;
            }
        }

        int count = savePreItem(preItem);
        if (count > 0) {
        	if(StringUtils.isEmpty(preItem.getDestField())){
        		preItem.setDestField("preItem_" + preItem.getId());
        		savePreItem(preItem);
        	}

            result.getData().put("id", preItem.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishPreItem(preItem);
        }
        return result;
    }

    /**
     * 原 PreItemDalImpl.save 逻辑下沉。
     */
    private int savePreItem(PreItemVO preItem) {
        PreItemPO preItemPO = preItemMapping.targetToSource(preItem);
        Date sysDate = new Date();
        int count = 0;
        if (preItemPO.getId() == null) {
            preItemPO.setCreateTime(sysDate);
            preItemPO.setUpdateTime(sysDate);
            count = preItemMapper.insert(preItemPO);
            preItem.setId(preItemPO.getId());
        } else {
            preItemPO.setUpdateTime(sysDate);
            count = preItemMapper.updateById(preItemPO);
        }
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        PreItemVO item = get(id[0]);
        int count = preItemMapper.delete(
                new LambdaQueryWrapper<PreItemPO>().in(PreItemPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
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
