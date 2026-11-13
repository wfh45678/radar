package com.pgmmers.radar.converter;


import com.pgmmers.radar.model.entity.FieldPO;
import com.pgmmers.radar.model.vo.model.FieldVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FieldMapping extends BaseMapping<FieldPO, FieldVO> {

}
