package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.FeaturePO;
import com.pgmmers.radar.model.vo.model.FeatureVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeatureMapping extends BaseMapping<FeaturePO, FeatureVO> {

    @Mapping(target = "definition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getDefinition()))")
    @Mapping(target = "output", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getOutput()))")
    @Override
    FeatureVO sourceToTarget(FeaturePO var1);

    @Mapping(target = "definition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getDefinition()))")
    @Mapping(target = "output", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getOutput()))")
    @Override
    FeaturePO targetToSource(FeatureVO var1);
}
