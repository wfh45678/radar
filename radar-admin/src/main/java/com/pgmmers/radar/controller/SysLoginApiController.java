package com.pgmmers.radar.controller;


import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.intercpt.ContextHolder;
import com.pgmmers.radar.intercpt.JsonWebTokenService;
import com.pgmmers.radar.intercpt.TokenBody;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.cache.CacheService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.util.CryptUtils;
import com.pgmmers.radar.vo.admin.UserVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/services/v1")
@Api(value = "SysLoginAPI", description = "用户登录相关操作接口",  tags = {"用户登录相关API"})
public class SysLoginApiController {

    private static final Logger logger = LoggerFactory.getLogger(SysLoginApiController.class);

    @Autowired
    private UserService userService;

    @Autowired
    ContextHolder contextHolder;

    @Autowired
    private JsonWebTokenService tokenService;

    @Autowired
    private CacheService cacheService;

    @PostMapping("/merchant/login")
    public CommonResult login(String loginName, String passwd, String captcha) {
        CommonResult ret = new CommonResult();
        ret.setSuccess(false);
        String checkResult = checkParam(loginName, passwd, passwd, captcha);
        if(!StringUtils.isEmpty(checkResult)){
            ret.setMsg(checkResult);
            return ret;
        }

        String encrypt = CryptUtils.sha(passwd, loginName).toUpperCase();
        logger.info("passwd sha:{}" , encrypt);
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(loginName);
        userQuery.setPasswd(encrypt);
        Optional<List<UserVO>> list = userService.list(loginName, encrypt);
        if (list.isPresent()) {
            List<UserVO> users = list.get();
            if(users.size() > 0) {
                String token = tokenService.createToken(new TokenBody(users.get(0).getUserName(), users.get(0).getUserName(), "",
                        2 * 3600, new HashMap<String, Object>(){
                    {
                        put("code", users.get(0).getCode());
                    }
                }));
                ret.getData().put("x-auth-token", token);
                ret.setSuccess(true);
            } else {
                ret.setMsg("用户名和密码错误！");
            }

        } else {
            ret.setMsg("用户名和密码错误！");
        }

        return ret;
    }

    @GetMapping("/merchant/logout")
	public CommonResult logout(HttpServletRequest request) {
		CommonResult ret = new CommonResult();
        String accessToken = contextHolder.getAttributeByType("x-auth-token", String.class);
        tokenService.addTokenBlacklist(accessToken);
        ret.setSuccess(true);
        return ret;
	}

    /**
     * 注册接口
     * @param
     * @return
     * @author xushuai
     */
    @PostMapping("/merchant/regist")
    public CommonResult regist(String loginName, String passwd, String verifyPasswd, String captcha, String giteeAccount) {
        CommonResult result = new CommonResult();

        String checkResult = checkParam(loginName, passwd, verifyPasswd, captcha);
        if(!StringUtils.isEmpty(checkResult)){
            result.setMsg(checkResult);
            return result;
        }

        // 校验账号的唯一性
        UserQuery userQuery = new UserQuery();
        userQuery.setUserName(loginName);
        CommonResult userResult = userService.query(userQuery);
        if (userResult.isSuccess()) {
            result.setMsg("用户名已经存在！");
            return result;
        }
        String encrypt = CryptUtils.sha(passwd, loginName).toUpperCase();
        UserVO userVO = new UserVO();
        userVO.setUserName(loginName);
        userVO.setPasswd(encrypt);
        userVO.setStatus("1");
        userVO.setVipLevel(0);
        userVO.setGiteeAccount(giteeAccount);
        userVO.setCode(loginName);
        userVO.setCreateTime(new Date());
        userVO.setUpdateTime(new Date());
        Integer integer = userService.insert(userVO);
        if (integer < 1) {
            result.setMsg("注册失败");
            return result;
        }
        result.setSuccess(true);
        result.setMsg("注册成功");
        return result;
    }

    private String checkParam(String loginName, String passwd, String verifyPasswd, String captcha){
        if(StringUtils.isEmpty(loginName)){
            return "登录名称不能为空";
        }
        if(StringUtils.isEmpty(passwd)){
            return "密码不能为空";
        }
        if(StringUtils.isEmpty(captcha)){
            return "验证码不能为空";
        }
        boolean validateCaptcha = cacheService.validateCaptcha(captcha);
        if (!validateCaptcha ) {
            return "验证码无效";
        }
        if(!passwd.equals(verifyPasswd)){
            return "新密码和确认密码输入不一致,请重新输入!";
        }
        return "";
    }
}
