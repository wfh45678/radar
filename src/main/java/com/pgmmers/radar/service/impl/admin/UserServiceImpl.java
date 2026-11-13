package com.pgmmers.radar.service.impl.admin;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pgmmers.radar.mapper.UserMapper;
import com.pgmmers.radar.model.query.PageResult;
import com.pgmmers.radar.model.query.UserQuery;
import com.pgmmers.radar.converter.UserMapping;
import com.pgmmers.radar.model.entity.UserPO;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.model.vo.admin.UserVO;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Resource
    private UserMapping userMapping;

    @Override
    public CommonResult query(UserQuery query) {
        CommonResult result = new CommonResult();
        PageResult<UserPO> page = queryPage(query);
        if (page.getList().size() > 0) {
            result.getData().put("page", page);
            result.setSuccess(true);
        }
        return result;
    }

    private PageResult<UserPO> queryPage(UserQuery query) {
        Page<UserPO> p = userMapper.selectPage(
                new Page<>(query.getPageNo(), query.getPageSize()),
                new LambdaQueryWrapper<UserPO>()
                        .eq(query.getUserName() != null, UserPO::getUserName, query.getUserName())
                        .eq(query.getPasswd() != null, UserPO::getPasswd, query.getPasswd()));
        return new PageResult<>(p);
    }

    @Override
    public Optional<List<UserVO>> list(String userName, String passwd) {
        List<UserPO> list = userMapper.selectList(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUserName, userName)
                        .eq(UserPO::getPasswd, passwd));
        List<UserVO> users = list.stream().map(userPO -> userMapping.sourceToTarget(userPO)).collect(Collectors.toList());
        return Optional.ofNullable(users);
    }

    @Override
    public UserVO getByUserName(String userName) {
        UserQuery query = new UserQuery();
        query.setUserName(userName);
        PageResult<UserPO> result = queryPage(query);
        UserVO  userVO = null;
        if (result.getRowCount() > 0) {
            userVO = userMapping.sourceToTarget(result.getList().get(0));
        }
        return userVO;
    }

    @Override
    public Integer insert(UserVO userVO) {
        UserPO userPO = userMapping.targetToSource(userVO);
        return userMapper.insert(userPO);
    }

    @Override
    public void updateLastLoginTime(UserVO user) {
        UserPO userPO = new UserPO();
        userPO.setId(user.getId());
        userPO.setUpdateTime(new Date());
        userMapper.updateById(userPO);
    }
}
