package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.pgmmers.radar.model.query.ScenarioQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.FeatureMapping;
import com.pgmmers.radar.converter.StrategyMapping;
import com.pgmmers.radar.converter.FieldMapping;
import com.pgmmers.radar.converter.ScenarioMapping;
import com.pgmmers.radar.converter.RuleMapping;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.mapper.FeatureMapper;
import com.pgmmers.radar.mapper.StrategyMapper;
import com.pgmmers.radar.mapper.DataListMetaMapper;
import com.pgmmers.radar.mapper.DataListRecordMapper;
import com.pgmmers.radar.mapper.DataListsMapper;
import com.pgmmers.radar.mapper.FieldMapper;
import com.pgmmers.radar.mapper.ScenarioMapper;
import com.pgmmers.radar.mapper.RuleMapper;
import com.pgmmers.radar.model.entity.FeaturePO;
import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.entity.ScenarioPO;
import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.impl.engine.plugin.PluginEntry;
import com.pgmmers.radar.service.impl.model.transfer.ScenarioGraphAssembler;
import com.pgmmers.radar.service.model.ScenarioService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.common.util.JsonUtils;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
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
public class ScenarioServiceImpl extends BaseLocalCacheService implements ScenarioService,
        SubscribeHandle {


    public static Logger logger = LoggerFactory
            .getLogger(ScenarioServiceImpl.class);

    @Autowired
    private ScenarioMapper scenarioMapper;
    @Autowired
    private FieldMapper fieldMapper;
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
    private ScenarioGraphAssembler graphAssembler;
    @Resource
    private ScenarioMapping modelMapping;
    @Resource
    private FieldMapping fieldMapping;
    @Resource
    private RuleMapping ruleMapping;
    @Resource
    private StrategyMapping strategyMapping;
    @Resource

    @Autowired
    private CacheService cacheService;

    @Value("${sys.conf.mongo-restore-days}")
    private Integer eventExpireDays;

    @Autowired
    private SearchEngineService searchService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private FeatureMapper featureMapper;
    @Resource
    private FeatureMapping featureMapping;
    @Autowired
    private PluginManager pluginManager;

    //  维护GUID到scenarioId的映射
    private Map<String, Long> guidMap;

    @PostConstruct
    public void init() {
        guidMap = listScenarioByDb(null).stream()
                .collect(Collectors.toMap(ScenarioVO::getGuid, ScenarioVO::getId));
        cacheService.subscribeModel(this);
    }

    /**
     * 原 ModelDalImpl.listScenario(code, status, isTemplate) 逻辑下沉。
     */
    private List<ScenarioVO> listScenarioByDb(String code, Integer status, Boolean isTemplate) {
        List<ScenarioPO> list = scenarioMapper.selectList(
                new LambdaQueryWrapper<ScenarioPO>()
                        .eq(!StringUtils.isEmpty(code), ScenarioPO::getCode, code)
                        .eq(status != null, ScenarioPO::getStatus, status)
                        .eq(isTemplate != null, ScenarioPO::getTemplate, isTemplate));
        if (list == null) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

    private List<ScenarioVO> listScenarioByDb(Integer status) {
        List<ScenarioPO> list = scenarioMapper.selectList(
                new LambdaQueryWrapper<ScenarioPO>()
                        .eq(status != null, ScenarioPO::getStatus, status));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return modelMapping.sourceToTarget(list);
    }

    @Override
    public List<ScenarioVO> listScenario(String merchantCode, Integer status) {
        return listScenarioByDb(merchantCode, status, null);
    }

    @Override
    public List<ScenarioVO> listScenario(Integer status) {
        return listScenarioByDb(status);
    }

    /**
     * 原 ModelDalImpl.getModelByGuid 的 @Cacheable("models") 平移到本方法。
     * 方法体保留原 Service 层的 guidMap / 本地缓存回退逻辑。
     */
    @Cacheable(value = "models", key = "#guid")
    @Override
    public ScenarioVO getModelByGuid(String guid) {
        Long scenarioId = guidMap.get(guid);
        ScenarioVO vo = null;
        if (scenarioId != null) {
            vo = (ScenarioVO) getByCache(scenarioId);
        }
        if (vo == null) {
            logger.info("getModelByGuid,{}", guid);
            List<ScenarioPO> models = scenarioMapper.selectList(
                    new LambdaQueryWrapper<ScenarioPO>().eq(ScenarioPO::getGuid, guid));
            if (!CollectionUtils.isEmpty(models)) {
                vo = modelMapping.sourceToTarget(models.get(0));
                //维护guid->scenarioId 映射数据
                guidMap.put(vo.getGuid(), vo.getId());
                localCache.put(vo.getId(), vo);
            }
        }
        return vo;
    }

    @Override
    public ScenarioVO getScenarioById(Long id) {
        return (ScenarioVO) getByCache(id);
    }

    /**
     * 原 ModelDalImpl.getScenarioById 逻辑下沉：直接读库（本地缓存 loader 与直读场景共用）。
     */
    private ScenarioVO getScenarioByIdFromDb(Long scenarioId) {
        ScenarioPO model = scenarioMapper.selectById(scenarioId);
        if (model != null) {
            return modelMapping.sourceToTarget(model);
        }
        return null;
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("model update message:{}", message);
        ScenarioVO vo = JsonUtils.fromJson(message, ScenarioVO.class);
//      删除本地缓存的规则模型
        invalidateCache(vo.getId());
    }

    /**
     * 原 ModelDalImpl.query 逻辑下沉：MyBatis-Plus 分页查询。
     */
    private PageResult<ScenarioVO> queryPage(ScenarioQuery query) {
        Page<ScenarioPO> p = scenarioMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<ScenarioPO>()
                        .like(!StringUtils.isEmpty(query.getScenarioName()),
                                ScenarioPO::getScenarioName, query.getScenarioName())
                        .like(!StringUtils.isEmpty(query.getLabel()),
                                ScenarioPO::getLabel, query.getLabel())
                        .eq(query.getStatus() != null, ScenarioPO::getStatus, query.getStatus())
                        .eq(!StringUtils.isEmpty(query.getMerchantCode()),
                                ScenarioPO::getCode, query.getMerchantCode()));
        List<ScenarioVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : modelMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult query(ScenarioQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    /**
     * 原 ModelDalImpl.save 逻辑下沉。
     */
    private int saveScenario(ScenarioVO model) {
        ScenarioPO modelPO = modelMapping.targetToSource(model);
        Date sysDate = new Date();
        int count = 0;
        if (model.getId() == null) {
            modelPO.setCreateTime(sysDate);
            modelPO.setUpdateTime(sysDate);
            count = scenarioMapper.insert(modelPO);
            model.setId(modelPO.getId());//返回id
        } else {
            modelPO.setUpdateTime(sysDate);
            count = scenarioMapper.updateById(modelPO);
        }
        return count;
    }

    @Override
    public CommonResult save(ScenarioVO model) {
        CommonResult result = new CommonResult();
        ScenarioQuery query = new ScenarioQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        query.setPageSize(100);
        PageResult<ScenarioVO> page = queryPage(query);
        if (page != null && page.getRowCount() > 0 && (model.getId() == null)) {
            for (ScenarioVO tmp : page.getList()) {
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
        int count = saveScenario(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getScenarioName())) {
                model.setScenarioName("model" + model.getId());
                saveScenario(model);
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
        ScenarioVO model = getScenarioByIdFromDb(ids[0]);
        if (model.getTemplate()) {
            result.setCode("701");
            result.setSuccess(false);
            result.setMsg("系统模板禁止删除！");
            return result;
        }
        // 原 ModelDalImpl.delete 逻辑下沉：删除模型及关联子表
        int count = scenarioMapper.delete(
                new LambdaQueryWrapper<ScenarioPO>().in(ScenarioPO::getId, Arrays.asList(ids)));
        fieldMapper.delete(new LambdaQueryWrapper<FieldPO>().in(FieldPO::getScenarioId, Arrays.asList(ids)));
        featureMapper.delete(new LambdaQueryWrapper<FeaturePO>().in(FeaturePO::getScenarioId, Arrays.asList(ids)));
        dataListMapper.delete(new LambdaQueryWrapper<DataListsPO>().in(DataListsPO::getScenarioId, Arrays.asList(ids)));
        strategyMapper.delete(new LambdaQueryWrapper<StrategyPO>().in(StrategyPO::getScenarioId, Arrays.asList(ids)));
        ruleMapper.delete(new LambdaQueryWrapper<RulePO>().in(RulePO::getScenarioId, Arrays.asList(ids)));

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
        ScenarioVO modelVO = getScenarioById(id);
        // 原 ModelDalImpl.listField / listPreItem 逻辑下沉（v4.1：统一特征）
        List<FieldPO> fieldPOList = fieldMapper.selectList(
                new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getScenarioId, id));
        List<FieldVO> fields = CollectionUtils.isEmpty(fieldPOList)
                ? new ArrayList<>() : fieldMapping.sourceToTarget(fieldPOList);
        List<FeaturePO> featurePOList = featureMapper.selectList(
                new LambdaQueryWrapper<FeaturePO>().eq(FeaturePO::getScenarioId, id));
        List<FeatureVO> features = CollectionUtils.isEmpty(featurePOList)
                ? new ArrayList<>() : featureMapping.sourceToTarget(featurePOList);
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
        JSONObject total = buildEsMappingJson(fields, features);

        // execute
        boolean isCreated = searchService
                .createIndex(modelVO.getGuid().toLowerCase(), modelVO.getScenarioName().toLowerCase(),
                        total.toJSONString());
        logger.info("index mapping:{} is create {}", total.toJSONString(), isCreated);
        if (isCreated) {
            modelVO.setStatus(StatusType.INACTIVE.getKey());
            int save = saveScenario(modelVO);
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
    private JSONObject buildEsMappingJson(List<FieldVO> fields, List<FeatureVO> features) {
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

        // feature mapping（v4.1：多列特征展开子 properties，单值特征标量）
        JSONObject featureJson = new JSONObject();
        for (FeatureVO feature : features) {
            com.fasterxml.jackson.databind.JsonNode output = feature.getOutput();
            if (output != null && output.isArray() && !output.isEmpty()) {
                JSONObject properties = new JSONObject();
                for (com.fasterxml.jackson.databind.JsonNode col : output) {
                    String fieldName = col.path("column").asText("");
                    if (fieldName.isEmpty()) {
                        continue;
                    }
                    String colType = col.path("type").asText("STRING");
                    String elaType = convertFieldType2ElasticType(colType);
                    properties.put(fieldName, JSON.parseObject(String.format(base, elaType)));
                }
                JSONObject prop = new JSONObject();
                prop.put("properties", properties);
                featureJson.put(feature.getFeatureName(), prop);
            } else {
                String elaType = "ENRICH".equals(feature.getType()) ? "keyword" : "double";
                featureJson.put(feature.getFeatureName(), JSON.parseObject(String.format(base, elaType)));
            }
        }

        // package
        JSONObject totalProperties = new JSONObject();
        JSONObject fieldsProperties = new JSONObject();
        JSONObject featuresProperties = new JSONObject();

        fieldsProperties.put("properties", fieldJson);
        featuresProperties.put("properties", featureJson);

        totalProperties.put("fields", fieldsProperties);
        totalProperties.put("features", featuresProperties);
        total.put("properties", totalProperties);
        return total;
    }

    /**
     * 原 ModelDalImpl.copy 逻辑下沉：复制模型及全部子对象。
     * v3.2 起装配段抽至 {@link ScenarioGraphAssembler}（与策略包导入共用同一份重映射逻辑）。
     */
    private int copyScenario(ScenarioVO model) {
        Long oldId = model.getId();
        model.setId(null);
        model.setStatus(StatusType.INIT.getKey());
        model.setGuid(UUID.randomUUID().toString().toUpperCase());
        model.setTemplate(false);
        int count = this.saveScenario(model);

        Long newId = model.getId();

        // 子对象装配（单事务，重映射逻辑与导入一致）
        count += graphAssembler.assemble(newId,
                fieldMapper.selectList(new LambdaQueryWrapper<FieldPO>().eq(FieldPO::getScenarioId, oldId)),
                featureMapper.selectList(new LambdaQueryWrapper<FeaturePO>().eq(FeaturePO::getScenarioId, oldId)),
                dataListMapper.selectList(new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getScenarioId, oldId)),
                dataListMetaOf(oldId),
                dataListRecordOf(oldId),
                strategyMapper.selectList(new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getScenarioId, oldId)),
                rulesOf(oldId));
        return count;
    }

    /** 源模型的名单 meta/record（装配入参收集）。 */
    private List<DataListMetaPO> dataListMetaOf(Long scenarioId) {
        List<Long> ids = dataListMapper.selectList(
                        new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getScenarioId, scenarioId))
                .stream().map(DataListsPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : dataListMetaMapper.selectList(
                        new LambdaQueryWrapper<DataListMetaPO>().in(DataListMetaPO::getDataListId, ids));
    }

    private List<DataListRecordPO> dataListRecordOf(Long scenarioId) {
        List<Long> ids = dataListMapper.selectList(
                        new LambdaQueryWrapper<DataListsPO>().eq(DataListsPO::getScenarioId, scenarioId))
                .stream().map(DataListsPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : dataListRecordMapper.selectList(
                        new LambdaQueryWrapper<DataListRecordPO>().in(DataListRecordPO::getDataListId, ids));
    }

    /** 源模型的规则（激活下挂，装配入参收集）。 */
    private List<RulePO> rulesOf(Long scenarioId) {
        List<Long> ids = strategyMapper.selectList(
                        new LambdaQueryWrapper<StrategyPO>().eq(StrategyPO::getScenarioId, scenarioId))
                .stream().map(StrategyPO::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of()
                : ruleMapper.selectList(new LambdaQueryWrapper<RulePO>().in(RulePO::getStrategyId, ids));
    }

    @Override
    public CommonResult copy(Long id, String merchantCode, String name, String label) {
        ScenarioVO model = getScenarioByIdFromDb(id);
        model.setScenarioName(name);
        model.setLabel(label);
        model.setCode(merchantCode);

        CommonResult result = new CommonResult();
        //检查是否重复
        ScenarioQuery query = new ScenarioQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        PageResult<ScenarioVO> page = queryPage(query);
        if (page != null && page.getRowCount() > 0) {
            for (ScenarioVO tmp : page.getList()) {
                if (tmp.getLabel().equals(model.getLabel())) {
                    result.setMsg("模型名称重复");
                    result.setSuccess(false);
                    return result;
                }
            }

        }

        int count = copyScenario(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getScenarioName())) {
                model.setScenarioName("model_" + model.getId());
                model.setCreateTime(new Date());
                saveScenario(model);
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
    public List<ScenarioVO> listTemplateScenario(boolean isTemplate) {
        return listScenarioByDb(null, null, true);
    }

    @Override
    public Object query(Long scenarioId) {
        return getScenarioByIdFromDb(scenarioId);
    }

}
