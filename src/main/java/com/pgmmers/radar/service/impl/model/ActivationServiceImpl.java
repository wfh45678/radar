package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.ActivationQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.ActivationMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.mapper.ActivationMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.ActivationPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.ActivationService;
import com.pgmmers.radar.model.vo.model.ActivationVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import com.pgmmers.radar.model.vo.model.RuleVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ActivationServiceImpl extends BaseLocalCacheService implements ActivationService, SubscribeHandle {

    @Override
    public Object query(Long modelId) {
        // 原 ModelDal.listActivation(modelId, null) 逻辑下沉
        List<ActivationPO> list = activationMapper.selectList(
                new LambdaQueryWrapper<ActivationPO>().eq(ActivationPO::getModelId, modelId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<ActivationVO>();
        }
        return activationMapping.sourceToTarget(list);
    }

    public static Logger logger = LoggerFactory.getLogger(ActivationServiceImpl.class);

    @Autowired
    private ActivationMapper activationMapper;
    @Resource
    private ActivationMapping activationMapping;

    @Autowired
    private RuleMapper ruleMapper;
    @Resource
    private RuleMapping ruleMapping;

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
        ActivationPO activation = activationMapper.selectById(id);
        if (activation != null) {
            return activationMapping.sourceToTarget(activation);
        }
        return null;
    }

    @Override
    public CommonResult query(ActivationQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 ActivationDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<ActivationVO> queryPage(ActivationQuery query) {
        Page<ActivationPO> p = activationMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<ActivationPO>()
                        .eq(ActivationPO::getModelId, query.getModelId())
                        .like(!StringUtils.isEmpty(query.getName()),
                                ActivationPO::getActivationName, query.getName())
                        .eq(query.getStatus() != null,
                                ActivationPO::getStatus, query.getStatus()));
        List<ActivationVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : activationMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(ActivationVO activation) {
        CommonResult result = new CommonResult();
        int count = saveActivation(activation);
        if (count > 0) {
        	if(StringUtils.isEmpty(activation.getActivationName())){
        		activation.setActivationName("activation_"+activation.getId());
        		saveActivation(activation);
        	}
            result.getData().put("id", activation.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishActivation(activation);
        }
        return result;
    }

    /**
     * 原 ActivationDalImpl.save 逻辑下沉。
     */
    private int saveActivation(ActivationVO activation) {
        ActivationPO activationPO = activationMapping.targetToSource(activation);
        Date sysDate = new Date();
        int count = 0;
        if (activation.getId() == null) {
            activationPO.setCreateTime(sysDate);
            activationPO.setUpdateTime(sysDate);
            count = activationMapper.insert(activationPO);
            activation.setId(activationPO.getId());// 返回id
        } else {
            activationPO.setUpdateTime(sysDate);
            count = activationMapper.updateById(activationPO);
        }
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        ActivationVO act = get(id[0]);
        int count = activationMapper.delete(
                new LambdaQueryWrapper<ActivationPO>().in(ActivationPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
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
		ActivationVO activation = get(activationId);
		activation.setRuleOrder(ruleOrder);
		int count = saveActivation(activation);
		if(count > 0){
			result.setSuccess(true);
			// 通知更新
            cacheService.publishActivation(activation);
		}
		return result;
	}

    @Override
    public CommonResult copy(Long id) {
        CommonResult result = new CommonResult();
        ActivationVO origVO = get(id);
        if (origVO == null) {
            return result;
        }
        // 原 RuleDal.listByActId 逻辑下沉
        List<RulePO> rulePoList = ruleMapper.selectList(
                new LambdaQueryWrapper<RulePO>().eq(RulePO::getActivationId, id));
        List<RuleVO> ruleList = ruleMapping.sourceToTarget(rulePoList);
        origVO.setId(null);
        origVO.setLabel(origVO.getLabel() + "_copy");
        origVO.setCreateTime(new Date());
        saveActivation(origVO);
        for (RuleVO ruleVO : ruleList) {
            ruleVO.setId(null);
            ruleVO.setActivationId(origVO.getId());
            ruleVO.setCreateTime(new Date());
            saveRule(ruleVO);
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * 原 RuleDal.save 逻辑下沉（复制规则时使用）。
     */
    private int saveRule(RuleVO rule) {
        RulePO rulePO = ruleMapping.targetToSource(rule);
        Date sysDate = new Date();
        int count = 0;
        if (rule.getId() == null) {
            rulePO.setCreateTime(sysDate);
            rulePO.setUpdateTime(sysDate);
            count = ruleMapper.insert(rulePO);
            rule.setId(rulePO.getId());// 返回id
        } else {
            rulePO.setUpdateTime(sysDate);
            count = ruleMapper.updateById(rulePO);
        }
        return count;
    }

    @Override
    public CommonResult updateStatus(Long activationId, Integer status) {
        CommonResult result = new CommonResult();
        ActivationVO activation = get(activationId);
        if (activation == null) return result;
        activation = new ActivationVO();
        activation.setId(activationId);
        activation.setStatus(status);
        saveActivation(activation);
        result.setSuccess(true);
        return result;
    }

    @PostConstruct
    public void init() {
        cacheService.subscribeActivation(this);
    }

}
