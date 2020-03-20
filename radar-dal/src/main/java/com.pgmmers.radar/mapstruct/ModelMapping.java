package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.ModelPO;
import com.pgmmers.radar.vo.model.ModelVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ModelMapping extends BaseMapping<ModelPO, ModelVO> {

}
