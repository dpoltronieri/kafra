package com.dpoltronieri.kafra.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class JdaConfig {

    @Value("${spring.jda.token}")
    private String token;

    public String getToken() {
        return token;
    }
}