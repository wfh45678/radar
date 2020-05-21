package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.AbstractionPO;
import com.pgmmers.radar.vo.model.AbstractionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbstractionMapping extends BaseMapping<AbstractionPO, AbstractionVO> {

    @Mapping(target = "dataCollectionNames", source = "")
    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.util.JsonUtils.getJsonNode(var1.getRuleDefinition()))")
    @Override
    AbstractionVO sourceToTarget(AbstractionPO var1);

    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.util.JsonUtils.jsonNodeToString(var1.getRuleDefinition()))")
    @Override
    AbstractionPO targetToSource(AbstractionVO var1);

}
