package org.cc.enterpriseagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class EnterpriseAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseAgentApplication.class, args);
    }

}
