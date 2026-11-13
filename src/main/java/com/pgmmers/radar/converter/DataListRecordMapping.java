package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.DataListRecordPO;
import com.pgmmers.radar.model.vo.model.DataListRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListRecordMapping extends BaseMapping<DataListRecordPO, DataListRecordVO> {

}
