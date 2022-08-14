package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.pgmmers.radar.dal.bean.ModelQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.data.MongoService;
import com.pgmmers.radar.service.engine.PluginServiceV2;
import com.pgmmers.radar.service.impl.engine.plugin.PluginManager;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.util.JsonUtils;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private ModelDal modelDal;

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
        guidMap = modelDal.listModel(null).stream()
                .collect(Collectors.toMap(ModelVO::getGuid, ModelVO::getId));
        cacheService.subscribeModel(this);
    }

    @Override
    public List<ModelVO> listModel(String merchantCode, Integer status) {
        return modelDal.listModel(merchantCode, status);
    }

    @Override
    public List<ModelVO> listModel(Integer status) {
        return modelDal.listModel(status);
    }

    @Override
    public ModelVO getModelByGuid(String guid) {
        Long modelId = guidMap.get(guid);
        ModelVO vo = null;
        if (modelId != null) {
            vo = (ModelVO) getByCache(modelId);
        }
        if (vo == null) {
            vo = modelDal.getModelByGuid(guid);
            //维护guid->modelId 映射数据
            guidMap.put(vo.getGuid(), vo.getId());
            localCache.put(vo.getId(),vo);
        }
        return vo;
    }

    @Override
    public ModelVO getModelById(Long id) {
        return (ModelVO) getByCache(id);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("model update message:{}", message);
        ModelVO vo = JsonUtils.fromJson(message, ModelVO.class);
//      删除本地缓存的规则模型
        invalidateCache(vo.getId());
    }

    @Override
    public CommonResult query(ModelQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", modelDal.query(query));
        return result;
    }

    @Override
    public CommonResult save(ModelVO model) {
        CommonResult result = new CommonResult();
        ModelQuery query = new ModelQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        query.setPageSize(100);
        PageResult<ModelVO> page = modelDal.query(query);
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
        int count = modelDal.save(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getModelName())) {
                model.setModelName("model" + model.getId());
                modelDal.save(model);
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
        ModelVO model = modelDal.getModelById(ids[0]);
        if (model.getTemplate()) {
            result.setCode("701");
            result.setSuccess(false);
            result.setMsg("系统模板禁止删除！");
            return result;
        }
        int count = modelDal.delete(ids);
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
        List<FieldVO> fields = modelDal.listField(id);
        List<PreItemVO> items = modelDal.listPreItem(id, null);
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
            int save = modelDal.save(modelVO);
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
            PluginServiceV2 plugin = pluginManager.pluginServiceMap(item.getPlugin());
            String columns = plugin.getMeta();
            if (columns == null) {
                String fieldType = plugin.getType();
                if (fieldType.equals("JSON")) {
                    //TODO: json类型需要另外处理
                } else {
                    String elaType = convertFieldType2ElasticType(fieldType);
                    String tmp = String.format(base, elaType);
                    preItemJson.put(item.getDestField(), JSON.parseObject(tmp));
                }
            } else {
                String meta = plugin.getMeta();
                List<JSONObject> fieldsJson = JSON.parseArray(meta, JSONObject.class);
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

    @Override
    public CommonResult copy(Long id, String merchantCode, String name, String label) {
        ModelVO model = modelDal.getModelById(id);
        model.setModelName(name);
        model.setLabel(label);
        model.setCode(merchantCode);

        CommonResult result = new CommonResult();
        //检查是否重复
        ModelQuery query = new ModelQuery();
        query.setMerchantCode(model.getCode());
        query.setLabel(model.getLabel());
        PageResult<ModelVO> page = modelDal.query(query);
        if (page != null && page.getRowCount() > 0) {
            for (ModelVO tmp : page.getList()) {
                if (tmp.getLabel().equals(model.getLabel())) {
                    result.setMsg("模型名称重复");
                    result.setSuccess(false);
                    return result;
                }
            }

        }

        int count = modelDal.copy(model);
        if (count > 0) {
            if (StringUtils.isEmpty(model.getModelName())) {
                model.setModelName("model_" + model.getId());
                model.setCreateTime(new Date());
                modelDal.save(model);
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
        return modelDal.listModel(null, null, true);
    }

    @Override
    public Object query(Long modelId) {
        return modelDal.getModelById(modelId);
    }

}
