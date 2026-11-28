package com.pgmmers.radar.service.impl.model;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.model.query.FeatureQuery;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.converter.FeatureMapping;
import com.pgmmers.radar.enums.FeatureType;
import com.pgmmers.radar.mapper.FeatureMapper;
import com.pgmmers.radar.model.entity.FeaturePO;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.cache.SubscribeHandle;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.service.model.FeatureService;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 特征服务实现（v4.1）：ENRICH/AGGREGATE/DERIVED/RATIO 统一 CRUD + 本地缓存 + Redis pub/sub 失效。
 * 保存语义沿袭原 PreItem/Abstraction：label 同模型查重、featureName 缺省自动补 feature_&lt;id&gt;。
 */
@Service
public class FeatureServiceImpl extends BaseLocalCacheService implements FeatureService, SubscribeHandle {

    public static Logger logger = LoggerFactory.getLogger(FeatureServiceImpl.class);

    @Autowired
    private FeatureMapper featureMapper;

    @Resource
    private FeatureMapping featureMapping;

    @Autowired
    private CacheService cacheService;

    @Override
    public List<FeatureVO> listFeature(Long scenarioId) {
        //noinspection unchecked
        return (List<FeatureVO>) getByCache(scenarioId);
    }

    @Override
    public void onMessage(String channel, String message) {
        logger.info("feature sub:{}", message);
        FeatureVO feature = JSON.parseObject(message, FeatureVO.class);
        if (feature != null) {
            invalidateCache(feature.getModelId());
        }
    }

    @PostConstruct
    public void init() {
        cacheService.subscribeFeature(this);
    }

    @Override
    public Object query(Long scenarioId) {
        List<FeaturePO> list = featureMapper.selectList(
                new LambdaQueryWrapper<FeaturePO>().eq(FeaturePO::getModelId, scenarioId));
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<FeatureVO>();
        }
        return featureMapping.sourceToTarget(list);
    }

    @Override
    public FeatureVO get(Long id) {
        FeaturePO feature = featureMapper.selectById(id);
        if (feature != null) {
            return featureMapping.sourceToTarget(feature);
        }
        return null;
    }

    @Override
    public CommonResult query(FeatureQuery query) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.getData().put("page", queryPage(query));
        return result;
    }

    private PageResult<FeatureVO> queryPage(FeatureQuery query) {
        Page<FeaturePO> p = featureMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<FeaturePO>()
                        .eq(query.getModelId() != null, FeaturePO::getModelId, query.getModelId())
                        .eq(!StringUtils.isEmpty(query.getType()), FeaturePO::getType, query.getType())
                        .like(!StringUtils.isEmpty(query.getLabel()), FeaturePO::getLabel, query.getLabel())
                        .eq(query.getStatus() != null, FeaturePO::getStatus, query.getStatus())
                        .orderByAsc(FeaturePO::getOrderNo)
                        .orderByAsc(FeaturePO::getId));
        List<FeatureVO> listVO = CollectionUtils.isEmpty(p.getRecords())
                ? new ArrayList<>() : featureMapping.sourceToTarget(p.getRecords());
        return new PageResult<>((int) p.getCurrent(), (int) p.getSize(), (int) p.getTotal(), listVO);
    }

    @Override
    public CommonResult save(FeatureVO feature) {
        CommonResult result = new CommonResult();
        // 同模型下 label 查重（沿袭原 PreItem/Abstraction 语义）
        Long count = featureMapper.selectCount(
                new LambdaQueryWrapper<FeaturePO>()
                        .eq(FeaturePO::getModelId, feature.getModelId())
                        .eq(FeaturePO::getLabel, feature.getLabel())
                        .ne(feature.getId() != null, FeaturePO::getId, feature.getId()));
        if (count != null && count > 0) {
            result.setMsg("特征名已存在");
            return result;
        }
        FeatureType.of(feature.getType());// 合法性预检（非法值落库后在编排层被隔离跳过）
        Date sysDate = new Date();
        FeaturePO featurePO = featureMapping.targetToSource(feature);
        if (feature.getId() == null) {
            featurePO.setCreateTime(sysDate);
            featurePO.setUpdateTime(sysDate);
            featureMapper.insert(featurePO);
            feature.setId(featurePO.getId());
        } else {
            featurePO.setUpdateTime(sysDate);
            featureMapper.updateById(featurePO);
        }
        if (StringUtils.isEmpty(feature.getFeatureName())) {
            feature.setFeatureName("feature_" + feature.getId());
            FeaturePO rename = featureMapping.targetToSource(feature);
            rename.setUpdateTime(sysDate);
            featureMapper.updateById(rename);
        }
        result.getData().put("id", feature.getId());
        result.setSuccess(true);
        // 通知更新
        cacheService.publishFeature(feature);
        return result;
    }

    @Override
    public CommonResult delete(Long[] id) {
        CommonResult result = new CommonResult();
        List<FeatureVO> features = new ArrayList<>();
        for (Long fid : id) {
            FeatureVO vo = get(fid);
            if (vo != null) {
                features.add(vo);
            }
        }
        int count = featureMapper.deleteBatchIds(Arrays.asList(id));
        if (count > 0) {
            result.setSuccess(true);
            for (FeatureVO vo : features) {
                cacheService.publishFeature(vo);
            }
        }
        return result;
    }

    @Override
    public CommonResult updateStatus(Long featureId, Integer status) {
        CommonResult result = new CommonResult();
        FeatureVO feature = get(featureId);
        if (feature != null) {
            feature.setStatus(status);
            FeaturePO featurePO = featureMapping.targetToSource(feature);
            featurePO.setUpdateTime(new Date());
            featureMapper.updateById(featurePO);
            result.setSuccess(true);
            cacheService.publishFeature(feature);
        }
        return result;
    }
}
