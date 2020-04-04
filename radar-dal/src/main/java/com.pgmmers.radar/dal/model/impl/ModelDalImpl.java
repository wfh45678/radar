package com.pgmmers.radar.dal.model.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.bean.ModelQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.ActivationMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ModelMapper;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.mapstruct.AbstractionMapping;
import com.pgmmers.radar.mapstruct.ActivationMapping;
import com.pgmmers.radar.mapstruct.FieldMapping;
import com.pgmmers.radar.mapstruct.ModelMapping;
import com.pgmmers.radar.mapstruct.PreItemMapping;
import com.pgmmers.radar.mapstruct.RuleMapping;
import com.pgmmers.radar.model.AbstractionPO;
import com.pgmmers.radar.model.ActivationPO;
import com.pgmmers.radar.model.DataListMetaPO;
import com.pgmmers.radar.model.DataListRecordPO;
import com.pgmmers.radar.model.DataListsPO;
import com.pgmmers.radar.model.FieldPO;
import com.pgmmers.radar.model.ModelPO;
import com.pgmmers.radar.model.PreItemPO;
import com.pgmmers.radar.model.RulePO;
import com.pgmmers.radar.util.BaseUtils;
import com.pgmmers.radar.vo.model.AbstractionVO;
import com.pgmmers.radar.vo.model.ActivationVO;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import com.pgmmers.radar.vo.model.RuleVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;


@Service
public class ModelDalImpl implements ModelDal {

    public static Logger logger = LoggerFactory.getLogger(ModelDalImpl.class);

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private AbstractionMapper abstractionMapper;

    @Autowired
    private ActivationMapper activationMapper;

    @Autowired
    private DataListsMapper dataListMapper;

    @Autowired
    private DataListMetaMapper dataListMetaMapper;

    @Autowired
    private DataListRecordMapper dataListRecordMapper;

    @Autowired
    private RuleMapper ruleMapper;

    @Autowired
    private PreItemMapper preItemMapper;
    @Autowired
    private PreItemMapping preItemMapping;
    @Resource
    private ModelMapping modelMapping;
    @Resource
    private FieldMapping fieldMapping;
    @Resource
    private RuleMapping ruleMapping;
    @Resource
    private AbstractionMapping abstractionMapping;
    @Resource
    private ActivationMapping activationMapping;

    @Cacheable(value = "models", key = "#guid")
    @Override
    public ModelVO getModelByGuid(String guid) {
        logger.info("getModelByGuid,{}", guid);
        ModelPO po = null;
        Example example = new Example(ModelPO.class);
        example.createCriteria().andEqualTo("guid", guid);
        List<ModelPO> models = modelMapper.selectByExample(example);
        if (models == null || models.size() == 0) {
            return null;
        } else {
            po = models.get(0);
        }
        return modelMapping.sourceToTarget(po);
    }

    //@Cacheable(value = "models", key = "#modelId")
    @Override
    public ModelVO getModelById(Long modelId) {
        logger.info("getModelById,{}", modelId);
        ModelPO model = modelMapper.selectByPrimaryKey(modelId);
        if (model != null) {
            return modelMapping.sourceToTarget(model);
        }
        return null;
    }

    @Override
    public List<AbstractionVO> listAbstraction(Long modelId) {
        logger.info("listAbstraction,{}", modelId);
        return listAbstraction(modelId, null);
    }

    @Override
    public List<ActivationVO> listActivation(Long modelId) {
        logger.info("listActivation,{}", modelId);
        return listActivation(modelId, null);
    }

