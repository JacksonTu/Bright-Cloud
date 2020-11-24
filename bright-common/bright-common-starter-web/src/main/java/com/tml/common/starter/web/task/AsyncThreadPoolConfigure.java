package com.tml.common.starter.web.task;

import com.tml.common.core.entity.constant.BrightConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description Spring 异步线程池配置
 * @Author TuMingLong
 * @Date 2020/4/3 21:16
 */
@EnableAsync
public class AsyncThreadPoolConfigure {

    @Resource
    private Environment env;

    /**
     * 对于CPU密集型任务，最大线程数是CPU线程数+1。对于IO密集型任务，尽量多配点，可以是CPU线程数*2，或者CPU线程数/(1-阻塞系数)。
     * maxPoolSize=new Double(Math.floor(Runtime.getRuntime().availableProcessors()/(1-0.9))).intValue()
     */
    @Bean(name = BrightConstant.ASYNC_POOL)
    public ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        // 设置最大线程数
        executor.setMaxPoolSize(new Double(Math.floor(Runtime.getRuntime().availableProcessors() / (1 - 0.9))).intValue());
        // 设置队列容量
        executor.setQueueCapacity(Runtime.getRuntime().availableProcessors());
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix(env.getProperty("spring.application.name") + "-Async-Thread");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationMillis(60);
        executor.initialize();
        return executor;
    }
}
