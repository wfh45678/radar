package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.AbstractionQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.AbstractionMapping;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.AbstractionService;
import com.pgmmers.radar.common.util.GroovyScriptUtil;
import com.pgmmers.radar.model.vo.model.AbstractionVO;
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
public class AbstractionServiceImpl extends BaseLocalCacheService implements AbstractionService,
        SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(AbstractionServiceImpl.class);

    @Override
    public Object query(Long modelId) {
        // 原 ModelDal.listAbstraction(modelId, null) 逻辑下沉
        List<AbstractionPO> list = abstractionMapper.selectList(
                new LambdaQueryWrapper<AbstractionPO>().eq(AbstractionPO::getModelId, modelId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<AbstractionVO>();
        }
        return abstractionMapping.sourceToTarget(list);
    }

    @Autowired
    private AbstractionMapper abstractionMapper;
    @Resource
    private AbstractionMapping abstractionMapping;
    @Autowired
    private CacheService cacheService;

    @Override
    public List<AbstractionVO> listAbstraction(Long modelId) {
        //noinspection unchecked
        return (List<AbstractionVO>) getByCache(modelId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("abstraction sub:{}", message);
        AbstractionVO abstraction = JSON.parseObject(message, AbstractionVO.class);
        if (abstraction != null) {
            invalidateCache(abstraction.getModelId());
        }

    }

    @Override
    public AbstractionVO get(Long id) {
        AbstractionPO abstraction = abstractionMapper.selectById(id);
        if (abstraction != null) {
            return abstractionMapping.sourceToTarget(abstraction);
        }
        return null;
    }

    @Override
    public CommonResult list(Long modelId) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("list", listAbstraction(modelId));
        return result;
    }

    @Override
    public CommonResult query(AbstractionQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 AbstractionDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<AbstractionVO> queryPage(AbstractionQuery query) {
        Page<AbstractionPO> p = abstractionMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<AbstractionPO>()
                        .eq(AbstractionPO::getModelId, query.getModelId())
                        .eq(query.getAggregateType() != null,
                                AbstractionPO::getAggregateType, query.getAggregateType())
                        .like(!StringUtils.isEmpty(query.getName()),
                                AbstractionPO::getName, query.getName())
                        .eq(query.getStatus() != null,
                                AbstractionPO::getStatus, query.getStatus()));
        List<AbstractionVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : abstractionMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(AbstractionVO abstraction) {
        CommonResult result = new CommonResult();
        if (abstraction.getId() != null) {
            AbstractionVO oldAbs = get(abstraction.getId());
            // 如果规则有更新，以前的编译好的groovy 对象用不到了，应该删除。
            if (!oldAbs.getRuleScript().equals(abstraction.getRuleScript())) {
                GroovyScriptUtil.removeInactiveScript(oldAbs.getRuleScript());
            }
        }
        int count = saveAbstraction(abstraction);
        if (count > 0) {
            if (StringUtils.isEmpty(abstraction.getName())) {
                abstraction.setName("abstraction_" + abstraction.getId());
                saveAbstraction(abstraction);
            }

            result.getData().put("id", abstraction.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishAbstraction(abstraction);
        }
        return result;
    }

    /**
     * 原 AbstractionDalImpl.save 逻辑下沉。
     */
    private int saveAbstraction(AbstractionVO abstraction) {
        AbstractionPO abstractionPO = abstractionMapping.targetToSource(abstraction);
        Date sysDate = new Date();
        int count = 0;
        if (abstraction.getId() == null) {
            abstractionPO.setCreateTime(sysDate);
            abstractionPO.setUpdateTime(sysDate);
            count = abstractionMapper.insert(abstractionPO);
            abstraction.setId(abstractionPO.getId());// 返回id
        } else {
            abstractionPO.setUpdateTime(sysDate);
            count = abstractionMapper.updateById(abstractionPO);
        }
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        AbstractionVO abs = get(id[0]);
        int count = abstractionMapper.delete(
                new LambdaQueryWrapper<AbstractionPO>().in(AbstractionPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
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
