package com.pgmmers.radar.mapstruct;

import com.pgmmers.radar.model.UserPO;
import com.pgmmers.radar.vo.admin.UserVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapping extends BaseMapping<UserPO, UserVO> {

}
