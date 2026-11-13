package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.RulePO;
import com.pgmmers.radar.model.vo.model.RuleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RuleMapping extends BaseMapping<RulePO, RuleVO> {
    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.getJsonNode(var1.getRuleDefinition()))")
    @Override
    RuleVO sourceToTarget(RulePO var1);


    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.common.util.JsonUtils.jsonNodeToString(var1.getRuleDefinition()))")
    @Override
    RulePO targetToSource(RuleVO var1);

}
