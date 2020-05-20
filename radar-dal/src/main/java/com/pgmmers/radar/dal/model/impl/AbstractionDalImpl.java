package com.pgmmers.radar.dal.model.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.bean.AbstractionQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.AbstractionDal;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapstruct.AbstractionMapping;
import com.pgmmers.radar.model.AbstractionPO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.AbstractionVO;
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
public class AbstractionDalImpl implements AbstractionDal {

    public static Logger logger = LoggerFactory.getLogger(AbstractionDalImpl.class);

    @Autowired
    private AbstractionMapper abstractionMapper;
    @Resource
    private AbstractionMapping abstractionMapping;

    @Override
    public AbstractionVO get(Long id) {
        AbstractionPO abstraction = abstractionMapper.selectByPrimaryKey(id);
        if (abstraction != null) {
            return abstractionMapping.sourceToTarget(abstraction);
        }
        return null;
    }

    @Override
    public List<AbstractionVO> list(Long modelId) {
        Example example = new Example(AbstractionPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", modelId);
        List<AbstractionPO> list = abstractionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return abstractionMapping.sourceToTarget(list);
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
        List<AbstractionVO> listVO;
        if (CollectionUtils.isEmpty(list)) {
            listVO = new ArrayList<>();
        } else {
            listVO = abstractionMapping.sourceToTarget(list);
        }
        PageResult<AbstractionVO> pageResult = new PageResult<>(page.getPageNum(),
                page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(AbstractionVO abstraction) {
        AbstractionPO abstractionPO = abstractionMapping.targetToSource(abstraction);
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
