package com.tml.gateway.enhance.auth;

import com.tml.gateway.enhance.properties.BrightCloudGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description 权限管理器
 * @since 2020/8/19 22:19
 */
@Slf4j
@Component
@EnableConfigurationProperties(BrightCloudGatewayProperties.class)
public class AccessManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final BrightCloudGatewayProperties gatewayProperties;

    public AccessManager(BrightCloudGatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    /**
     * 实现权限验证判断
     */
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authenticationMono, AuthorizationContext authorizationContext) {
        ServerWebExchange exchange = authorizationContext.getExchange();
        //请求资源
        String requestPath = exchange.getRequest().getURI().getPath();
        // 是否直接放行
        if (permitAll(requestPath)) {
            return Mono.just(new AuthorizationDecision(true));
        }

        return authenticationMono.map(auth -> {
            return new AuthorizationDecision(checkAuthorities(exchange, auth, requestPath));
        }).defaultIfEmpty(new AuthorizationDecision(false));

    }

    /**
     * 校验是否属于静态资源
     *
     * @param requestPath 请求路径
     * @return
     */
    private boolean permitAll(String requestPath) {
        List<String> anonUris=gatewayProperties.getAnonUris();
        if(anonUris!=null && anonUris.size()>0){
            return anonUris.stream()
                    .filter(r -> antPathMatcher.match(r, requestPath)).findFirst().isPresent();
        }
        return false;
    }

    /**
     * 权限校验
     * @param exchange
     * @param auth
     * @param requestPath
     * @return
     */
    private boolean checkAuthorities(ServerWebExchange exchange, Authentication auth, String requestPath) {
        if (auth instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) auth;
            String clientId = authentication.getOAuth2Request().getClientId();
            log.info("clientId is: {}", clientId);
        }

        Object principal = auth.getPrincipal();
        log.info("user info is: {}", principal.toString());
        return true;
    }
}
