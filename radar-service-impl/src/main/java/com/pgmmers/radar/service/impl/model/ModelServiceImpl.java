package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.pgmmers.radar.dal.bean.ModelQuery;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.model.ModelDal;
import com.pgmmers.radar.enums.FieldType;
import com.pgmmers.radar.enums.PluginType;
import com.pgmmers.radar.enums.StatusType;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.impl.util.MongodbUtil;
import com.pgmmers.radar.service.model.ModelService;
import com.pgmmers.radar.service.search.SearchEngineService;
import com.pgmmers.radar.vo.model.FieldVO;
import com.pgmmers.radar.vo.model.ModelVO;
import com.pgmmers.radar.vo.model.PreItemVO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ModelServiceImpl implements ModelService, SubscribeHandle {

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

    private List<ModelVO> modelList = new ArrayList<>();

    @PostConstruct
    public void init() {
        modelList = modelDal.listModel(null);
        cacheService.subscribeModel(this);
    }

    @Override
    public List<ModelVO> listModel(String merchantCode, Integer status) {
        List<ModelVO> modelList = modelDal.listModel(merchantCode, status);
        return modelList;
    }

    @Override
    public List<ModelVO> listModel(Integer status) {
        if (modelList == null) {
            modelList = modelDal.listModel(null);
        }
        return modelList;
    }

    @Override
    public ModelVO getModelByGuid(String guid) {
        for (ModelVO mod : modelList) {
            if (mod.getGuid().equals(guid)) {
                return mod;
            }
        }
        ModelVO model = modelDal.getModelByGuid(guid);
        return model;
    }

    @Override
    public ModelVO getModelById(Long id) {
        return modelDal.getModelById(id);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("model sub:{}", message);
        modelList = modelDal.listModel(null);
    }

    @Override
    public ModelVO get(Long id) {
        return modelDal.getModelById(id);
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
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        ModelVO model = modelDal.getModelById(id[0]);
        if (model.getTemplate()) {
            result.setCode("701");
            result.setSuccess(false);
            result.setMsg("系统模板禁止删除！");
            return result;
        }
        int count = modelDal.delete(id);
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
        ModelVO modelVO = modelDal.getModelById(id);
        List<FieldVO> fields = modelDal.listField(id);
        List<PreItemVO> items = modelDal.listPreItem(id, null);
        String collectionName = "entity_" + id;
        MongodbUtil.mongoTemplate.getCollection(collectionName).drop();
        MongodbUtil.mongoTemplate.createCollection(collectionName);
        List<IndexModel> indexes = new ArrayList<>();

        if (fields == null) {
            result.setMsg("请先为模型配置字段");
            return result;
        }
        // 为需要索引的字段添加索引
        for (FieldVO field : fields) {
            if (field.getIndexed().booleanValue()) {
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

        MongodbUtil.getCollection(collectionName).createIndexes(indexes);
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
            modelDal.save(modelVO);
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
            String pluginType = item.getPlugin();
            PluginType plugin = Enum.valueOf(PluginType.class, pluginType);
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

}
