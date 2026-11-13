package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.ModelPO;
import com.pgmmers.radar.model.vo.model.ModelVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModelMapping extends BaseMapping<ModelPO, ModelVO> {

}
