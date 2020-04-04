package com.pgmmers.radar.service.impl.admin;


import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.dal.model.UserDal;
import com.pgmmers.radar.mapstruct.UserMapping;
import com.pgmmers.radar.model.UserPO;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.vo.admin.UserVO;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDal userDal;
    @Resource
    private UserMapping userMapping;

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
        List<UserVO> users = list.stream().map(userPO -> userMapping.sourceToTarget(userPO)).collect(Collectors.toList());
        return Optional.ofNullable(users);
    }

    @Override
    public UserVO getByUserName(String userName) {
        UserQuery query = new UserQuery();
        query.setUserName(userName);
        PageResult<UserPO> result = userDal.query(query);
        UserVO  userVO = null;
        if (result.getRowCount() > 0) {
            userVO = userMapping.sourceToTarget(result.getList().get(0));
        }
        return userVO;
    }

    @Override
    public Integer insert(UserVO userVO) {
        UserPO userPO = userMapping.targetToSource(userVO);
        Integer integer = userDal.insert(userPO);
        return integer;
    }
}
