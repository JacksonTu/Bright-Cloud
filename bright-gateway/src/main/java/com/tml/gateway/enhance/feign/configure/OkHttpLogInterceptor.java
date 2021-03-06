package com.tml.gateway.enhance.feign.configure;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

import java.io.IOException;

/**
 * @author TuMingLong
 * @description OkHttp日志拦截器
 * @sence 2020/3/8 15:40
 */
@Slf4j
public class OkHttpLogInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        log.info("OkHttpUrl:" + chain.request().url());
        return chain.proceed(chain.request());
    }
}
