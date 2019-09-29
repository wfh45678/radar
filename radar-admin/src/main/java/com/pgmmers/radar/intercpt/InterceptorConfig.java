package com.pgmmers.radar.intercpt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 接口验证,session 过期验证。
 */
//@Configuration
public class InterceptorConfig extends WebMvcConfigurerAdapter {

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/services/**");
        super.addInterceptors(registry);
    }

}