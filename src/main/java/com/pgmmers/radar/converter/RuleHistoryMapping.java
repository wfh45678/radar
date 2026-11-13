package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.RuleHistoryPO;
import com.pgmmers.radar.model.vo.model.RuleHistoryVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleHistoryMapping extends BaseMapping<RuleHistoryPO, RuleHistoryVO> {

}
