package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.RuleHistoryPO;
import com.pgmmers.radar.vo.model.RuleHistoryVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RuleHistoryMapping extends BaseMapping<RuleHistoryPO, RuleHistoryVO> {

}
