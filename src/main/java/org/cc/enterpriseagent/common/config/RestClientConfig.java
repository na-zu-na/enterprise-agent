package org.cc.enterpriseagent.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


//向其他的服务器发送消息
@Configuration
public class RestClientConfig {
    @Bean
    public static RestClient restClient(){
        return RestClient.builder().build();
    }
}
