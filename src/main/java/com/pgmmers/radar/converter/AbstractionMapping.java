package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.AbstractionPO;
import com.pgmmers.radar.model.vo.model.AbstractionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbstractionMapping extends BaseMapping<AbstractionPO, AbstractionVO> {

    @Mapping(target = "dataCollectionNames", ignore = true)
    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getRuleDefinition()))")
    @Override
    AbstractionVO sourceToTarget(AbstractionPO var1);

    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getRuleDefinition()))")
    @Override
    AbstractionPO targetToSource(AbstractionVO var1);

}
