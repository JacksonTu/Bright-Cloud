package com.tml.auth.service.impl;

import com.tml.common.core.entity.constant.CacheConstant;
import com.tml.common.starter.redis.service.RedisService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Yuuki
 */
@Slf4j
@Service
public class RedisClientDetailsService extends JdbcClientDetailsService {

    private final RedisService redisService;

    public RedisClientDetailsService(DataSource dataSource, RedisService redisService) {
        super(dataSource);
        this.redisService = redisService;
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws InvalidClientException {
        ClientDetails clientDetails = null;
        String value = (String) redisService.hget(CacheConstant.CLIENT_DETAILS_CACHE, clientId);
        if (StringUtils.isBlank(value)) {
            clientDetails = cacheAndGetClient(clientId);
        } else {
            clientDetails = JSONObject.parseObject(value, BaseClientDetails.class);
        }

        return clientDetails;
    }

    /**
     * 缓存 client并返回 client
     *
     * @param clientId clientId
     */
    public ClientDetails cacheAndGetClient(String clientId) {
        ClientDetails clientDetails = null;
        clientDetails = super.loadClientByClientId(clientId);
        if (clientDetails != null) {
            BaseClientDetails baseClientDetails = (BaseClientDetails) clientDetails;
            Set<String> autoApproveScopes = baseClientDetails.getAutoApproveScopes();
            if (CollectionUtils.isNotEmpty(autoApproveScopes)) {
                baseClientDetails.setAutoApproveScopes(
                        autoApproveScopes.stream().map(this::convert).collect(Collectors.toSet())
                );
            }
            redisService.hset(CacheConstant.CLIENT_DETAILS_CACHE, clientId, JSONObject.toJSONString(baseClientDetails));
        }
        return clientDetails;
    }

    /**
     * 将 oauth_client_details全表刷入 redis
     */
    public void loadAllClientToCache() {
        if (redisService.hasKey(CacheConstant.CLIENT_DETAILS_CACHE)) {
            return;
        }
        log.info("将oauth_client_details全表刷入redis");

        List<ClientDetails> list = super.listClientDetails();
        if (CollectionUtils.isEmpty(list)) {
            log.error("oauth_client_details表数据为空，请检查");
            return;
        }
        list.forEach(client -> redisService.hset(CacheConstant.CLIENT_DETAILS_CACHE, client.getClientId(), JSONObject.toJSONString(client)));
    }

    private String convert(String value) {
        final String logicTrue = "1";
        return logicTrue.equals(value) ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
    }
}
