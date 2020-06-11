package com.pgmmers.radar.dal.model.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.bean.DataListQuery;
import com.pgmmers.radar.dal.bean.DataListRecordQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.DataListDal;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapstruct.DataListMetaMapping;
import com.pgmmers.radar.mapstruct.DataListRecordMapping;
import com.pgmmers.radar.mapstruct.DataListsMapping;
import com.pgmmers.radar.model.DataListMetaPO;
import com.pgmmers.radar.model.DataListRecordPO;
import com.pgmmers.radar.model.DataListsPO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import com.pgmmers.radar.vo.model.DataListsVO;
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
public class DataListDalImpl implements DataListDal {

    public static Logger logger = LoggerFactory.getLogger(DataListDalImpl.class);

    @Autowired
    private DataListsMapper dataListMapper;
    @Resource
    private DataListsMapping dataListsMapping;
    @Autowired
    private DataListRecordMapper dataListRecordMapper;
    @Resource
    private DataListRecordMapping dataListRecordMapping;
    @Autowired
    private DataListMetaMapper dataListMetaMapper;
    @Resource
    private DataListMetaMapping dataListMetaMapping;

    @Override
    public List<DataListMetaVO> listDataMeta(Long dataListId) {
        logger.info("listDataRecord,{}", dataListId);
        Example example = new Example(DataListMetaPO.class);
        example.createCriteria().andEqualTo("dataListId", dataListId);
        List<DataListMetaPO> list = dataListMetaMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListMetaMapping.sourceToTarget(list);
    }

    @Override
    public List<DataListRecordVO> listDataRecord(Long dataListId) {
        logger.info("listDataRecord,{}", dataListId);
        Example example = new Example(DataListRecordPO.class);
        example.createCriteria().andEqualTo("dataListId", dataListId);
        List<DataListRecordPO> list = dataListRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListRecordMapping.sourceToTarget(list);
    }

    @Override
    public DataListMetaVO getListMetaById(Long metaId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DataListsVO> listDataLists(Long modelId, Integer status) {
        logger.info("listDataLists,{},{}", modelId, status);
        Example example = new Example(DataListsPO.class);
        Example.Criteria cri = example.createCriteria();
        cri.andEqualTo("modelId", modelId);
        if (status != null) {
            cri.andEqualTo("status", status);
        }
        List<DataListsPO> list = dataListMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListsMapping.sourceToTarget(list);
    }

    @Override
    public DataListsVO get(Long id) {
        DataListsPO dataListsPO = dataListMapper.selectByPrimaryKey(id);
        if (dataListsPO != null) {
            return dataListsMapping.sourceToTarget(dataListsPO);
        }
        return null;
    }

    @Override
    public List<DataListsVO> list(Long modelId) {
        Example example = new Example(DataListsPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("modelId", Arrays.asList(modelId, 0L));
        List<DataListsPO> list = dataListMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return dataListsMapping.sourceToTarget(list);
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
        List<DataListsVO> listVO;
        if (CollectionUtils.isEmpty(list)) {
            listVO = new ArrayList<>();
        } else {
            listVO = dataListsMapping.sourceToTarget(list);
        }
        PageResult<DataListsVO> pageResult = new PageResult<>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int save(DataListsVO datalist) {
        DataListsPO dataListsPO = dataListsMapping.targetToSource(datalist);
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
           return dataListMetaMapping.sourceToTarget(dataListMetaPO);
        }
        return null;
    }

    @Override
    public int saveMeta(DataListMetaVO dataListMeta) {
        DataListMetaPO dataListMetaPO = dataListMetaMapping.targetToSource(dataListMeta);
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
            return dataListRecordMapping.sourceToTarget(dataListRecordPO);
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
        List<DataListRecordVO> listVO ;
        if (CollectionUtils.isEmpty(list)){
            listVO = new ArrayList<>();
        }else {
            listVO=dataListRecordMapping.sourceToTarget(list);
        }
        PageResult<DataListRecordVO> pageResult = new PageResult<>(page.getPageNum(),
                page.getPageSize(), (int) page.getTotal(), listVO);
        return pageResult;
    }

    @Override
    public int saveRecord(DataListRecordVO dataListRecord) {
        DataListRecordPO dataListRecordPO = dataListRecordMapping.targetToSource(dataListRecord);
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
