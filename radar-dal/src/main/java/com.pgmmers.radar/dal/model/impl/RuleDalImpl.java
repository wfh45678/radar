package com.pgmmers.radar.dal.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.RuleHistoryQuery;
import com.pgmmers.radar.dal.bean.RuleQuery;
import com.pgmmers.radar.dal.model.RuleDal;
import com.pgmmers.radar.dal.util.POVOUtils;
import com.pgmmers.radar.mapper.RuleHistoryMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.RuleHistoryPO;
import com.pgmmers.radar.model.RulePO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.RuleHistoryVO;
import com.pgmmers.radar.vo.model.RuleVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import tk.mybatis.mapper.entity.Example;


@Service
public class RuleDalImpl implements RuleDal {

    public static Logger logger = LoggerFactory.getLogger(RuleDalImpl.class);

    @Autowired
    private RuleMapper ruleMapper;
    
    @Autowired
    private RuleHistoryMapper ruleHistoryMapper;

    @Override
    public RuleVO get(Long id) {
        RulePO rule = ruleMapper.selectByPrimaryKey(id);
        if (rule != null) {
            RuleVO ruleVO = POVOUtils.copyFromRulePO(rule);
            return ruleVO;
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



        criteria.andEqualTo("activationId",query.getActivationId());
        if (!StringUtils.isEmpty(query.getName())) {
            criteria.andLike("name", BaseUtils.buildLike(query.getName()));
        }
        if (query.getStatus() != null) {
           criteria.andEqualTo("status", query.getStatus());
        }
        List<RulePO> list = ruleMapper.selectByExample(example);


        Page<RulePO> page = (Page<RulePO>) list;

        List<RuleVO> listVO = new ArrayList<RuleVO>();
        for (RulePO rulePO : page.getResult()) {
//            RuleVO ruleVO = new RuleVO();
//            BeanUtils.copyProperties(rulePO, ruleVO);
            RuleVO ruleVO = POVOUtils.copyFromRulePO(rulePO);
            listVO.add(ruleVO);
        }

        PageResult<RuleVO> pageResult = new PageResult<RuleVO>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(RuleVO rule) {
        RulePO rulePO = POVOUtils.copyFromRuleVO(rule);
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

        Example example = new Example(RuleHistoryVO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ruleId", query.getRuleId());
        List<RuleHistoryPO> list = ruleHistoryMapper.selectByExample(example);
        Page<RuleHistoryPO> page = (Page<RuleHistoryPO>) list;

        List<RuleHistoryVO> listVO = new ArrayList<RuleHistoryVO>();
        for (RuleHistoryPO ruleHistoryPO : page.getResult()) {
            RuleHistoryVO ruleHistoryVO = new RuleHistoryVO();
            BeanUtils.copyProperties(ruleHistoryPO, ruleHistoryVO);
            listVO.add(ruleHistoryVO);
        }

        PageResult<RuleHistoryVO> pageResult = new PageResult<RuleHistoryVO>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
	}

	@Override
	public int saveHistory(RuleHistoryVO ruleHistory) {
		RuleHistoryPO ruleHistoryPO = new RuleHistoryPO();
        BeanUtils.copyProperties(ruleHistory, ruleHistoryPO);
        Date sysDate = new Date();
        int count = 0;
        ruleHistoryPO.setUpdateTime(sysDate);
        count = ruleHistoryMapper.insertSelective(ruleHistoryPO);
        ruleHistory.setId(ruleHistoryPO.getId());// 返回id
        return count;
	}

}
