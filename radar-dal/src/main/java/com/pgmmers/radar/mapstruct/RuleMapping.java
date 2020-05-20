package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.RulePO;
import com.pgmmers.radar.vo.model.RuleVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RuleMapping extends BaseMapping<RulePO, RuleVO> {
    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.util.JsonUtils.getJsonNode(var1.getRuleDefinition()))")
    @Override
    RuleVO sourceToTarget(RulePO var1);


    @Mapping(target = "ruleDefinition", expression = "java(com.pgmmers.radar.util.JsonUtils.jsonNodeToString(var1.getRuleDefinition()))")
    @Override
    RulePO targetToSource(RuleVO var1);

}
