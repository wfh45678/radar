package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.ScenarioPO;
import com.pgmmers.radar.model.vo.model.ScenarioVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScenarioMapping extends BaseMapping<ScenarioPO, ScenarioVO> {

}
