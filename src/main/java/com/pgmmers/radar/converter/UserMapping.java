package com.pgmmers.radar.converter;

import com.pgmmers.radar.model.entity.UserPO;
import com.pgmmers.radar.model.vo.admin.UserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapping extends BaseMapping<UserPO, UserVO> {

}
