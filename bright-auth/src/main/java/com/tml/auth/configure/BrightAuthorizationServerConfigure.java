package com.tml.auth.configure;

import com.tml.auth.converter.BrightJwtTokenService;
import com.tml.auth.converter.BrightRedisTokenService;
import com.tml.auth.handler.BrightWebResponseExceptionTranslator;
import com.tml.auth.properties.BrightAuthProperties;
import com.tml.auth.service.impl.RedisClientDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import java.util.UUID;

/**
 * 认证服务器配置
 *
 * @Author TuMingLong
 */
@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class BrightAuthorizationServerConfigure extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailService;
    private final PasswordEncoder passwordEncoder;
    private final BrightWebResponseExceptionTranslator exceptionTranslator;
    private final BrightAuthProperties properties;
    private final RedisClientDetailsService redisClientDetailsService;
    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(redisClientDetailsService);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.tokenStore(tokenStore())
                .userDetailsService(userDetailService)
                .authenticationManager(authenticationManager)
                .exceptionTranslator(exceptionTranslator);
        if (properties.getEnableJwt()) {
            endpoints.accessTokenConverter(jwtAccessTokenConverter());
        }
    }

    @Bean
    public TokenStore tokenStore() {
        if (properties.getEnableJwt()) {
            return new JwtTokenStore(jwtAccessTokenConverter());
        } else {
            RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
            // 解决每次生成的 token都一样的问题
            redisTokenStore.setAuthenticationKeyGenerator(oAuth2Authentication -> UUID.randomUUID().toString());
            return redisTokenStore;
        }
    }

    @Bean
    @Primary
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();

        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setClientDetailsService(redisClientDetailsService);
        return tokenServices;
    }

    /**
     * 构建jwt token转换器
     *
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
        DefaultAccessTokenConverter defaultAccessTokenConverter = (DefaultAccessTokenConverter) accessTokenConverter.getAccessTokenConverter();
        DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
        userAuthenticationConverter.setUserDetailsService(userDetailService);
        defaultAccessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
        accessTokenConverter.setSigningKey(properties.getJwtAccessKey()); //对称秘钥，资源服务器使用该秘钥验证
        return accessTokenConverter;

//        BrightJwtAccessTokenConverter accessTokenConverter = new BrightJwtAccessTokenConverter();
//        DefaultAccessTokenConverter defaultAccessTokenConverter = (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
//        BrightUserAuthenticationConverter userAuthenticationConverter = new BrightUserAuthenticationConverter();
//        defaultAccessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
//        accessTokenConverter.setAccessTokenConverter(defaultAccessTokenConverter);
//        accessTokenConverter.setSigningKey(properties.getJwtAccessKey());//对称秘钥，资源服务器使用该秘钥验证
//        return accessTokenConverter;
    }

    /**
     * token服务
     *
     * @return
     */
    @Bean
    public ResourceServerTokenServices resourceServerTokenServices() {
        if (properties.getEnableJwt()) {
            BrightJwtTokenService tokenService = new BrightJwtTokenService();
            tokenService.setTokenStore(tokenStore());
            tokenService.setJwtAccessTokenConverter(jwtAccessTokenConverter());
            return tokenService;
        } else {
            BrightRedisTokenService tokenService = new BrightRedisTokenService();
            tokenService.setTokenStore(tokenStore());
            return tokenService;
        }
    }

    @Bean
    public ResourceOwnerPasswordTokenGranter resourceOwnerPasswordTokenGranter(AuthenticationManager authenticationManager, OAuth2RequestFactory oAuth2RequestFactory) {
        DefaultTokenServices defaultTokenServices = defaultTokenServices();
        if (properties.getEnableJwt()) {
            defaultTokenServices.setTokenEnhancer(jwtAccessTokenConverter());
        }
        return new ResourceOwnerPasswordTokenGranter(authenticationManager, defaultTokenServices, redisClientDetailsService, oAuth2RequestFactory);
    }

    @Bean
    public DefaultOAuth2RequestFactory oAuth2RequestFactory() {
        return new DefaultOAuth2RequestFactory(redisClientDetailsService);
    }

}
