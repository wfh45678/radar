package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.DataListsPO;
import com.pgmmers.radar.model.vo.model.DataListsVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListsMapping extends BaseMapping<DataListsPO, DataListsVO> {

}
