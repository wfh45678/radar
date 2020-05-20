package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.ActivationPO;
import com.pgmmers.radar.vo.model.ActivationVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivationMapping extends BaseMapping<ActivationPO, ActivationVO> {

}
