package com.pgmmers.radar.intercpt;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.pgmmers.radar.service.common.CommonResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 目前这个类是预留的， session 这一块，后续会考虑升级成 JWB(JSON WEB TOKEN).
 * @author feihu.wang
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private ContextHolder contextHolder;

    @Autowired
    private JsonWebTokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HandlerMethod method = (HandlerMethod) handler;
        logger.info("Interceptor preHandle, {}", method.getMethod().getName());
        String uri = request.getRequestURI();
        logger.info("uri:{}", uri);
        CommonResult result = new CommonResult();
        Map<String, Object> additionalProperties = Maps.newHashMap();
        if (uri.startsWith("/services") && !uri.contains("/user/login") && !uri.contains("/user/regist") && !uri.contains("/common")) {
            Context context = new Context();
            String accessToken = request.getHeader("x-auth-token");
            if(StringUtils.isEmpty(accessToken)) {
                accessToken = request.getParameter("x-auth-token");
            }
            if (StringUtils.isEmpty(accessToken)) {
                result.setCode("601");
                result.setMsg("无token，请重新登录");
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().write(JSON.toJSONString(result));
                return false;
            }
            if (tokenService.existsTokenBlacklist(accessToken)) {
                result.setCode("602");
                result.setMsg("token失效,请重新登录");
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().write(JSON.toJSONString(result));
                return false;
            }
            try {
                TokenBody tokenBody = tokenService.validateToken(accessToken);
                context.setUsername(tokenBody.getSubject());
                context.setDisplayName(tokenBody.getDisplayName());
                context.setOperationName("");
                context.setCode((String)tokenBody.getAdditionalProperties().get("code"));
                additionalProperties.put("x-auth-token", accessToken);
                context.setAttributes(additionalProperties);
                contextHolder.putContext(context);
            } catch (Exception e) {
                logger.error("token 解析失败", e);
                result.setCode("603");
                result.setMsg("token解析失败,请重新获取");
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().write(JSON.toJSONString(result));
                return false;
            }
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        HandlerMethod method = (HandlerMethod) handler;
        logger.info("HandlerInterceptor postHandle, {}", method.getMethod().getName());

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        HandlerMethod method = (HandlerMethod) handler;
        contextHolder.clearContext();
        logger.info("HandlerInterceptor afterCompletion, {}", method.getMethod().getName());
    }

}