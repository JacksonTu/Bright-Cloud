package com.tml.gateway.enhance.service.impl;


import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.tml.api.system.entity.*;
import com.tml.common.core.entity.CommonResult;
import com.tml.common.core.entity.ResultBody;
import com.tml.common.core.utils.BrightUtil;
import com.tml.gateway.enhance.feign.RemoteGatewayFeignService;
import com.tml.gateway.enhance.service.RouteEnhanceService;
import com.tml.gateway.enhance.utils.AddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteEnhanceServiceImpl implements RouteEnhanceService {

    private static final String METHOD_ALL = "ALL";
    private static final String TOKEN_CHECK_URL = "/auth/user";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final RemoteGatewayFeignService remoteGatewayFeignService;

    @Override
    public Mono<Void> filterBlockList(ServerWebExchange exchange) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        try {
            URI originUri = getGatewayOriginalRequestUrl(exchange);
            if (originUri != null) {
                String requestIp = BrightUtil.getServerHttpRequestIpAddress(request);
                String requestMethod = request.getMethodValue();
                AtomicBoolean forbid = new AtomicBoolean(false);

                ResultBody<List<GatewayBlockList>> resultBody = remoteGatewayFeignService.listGatewayBlockList();
                List<GatewayBlockList> blockLists = Lists.newArrayList();
                if (resultBody.getCode() == 200 && resultBody.getData().size() > 0) {
                    blockLists = resultBody.getData();
                }

                checkBlockList(forbid, blockLists, originUri, requestMethod);

                log.info("BlockList verification completed - {}", stopwatch.stop());
                if (forbid.get()) {
                    return BrightUtil.makeWebFluxResponse(response, MediaType.APPLICATION_JSON_VALUE,
                            HttpStatus.NOT_ACCEPTABLE, new CommonResult().message("黑名单限制，禁止访问"));
                }
            } else {
                log.info("BlockList verification info: Request IP not obtained, no blockList check - {}", stopwatch.stop());
            }
        } catch (Exception e) {
            log.warn("BlockList verification failed : {} - {}", stopwatch.stop(), e.getMessage());
        }
        return null;
    }

    @Override
    public Mono<Void> filterRateLimit(ServerWebExchange exchange) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        try {
            URI originUri = getGatewayOriginalRequestUrl(exchange);
            if (originUri != null) {
                String requestIp = BrightUtil.getServerHttpRequestIpAddress(request);
                String requestMethod = request.getMethodValue();
                AtomicBoolean limit = new AtomicBoolean(false);
                System.out.println("originUri.getPath(): "+originUri.getPath());

                GatewayRouteLimitRule routeLimitRule = null;

                ResultBody<GatewayRouteLimitRule> resultBody = remoteGatewayFeignService.getGatewayRouteLimitRule(originUri.getPath(), METHOD_ALL);

                if (resultBody.getCode() == 200 && resultBody.getData() == null) {
                    ResultBody<GatewayRouteLimitRule> resultBody2 = remoteGatewayFeignService.getGatewayRouteLimitRule(originUri.getPath(), requestMethod);
                    if (resultBody2.getCode() == 200) {
                        routeLimitRule = resultBody2.getData();
                    }
                }
                if (routeLimitRule != null) {
                    Mono<Void> result = checkRateLimit(limit, routeLimitRule, originUri, requestIp, requestMethod, response);
                    log.info("Rate limit verification completed - {}", stopwatch.stop());
                    if (result != null) {
                        return result;
                    }
                }
            } else {
                log.info("Rate limit verification info: Request IP not obtained, no rate limit filter - {}", stopwatch.stop());
            }
        } catch (Exception e) {
            log.warn("Rate limit verification failure : {} - {}", stopwatch.stop(), e.getMessage());
        }
        return null;
    }

    @Override
    public void saveRouteLog(ServerWebExchange exchange) {
        URI originUri = getGatewayOriginalRequestUrl(exchange);
        // /auth/user为令牌校验请求，是系统自发行为，非用户请求，故不记录
        if (!StringUtils.equalsIgnoreCase(TOKEN_CHECK_URL, originUri.getPath())) {
            URI url = getGatewayRequestUrl(exchange);
            Route route = getGatewayRoute(exchange);
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String ipAddress = BrightUtil.getServerHttpRequestIpAddress(request);
            int httpStatus = response.getStatusCode().value();
            Map<String, String> headers = request.getHeaders().toSingleValueMap();
            String userAgent = headers.get("user-agent");
            String authorization = headers.get("authorization");
            if (url != null && route != null) {
                GatewayRouteLog routeLog = new GatewayRouteLog();
                routeLog.setIp(ipAddress);
                routeLog.setRequestUri(originUri.getPath());
                routeLog.setRequestMethod(request.getMethodValue());
                routeLog.setTargetServer(route.getId());
                routeLog.setTargetUri(url.getPath());
                routeLog.setLocation(AddressUtil.getCityInfo(ipAddress));
                routeLog.setHeaders(JSONObject.toJSONString(headers));
                routeLog.setHttpStatus(httpStatus);
                routeLog.setUserAgent(userAgent);
                routeLog.setAuthentication(authorization);
                routeLog.setCreateTimeFrom(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
                remoteGatewayFeignService.saveGatewayRouteLog(routeLog);
            }
        }
    }

    @Override
    public void saveBlockLog(ServerWebExchange exchange) {
        URI originUri = getGatewayOriginalRequestUrl(exchange);
        ServerHttpRequest request = exchange.getRequest();
        String requestIp = BrightUtil.getServerHttpRequestIpAddress(request);
        if (originUri != null) {
            GatewayBlockListLog blockListLog = new GatewayBlockListLog();
            blockListLog.setIp(requestIp);
            blockListLog.setRequestMethod(request.getMethodValue());
            blockListLog.setRequestUri(originUri.getPath());
            blockListLog.setCreateTimeFrom(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
            remoteGatewayFeignService.saveGatewayBlockListLog(blockListLog);
            log.info("----- Store blockList logs -----");
        }
    }

    @Override
    public void saveRateLimitLog(ServerWebExchange exchange) {
        URI originUri = getGatewayOriginalRequestUrl(exchange);
        ServerHttpRequest request = exchange.getRequest();
        String requestIp = BrightUtil.getServerHttpRequestIpAddress(request);
        if (originUri != null) {
            GatewayRouteLimitRuleLog routeLimitRuleLog = new GatewayRouteLimitRuleLog();
            routeLimitRuleLog.setIp(requestIp);
            routeLimitRuleLog.setRequestMethod(request.getMethodValue());
            routeLimitRuleLog.setRequestUri(originUri.getPath());
            routeLimitRuleLog.setCreateTimeFrom(DateUtil.format(LocalDateTime.now(), DatePattern.NORM_DATETIME_PATTERN));
            remoteGatewayFeignService.saveGatewayRouteLimitRuleLog(routeLimitRuleLog);
            log.info("----- Store rate limit logs -----");
        }
    }

    private void checkBlockList(AtomicBoolean forbid, List<GatewayBlockList> blockLists, URI uri, String requestMethod) {
        for (GatewayBlockList blockList : blockLists) {
            if (pathMatcher.match(blockList.getRequestUri(), uri.getPath()) && GatewayBlockList.OPEN.equals(blockList.getStatus())) {
                if (GatewayBlockList.METHOD_ALL.equalsIgnoreCase(blockList.getRequestMethod())
                        || StringUtils.equalsIgnoreCase(requestMethod, blockList.getRequestMethod())) {
                    if (blockList.getLimitFrom() != null && blockList.getLimitTo() != null) {
                        LocalDateTime now = LocalDateTime.now();
                        if (now.isAfter(blockList.getLimitFrom()) && now.isBefore(blockList.getLimitTo())) {
                            forbid.set(true);
                        }
                    } else {
                        forbid.set(true);
                    }
                }
            }
            if (forbid.get()) {
                break;
            }
        }
    }

    private Mono<Void> checkRateLimit(AtomicBoolean limit, GatewayRouteLimitRule rule, URI uri,
                                      String requestIp, String requestMethod, ServerHttpResponse response) {
        boolean isRateLimitRuleHit = GatewayRouteLimitRule.OPEN.equals(rule.getStatus())
                && (GatewayRouteLimitRule.METHOD_ALL.equalsIgnoreCase(rule.getRequestMethod())
                || StringUtils.equalsIgnoreCase(requestMethod, rule.getRequestMethod()));
        if (isRateLimitRuleHit) {
            if (rule.getLimitFrom() != null && rule.getLimitTo() != null) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isAfter(rule.getLimitFrom()) && now.isBefore(rule.getLimitTo())) {
                    limit.set(true);
                }
            } else {
                limit.set(true);
            }
        }
        if (limit.get()) {
            String requestUri = uri.getPath();
            int count = 0;
            ResultBody<Integer> resultBody = remoteGatewayFeignService.getCurrentRequestCount(requestUri, requestIp);
            if (resultBody.getCode() == 200) {
                count = resultBody.getData();
            }
            if (count == 0) {
                remoteGatewayFeignService.setCurrentRequestCount(requestUri, requestIp, rule.getIntervalSec());
            } else if (count >= rule.getCount()) {
                return BrightUtil.makeWebFluxResponse(response, MediaType.APPLICATION_JSON_VALUE,
                        HttpStatus.TOO_MANY_REQUESTS, new CommonResult().message("访问频率超限，请稍后再试"));
            } else {
                remoteGatewayFeignService.incrCurrentRequestCount(requestUri, requestIp);
            }
        }
        return null;
    }

    private URI getGatewayOriginalRequestUrl(ServerWebExchange exchange) {
        LinkedHashSet<URI> uris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        URI originUri = null;
        if (uris != null) {
            originUri = uris.stream().findFirst().orElse(null);
        }
        return originUri;
    }

    private URI getGatewayRequestUrl(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
    }

    private Route getGatewayRoute(ServerWebExchange exchange) {
        return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    }
}
