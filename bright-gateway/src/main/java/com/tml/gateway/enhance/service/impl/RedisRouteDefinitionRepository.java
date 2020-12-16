package com.tml.gateway.enhance.service.impl;

import com.google.common.collect.Lists;
import com.tml.common.core.entity.constant.CacheConstant;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.common.starter.redis.service.RedisService;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description RouteDefinitionRepository redis实现
 * @since 2020/8/15 10:06
 */
@Component
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    @Resource
    private RedisService redisService;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = Lists.newArrayList();
        redisTemplate.opsForHash().values(CacheConstant.GATEWAY_DYNAMIC_ROUTE_CACHE)
                .stream()
                .forEach(routeDefinition -> routeDefinitions.add(JacksonUtil.toObject(routeDefinition.toString(), RouteDefinition.class)));

        return Flux.fromIterable(routeDefinitions);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            redisTemplate.opsForHash().put(CacheConstant.GATEWAY_DYNAMIC_ROUTE_CACHE, routeDefinition.getId(), JacksonUtil.toJson(routeDefinition));
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        routeId.subscribe(id -> {
            if (redisTemplate.opsForHash().hasKey(CacheConstant.GATEWAY_DYNAMIC_ROUTE_CACHE, id)) {
                redisTemplate.opsForHash().delete(CacheConstant.GATEWAY_DYNAMIC_ROUTE_CACHE, id);
            }
        });
        return Mono.empty();
    }
}
