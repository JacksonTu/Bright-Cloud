package com.tml.gateway.enhance.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Data
@ConfigurationProperties(prefix = "bright.gateway")
public class BrightCloudGatewayProperties {

    /**
     * 是否开启安全配置
     */
    private Boolean enhance;

    /**
     * 免认证资源路径，支持通配符
     */
    private List<String> anonUris;

}
