package com.pgmmers.radar.dal.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pgmmers.radar.dal.bean.FieldQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.FieldDal;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.model.FieldPO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.FieldVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import tk.mybatis.mapper.entity.Example;


@Service
public class FieldDalImpl implements FieldDal {

    public static Logger logger = LoggerFactory.getLogger(FieldDalImpl.class);

    @Autowired
    private FieldMapper fieldMapper;

    @Override
    public FieldVO get(Long id) {
        FieldPO field = fieldMapper.selectByPrimaryKey(id);
        if (field != null) {
            FieldVO fieldVO = new FieldVO();
            BeanUtils.copyProperties(field, fieldVO);
            return fieldVO;
        }
        return null;
    }

    @Override
    public PageResult<FieldVO> query(FieldQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(FieldPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", query.getModelId());
        if (!StringUtils.isEmpty(query.getFieldName())) {
            criteria.andLike("fieldName", BaseUtils.buildLike(query.getFieldName()));
        }
        if (!StringUtils.isEmpty(query.getFieldType())) {
            criteria.andEqualTo("fieldType", query.getFieldType());
        }
        List<FieldPO> list = fieldMapper.selectByExample(example);
        Page<FieldPO> page = (Page<FieldPO>) list;

        List<FieldVO> listVO = new ArrayList<>();
        for (FieldPO fieldPO : page.getResult()) {
            FieldVO fieldVO = new FieldVO();
            BeanUtils.copyProperties(fieldPO, fieldVO);
            listVO.add(fieldVO);
        }

        PageResult<FieldVO> pageResult = new PageResult<FieldVO>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(FieldVO field) {
        FieldPO fieldPO = new FieldPO();
        BeanUtils.copyProperties(field, fieldPO);
        Date sysDate = new Date();
        int count = 0;
        if (field.getId() == null) {
            fieldPO.setCreateTime(sysDate);
            fieldPO.setUpdateTime(sysDate);
            count = fieldMapper.insertSelective(fieldPO);
            field.setId(fieldPO.getId());// 返回id
        } else {
            fieldPO.setUpdateTime(sysDate);
            count = fieldMapper.updateByPrimaryKeySelective(fieldPO);
        }
        return count;
    }

    @Override
    public int delete(Long[] id) {
        Example example = new Example(FieldPO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = fieldMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

}
