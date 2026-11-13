package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.DataListMetaPO;
import com.pgmmers.radar.model.vo.model.DataListMetaVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListMetaMapping extends BaseMapping<DataListMetaPO, DataListMetaVO> {

}
