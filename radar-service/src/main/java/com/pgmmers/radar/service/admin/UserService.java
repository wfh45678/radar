package com.pgmmers.radar.service.admin;


import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.admin.UserVO;

import java.util.List;
import java.util.Optional;

public interface UserService {

    CommonResult query(UserQuery query);

    Optional<List<UserVO>> list(String userName, String passwd);

    UserVO getByUserName(String userName);

    Integer insert(UserVO userVO);
}
