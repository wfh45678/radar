package com.pgmmers.radar.controller;


import com.pgmmers.radar.dal.bean.UserQuery;
import com.pgmmers.radar.service.admin.UserService;
import com.pgmmers.radar.service.common.CommonResult;
import com.pgmmers.radar.util.CryptUtils;
import com.pgmmers.radar.vo.admin.UserVO;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/services/v1")
@Api(value = "SysLoginAPI", description = "用户登录相关操作接口",  tags = {"用户登录相关API"})
public class SysLoginApiController {

    private static final Logger logger = LoggerFactory.getLogger(SysLoginApiController.class);

    @Autowired
    private UserService userService;


    @PostMapping("/merchant/login")
    public CommonResult login(String loginName, String passwd, String captcha, HttpServletRequest request) {
        CommonResult ret = new CommonResult();


        HttpSession session = request.getSession(true);
        String sourceCaptcha = (String) session.getAttribute("captcha");
        session.removeAttribute("captcha");
        if (captcha == null || !captcha.equalsIgnoreCase(sourceCaptcha) ) {
            ret.setMsg("验证码无效");
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
            session.setAttribute("user", users.get(0));
            ret.setSuccess(true);
        } else {
            ret.setMsg("用户名和密码错误！");
        }

        return ret;
    }

    @GetMapping("/merchant/logout")
	public CommonResult logout(HttpServletRequest request) {
		CommonResult ret = new CommonResult();

        request.getSession(true).invalidate();
        ret.setSuccess(true);
        return ret;
	}

    /**
     * 注册接口
     * @param request
     * @return
     */
    @GetMapping("/merchant/regist")
    public CommonResult regist(HttpServletRequest request) {
        CommonResult ret = new CommonResult();
        ret.setSuccess(true);
        return ret;
    }
}
