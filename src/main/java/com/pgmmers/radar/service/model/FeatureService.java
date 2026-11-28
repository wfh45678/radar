package com.pgmmers.radar.service.model;

import com.pgmmers.radar.model.query.FeatureQuery;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import com.pgmmers.radar.service.common.CommonResult;
import java.util.List;

/**
 * 特征服务（v4.1 特征工程重构）：统一管理 ENRICH/AGGREGATE/DERIVED/RATIO 四类特征。
 */
public interface FeatureService {

    List<FeatureVO> listFeature(Long modelId);

    FeatureVO get(Long id);

    CommonResult query(FeatureQuery query);

    CommonResult save(FeatureVO feature);

    CommonResult delete(Long[] id);

    CommonResult updateStatus(Long featureId, Integer status);
}
