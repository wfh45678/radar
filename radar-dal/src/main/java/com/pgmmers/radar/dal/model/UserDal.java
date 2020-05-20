package com.pgmmers.radar.dal.model;

import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.model.UserPO;

import java.util.List;

public interface UserDal {
     PageResult<UserPO> query(UserQuery query);

     List<UserPO> list(String name, String pass);

     Integer insert(UserPO userPO);
}
