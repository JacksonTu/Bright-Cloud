package com.tml.gateway.doc.configure;

import com.google.common.collect.Lists;
import com.tml.common.core.entity.constant.CacheConstant;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.gateway.doc.properties.BrightDocGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(value = "bright.doc.gateway.enable", havingValue = "true", matchIfMissing = true)
public class BrightDocGatewayResourceConfigure implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;

    private final RedisTemplate redisTemplate;

    private final DiscoveryClient discoveryClient;

    private final BrightDocGatewayProperties brightDocGatewayProperties;

    public BrightDocGatewayResourceConfigure(RouteLocator routeLocator, RedisTemplate redisTemplate, DiscoveryClient discoveryClient, BrightDocGatewayProperties brightDocGatewayProperties) {
        this.routeLocator = routeLocator;
        this.redisTemplate = redisTemplate;
        this.discoveryClient = discoveryClient;
        this.brightDocGatewayProperties = brightDocGatewayProperties;
    }

    @Override
    public List<SwaggerResource> get() {
        List<RouteDefinition> routeDefinitions = Lists.newArrayList();
        redisTemplate.opsForHash().values(CacheConstant.GATEWAY_DYNAMIC_ROUTE_CACHE)
                .stream()
                .forEach(routeDefinition -> routeDefinitions.add(JacksonUtil.toObject(routeDefinition.toString(), RouteDefinition.class)));

        /**
         * 真实的微服务
         */
        String realResources = brightDocGatewayProperties.getResources();
        List<String> realResourceList = stringToList(realResources);

        List<SwaggerResource> resources = new ArrayList<>();
        List<String> routes = new ArrayList<>();
        routeLocator.getRoutes().subscribe(route -> routes.add(route.getId()));
        routeDefinitions.stream()
                .filter(routeDefinition -> routes.contains(routeDefinition.getId()))
                .filter(routeDefinition -> realResourceList.contains(routeDefinition.getId()))
                .forEach(route -> {
                    route.getPredicates().stream()
                            .filter(predicateDefinition -> ("Path").equalsIgnoreCase(predicateDefinition.getName()))
                            .forEach(predicateDefinition -> resources.add(swaggerResource(route.getId(),
                                    predicateDefinition.getArgs().get("pattern")
                                            .replace("**", "v2/api-docs"))));
                });

        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        log.info("name:{},location:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }

    private List<String> stringToList(String s) {
        if (StringUtils.isNoneBlank(s)) {
            String[] arr = s.toLowerCase().split(",");
            return Arrays.asList(arr);
        }
        return null;
    }
}

