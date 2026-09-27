package com.flowershop.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
            .title("网上花店平台接口文档")
            .description("基于 Spring Boot 的网上花店平台后端接口")
            .version("1.0.0")
            .contact(new Contact().name("员工A GPT-5.4")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("认证与公共接口")
            .pathsToMatch("/api/auth/**", "/api/common/**")
            .build();
    }

    @Bean
    public GroupedOpenApi customerApi() {
        return GroupedOpenApi.builder()
            .group("买家端接口")
            .pathsToMatch("/api/customer/**")
            .build();
    }

    @Bean
    public GroupedOpenApi merchantApi() {
        return GroupedOpenApi.builder()
            .group("商家端接口")
            .pathsToMatch("/api/merchant/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("管理员端接口")
            .pathsToMatch("/api/admin/**")
            .build();
    }
}
