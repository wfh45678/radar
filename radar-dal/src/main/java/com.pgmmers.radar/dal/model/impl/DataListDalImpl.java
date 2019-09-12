package com.pgmmers.radar.dal.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.DataListDal;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.model.DataListMetaPO;
import com.pgmmers.radar.model.DataListRecordPO;
import com.pgmmers.radar.model.DataListsPO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
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
public class DataListDalImpl implements DataListDal {

    public static Logger logger = LoggerFactory.getLogger(DataListDalImpl.class);

    @Autowired
    private DataListsMapper dataListMapper;

    @Autowired
    private DataListRecordMapper dataListRecordMapper;

    @Autowired
    private DataListMetaMapper dataListMetaMapper;

    @Override
    public List<DataListMetaVO> listDataMeta(Long dataListId) {
        logger.info("listDataRecord,{}", dataListId);
        List<DataListMetaVO> volist = new ArrayList<>();
        Example example = new Example(DataListMetaPO.class);
        example.createCriteria().andEqualTo("dataListId", dataListId);
        DataListMetaVO vo;
        List<DataListMetaPO> list = dataListMetaMapper.selectByExample(example);
        for (DataListMetaPO po : list) {
            vo = new DataListMetaVO();
            BeanUtils.copyProperties(po, vo);
            volist.add(vo);
        }
        return volist;
    }

    @Override
    public List<DataListRecordVO> listDataRecord(Long dataListId) {
        logger.info("listDataRecord,{}", dataListId);
        List<DataListRecordVO> voList = new ArrayList<>();
        Example example = new Example(DataListRecordPO.class);
        example.createCriteria().andEqualTo("dataListId", dataListId);
        List<DataListRecordPO> list = dataListRecordMapper.selectByExample(example);
        DataListRecordVO vo;
        for (DataListRecordPO po : list) {
            vo = new DataListRecordVO();
            BeanUtils.copyProperties(po, vo);
            voList.add(vo);
        }

        return voList;
    }

