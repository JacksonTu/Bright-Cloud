package com.tml.server.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tml.api.system.entity.GatewayBlockList;
import com.tml.common.core.entity.QueryRequest;
import com.tml.common.core.entity.constant.CacheConstant;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.common.starter.redis.service.RedisService;
import com.tml.server.system.mapper.GatewayBlockListMapper;
import com.tml.server.system.service.IGatewayBlockListService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 黑名单 Service实现
 *
 * @author JacksonTu
 * @date 2020-08-13 09:46:55
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class GatewayBlockListServiceImpl extends ServiceImpl<GatewayBlockListMapper, GatewayBlockList> implements IGatewayBlockListService {

    private final RedisService redisService;

    @Override
    public IPage<GatewayBlockList> pageGatewayBlockList(QueryRequest request, GatewayBlockList gatewayBlockList) {
        LambdaQueryWrapper<GatewayBlockList> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(StringUtils.isNoneBlank(gatewayBlockList.getIp()), GatewayBlockList::getIp, gatewayBlockList.getIp())
                .eq(StringUtils.isNoneBlank(gatewayBlockList.getRequestUri()), GatewayBlockList::getRequestUri, gatewayBlockList.getRequestUri())
                .eq(StringUtils.isNoneBlank(gatewayBlockList.getRequestMethod()), GatewayBlockList::getRequestMethod, gatewayBlockList.getRequestMethod());
        Page<GatewayBlockList> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<GatewayBlockList> listGatewayBlockList(GatewayBlockList gatewayBlockList) {
        LambdaQueryWrapper<GatewayBlockList> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(StringUtils.isNoneBlank(gatewayBlockList.getIp()), GatewayBlockList::getIp, gatewayBlockList.getIp())
                .eq(StringUtils.isNoneBlank(gatewayBlockList.getRequestUri()), GatewayBlockList::getRequestUri, gatewayBlockList.getRequestUri())
                .eq(StringUtils.isNoneBlank(gatewayBlockList.getRequestMethod()), GatewayBlockList::getRequestMethod, gatewayBlockList.getRequestMethod());
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public boolean check(String ip, String requestUri, String requestMethod) {
        int count = 0;
        LambdaQueryWrapper<GatewayBlockList> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(StringUtils.isNoneBlank(requestUri), GatewayBlockList::getRequestUri, requestUri)
                .eq(StringUtils.isNoneBlank(requestMethod), GatewayBlockList::getRequestMethod, requestMethod);
        count = this.count(queryWrapper);
        if (count > 0) {
            return false;
        }
        queryWrapper.eq(StringUtils.isNoneBlank(ip), GatewayBlockList::getIp, ip);
        count = this.count(queryWrapper);
        if (count > 0) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGatewayBlockList(GatewayBlockList gatewayBlockList) {
        boolean save = this.save(gatewayBlockList);
        if (save && gatewayBlockList.getStatus().equals("1")) {
            String key = CacheConstant.GATEWAY_BLOCK_LIST_CACHE;
            redisService.sSet(key, JacksonUtil.toJson(gatewayBlockList));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGatewayBlockList(GatewayBlockList gatewayBlockList) {
        boolean update = this.saveOrUpdate(gatewayBlockList);
        if (update) {
            String key = CacheConstant.GATEWAY_BLOCK_LIST_CACHE;
            redisService.setRemove(key, JacksonUtil.toJson(gatewayBlockList));
            if (gatewayBlockList.getStatus().equals("1")) {
                redisService.sSet(key, JacksonUtil.toJson(gatewayBlockList));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGatewayBlockList(String[] ids) {
        List<String> list = Arrays.asList(ids);
        list.stream().forEach(s -> {
            boolean remove = this.removeById(s);
            if (remove) {
                String key = CacheConstant.GATEWAY_BLOCK_LIST_CACHE;
                redisService.setRemove(key, JacksonUtil.toJson(this.getById(s)));
            }
        });

    }

    @Override
    public List<GatewayBlockList> listGatewayBlockList() {
        String key = CacheConstant.GATEWAY_BLOCK_LIST_CACHE;
        if (redisService.hasKey(key)) {
            List<GatewayBlockList> gatewayBlackLists = Lists.newArrayList();
            Set<Object> set = redisService.sGet(key);
            if (set != null && set.size() > 0) {
                set.stream().forEach(o -> {
                    GatewayBlockList blockList = JacksonUtil.toObject(o.toString(), GatewayBlockList.class);
                    gatewayBlackLists.add(blockList);
                });
            }
            return gatewayBlackLists;
        } else {
            LambdaQueryWrapper<GatewayBlockList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(GatewayBlockList::getStatus, "1");
            return this.list(queryWrapper);
        }
    }

    @Override
    public void cacheGatewayBlockList() {
        LambdaQueryWrapper<GatewayBlockList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GatewayBlockList::getStatus, "1");
        List<GatewayBlockList> list = this.list(queryWrapper);
        if (list != null && list.size() > 0) {
            String key = CacheConstant.GATEWAY_BLOCK_LIST_CACHE;
            //缓存黑名单之前，判断是否有缓存，若有先清除缓存，保证跟数据库中一致
            if(redisService.hasKey(key)){
                redisService.del(key);
            }
            list.stream().forEach(gatewayBlockList -> {
                redisService.sSet(key, JacksonUtil.toJson(gatewayBlockList));
            });
        }
    }
}
