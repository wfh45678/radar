package com.pgmmers.radar.dal.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pgmmers.radar.dal.bean.AbstractionQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.AbstractionDal;
import com.pgmmers.radar.dal.util.POVOUtils;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.model.AbstractionPO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.AbstractionVO;
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
public class AbstractionDalImpl implements AbstractionDal {

    public static Logger logger = LoggerFactory.getLogger(AbstractionDalImpl.class);

    @Autowired
    private AbstractionMapper abstractionMapper;

    @Override
    public AbstractionVO get(Long id) {
        AbstractionPO abstraction = abstractionMapper.selectByPrimaryKey(id);
        if (abstraction != null) {
            AbstractionVO abstractionVO = new AbstractionVO();
            BeanUtils.copyProperties(abstraction, abstractionVO);
            return abstractionVO;
        }
        return null;
    }

    @Override
    public List<AbstractionVO> list(Long modelId) {
        Example example = new Example(AbstractionPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", modelId);
        List<AbstractionPO> list = abstractionMapper.selectByExample(example);

        List<AbstractionVO> listVO = new ArrayList<AbstractionVO>();
        for (AbstractionPO abstractionPO : list) {
            AbstractionVO abstractionVO = new AbstractionVO();
            BeanUtils.copyProperties(abstractionPO, abstractionVO);
            listVO.add(abstractionVO);
        }
        return listVO;
    }

    @Override
    public PageResult<AbstractionVO> query(AbstractionQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(AbstractionPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", query.getModelId());

        if (query.getAggregateType() != null) {
            criteria.andEqualTo("aggregateType", query.getAggregateType());
        }
        if (!StringUtils.isEmpty(query.getName())) {
            criteria.andLike("name", BaseUtils.buildLike(query.getName()));
        }
        if (query.getStatus() != null) {
            criteria.andEqualTo("status", query.getStatus());
        }

        List<AbstractionPO> list = abstractionMapper.selectByExample(example);
        Page<AbstractionPO> page = (Page<AbstractionPO>) list;

        List<AbstractionVO> listVO = new ArrayList<AbstractionVO>();
        for (AbstractionPO abstractionPO : page.getResult()) {
            AbstractionVO abstractionVO = new AbstractionVO();
            abstractionVO = POVOUtils.copyFromAbstractPO(abstractionPO);
            listVO.add(abstractionVO);
        }

//        for (AbstractionPO abstractionPO : page.getResult()) {
//            AbstractionVO abstractionVO = new AbstractionVO();
//            BeanUtils.copyProperties(abstractionPO, abstractionVO);
//            listVO.add(abstractionVO);
//        }

        PageResult<AbstractionVO> pageResult = new PageResult<AbstractionVO>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(AbstractionVO abstraction) {
        AbstractionPO abstractionPO = POVOUtils.copyFromAbstractVO(abstraction);
        Date sysDate = new Date();
        int count = 0;
        if (abstraction.getId() == null) {
            abstractionPO.setCreateTime(sysDate);
            abstractionPO.setUpdateTime(sysDate);
            count = abstractionMapper.insertSelective(abstractionPO);
            abstraction.setId(abstractionPO.getId());// 返回id
        } else {
            abstractionPO.setUpdateTime(sysDate);
            count = abstractionMapper.updateByPrimaryKeySelective(abstractionPO);
        }
        return count;
    }

    @Override
    public int delete(Long[] id) {
        Example example = new Example(AbstractionPO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = abstractionMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

}
