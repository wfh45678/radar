package com.pgmmers.radar.config;

import com.pgmmers.radar.intercpt.JsonWebTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建tokenserviceBean
 * @author xushuai
 */
@Configuration
public class TokenServerConfig {
    public TokenServerConfig() {
    }

    @Bean
    public JsonWebTokenService tokenService() {
        return new JsonWebTokenService("HS256", "radar_123456");
    }
}