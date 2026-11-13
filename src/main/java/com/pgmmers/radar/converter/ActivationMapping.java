package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.ActivationPO;
import com.pgmmers.radar.model.vo.model.ActivationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivationMapping extends BaseMapping<ActivationPO, ActivationVO> {

}
