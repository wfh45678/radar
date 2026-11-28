package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.StrategyQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.model.query.RuleHistoryQuery;
import com.pgmmers.radar.model.query.RuleQuery;
import com.pgmmers.radar.converter.StrategyMapping;
import com.pgmmers.radar.converter.ScenarioMapping;
import com.pgmmers.radar.converter.RuleHistoryMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.ScenarioMapper;
import com.pgmmers.radar.mapper.RuleHistoryMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.ScenarioPO;
import com.pgmmers.radar.model.entity.RuleHistoryPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.common.util.DateUtils;
import com.pgmmers.radar.common.util.GroovyScriptUtil;
import com.pgmmers.radar.model.vo.model.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.text.ParseException;
import java.util.*;

@Service
public class RuleServiceImpl extends BaseLocalCacheService implements RuleService, SubscribeHandle {

    public static Logger logger = LoggerFactory
            .getLogger(RuleServiceImpl.class);

    @Override
    public Object query(Long strategyId) {
        // 原 ModelDal.listRules(null, strategyId, null) 逻辑下沉
        return listRules(null, strategyId, null);
    }

    @Autowired
    private RuleMapper ruleMapper;
    @Autowired
    private RuleHistoryMapper ruleHistoryMapper;
    @Autowired
    private StrategyMapper strategyMapper;
    @Autowired
    private ScenarioMapper scenarioMapper;
    @Resource
    private RuleMapping ruleMapping;
    @Resource
    private RuleHistoryMapping ruleHistoryMapping;
    @Resource
    private StrategyMapping strategyMapping;
    @Resource
    private ScenarioMapping modelMapping;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SearchEngineService searchService;

    public Map<Long, List<RuleVO>> contextMap = new HashMap<>();

    /**
     * 原 ModelDalImpl.listRules 逻辑下沉。
     */
    private List<RuleVO> listRules(Long scenarioId, Long strategyId, Integer status) {
        List<RulePO> rulePoList = ruleMapper.selectList(
                new LambdaQueryWrapper<RulePO>()
                        .eq(scenarioId != null, RulePO::getScenarioId, scenarioId)
                        .eq(strategyId != null, RulePO::getStrategyId, strategyId)
                        .eq(status != null, RulePO::getStatus, status));
        if (CollectionUtils.isEmpty(rulePoList)) {
            return new ArrayList<>();
        }
        return ruleMapping.sourceToTarget(rulePoList);
    }

    public List<RuleVO> listRule(Long scenarioId, Long strategyId, Integer status) {
        return listRules(scenarioId, strategyId, status);
    }

    @Override
    public List<RuleVO> listRule(Long strategyId) {
        //noinspection unchecked
        return (List<RuleVO>) getByCache(strategyId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("rule sub:{}", message);
        try {
            // v5.1 修复：只取失效所需的 strategyId（RuleVO 含 JsonNode 字段，fastjson2 反序列化崩溃）
            com.alibaba.fastjson2.JSONObject obj = JSON.parseObject(message);
            if (obj != null) {
                invalidateCache(obj.getLong("strategyId"));
            }
        } catch (Exception e) {
            logger.warn("rule sub message handle failed: {}", e.getMessage());
        }
    }

    @Override
    public RuleVO get(Long id) {
        RulePO rule = ruleMapper.selectById(id);
        if (rule != null) {
            return ruleMapping.sourceToTarget(rule);
        }
        return null;
    }

    /**
     * 原 StrategyDalImpl.get 逻辑下沉。
     */
    private StrategyVO getStrategy(Long id) {
        StrategyPO strategy = strategyMapper.selectById(id);
        if (strategy != null) {
            return strategyMapping.sourceToTarget(strategy);
        }
        return null;
    }

    @Override
    public CommonResult query(RuleQuery query) {
        CommonResult result = new CommonResult();
        StrategyVO strategy = getStrategy(query.getStrategyId());

        result.setSuccess(true);
        result.getData().put("ruleOrder", strategy.getRuleOrder());
        result.getData().put("page", queryRulePage(query));
        return result;
    }

    /**
     * 原 RuleDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<RuleVO> queryRulePage(RuleQuery query) {
        Page<RulePO> p = ruleMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<RulePO>()
                        .eq(query.getStrategyId() != null,
                                RulePO::getStrategyId, query.getStrategyId())
                        .like(!StringUtils.isEmpty(query.getName()),
                                RulePO::getName, query.getName())
                        .eq(query.getStatus() != null,
                                RulePO::getStatus, query.getStatus()));
        List<RuleVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : ruleMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(RuleVO rule,String userCode) {
        CommonResult result = new CommonResult();
        if (rule.getId() != null) {
            RuleVO oldRule = get(rule.getId());
            // 如果规则有更新，以前的编译好的groovy 对象用不到了，应该删除。
            if (!oldRule.getScripts().equals(rule.getScripts())) {
                GroovyScriptUtil.removeInactiveScript(oldRule.getScripts());
            }
        }
        int count = saveRule(rule);
        if (count > 0) {
        	if(StringUtils.isEmpty(rule.getName())){
        		rule.setName("rule_"+rule.getId());
        		saveRule(rule);
        	}
            result.getData().put("id", rule.getId());
            result.setSuccess(true);

            // 存储History
            RuleHistoryVO ruleHistoryVO=new RuleHistoryVO();
            ruleHistoryVO.setRuleId(rule.getId());
            ruleHistoryVO.setUserCode(userCode);
            ruleHistoryVO.setLabel(rule.getLabel());
            ruleHistoryVO.setInitScore(rule.getInitScore());
            ruleHistoryVO.setBaseNum(rule.getBaseNum());
            ruleHistoryVO.setOperator(rule.getOperator());
            ruleHistoryVO.setFeatureName(rule.getFeatureName());
            ruleHistoryVO.setRate(rule.getRate());
            ruleHistoryVO.setRuleDefinition(rule.getRuleDefinition().asText());
            ruleHistoryVO.setUpdateTime(rule.getUpdateTime());
            saveRuleHistory(ruleHistoryVO);

            // 通知更新
            cacheService.publishRule(rule);
        }
        return result;
    }

    /**
     * 原 RuleDalImpl.save 逻辑下沉。
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

    /**
     * 原 RuleDalImpl.saveHistory 逻辑下沉。
     */
    private int saveRuleHistory(RuleHistoryVO ruleHistory) {
        RuleHistoryPO ruleHistoryPO = ruleHistoryMapping.targetToSource(ruleHistory);
        Date sysDate = new Date();
        ruleHistoryPO.setUpdateTime(sysDate);
        int count = ruleHistoryMapper.insert(ruleHistoryPO);
        ruleHistory.setId(ruleHistoryPO.getId());// 返回id
        return count;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        RuleVO rule = get(id[0]);
        int count = ruleMapper.delete(
                new LambdaQueryWrapper<RulePO>().in(RulePO::getId, Arrays.asList(id)));
        // TODO 删除关联子表
        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishRule(rule);
        }
        return result;
    }

