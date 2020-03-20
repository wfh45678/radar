package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.PreItemPO;
import com.pgmmers.radar.vo.model.PreItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PreItemMapping extends BaseMapping<PreItemPO, PreItemVO> {
    @Mapping(target = "configJson", expression = "java(com.pgmmers.radar.util.JsonUtils.getJsonNode(var1.getConfigJson()))")
    @Override
    PreItemVO sourceToTarget(PreItemPO var1);


    @Mapping(target = "configJson", expression = "java(com.pgmmers.radar.util.JsonUtils.jsonNodeToString(var1.getConfigJson()))")
    @Override
    PreItemPO targetToSource(PreItemVO var1);


}