    @Override
    public List<FieldVO> listField(Long modelId) {
        logger.info("listField,{}", modelId);
        Example example = new Example(FieldPO.class);
        example.createCriteria().andEqualTo("modelId", modelId);
        List<FieldPO> list = fieldMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        } else {
            return fieldMapping.sourceToTarget(list);
        }
    }

    @Override
    public List<ModelVO> listModel(String merchantCode, Integer status) {
        logger.info("listAbstraction,merchantCode:{},status:{}", merchantCode, status);
        return listModel(merchantCode, status, null);
    }

    @Override
    public List<ModelVO> listModel(Integer status) {
        logger.info("listAbstraction,status:{}", status);
        Example example = new Example(ModelPO.class);
        if (status != null) {
            example.createCriteria().andEqualTo("status", status);
        }
        List<ModelPO> list = modelMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

    @Override
    public List<AbstractionVO> listAbstraction(Long modelId, Integer status) {
        logger.info("listAbstraction,{}", modelId);
        Example example = new Example(AbstractionPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", modelId);
        if (status != null) {
            criteria.andEqualTo("status", status);
        }
        List<AbstractionPO> list = abstractionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return abstractionMapping.sourceToTarget(list);

    }

    @Override
    public List<ActivationVO> listActivation(Long modelId, Integer status) {
        logger.info("listActivation,{}", modelId);
        Example example = new Example(ActivationPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", modelId);
        if (status != null) {
            criteria.andEqualTo("status", status);
        }
        List<ActivationPO> list = activationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return activationMapping.sourceToTarget(list);
    }


    @Override
    public List<RuleVO> listRules(Long modelId, Long activationId, Integer status) {
        logger.info("listRules:{},{},{}", modelId, activationId, status);
        Example example = new Example(RulePO.class);
        Example.Criteria criteria = example.createCriteria();
        if (modelId != null) {
            criteria.andEqualTo("modelId", modelId);
        }
        if (activationId != null) {
            criteria.andEqualTo("activationId", activationId);
        }
        if (status != null) {
            criteria.andEqualTo("status", status);
        }
        List<RulePO> rulePoList = ruleMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(rulePoList)) {
            return new ArrayList<>();
        }
        return ruleMapping.sourceToTarget(rulePoList);
    }

    @Override
    public List<PreItemVO> listPreItem(Long modelId, Integer status) {
        logger.info("listRules:{},{}", modelId, status);

        Example example = new Example(PreItemPO.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("modelId", modelId);
        if (status != null) {
            criteria.andEqualTo("status", status);
        }
        List<PreItemPO> itemPOList = preItemMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(itemPOList)){
            return new ArrayList<>();
        }
        return preItemMapping.sourceToTarget(itemPOList);
    }

    @Override
    public PageResult<ModelVO> query(ModelQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());

        Example example = new Example(ModelPO.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(query.getModelName())) {
            criteria.andLike("modelName", BaseUtils.buildLike(query.getModelName()));
        }
        if (!StringUtils.isEmpty(query.getLabel())) {
            criteria.andLike("label", BaseUtils.buildLike(query.getLabel()));
        }
        if (query.getStatus() != null) {
            criteria.andEqualTo("status", query.getStatus());
        }
        if (!StringUtils.isEmpty(query.getMerchantCode())) {
            criteria.andEqualTo("code", query.getMerchantCode());
        }
        List<ModelPO> list = modelMapper.selectByExample(example);
        Page<ModelPO> page = (Page<ModelPO>) list;
        List<ModelVO> listVO;
        if (CollectionUtils.isEmpty(list)) {
            listVO = new ArrayList<>();
        } else {
            listVO = modelMapping.sourceToTarget(list);
        }
        return new PageResult<>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), listVO);
    }

    //@CachePut(value = "models", key = "#model.id")
    @Override
    public int save(ModelVO model) {
        ModelPO modelPO = modelMapping.targetToSource(model);
        Date sysDate = new Date();
        int count = 0;
        if (model.getId() == null) {
            modelPO.setCreateTime(sysDate);
            modelPO.setUpdateTime(sysDate);
            count = modelMapper.insertSelective(modelPO);
            model.setId(modelPO.getId());//返回id
        } else {
            modelPO.setUpdateTime(sysDate);
            count = modelMapper.updateByPrimaryKeySelective(modelPO);
        }
        return count;
    }

    @Override
    public int delete(Long[] id) {
        Example example = new Example(ModelPO.class);
        example.createCriteria().andIn("id", Arrays.asList(id));
        int count = modelMapper.deleteByExample(example);
        // 删除关联子表
        example = new Example(FieldPO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        fieldMapper.deleteByExample(example);

        example = new Example(PreItemPO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        preItemMapper.deleteByExample(example);

        example = new Example(DataListsPO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        dataListMapper.deleteByExample(example);

        example = new Example(AbstractionPO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        abstractionMapper.deleteByExample(example);

        example = new Example(ActivationPO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        activationMapper.deleteByExample(example);

        example = new Example(RulePO.class);
        example.createCriteria().andIn("modelId", Arrays.asList(id));
        ruleMapper.deleteByExample(example);

        return count;
    }

    @Override
    public int copy(ModelVO model) {
        Long oldId = model.getId();
        model.setId(null);
        model.setStatus(StatusType.INIT.getKey());
        model.setGuid(UUID.randomUUID().toString().toUpperCase());
        model.setTemplate(false);
        int count = this.save(model);

        Long newId = model.getId();
        Date sysDate = new Date();

        //复制field
        Example fieldExample = new Example(FieldPO.class);
        fieldExample.createCriteria().andEqualTo("modelId", oldId);
        List<FieldPO> fieldList = fieldMapper.selectByExample(fieldExample);
        for (FieldPO po : fieldList) {
            po.setModelId(newId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            fieldMapper.insertSelective(po);
        }

        //复制preItem
        Example preItemExample = new Example(PreItemPO.class);
        preItemExample.createCriteria().andEqualTo("modelId", oldId);
        List<PreItemPO> preItemList = preItemMapper.selectByExample(preItemExample);
        for (PreItemPO po : preItemList) {
            po.setModelId(newId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            preItemMapper.insertSelective(po);
        }

        //复制dataList
        Example dataListsExample = new Example(DataListsPO.class);
        dataListsExample.createCriteria().andEqualTo("modelId", oldId);
        List<DataListsPO> dataListsList = dataListMapper.selectByExample(dataListsExample);
        for (DataListsPO po : dataListsList) {
            Long oldDataListId = po.getId();

            po.setModelId(newId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            dataListMapper.insertSelective(po);

            Long newDataListId = po.getId();

            //复制dataListMeta
            Example dataListMetaExample = new Example(DataListMetaPO.class);
            dataListMetaExample.createCriteria().andEqualTo("dataListId", oldDataListId);
            List<DataListMetaPO> dataListMetaList = dataListMetaMapper
                    .selectByExample(dataListMetaExample);
            for (DataListMetaPO metaPO : dataListMetaList) {
                metaPO.setDataListId(newDataListId);
                metaPO.setId(null);
                metaPO.setCreateTime(sysDate);
                metaPO.setUpdateTime(sysDate);
                dataListMetaMapper.insertSelective(metaPO);
            }

            //复制dataListRecord
            Example dataListRecordExample = new Example(DataListRecordPO.class);
            dataListRecordExample.createCriteria().andEqualTo("dataListId", oldDataListId);
            List<DataListRecordPO> dataListRecordList = dataListRecordMapper
                    .selectByExample(dataListRecordExample);
            for (DataListRecordPO recordPO : dataListRecordList) {
                recordPO.setDataListId(newDataListId);
                recordPO.setId(null);
                recordPO.setCreateTime(sysDate);
                recordPO.setUpdateTime(sysDate);
                dataListRecordMapper.insertSelective(recordPO);
            }
        }

        //复制abstraction
        Example abstractionExample = new Example(AbstractionPO.class);
        abstractionExample.createCriteria().andEqualTo("modelId", oldId);
        List<AbstractionPO> abstractionList = abstractionMapper.selectByExample(abstractionExample);
        for (AbstractionPO po : abstractionList) {
            po.setModelId(newId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            abstractionMapper.insertSelective(po);
        }

        //复制activation
        Example activationExample = new Example(ActivationPO.class);
        activationExample.createCriteria().andEqualTo("modelId", oldId);
        List<ActivationPO> activationList = activationMapper.selectByExample(activationExample);
        for (ActivationPO po : activationList) {
            Long oldActivationID = po.getId();

            po.setModelId(newId);
            po.setId(null);
            po.setCreateTime(sysDate);
            po.setUpdateTime(sysDate);
            activationMapper.insertSelective(po);

            Long newActivationID = po.getId();

            //复制rule
            Example ruleExample = new Example(RulePO.class);
            ruleExample.createCriteria().andEqualTo("activationId", oldActivationID);
            List<RulePO> ruleList = ruleMapper.selectByExample(ruleExample);
            for (RulePO rulePO : ruleList) {
                rulePO.setModelId(newId);
                rulePO.setActivationId(newActivationID);
                rulePO.setId(null);
                rulePO.setCreateTime(sysDate);
                rulePO.setUpdateTime(sysDate);
                ruleMapper.insertSelective(rulePO);
            }
        }

        return count;
    }

    @Override
    public List<ModelVO> listModel(String merchantCode, Integer status,
            Boolean isTemplate) {
        Example example = new Example(ModelPO.class);
        Example.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(merchantCode)) {
            criteria.andEqualTo("code", merchantCode);
        }
        if (status != null) {
            criteria.andEqualTo("status", status);
        }
        if (isTemplate != null) {
            criteria.andEqualTo("template", isTemplate);
        }
        List<ModelPO> list = modelMapper.selectByExample(example);
        if (list == null) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

}
