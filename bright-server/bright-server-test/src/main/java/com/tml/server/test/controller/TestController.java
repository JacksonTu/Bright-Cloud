package com.tml.server.test.controller;

import com.tml.common.core.entity.CommonResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Slf4j
@Validated
@RestController
@RequestMapping("task")
@RequiredArgsConstructor
public class TestController {

    private final RedissonClient redissonClient;

    @GetMapping("getLock")
    public CommonResult getLock() throws InterruptedException {
        RLock lock = redissonClient.getLock("anyLock");

        // 尝试加锁，最多等待100秒，上锁以后10秒自动解锁
        boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);
        if (res) {
            try {
                return new CommonResult().data("加锁");
            } finally {
                lock.unlock();
            }
        }
        return new CommonResult().message("加锁失败");
    }

}
