package org.cc.enterpriseagent.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


//向其他的服务器发送消息
@Configuration
public class RestClientConfig {
    @Bean
    public RestClient restClient(@Value("${ai.service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
