package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.DataListsPO;
import com.pgmmers.radar.vo.model.DataListsVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListsMapping extends BaseMapping<DataListsPO, DataListsVO> {

}
