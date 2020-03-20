package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.DataListMetaPO;
import com.pgmmers.radar.vo.model.DataListMetaVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListMetaMapping extends BaseMapping<DataListMetaPO, DataListMetaVO> {

}
