package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;

import com.pgmmers.radar.dal.bean.ActivationQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.dal.model.ActivationDal;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.dal.model.RuleDal;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.RuleService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.util.GroovyScriptUtil;
import com.pgmmers.radar.vo.model.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class RuleServiceImpl extends BaseLocalCacheService implements RuleService, SubscribeHandle {

    public static Logger logger = LoggerFactory
            .getLogger(RuleServiceImpl.class);

    @Override
    public Object query(Long activationId) {
        return modelDal.listRules(null, activationId, null);
    }

    @Autowired
    private ModelDal modelDal;
    @Autowired
    private RuleDal ruleDal;
    @Autowired
    private ActivationDal activationDal;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SearchEngineService searchService;

    public Map<Long, List<RuleVO>> contextMap = new HashMap<>();

    public List<RuleVO> listRule(Long modelId, Long activationId, Integer status) {
        return modelDal.listRules(modelId, activationId, status);
    }

    @Override
    public List<RuleVO> listRule(Long activationId) {
        //noinspection unchecked
        return (List<RuleVO>) getByCache(activationId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("rule sub:{}", message);
        RuleVO rule = JSON.parseObject(message, RuleVO.class);
        if (rule != null) {
            invalidateCache(rule.getActivationId());
        }
    }

    @Override
    public RuleVO get(Long id) {
        return ruleDal.get(id);
    }

    @Override
    public CommonResult query(RuleQuery query) {
        CommonResult result = new CommonResult();
        ActivationVO activation = activationDal.get(query.getActivationId());

        result.setSuccess(true);
        result.getData().put("ruleOrder", activation.getRuleOrder());
        result.getData().put("page", ruleDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(RuleVO rule,String merchantCode) {
        CommonResult result = new CommonResult();
        if (rule.getId() != null) {
            RuleVO oldRule = ruleDal.get(rule.getId());
            // 如果规则有更新，以前的编译好的groovy 对象用不到了，应该删除。
            if (!oldRule.getScripts().equals(rule.getScripts())) {
                GroovyScriptUtil.removeInactiveScript(oldRule.getScripts());
            }
        }
        int count = ruleDal.save(rule);
        if (count > 0) {
        	if(StringUtils.isEmpty(rule.getName())){
        		rule.setName("rule_"+rule.getId());
        		ruleDal.save(rule);
        	}
            result.getData().put("id", rule.getId());
            result.setSuccess(true);

            // 存储History
            RuleHistoryVO ruleHistoryVO=new RuleHistoryVO();
            ruleHistoryVO.setRuleId(rule.getId());
            ruleHistoryVO.setMerchantCode(merchantCode);
            ruleHistoryVO.setLabel(rule.getLabel());
            ruleHistoryVO.setInitScore(rule.getInitScore());
            ruleHistoryVO.setBaseNum(rule.getBaseNum());
            ruleHistoryVO.setOperator(rule.getOperator());
            ruleHistoryVO.setAbstractionName(rule.getAbstractionName());
            ruleHistoryVO.setRate(rule.getRate());
            ruleHistoryVO.setRuleDefinition(rule.getRuleDefinition().asText());
            ruleHistoryVO.setUpdateTime(rule.getUpdateTime());
            ruleDal.saveHistory(ruleHistoryVO);

            // 通知更新
            cacheService.publishRule(rule);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        RuleVO rule = ruleDal.get(id[0]);
        int count = ruleDal.delete(id);
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
    public CommonResult getHitSorts(Long modelId) {
        CommonResult result = new CommonResult();
        Set<RuleHitsVO> treeSet = new TreeSet<>();
        ModelVO model = modelDal.getModelById(modelId);
        String keyTempl = "hitsDetail.${activationName}.rule_${ruleId}.key";
        ActivationQuery actQuery = new ActivationQuery();
        actQuery.setModelId(modelId);
        // page size default is 10.
        actQuery.setPageSize(100);
        PageResult<ActivationVO> actResult = activationDal.query(actQuery);
        List<ActivationVO> actList = actResult.getList();
        for (ActivationVO act : actList) {
            RuleQuery query = new RuleQuery();
            query.setActivationId(act.getId());
            query.setPageSize(100);
            PageResult<RuleVO> page = ruleDal.query(query);
            List<RuleVO> list = page.getList();
            for (RuleVO rule : list) {
                String keyStr = keyTempl.replace("${activationName}", act.getActivationName());

                keyStr = keyStr.replace("${ruleId}", rule.getId() + "");
                long qty = 0;
                try {
                    qty = searchService.count(model.getGuid().toLowerCase(),
                            QueryBuilders.termQuery(keyStr,rule.getId() + ""), null);
                } catch (Exception e) {
                    logger.error("search error", e);
                }
                if (qty > 0){
                    RuleHitsVO hit = new RuleHitsVO();
                    hit.setId(rule.getId());
                    hit.setActivationName(act.getActivationName());
                    hit.setRuleLable(rule.getLabel());
                    hit.setActivationLable(act.getLabel());
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
	public CommonResult queryHistory(RuleHistoryQuery query) {
		CommonResult result = new CommonResult();

        result.setSuccess(true);
        result.getData().put("page", ruleDal.queryHistory(query));
        return result;
	}

    @Override
    public List<RuleVO> listRuleByModelId(Long modelId) {
        return modelDal.listRules(modelId, null, null);
    }
}
