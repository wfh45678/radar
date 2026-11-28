package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.common.util.JsonUtils;
import com.pgmmers.radar.model.query.StrategyQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.StrategyMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.mapper.StrategyHistoryMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.StrategyHistoryPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.StrategyService;
import com.pgmmers.radar.model.vo.model.StrategyHistoryVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;

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
public class StrategyServiceImpl extends BaseLocalCacheService implements StrategyService, SubscribeHandle {

    @Override
    public Object query(Long scenarioId) {
        // 原 ModelDal.listStrategy(scenarioId, null) 逻辑下沉
        List<StrategyPO> list = strategyMapper.selectList(
                new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getModelId, scenarioId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<StrategyVO>();
        }
        return strategyMapping.sourceToTarget(list);
    }

    public static Logger logger = LoggerFactory.getLogger(StrategyServiceImpl.class);

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private StrategyHistoryMapper strategyHistoryMapper;

    @Resource
    private StrategyMapping strategyMapping;

    @Autowired
    private RuleMapper ruleMapper;
    @Resource
    private RuleMapping ruleMapping;

    @Autowired
    private CacheService cacheService;

    @Override
    public List<StrategyVO> listStrategy(Long scenarioId) {
        //noinspection unchecked
        return (List<StrategyVO>) getByCache(scenarioId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("strategy sub:{}", message);
        StrategyVO act = JSON.parseObject(message, StrategyVO.class);
        if (act != null) {
            invalidateCache(act.getModelId());
        }
    }

    @Override
    public StrategyVO get(Long id) {
        StrategyPO strategy = strategyMapper.selectById(id);
        if (strategy != null) {
            return strategyMapping.sourceToTarget(strategy);
        }
        return null;
    }

    @Override
    public CommonResult query(StrategyQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 StrategyDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<StrategyVO> queryPage(StrategyQuery query) {
        Page<StrategyPO> p = strategyMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<StrategyPO>()
                        .eq(StrategyPO::getModelId, query.getModelId())
                        .like(!StringUtils.isEmpty(query.getName()),
                                StrategyPO::getStrategyName, query.getName())
                        .eq(query.getStatus() != null,
                                StrategyPO::getStatus, query.getStatus()));
        List<StrategyVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : strategyMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(StrategyVO strategy, String operator) {
        CommonResult result = new CommonResult();
        int count = saveStrategy(strategy);
        if (count > 0) {
        	if(StringUtils.isEmpty(strategy.getStrategyName())){
        		strategy.setStrategyName("strategy_"+strategy.getId());
        		saveStrategy(strategy);
        	}
            // 类型化定义快照（v4.0）：评分卡/决策树每次保存记历史，规则集沿用 rule_history
            saveDefinitionHistory(strategy, operator);
            result.getData().put("id", strategy.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishStrategy(strategy);
        }
        return result;
    }

    /** 评分卡/决策树定义快照；失败不阻断保存（审计旁路） */
    private void saveDefinitionHistory(StrategyVO strategy, String operator) {
        if (strategy.getDefinition() == null || strategy.getDefinition().isNull()
                || strategy.getDefinition().isEmpty()) {
            return;
        }
        try {
            StrategyHistoryPO history = new StrategyHistoryPO();
            history.setStrategyId(strategy.getId());
            history.setMerchantCode(operator);
            history.setType(strategy.getType());
            history.setDefinition(JsonUtils.jsonNodeToString(strategy.getDefinition()));
            history.setUpdateTime(new Date());
            strategyHistoryMapper.insert(history);
        } catch (Exception e) {
            logger.warn("strategy history snapshot failed, strategyId={}", strategy.getId(), e);
        }
    }

    @Override
    public List<StrategyHistoryVO> listHistory(Long strategyId) {
        List<StrategyHistoryPO> list = strategyHistoryMapper.selectList(
                new LambdaQueryWrapper<StrategyHistoryPO>()
                        .eq(StrategyHistoryPO::getStrategyId, strategyId)
                        .orderByDesc(StrategyHistoryPO::getUpdateTime)
                        .last("LIMIT 50"));
        List<StrategyHistoryVO> vos = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return vos;
        }
        for (StrategyHistoryPO po : list) {
            StrategyHistoryVO vo = new StrategyHistoryVO();
            vo.setId(po.getId());
            vo.setStrategyId(po.getStrategyId());
            vo.setMerchantCode(po.getMerchantCode());
            vo.setType(po.getType());
            vo.setDefinition(JsonUtils.getJsonNode(po.getDefinition()));
            vo.setUpdateTime(po.getUpdateTime());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 原 StrategyDalImpl.save 逻辑下沉。
     */
    private int saveStrategy(StrategyVO strategy) {
        StrategyPO strategyPO = strategyMapping.targetToSource(strategy);
        Date sysDate = new Date();
        int count = 0;
        if (strategy.getId() == null) {
            strategyPO.setCreateTime(sysDate);
            strategyPO.setUpdateTime(sysDate);
            count = strategyMapper.insert(strategyPO);
            strategy.setId(strategyPO.getId());// 返回id
        } else {
            strategyPO.setUpdateTime(sysDate);
            count = strategyMapper.updateById(strategyPO);
        }
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        StrategyVO act = get(id[0]);
        int count = strategyMapper.delete(
                new LambdaQueryWrapper<StrategyPO>().in(StrategyPO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishStrategy(act);
        }
        return result;
    }

    @Override
	public CommonResult updateOrder(Long strategyId, String ruleOrder) {
    	CommonResult result = new CommonResult();
		StrategyVO strategy = get(strategyId);
		strategy.setRuleOrder(ruleOrder);
		int count = saveStrategy(strategy);
		if(count > 0){
			result.setSuccess(true);
			// 通知更新
            cacheService.publishStrategy(strategy);
		}
		return result;
	}

    @Override
    public CommonResult copy(Long id) {
        CommonResult result = new CommonResult();
        StrategyVO origVO = get(id);
        if (origVO == null) {
            return result;
        }
        // 原 RuleDal.listByActId 逻辑下沉
        List<RulePO> rulePoList = ruleMapper.selectList(
                new LambdaQueryWrapper<RulePO>().eq(RulePO::getStrategyId, id));
        List<RuleVO> ruleList = ruleMapping.sourceToTarget(rulePoList);
        origVO.setId(null);
        origVO.setLabel(origVO.getLabel() + "_copy");
        origVO.setCreateTime(new Date());
        saveStrategy(origVO);
        for (RuleVO ruleVO : ruleList) {
            ruleVO.setId(null);
            ruleVO.setStrategyId(origVO.getId());
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
    public CommonResult updateStatus(Long strategyId, Integer status) {
        CommonResult result = new CommonResult();
        StrategyVO strategy = get(strategyId);
        if (strategy == null) return result;
        strategy = new StrategyVO();
        strategy.setId(strategyId);
        strategy.setStatus(status);
        saveStrategy(strategy);
        result.setSuccess(true);
        return result;
    }

    @PostConstruct
    public void init() {
        cacheService.subscribeStrategy(this);
    }

}
