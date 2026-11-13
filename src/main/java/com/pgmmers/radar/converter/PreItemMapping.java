package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.PreItemPO;
import com.pgmmers.radar.model.vo.model.PreItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PreItemMapping extends BaseMapping<PreItemPO, PreItemVO> {
    @Mapping(target = "configJson", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getConfigJson()))")
    @Override
    PreItemVO sourceToTarget(PreItemPO var1);


    @Mapping(target = "configJson", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getConfigJson()))")
    @Override
    PreItemPO targetToSource(PreItemVO var1);


}
