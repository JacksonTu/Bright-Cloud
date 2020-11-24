package com.tml.auth.scan;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tml.common.core.utils.JacksonUtil;
import com.tml.common.core.utils.ReflectionUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 接口扫描
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Slf4j
@Component
public class RequestMappingScan implements ApplicationListener<ApplicationReadyEvent> {

    private static final AntPathMatcher pathMatch = new AntPathMatcher();
    private final ExecutorService threadPool = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            new Double(Runtime.getRuntime().availableProcessors() / (1 - 0.9)).intValue(),
            1l,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors()),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    @Resource
    private Processor pipe;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        Environment env = applicationContext.getEnvironment();
        // 服务名称
        String serviceId = env.getProperty("spring.application.name", "application");
        // 所有接口映射
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        // 获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        List<RequestMatcher> permitAll = Lists.newArrayList();
        try {
            // 获取所有安全配置适配器
            Map<String, WebSecurityConfigurerAdapter> securityConfigurerAdapterMap = applicationContext.getBeansOfType(WebSecurityConfigurerAdapter.class);
            Iterator<Map.Entry<String, WebSecurityConfigurerAdapter>> iterable = securityConfigurerAdapterMap.entrySet().iterator();
            while (iterable.hasNext()) {
                WebSecurityConfigurerAdapter configurer = iterable.next().getValue();
                HttpSecurity httpSecurity = (HttpSecurity) ReflectUtil.getFieldValue(configurer, "http");

                FilterSecurityInterceptor filterSecurityInterceptor = httpSecurity.getSharedObject(FilterSecurityInterceptor.class);
                FilterInvocationSecurityMetadataSource metadataSource = filterSecurityInterceptor.getSecurityMetadataSource();
                Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = (Map) ReflectionUtil.getFieldValue(metadataSource, "requestMap");
                Iterator<Map.Entry<RequestMatcher, Collection<ConfigAttribute>>> requestIterable = requestMap.entrySet().iterator();
                while (requestIterable.hasNext()) {
                    Map.Entry<RequestMatcher, Collection<ConfigAttribute>> match = requestIterable.next();
                    if (match.getValue().toString().contains("permitAll")) {
                        permitAll.add(match.getKey());
                    }
                }
            }
        } catch (Exception e) {
            log.error("error:{}", e);
        }
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> m : map.entrySet()) {
            RequestMappingInfo info = m.getKey();
            HandlerMethod method = m.getValue();
            if (method.getMethodAnnotation(ApiIgnore.class) != null) {
                // 忽略的接口不扫描
                continue;
            }
            Set<MediaType> mediaTypeSet = info.getProducesCondition().getProducibleMediaTypes();
            for (MethodParameter params : method.getMethodParameters()) {
                if (params.hasParameterAnnotation(RequestBody.class)) {
                    mediaTypeSet.add(MediaType.APPLICATION_JSON);
                    break;
                }
            }
            String mediaTypes = getMediaTypes(mediaTypeSet);
            // 请求类型
            RequestMethodsRequestCondition methodsCondition = info.getMethodsCondition();
            String methods = getMethods(methodsCondition.getMethods());
            // 请求路径
            PatternsRequestCondition p = info.getPatternsCondition();
            String urls = getUrls(p.getPatterns());
            Map<String, String> api = Maps.newHashMap();
            // 类名
            String className = method.getMethod().getDeclaringClass().getName();
            // 方法名
            String methodName = method.getMethod().getName();
            String fullName = className + "." + methodName;
            // md5码
            String md5 = SecureUtil.md5(serviceId + urls);
            String name = "";
            String desc = "";
            // 是否需要安全认证 默认:1-是 0-否
            String isAuth = "1";
            // 匹配项目中.permitAll()配置
            for (String url : p.getPatterns()) {
                for (RequestMatcher requestMatcher : permitAll) {
                    if (requestMatcher instanceof AntPathRequestMatcher) {
                        AntPathRequestMatcher pathRequestMatcher = (AntPathRequestMatcher) requestMatcher;
                        if (pathMatch.match(pathRequestMatcher.getPattern(), url)) {
                            // 忽略验证
                            isAuth = "0";
                        }
                    }
                }
            }

            ApiOperation apiOperation = method.getMethodAnnotation(ApiOperation.class);
            if (apiOperation != null) {
                name = apiOperation.value();
                desc = apiOperation.notes();
            }
            name = StringUtils.isBlank(name) ? methodName : name;
            api.put("apiName", name);
            api.put("apiCode", md5);
            api.put("apiDesc", desc);
            api.put("path", urls);
            api.put("className", className);
            api.put("methodName", methodName);
            api.put("md5", md5);
            api.put("requestMethod", methods);
            api.put("serviceId", serviceId);
            api.put("contentType", mediaTypes);
            api.put("isAuth", isAuth);
            list.add(api);
        }
        Map resource = Maps.newHashMap();
        resource.put("application", serviceId);
        resource.put("mapping", list);
        log.info("ApplicationReadyEvent:[{}]", serviceId);
        threadPool.submit(() -> {
            pipe.output().send(MessageBuilder.withPayload(JacksonUtil.toJson(resource)).build());

        });
    }


    private String getMediaTypes(Set<MediaType> mediaTypes) {
        StringBuilder sbf = new StringBuilder();
        for (MediaType mediaType : mediaTypes) {
            sbf.append(mediaType.toString()).append(",");
        }
        if (mediaTypes.size() > 0) {
            sbf.deleteCharAt(sbf.length() - 1);
        }
        return sbf.toString();
    }

    private String getMethods(Set<RequestMethod> requestMethods) {
        StringBuilder sbf = new StringBuilder();
        for (RequestMethod requestMethod : requestMethods) {
            sbf.append(requestMethod.toString()).append(",");
        }
        if (requestMethods.size() > 0) {
            sbf.deleteCharAt(sbf.length() - 1);
        }
        return sbf.toString();
    }

    private String getUrls(Set<String> urls) {
        StringBuilder sbf = new StringBuilder();
        for (String url : urls) {
            sbf.append(url).append(",");
        }
        if (urls.size() > 0) {
            sbf.deleteCharAt(sbf.length() - 1);
        }
        return sbf.toString();
    }
}
