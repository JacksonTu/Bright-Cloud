package com.tml.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;

/**
 * @author JacksonTu
 * @version 1.0
 * @description com.hdw.zookeeper
 * @since 2020/9/2 16:30
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ZookeeperLockTest {

    @Autowired
    private CuratorFramework client;

    @Test
    public void testLock(){
        //4 分布式锁
        final InterProcessMutex mutex = new InterProcessMutex(client, "/curator/distributionLock");
        //读写锁
        //InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, "/curator/readWriterLock");

        ExecutorService threadPool=
                new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                        (int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9)),
                        60l,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors()),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.CallerRunsPolicy()
                );
        CountDownLatch latch = new CountDownLatch(5);
        try {
            for (int i = 0; i < 5; i++) {
                threadPool.submit(()->{
                    boolean flag = false;
                    try {
                        //尝试获取锁，最多等待5秒
                        flag = mutex.acquire(5, TimeUnit.SECONDS);
                        Thread currentThread = Thread.currentThread();
                        if(flag){
                            System.out.println("线程: "+currentThread.getId()+"获取锁成功");
                        }else{
                            System.out.println("线程: "+currentThread.getId()+"获取锁失败");
                        }
                        //模拟业务逻辑，延时4秒
                        TimeUnit.SECONDS.sleep(4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally{
                        latch.countDown();
                        if(flag){
                            try {
                                mutex.release();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }catch (Exception e){
           e.printStackTrace();
        }finally {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testLock2(){
        //4 分布式锁
        final InterProcessMutex mutex = new InterProcessMutex(client, "/curator/distributionLock");
        //读写锁
        //InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, "/curator/readWriterLock");

        ExecutorService threadPool=
                new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                        (int) (Runtime.getRuntime().availableProcessors() / (1 - 0.9)),
                        60l,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors()),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.CallerRunsPolicy()
                );
        try {
            for (int i = 0; i < 5; i++) {
                threadPool.submit(()->{
                    boolean flag = false;
                    try {
                        //尝试获取锁，最多等待5秒
                        flag = mutex.acquire(5, TimeUnit.SECONDS);
                        Thread currentThread = Thread.currentThread();
                        if(flag){
                            System.out.println("线程: "+currentThread.getId()+"获取锁成功");
                        }else{
                            System.out.println("线程: "+currentThread.getId()+"获取锁失败");
                        }
                        //模拟业务逻辑，延时4秒
                        TimeUnit.SECONDS.sleep(4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally{
                        if(flag){
                            try {
                                mutex.release();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
