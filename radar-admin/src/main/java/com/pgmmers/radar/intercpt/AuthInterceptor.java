package com.pgmmers.radar.intercpt;


import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        Map<String, Object> additionalProperties = Maps.newHashMap();
    //    Object user = request.getSession().getAttribute("user");
        if (uri.startsWith("/services") && !uri.contains("/merchant/login") && !uri.contains("/merchant/regist") && !uri.contains("/common")) {
            //            if (user == null) {
//                logger.info("session expired.");
//                return true;
//            }
            Context context = new Context();
            String accessToken = request.getHeader("X-Access-Token");
            if(StringUtils.isEmpty(accessToken)) {
                accessToken = request.getParameter("Header-Access-Token");
            }
            if (StringUtils.isEmpty(accessToken)) {
                throw new RuntimeException("无token，请重新登录");
            }
            if(tokenService.existsTokenBlacklist(accessToken)){
                throw new RuntimeException("token已登出失效,请重新登录");
            }
            TokenBody result = tokenService.validateToken(accessToken);
            context.setUsername(result.getSubject());
            context.setDisplayName(result.getDisplayName());
            context.setOperationName("");
            additionalProperties.put("X-Access-Token", accessToken);
            context.setAttributes(additionalProperties);
            contextHolder.putContext(context);
            return true;
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