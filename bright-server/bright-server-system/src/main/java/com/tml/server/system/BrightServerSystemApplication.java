package com.tml.server.system;

import com.tml.common.starter.security.annotation.EnableBrightCloudResourceServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableBrightCloudResourceServer
@MapperScan("com.tml.server.system.mapper")
@EnableBinding(Processor.class)
@RefreshScope
public class BrightServerSystemApplication {

    public static void main(String[] args) {

        new SpringApplicationBuilder(BrightServerSystemApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
}
