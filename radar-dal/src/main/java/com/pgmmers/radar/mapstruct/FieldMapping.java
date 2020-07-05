package com.pgmmers.radar.mapstruct;


import com.pgmmers.radar.model.FieldPO;
import com.pgmmers.radar.vo.model.FieldVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FieldMapping extends BaseMapping<FieldPO, FieldVO> {

}
