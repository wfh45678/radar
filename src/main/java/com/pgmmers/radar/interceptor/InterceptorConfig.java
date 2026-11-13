package com.pgmmers.radar.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 接口验证,session 过期验证。
 * 单体融合后：JWT 拦截器仍只作用于管理端 /services/**，
 * 其中引擎端事件接口与管理端免登录接口通过排除路径放行。
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/services/**")
                .excludePathPatterns(
                        // 引擎端公开接口（事件接入）
                        "/services/v1/upload",
                        "/services/v1/uploadInfo",
                        "/services/v1/getScore",
                        "/services/v1/syncStatus",
                        // 管理端免登录接口
                        "/services/v1/user/login",
                        "/services/v1/user/regist",
                        "/services/v1/common/**");
    }

}
