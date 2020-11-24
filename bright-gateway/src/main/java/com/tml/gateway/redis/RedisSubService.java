package com.tml.gateway.redis;

import com.google.common.collect.Lists;
import com.tml.api.system.dto.RouteDefinitionDTO;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.common.starter.redis.entity.RedisSimpleMessage;
import com.tml.gateway.enhance.service.impl.GatewayDynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description redis 消息订阅服务
 * @since 2020/8/15 10:59
 */
@Slf4j
@Component
public class RedisSubService {

    @Resource
    private GatewayDynamicRouteService gatewayDynamicRouteService;

    public void onMessage(String jsonMsg) {
        log.info("redis received: {} ", jsonMsg);
        RedisSimpleMessage message = JacksonUtil.toObject(jsonMsg, RedisSimpleMessage.class);
        if (message.getStatus() == 1) {
            log.info("----- add route -----");
            RouteDefinitionDTO definitionDTO = JacksonUtil.toObject(message.getContent(), RouteDefinitionDTO.class);
            RouteDefinition routeDefinition = getRouteDefinition(definitionDTO);
            gatewayDynamicRouteService.addRoute(routeDefinition);
            gatewayDynamicRouteService.refreshRoute();
        }
        if (message.getStatus() == 2) {
            log.info("----- update route -----");
            RouteDefinitionDTO definitionDTO = JacksonUtil.toObject(message.getContent(), RouteDefinitionDTO.class);
            RouteDefinition routeDefinition = getRouteDefinition(definitionDTO);
            gatewayDynamicRouteService.updateRoute(routeDefinition);
            gatewayDynamicRouteService.refreshRoute();
        }
        if (message.getStatus() == 3) {
            log.info("----- delete route -----");
            gatewayDynamicRouteService.deleteRoute(message.getContent());
            gatewayDynamicRouteService.refreshRoute();
        }
    }

    private RouteDefinition getRouteDefinition(RouteDefinitionDTO definitionDTO) {
        List<PredicateDefinition> predicates = Lists.newArrayList();
        definitionDTO.getPredicates().stream().forEach(dto -> {
                    PredicateDefinition predicateDefinition = new PredicateDefinition();
                    BeanUtils.copyProperties(dto, predicateDefinition);
                    predicates.add(predicateDefinition);
                }
        );

        List<FilterDefinition> filters = Lists.newArrayList();
        definitionDTO.getFilters().stream().forEach(dto -> {
                    FilterDefinition filterDefinition = new FilterDefinition();
                    BeanUtils.copyProperties(dto, filterDefinition);
                    filters.add(filterDefinition);
                }
        );
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(definitionDTO.getId());
        routeDefinition.setUri(definitionDTO.getUri());
        routeDefinition.setOrder(definitionDTO.getOrder());
        routeDefinition.setPredicates(predicates);
        routeDefinition.setFilters(filters);
        routeDefinition.setMetadata(definitionDTO.getMetadata());
        return routeDefinition;
    }
}
