package com.pgmmers.radar.intercpt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HandlerMethod method = (HandlerMethod) handler;
        logger.info("Interceptor preHandle, {}", method.getMethod().getName());
        String uri = request.getRequestURI();
        logger.info("uri:{}", uri);
        Object user = request.getSession().getAttribute("user");
        if (uri.startsWith("/services")&& !uri.contains("/merchant") && !uri.contains("/common")) {

            if (user == null) {
                logger.info("session expired, redirect to index.html");
                return true;
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
        logger.info("HandlerInterceptor afterCompletion, {}", method.getMethod().getName());
    }

}