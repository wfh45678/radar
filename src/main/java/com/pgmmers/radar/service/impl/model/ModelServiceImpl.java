package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.pgmmers.radar.model.query.ModelQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.AbstractionMapping;
import com.pgmmers.radar.converter.StrategyMapping;
import com.pgmmers.radar.converter.FieldMapping;
import com.pgmmers.radar.converter.ModelMapping;
import com.pgmmers.radar.converter.PreItemMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.mapper.AbstractionMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ModelMapper;
import com.pgmmers.radar.mapper.PreItemMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.model.transfer.ModelGraphAssembler;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.common.util.JsonUtils;
import com.pgmmers.radar.model.vo.model.AbstractionVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import com.pgmmers.radar.model.vo.model.RuleVO;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class ModelServiceImpl extends BaseLocalCacheService implements ModelService,
        SubscribeHandle {


    public static Logger logger = LoggerFactory
            .getLogger(ModelServiceImpl.class);

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FieldMapper fieldMapper;
    @Autowired
    private AbstractionMapper abstractionMapper;
    @Autowired
    private StrategyMapper strategyMapper;
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
    private ModelGraphAssembler graphAssembler;
    @Resource
    private ModelMapping modelMapping;
    @Resource
    private FieldMapping fieldMapping;
    @Resource
    private RuleMapping ruleMapping;
    @Resource
    private AbstractionMapping abstractionMapping;
    @Resource
    private StrategyMapping strategyMapping;
    @Resource
    private PreItemMapping preItemMapping;

    @Autowired
    private CacheService cacheService;

    @Value("${sys.conf.mongo-restore-days}")
    private Integer eventExpireDays;

    @Autowired
    private SearchEngineService searchService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private PluginManager pluginManager;

    //  维护GUID到modelId的映射
    private Map<String, Long> guidMap;

    @PostConstruct
    public void init() {
        guidMap = listModelByDb(null).stream()
                .collect(Collectors.toMap(ModelVO::getGuid, ModelVO::getId));
        cacheService.subscribeModel(this);
    }

    /**
     * 原 ModelDalImpl.listModel(code, status, isTemplate) 逻辑下沉。
     */
    private List<ModelVO> listModelByDb(String code, Integer status, Boolean isTemplate) {
        List<ModelPO> list = modelMapper.selectList(
                new LambdaQueryWrapper<ModelPO>()
                        .eq(!StringUtils.isEmpty(code), ModelPO::getCode, code)
                        .eq(status != null, ModelPO::getStatus, status)
                        .eq(isTemplate != null, ModelPO::getTemplate, isTemplate));
        if (list == null) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

    private List<ModelVO> listModelByDb(Integer status) {
        List<ModelPO> list = modelMapper.selectList(
                new LambdaQueryWrapper<ModelPO>()
                        .eq(status != null, ModelPO::getStatus, status));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

    @Override
    public List<ModelVO> listModel(String merchantCode, Integer status) {
        return listModelByDb(merchantCode, status, null);
    }

    @Override
    public List<ModelVO> listModel(Integer status) {
        return listModelByDb(status);
    }

    /**
     * 原 ModelDalImpl.getModelByGuid 的 @Cacheable("models") 平移到本方法。
     * 方法体保留原 Service 层的 guidMap / 本地缓存回退逻辑。
     */
    @Cacheable(value = "models", key = "#guid")
    @Override
    public ModelVO getModelByGuid(String guid) {
        Long modelId = guidMap.get(guid);
        ModelVO vo = null;
        if (modelId != null) {
            vo = (ModelVO) getByCache(modelId);
        }
        if (vo == null) {
            logger.info("getModelByGuid,{}", guid);
            List<ModelPO> models = modelMapper.selectList(
                    new LambdaQueryWrapper<ModelPO>().eq(ModelPO::getGuid, guid));
            if (!CollectionUtils.isEmpty(models)) {
                vo = modelMapping.sourceToTarget(models.get(0));
                //维护guid->modelId 映射数据
                guidMap.put(vo.getGuid(), vo.getId());
                localCache.put(vo.getId(), vo);
            }
        }
        return vo;
    }

    @Override
    public ModelVO getModelById(Long id) {
        return (ModelVO) getByCache(id);
    }

    /**
     * 原 ModelDalImpl.getModelById 逻辑下沉：直接读库（本地缓存 loader 与直读场景共用）。
     */
    private ModelVO getModelByIdFromDb(Long modelId) {
        ModelPO model = modelMapper.selectById(modelId);
        if (model != null) {
            return modelMapping.sourceToTarget(model);
        }
        return null;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("model update message:{}", message);
        ModelVO vo = JsonUtils.fromJson(message, ModelVO.class);
//      删除本地缓存的规则模型
        invalidateCache(vo.getId());
    }

    /**
     * 原 ModelDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<ModelVO> queryPage(ModelQuery query) {
        Page<ModelPO> p = modelMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<ModelPO>()
                        .like(!StringUtils.isEmpty(query.getModelName()),
                                ModelPO::getModelName, query.getModelName())
                        .like(!StringUtils.isEmpty(query.getLabel()),
                                ModelPO::getLabel, query.getLabel())
                        .eq(query.getStatus() != null, ModelPO::getStatus, query.getStatus())
                        .eq(!StringUtils.isEmpty(query.getMerchantCode()),
                                ModelPO::getCode, query.getMerchantCode()));
        List<ModelVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : modelMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult query(ModelQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 ModelDalImpl.save 逻辑下沉。
     */
    private int saveModel(ModelVO model) {
        ModelPO modelPO = modelMapping.targetToSource(model);
        Date sysDate = new Date();
        int count = 0;
        if (model.getId() == null) {
            modelPO.setCreateTime(sysDate);
            modelPO.setUpdateTime(sysDate);
            count = modelMapper.insert(modelPO);
            model.setId(modelPO.getId());//返回id
        } else {
            modelPO.setUpdateTime(sysDate);
            count = modelMapper.updateById(modelPO);
        }
        return count;
    }

    @Override
    public CommonResult save(ModelVO model) {
        CommonResult result = new CommonResult();
        ModelQuery query = new ModelQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        query.setPageSize(100);
        PageResult<ModelVO> page = queryPage(query);
        if (page != null && page.getRowCount() > 0 && (model.getId() == null)) {
            for (ModelVO tmp : page.getList()) {
                if (tmp.getLabel().equals(model.getLabel())) {
                    result.setMsg("模型名称重复");
                    result.setSuccess(false);
                    return result;
                }
            }
        }
        if (model.getId() == null) {
            model.setStatus(StatusType.INIT.getKey());
            model.setGuid(UUID.randomUUID().toString().toUpperCase());
        }
        int count = saveModel(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getModelName())) {
                model.setModelName("model" + model.getId());
                saveModel(model);
            }

            result.getData().put("id", model.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishModel(model);
        }
        return result;
    }

    @Override
    public CommonResult delete(Long[] ids) {
        CommonResult result = new CommonResult();
        ModelVO model = getModelByIdFromDb(ids[0]);
        if (model.getTemplate()) {
            result.setCode("701");
            result.setSuccess(false);
            result.setMsg("系统模板禁止删除！");
            return result;
        }
        // 原 ModelDalImpl.delete 逻辑下沉：删除模型及关联子表
        int count = modelMapper.delete(
                new LambdaQueryWrapper<ModelPO>().in(ModelPO::getId, Arrays.asList(ids)));
        fieldMapper.delete(new LambdaQueryWrapper<FieldPO>().in(FieldPO::getModelId, Arrays.asList(ids)));
        preItemMapper.delete(new LambdaQueryWrapper<PreItemPO>().in(PreItemPO::getModelId, Arrays.asList(ids)));
        dataListMapper.delete(new LambdaQueryWrapper<DataListsPO>().in(DataListsPO::getModelId, Arrays.asList(ids)));
        abstractionMapper.delete(new LambdaQueryWrapper<AbstractionPO>().in(AbstractionPO::getModelId, Arrays.asList(ids)));
        strategyMapper.delete(new LambdaQueryWrapper<StrategyPO>().in(StrategyPO::getModelId, Arrays.asList(ids)));
        ruleMapper.delete(new LambdaQueryWrapper<RulePO>().in(RulePO::getModelId, Arrays.asList(ids)));

        if (count > 0) {
            result.setSuccess(true);
            // 通知更新
            cacheService.publishModel(model);
        }
        return result;
    }

    @Override
    public CommonResult build(Long id) throws IOException {
        CommonResult result = new CommonResult();
        ModelVO modelVO = getModelById(id);
        // 原 ModelDalImpl.listField / listPreItem 逻辑下沉
        List<FieldPO> fieldPOList = fieldMapper.selectList(
                new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getModelId, id));
        List<FieldVO> fields = CollectionUtils.isEmpty(fieldPOList)
                ? new ArrayList<>() : fieldMapping.sourceToTarget(fieldPOList);
        List<PreItemPO> itemPOList = preItemMapper.selectList(
                new LambdaQueryWrapper<PreItemPO>().eq(PreItemPO::getModelId, id));
        List<PreItemVO> items = CollectionUtils.isEmpty(itemPOList)
                ? new ArrayList<>() : preItemMapping.sourceToTarget(itemPOList);
        String collectionName = "entity_" + id;
        mongoService.getCollection(collectionName).drop();
        mongoService.getMongoTemplate().createCollection(collectionName);
        List<IndexModel> indexes = new ArrayList<>();

        if (fields == null) {
            result.setMsg("请先为模型配置字段");
            return result;
        }
        // 为需要索引的字段添加索引
        for (FieldVO field : fields) {
            if (field.getIndexed()) {
                Document indexKey = new Document();
                indexKey.put(field.getFieldName(), 1);
                IndexModel index = new IndexModel(indexKey);
                indexes.add(index);
            }
        }

        Document ttlKeys = new Document();
        ttlKeys.put("radar_ref_datetime", 1);
        IndexOptions options = new IndexOptions();
        options.expireAfter((long) eventExpireDays, TimeUnit.DAYS);
        IndexModel ttlIndex = new IndexModel(ttlKeys, options);
        indexes.add(ttlIndex);

        // add status.
        Document statusKey = new Document();
        statusKey.put("status", 1);
        IndexModel statusIndex = new IndexModel(statusKey);
        indexes.add(statusIndex);

        mongoService.getCollection(collectionName).createIndexes(indexes);
//

        // 重建es index
        JSONObject total = buildEsMappingJson(fields, items);

        // execute
        boolean isCreated = searchService
                .createIndex(modelVO.getGuid().toLowerCase(), modelVO.getModelName().toLowerCase(),
                        total.toJSONString());
        logger.info("index mapping:{} is create {}", total.toJSONString(), isCreated);
        if (isCreated) {
            modelVO.setStatus(StatusType.INACTIVE.getKey());
            int save = saveModel(modelVO);
            // 通知更新
            if (save > 0) {
                cacheService.publishModel(modelVO);
            }
        } else {
            result.setMsg("重建索引失败");
        }
        result.setSuccess(isCreated);
        return result;
    }

    /**
     * recreate elastic mapping
     */
    private JSONObject buildEsMappingJson(List<FieldVO> fields, List<PreItemVO> items) {
        //
        JSONObject total = new JSONObject();

        // field mapping
        JSONObject fieldJson = new JSONObject();
        String base = "{\"type\": \"%s\"}";
        for (FieldVO field : fields) {
            String fieldType = field.getFieldType();
            String elaType = convertFieldType2ElasticType(fieldType);
            String tmp = String.format(base, elaType);
            fieldJson.put(field.getFieldName(), JSON.parseObject(tmp));
        }

        // pre item mapping
        JSONObject preItemJson = new JSONObject();
        for (PreItemVO item : items) {
            PluginEntry plugin = pluginManager.entry(item.getPlugin());
            String columns = plugin == null ? null : plugin.legacyMetaJson();
            if (columns == null) {
                String fieldType = plugin == null ? "STRING" : plugin.getType();
                if ("JSON".equals(fieldType)) {
                    //TODO: json类型需要另外处理
                } else {
                    String elaType = convertFieldType2ElasticType(fieldType);
                    String tmp = String.format(base, elaType);
                    preItemJson.put(item.getDestField(), JSON.parseObject(tmp));
                }
            } else {
                List<JSONObject> fieldsJson = JSON.parseArray(columns, JSONObject.class);
                JSONObject properties = new JSONObject();
                for (JSONObject json : fieldsJson) {
                    String fieldName = json.getString("column");
                    String fieldType = json.getString("type");
                    String elaType = convertFieldType2ElasticType(fieldType);
                    String tmp = String.format(base, elaType);
                    properties.put(fieldName, JSON.parseObject(tmp));
                }
                JSONObject prop = new JSONObject();
                prop.put("properties", properties);
                preItemJson.put(item.getDestField(), prop);
            }
        }

        // package
        JSONObject totalProperties = new JSONObject();
        JSONObject fieldsProperties = new JSONObject();
        JSONObject preitemsProperties = new JSONObject();

        fieldsProperties.put("properties", fieldJson);
        preitemsProperties.put("properties", preItemJson);

        totalProperties.put("fields", fieldsProperties);
        totalProperties.put("preItems", preitemsProperties);
        total.put("properties", totalProperties);
        return total;
    }

    /**
     * 原 ModelDalImpl.copy 逻辑下沉：复制模型及全部子对象。
     * v3.2 起装配段抽至 {@link ModelGraphAssembler}（与策略包导入共用同一份重映射逻辑）。
     */
    private int copyModel(ModelVO model) {
        Long oldId = model.getId();
        model.setId(null);
        model.setStatus(StatusType.INIT.getKey());
        model.setGuid(UUID.randomUUID().toString().toUpperCase());
        model.setTemplate(false);
        int count = this.saveModel(model);

        Long newId = model.getId();

        // 子对象装配（单事务，重映射逻辑与导入一致）
        count += graphAssembler.assemble(newId,
                fieldMapper.selectList(new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getModelId, oldId)),
                preItemMapper.selectList(new LambdaQueryWrapper<PreItemPO>().eq(PreItemPO::getModelId, oldId)),
                dataListMapper.selectList(new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getModelId, oldId)),
                dataListMetaOf(oldId),
                dataListRecordOf(oldId),
                abstractionMapper.selectList(new LambdaQueryWrapper<AbstractionPO>().eq(AbstractionPO::getModelId, oldId)),
                strategyMapper.selectList(new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getModelId, oldId)),
                rulesOf(oldId));
        return count;
    }

    /** 源模型的名单 meta/record（装配入参收集）。 */
    private List<DataListMetaPO> dataListMetaOf(Long modelId) {
        List<Long> ids = dataListMapper.selectList(
                        new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getModelId, modelId))
                .stream().map(DataListsPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : dataListMetaMapper.selectList(
                        new LambdaQueryWrapper<DataListMetaPO>().in(DataListMetaPO::getDataListId, ids));
    }

    private List<DataListRecordPO> dataListRecordOf(Long modelId) {
        List<Long> ids = dataListMapper.selectList(
                        new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getModelId, modelId))
                .stream().map(DataListsPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : dataListRecordMapper.selectList(
                        new LambdaQueryWrapper<DataListRecordPO>().in(DataListRecordPO::getDataListId, ids));
    }

    /** 源模型的规则（激活下挂，装配入参收集）。 */
    private List<RulePO> rulesOf(Long modelId) {
        List<Long> ids = strategyMapper.selectList(
                        new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getModelId, modelId))
                .stream().map(StrategyPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : ruleMapper.selectList(new LambdaQueryWrapper<RulePO>().in(RulePO::getStrategyId, ids));
    }

    @Override
    public CommonResult copy(Long id, String merchantCode, String name, String label) {
        ModelVO model = getModelByIdFromDb(id);
        model.setModelName(name);
        model.setLabel(label);
        model.setCode(merchantCode);

        CommonResult result = new CommonResult();
        //检查是否重复
        ModelQuery query = new ModelQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        PageResult<ModelVO> page = queryPage(query);
        if (page != null && page.getRowCount() > 0) {
            for (ModelVO tmp : page.getList()) {
                if (tmp.getLabel().equals(model.getLabel())) {
                    result.setMsg("模型名称重复");
                    result.setSuccess(false);
                    return result;
                }
            }

        }

        int count = copyModel(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getModelName())) {
                model.setModelName("model_" + model.getId());
                model.setCreateTime(new Date());
                saveModel(model);
            }

            result.getData().put("id", model.getId());
            result.setSuccess(true);
            // 通知更新
            cacheService.publishModel(model);
        }
        return result;
    }

    public String convertFieldType2ElasticType(String fieldType) {
        FieldType type = Enum.valueOf(FieldType.class, fieldType);
        String tmp;
        switch (type) {
            case STRING:
                tmp = "keyword";
                break;
            case INTEGER:
                tmp = "integer";
                break;
            case LONG:
                tmp = "long";
                break;
            case DOUBLE:
                tmp = "double";
                break;
            default:
                tmp = "text";
                break;
        }
        return tmp;
    }

    @Override
    public List<ModelVO> listTemplateModel(boolean isTemplate) {
        return listModelByDb(null, null, true);
    }

    @Override
    public Object query(Long modelId) {
        return getModelByIdFromDb(modelId);
    }

}
