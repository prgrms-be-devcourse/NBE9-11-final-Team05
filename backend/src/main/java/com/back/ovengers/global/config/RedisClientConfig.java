package com.back.ovengers.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RedisClientConfig {
    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
