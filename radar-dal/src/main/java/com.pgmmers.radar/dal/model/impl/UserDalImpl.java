package com.pgmmers.radar.dal.model.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pgmmers.radar.dal.bean.PageResult;
import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.dal.model.UserDal;
import com.pgmmers.radar.mapper.UserMapper;
import com.pgmmers.radar.model.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDalImpl implements UserDal {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<UserPO> query(UserQuery query) {
        PageHelper.startPage(query.getPageNo(), query.getPageSize());


        UserPO userPO = new UserPO();
        userPO.setUserName(query.getUserName());
        userPO.setPasswd(query.getPasswd());
        Example userExample = new Example(UserPO.class);
        userExample.createCriteria().andEqualTo(userPO);

        List<UserPO> users = userMapper.selectByExample(userExample);

        if (users == null ) {
            users = new ArrayList<>();
        }

        Page<UserPO> page = (Page<UserPO>) users;

        PageResult<UserPO> pageResult = new PageResult<>(page.getPageNum(), page.getPageSize(),
                (int) page.getTotal(), users);
        return pageResult;
    }

    @Override
    public List<UserPO> list(String name, String pass) {
        Example example = new Example(UserPO.class);
        example.createCriteria().andEqualTo("userName", name).andEqualTo("passwd",pass);
        List<UserPO> list = userMapper.selectByExample(example);
        return list;
    }

    @Override
    public Integer insert(UserPO userPO) {
        Integer integer = userMapper.insert(userPO);
        return integer;
    }

}
