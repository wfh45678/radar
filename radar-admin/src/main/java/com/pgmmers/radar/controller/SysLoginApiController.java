package com.pgmmers.radar.controller;


import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.intercpt.ContextHolder;
import com.pgmmers.radar.intercpt.JsonWebTokenService;
import com.pgmmers.radar.intercpt.TokenBody;
import com.pgmmers.radar.service.admin.UserService;
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
import javax.servlet.http.HttpSession;
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

    @PostMapping("/merchant/login")
    public CommonResult login(String loginName, String passwd, String captcha, HttpServletRequest request) {
        CommonResult ret = new CommonResult();
        ret.setSuccess(false);
        HttpSession session = request.getSession(true);
        String checkResult = checkParam(loginName, passwd, captcha, session);
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
                session.setAttribute("user", users.get(0));
                String token = tokenService.createToken(new TokenBody(users.get(0).getUserName(), "", "",
                        2 * 3600, new HashMap<>()));
                ret.getData().put("x-auth-token", token);
                ret.setSuccess(true);
            } else
                ret.setMsg("用户名和密码错误！");
        } else {
            ret.setMsg("用户名和密码错误！");
        }

        return ret;
    }

    @GetMapping("/merchant/logout")
	public CommonResult logout(HttpServletRequest request) {
		CommonResult ret = new CommonResult();
        String accessToken = contextHolder.getAttributeByType("x-auth-token", String.class);
        tokenService.logout(accessToken);
        request.getSession(true).invalidate();
        ret.setSuccess(true);
        return ret;
	}

    /**
     * 注册接口
     * @param request
     * @return
     * @author xushuai
     */
    @PostMapping("/merchant/regist")
    public CommonResult regist(String loginName, String passwd, String verifyPasswd, String captcha, HttpServletRequest request) {
        CommonResult ret = new CommonResult();

        HttpSession session = request.getSession(true);
        String checkResult = checkParam(loginName, passwd, captcha, session);
        if(!StringUtils.isEmpty(checkResult)){
            ret.setMsg(checkResult);
            return ret;
        }
        if(!passwd.equals(verifyPasswd)){
            ret.setMsg("新密码和确认密码输入不一致,请重新输入!");
            return ret;
        }
        String encrypt = CryptUtils.sha(passwd, loginName).toUpperCase();
        UserVO userVO = new UserVO();
        userVO.setUserName(loginName);
        userVO.setPasswd(encrypt);
        userVO.setStatus("1");
        userVO.setCode(loginName);
        userVO.setCreateTime(new Date());
        userVO.setUpdateTime(new Date());
        Integer integer = userService.insert(userVO);
        if (integer < 1) {
            ret.setMsg("注册失败");
            return ret;
        }
        ret.setSuccess(true);
        ret.setMsg("注册成功");
        return ret;
    }

    private String checkParam(String loginName, String passwd, String captcha, HttpSession session){
        if(StringUtils.isEmpty(loginName)){
            return "登录名称不能为空";
        }
        if(StringUtils.isEmpty(passwd)){
            return "密码不能为空";
        }
        if(StringUtils.isEmpty(captcha)){
            return "验证码不能为空";
        }
        String sourceCaptcha = (String) session.getAttribute("captcha");
        session.removeAttribute("captcha");
        if (captcha == null || !captcha.equalsIgnoreCase(sourceCaptcha) ) {
            return "验证码无效";
        }
        return "";
    }
}
