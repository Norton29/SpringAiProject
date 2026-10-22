package com.nff.springiaproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader("X-API-VERSION")
                .addSupportedVersions(SUPPORTED_VERSIONS)
                .setDefaultVersion("v1");
    }
}
