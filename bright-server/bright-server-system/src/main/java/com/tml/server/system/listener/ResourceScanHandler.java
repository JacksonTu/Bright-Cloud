package com.tml.server.system.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.common.starter.redis.service.RedisService;
import com.tml.api.system.entity.SysApi;
import com.tml.server.system.service.ISysApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JacksonTu
 * @version 1.0
 * @description com.tml.server.system.listener
 * @since 2020/8/20 18:20
 */
@Slf4j
@Component
public class ResourceScanHandler {

    @Resource
    private RedisService redisService;

    @Resource
    private ISysApiService apiService;

    //消息监听处理
    @StreamListener(Processor.INPUT)
    public void process(String message) {
        log.info("api info : " + message);
        Map<String, Object> params = JacksonUtil.toObject(message, Map.class);
        String serviceId = params.get("application").toString();
        String key = "bright.cloud.api.resource:" + serviceId;
        Object object = redisService.get(key);
        if (object != null) {
            //3分钟内未失效，不再更新资源
            return;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) params.get("mapping");
        if (list != null && list.size() > 0) {
            list.stream().forEach(stringMap -> {
                HashMap<String, String> mapping = CollUtil.newHashMap();
                SysApi sysApi = BeanUtil.mapToBean(stringMap, SysApi.class, false, CopyOptions.create().setFieldMapping(mapping));
                SysApi save = this.apiService.getSysApiByApiCode(sysApi.getApiCode());
                if (save == null) {
                    sysApi.setIsOpen(0);
                    sysApi.setIsPersist(1);
                    sysApi.setCreateTime(new Date());
                    apiService.saveSysApi(sysApi);
                } else {
                    sysApi.setApiId(save.getApiId());
                    sysApi.setUpdateTime(new Date());
                    apiService.updateSysApi(sysApi);
                }
                // 使用 Stream API 遍历 HashMap
//            stringMap.entrySet().stream().forEach((entry) -> {
//                System.out.println(entry.getKey() + ":" + entry.getValue());
//            });
            });

            redisService.set(key, list.size(), 180L);
        }
    }
}
