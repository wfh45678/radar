package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.MobileInfoPO;
import com.pgmmers.radar.vo.data.MobileInfoVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MobileInfoMapping extends BaseMapping<MobileInfoPO, MobileInfoVO> {

}
