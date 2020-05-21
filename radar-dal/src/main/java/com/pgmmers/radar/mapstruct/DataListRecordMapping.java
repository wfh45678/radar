package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.DataListRecordPO;
import com.pgmmers.radar.vo.model.DataListRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DataListRecordMapping extends BaseMapping<DataListRecordPO, DataListRecordVO> {

}
