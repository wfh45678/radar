package com.pgmmers.radar.service.impl.admin;


import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.dal.model.UserDal;
import com.pgmmers.radar.model.UserPO;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.admin.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDal userDal;

    @Override
    public CommonResult query(UserQuery query) {
        CommonResult result = new CommonResult();
        PageResult<UserPO> page = userDal.query(query);
        if (page.getList().size() > 0) {
            result.getData().put("page", page);
            result.setSuccess(true);
        }
        return result;
    }

    @Override
    public Optional<List<UserVO>> list(String userName, String passwd) {
        List<UserPO> list = userDal.list(userName, passwd);
        List<UserVO> users= list.stream().map(userPO -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(userPO, vo);
            return vo;
        }).collect(Collectors.toList());
        return Optional.ofNullable(users);
    }

}
