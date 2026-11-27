package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.StrategyPO;
import com.pgmmers.radar.model.vo.model.StrategyVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StrategyMapping extends BaseMapping<StrategyPO, StrategyVO> {

    @Mapping(target = "definition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getDefinition()))")
    @Override
    StrategyVO sourceToTarget(StrategyPO var1);

    @Mapping(target = "definition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getDefinition()))")
    @Override
    StrategyPO targetToSource(StrategyVO var1);
}
