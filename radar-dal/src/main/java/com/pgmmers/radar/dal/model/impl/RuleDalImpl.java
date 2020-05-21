package com.pgmmers.radar.dal.model.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.dal.model.RuleDal;
import com.pgmmers.radar.mapper.RuleHistoryMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.mapstruct.RuleHistoryMapping;
import com.pgmmers.radar.mapstruct.RuleMapping;
import com.pgmmers.radar.model.RuleHistoryPO;
import com.pgmmers.radar.model.RulePO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.RuleHistoryVO;
import com.pgmmers.radar.vo.model.RuleVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;


@Service
public class RuleDalImpl implements RuleDal {

    public static Logger logger = LoggerFactory.getLogger(RuleDalImpl.class);

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private RuleHistoryMapper ruleHistoryMapper;
    @Resource
    private RuleHistoryMapping ruleHistoryMapping;
    @Resource
    private RuleMapping ruleMapping;

    @Override
    public RuleVO get(Long id) {
        RulePO rule = ruleMapper.selectByPrimaryKey(id);
        if (rule != null) {
            return ruleMapping.sourceToTarget(rule);
        }
        return null;
    }

    @Override
    public PageResult<RuleVO> query(RuleQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(RulePO.class);
        Example.Criteria criteria = example.createCriteria();
        RulePO rule = new RulePO();
        rule.setActivationId(query.getActivationId());
        rule.setStatus(query.getStatus());
        rule.setName(query.getName());

        criteria.andEqualTo("activationId", query.getActivationId());
        if (!StringUtils.isEmpty(query.getName())) {
            criteria.andLike("name", BaseUtils.buildLike(query.getName()));
        }
        if (query.getStatus() != null) {
            criteria.andEqualTo("status", query.getStatus());
        }
        List<RulePO> list = ruleMapper.selectByExample(example);

        Page<RulePO> page = (Page<RulePO>) list;

        List<RuleVO> listVO;
        if (CollectionUtils.isEmpty(list)) {
            listVO = new ArrayList<>();
        } else {
            listVO = ruleMapping.sourceToTarget(list);
        }

        PageResult<RuleVO> pageResult = new PageResult<>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(RuleVO rule) {
        RulePO rulePO = ruleMapping.targetToSource(rule);
        Date sysDate = new Date();
        int count = 0;
        if (rule.getId() == null) {
            rulePO.setCreateTime(sysDate);
            rulePO.setUpdateTime(sysDate);
            count = ruleMapper.insertSelective(rulePO);
            rule.setId(rulePO.getId());// 返回id
        } else {
            rulePO.setUpdateTime(sysDate);
            count = ruleMapper.updateByPrimaryKeySelective(rulePO);
        }
        return count;
    }

    @Override
    public int delete(Long[] id) {
        Example example = new Example(RulePO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = ruleMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

    @Override
    public PageResult<RuleHistoryVO> queryHistory(RuleHistoryQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(RuleHistoryPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ruleId", query.getRuleId());
        List<RuleHistoryPO> list = ruleHistoryMapper.selectByExample(example);
        Page<RuleHistoryPO> page = (Page<RuleHistoryPO>) list;

        List<RuleHistoryVO> listVO;
        if (CollectionUtils.isEmpty(list)) {
            listVO = new ArrayList<>();
        } else {
            listVO = ruleHistoryMapping.sourceToTarget(list);
        }
        PageResult<RuleHistoryVO> pageResult = new PageResult<RuleHistoryVO>(page.getPageNum(),
                page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int saveHistory(RuleHistoryVO ruleHistory) {
        RuleHistoryPO ruleHistoryPO = ruleHistoryMapping.targetToSource(ruleHistory);
        Date sysDate = new Date();
        int count = 0;
        ruleHistoryPO.setUpdateTime(sysDate);
        count = ruleHistoryMapper.insertSelective(ruleHistoryPO);
        ruleHistory.setId(ruleHistoryPO.getId());// 返回id
        return count;
    }

}