    @Override
    public DataListMetaVO getListMetaById(Long metaId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataListsVO> listDataLists(Long modelId, Integer status) {
        logger.info("listDataLists,{},{}", modelId, status);
        List<DataListsVO> dataLists = new ArrayList<>();
        Example example = new Example(DataListsPO.class);
        Example.Criteria cri = example.createCriteria();
        if (status != null) {
            cri.andEqualTo("status", status);
        }
        List<DataListsPO> list = dataListMapper.selectByExample(example);
        DataListsVO vo;
        if (list == null || list.size() == 0) {
            return null;
        } else {
            for (DataListsPO po : list) {
                vo = new DataListsVO();
                BeanUtils.copyProperties(po, vo);
                dataLists.add(vo);
            }
        }
        return dataLists;
    }

    @Override
    public DataListsVO get(Long id) {
        DataListsPO dataListsPO = dataListMapper.selectByPrimaryKey(id);
        if (dataListsPO != null) {
            DataListsVO dataListsVO = new DataListsVO();
            BeanUtils.copyProperties(dataListsPO, dataListsVO);
            return dataListsVO;
        }
        return null;
    }

    @Override
    public List<DataListsVO> list(Long modelId) {
        Example example = new Example(DataListsPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("modelId", Arrays.asList(modelId, 0L));
        List<DataListsPO> list = dataListMapper.selectByExample(example);

        List<DataListsVO> listVO = new ArrayList<>();
        for (DataListsPO dataListsPO : list) {
            DataListsVO dataListsVO = new DataListsVO();
            BeanUtils.copyProperties(dataListsPO, dataListsVO);
            listVO.add(dataListsVO);
        }
        return listVO;
    }

    @Override
    public PageResult<DataListsVO> query(DataListQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(DataListsPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", query.getModelId());
        if (!StringUtils.isEmpty(query.getName())) {
            criteria.andLike("name", BaseUtils.buildLike(query.getName()));
        }
        if (!StringUtils.isEmpty(query.getListType())) {
            criteria.andEqualTo("listType", query.getListType());
        }
        if (query.getStatus() != null) {
            criteria.andEqualTo("status", query.getStatus());
        }
        List<DataListsPO> list = dataListMapper.selectByExample(example);
        Page<DataListsPO> page = (Page<DataListsPO>) list;

        List<DataListsVO> listVO = new ArrayList<>();
        for (DataListsPO dataListsPO : page.getResult()) {
            DataListsVO dataListsVO = new DataListsVO();
            BeanUtils.copyProperties(dataListsPO, dataListsVO);
            listVO.add(dataListsVO);
        }

        PageResult<DataListsVO> pageResult = new PageResult<>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(DataListsVO datalist) {
        DataListsPO dataListsPO = new DataListsPO();
        BeanUtils.copyProperties(datalist, dataListsPO);
        Date sysDate = new Date();
        int count = 0;
        if (datalist.getId() == null) {
            dataListsPO.setCreateTime(sysDate);
            dataListsPO.setUpdateTime(sysDate);
            count = dataListMapper.insertSelective(dataListsPO);
            datalist.setId(dataListsPO.getId());// 返回id
        } else {
            dataListsPO.setUpdateTime(sysDate);
            count = dataListMapper.updateByPrimaryKeySelective(dataListsPO);
        }
        return count;
    }

    @Override
    public int delete(Long[] id) {
        Example example = new Example(DataListsPO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = dataListMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

    @Override
    public DataListMetaVO getMeta(Long id) {
        DataListMetaPO dataListMetaPO = dataListMetaMapper.selectByPrimaryKey(id);
        if (dataListMetaPO != null) {
            DataListMetaVO dataListMetaVO = new DataListMetaVO();
            BeanUtils.copyProperties(dataListMetaPO, dataListMetaVO);
            return dataListMetaVO;
        }
        return null;
    }

    @Override
    public int saveMeta(DataListMetaVO dataListMeta) {
        DataListMetaPO dataListMetaPO = new DataListMetaPO();
        BeanUtils.copyProperties(dataListMeta, dataListMetaPO);
        Date sysDate = new Date();
        int count = 0;
        if (dataListMetaPO.getId() == null) {
            dataListMetaPO.setCreateTime(sysDate);
            dataListMetaPO.setUpdateTime(sysDate);
            count = dataListMetaMapper.insertSelective(dataListMetaPO);
            dataListMeta.setId(dataListMetaPO.getId());// 返回id
        } else {
            dataListMetaPO.setUpdateTime(sysDate);
            count = dataListMetaMapper.updateByPrimaryKeySelective(dataListMetaPO);
        }
        return count;
    }

    @Override
    public int deleteMeta(List<Long> listId) {
        Example example = new Example(DataListMetaPO.class);
        example.createCriteria().andIn("id", listId);
        int count = dataListMetaMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

    @Override
    public DataListRecordVO getRecord(Long id) {
        DataListRecordPO dataListRecordPO = dataListRecordMapper.selectByPrimaryKey(id);
        if (dataListRecordPO != null) {
            DataListRecordVO dataListRecordVO = new DataListRecordVO();
            BeanUtils.copyProperties(dataListRecordPO, dataListRecordVO);
            return dataListRecordVO;
        }
        return null;
    }

    @Override
    public PageResult<DataListRecordVO> queryRecord(DataListRecordQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(DataListRecordPO.class);
        example.createCriteria().andEqualTo("dataListId", query.getDataListId());
        List<DataListRecordPO> list = dataListRecordMapper.selectByExample(example);
        Page<DataListRecordPO> page = (Page<DataListRecordPO>) list;

        List<DataListRecordVO> listVO = new ArrayList<>();
        for (DataListRecordPO dataListRecordPO : page.getResult()) {
            DataListRecordVO dataListRecordVO = new DataListRecordVO();
            BeanUtils.copyProperties(dataListRecordPO, dataListRecordVO);
            listVO.add(dataListRecordVO);
        }

        PageResult<DataListRecordVO> pageResult = new PageResult<>(page.getPageNum(),
                page.getPageSize(), (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int saveRecord(DataListRecordVO dataListRecord) {
        DataListRecordPO dataListRecordPO = new DataListRecordPO();
        BeanUtils.copyProperties(dataListRecord, dataListRecordPO);
        Date sysDate = new Date();
        int count = 0;
        if (dataListRecordPO.getId() == null) {
            dataListRecordPO.setCreateTime(sysDate);
            dataListRecordPO.setUpdateTime(sysDate);
            count = dataListRecordMapper.insertSelective(dataListRecordPO);
            dataListRecord.setId(dataListRecordPO.getId());// 返回id
        } else {
            dataListRecordPO.setUpdateTime(sysDate);
            count = dataListRecordMapper.updateByPrimaryKeySelective(dataListRecordPO);
        }
        return count;
    }

    @Override
    public int deleteRecord(Long[] id) {
        Example example = new Example(DataListRecordPO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = dataListRecordMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

    @Override
    public int deleteRecord(Long dataListId) {
        Example example = new Example(DataListRecordPO.class);
        example.createCriteria().andEqualTo("dataListId", dataListId);
        int count = dataListRecordMapper.deleteByExample(example);
        // TODO 删除关联子表
        return count;
    }

}