    @PostConstruct
    public void init() {
        cacheService.subscribeRule(this);
    }

    @Override
    public CommonResult getHitSorts(Long scenarioId) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH , -1);
        return getHitSorts(scenarioId, now.getTimeInMillis(), Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public CommonResult getHitSorts(Long scenarioId, Long beginTime, Long endTime) {
        CommonResult result = new CommonResult();
        Set<RuleHitsVO> treeSet = new TreeSet<>();
        // 原 ModelDal.getScenarioById 逻辑下沉
        ScenarioPO modelPO = scenarioMapper.selectById(scenarioId);
        ScenarioVO model = modelPO == null ? null : modelMapping.sourceToTarget(modelPO);
        String keyTempl = "hitsDetail.${strategyName}.rule_${ruleId}.key";
        StrategyQuery actQuery = new StrategyQuery();
        actQuery.setScenarioId(scenarioId);
        // page size default is 10.
        actQuery.setPageSize(100);
        // 原 StrategyDalImpl.query 逻辑下沉
        Page<StrategyPO> actPage = strategyMapper.selectPage(
                new Page<>(actQuery.getPageNo(), actQuery.getPageSize()),
                new LambdaQueryWrapper<StrategyPO>()
                        .eq(StrategyPO::getScenarioId, actQuery.getScenarioId())
                        .like(!StringUtils.isEmpty(actQuery.getName()),
                                StrategyPO::getStrategyName, actQuery.getName())
                        .eq(actQuery.getStatus() != null,
                                StrategyPO::getStatus, actQuery.getStatus()));
        List<StrategyVO> actList = strategyMapping.sourceToTarget(actPage.getRecords());
        for (StrategyVO act : actList) {
            RuleQuery query = new RuleQuery();
            query.setStrategyId(act.getId());
            query.setPageSize(100);
            PageResult<RuleVO> page = queryRulePage(query);
            List<RuleVO> list = page.getList();
            for (RuleVO rule : list) {
                String keyStr = keyTempl.replace("${strategyName}", act.getStrategyName());

                keyStr = keyStr.replace("${ruleId}", rule.getId() + "");
                long qty = 0;
                try {
                    QueryBuilder filter = QueryBuilders
                            .rangeQuery("fields." + model.getReferenceDate())
                            .from(beginTime).to(endTime);
                    qty = searchService.count(model.getGuid().toLowerCase(),
                            QueryBuilders.termQuery(keyStr,rule.getId() + ""), filter);
                } catch (Exception e) {
                    logger.error("search error", e);
                }
                if (qty > 0){
                    RuleHitsVO hit = new RuleHitsVO();
                    hit.setId(rule.getId());
                    hit.setStrategyName(act.getStrategyName());
                    hit.setRuleLable(rule.getLabel());
                    hit.setStrategyLable(act.getLabel());
                    hit.setCount(qty);
                    treeSet.add(hit);
                }
            }
        }

        result.setSuccess(true);
        result.getData().put("hits", treeSet);
        return result;
    }

    @Override
    public CommonResult getHitSorts(Long scenarioId, String beginTime, String endTime) {
        CommonResult result = new CommonResult();
        try {
            logger.info("search hits:{},{}", beginTime, endTime);
            long begin = DateUtils.parseDateTime(beginTime).getTime();
            long end = DateUtils.parseDateTime(endTime).getTime();
            if (end > begin) {
                return getHitSorts(scenarioId, begin, end);
            }
        } catch (ParseException e) {
            logger.error("时间格式错误", e);
            result.setMsg("时间格式错误！");
        }
        return result;
    }

    @Override
	public CommonResult queryHistory(RuleHistoryQuery query) {
		CommonResult result = new CommonResult();

        result.setSuccess(true);
        result.getData().put("page", queryHistoryPage(query));
        return result;
	}

    /**
     * 原 RuleDalImpl.queryHistory 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<RuleHistoryVO> queryHistoryPage(RuleHistoryQuery query) {
        Page<RuleHistoryPO> p = ruleHistoryMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<RuleHistoryPO>()
                        .eq(RuleHistoryPO::getRuleId, query.getRuleId()));
        List<RuleHistoryVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : ruleHistoryMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public List<RuleVO> listRuleByScenarioId(Long scenarioId) {
        return listRules(scenarioId, null, null);
    }
}
