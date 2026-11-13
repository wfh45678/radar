package com.pgmmers.radar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 配置（替代 springfox SwaggerConfig）。
 * 管理端与引擎端 API 分组展示，管理端使用 x-auth-token 请求头鉴权。
 *
 * @author feihu.wang
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI radarOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Radar API")
                        .description("risk engine api documentation")
                        .termsOfService("http://radar.pgmmer.top")
                        .contact(new Contact().name("Radar")
                                .url("http://radar.pgmmer.top")
                                .email("wfh45678@163.com")))
                .components(new Components().addSecuritySchemes("x-auth-token",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("x-auth-token")))
                .addSecurityItem(new SecurityRequirement().addList("x-auth-token"));
    }

    /**
     * 管理端 API 分组（JWT 保护，除登录/注册/公共接口外）。
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("管理端 API")
                .packagesToScan("com.pgmmers.radar.controller.admin")
                .pathsToMatch("/services/**")
                .build();
    }

    /**
     * 引擎端 API 分组（公开，事件接入）。
     */
    @Bean
    public GroupedOpenApi engineApi() {
        return GroupedOpenApi.builder()
                .group("engine")
                .displayName("引擎端 API")
                .packagesToScan("com.pgmmers.radar.controller.engine")
                .pathsToMatch("/services/**")
                .build();
    }
}
