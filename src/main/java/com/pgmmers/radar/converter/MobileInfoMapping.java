package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.MobileInfoPO;
import com.pgmmers.radar.model.vo.data.MobileInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MobileInfoMapping extends BaseMapping<MobileInfoPO, MobileInfoVO> {

}
