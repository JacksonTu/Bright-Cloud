package com.tml.auth.configure;

import com.tml.auth.filter.ValidateCodeFilter;
import com.tml.auth.handler.BrightWebLoginFailureHandler;
import com.tml.auth.handler.BrightWebLoginSuccessHandler;
import com.tml.common.core.entity.constant.EndpointConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * WebSecurity配置
 *
 * @Author TuMingLong
 */
@Order(2)
@EnableWebSecurity
@RequiredArgsConstructor
public class BrightSecurityConfigure extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailService;
    private final ValidateCodeFilter validateCodeFilter;
    private final PasswordEncoder passwordEncoder;
    private final BrightWebLoginSuccessHandler successHandler;
    private final BrightWebLoginFailureHandler failureHandler;


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
                .requestMatchers()
                .antMatchers(EndpointConstant.OAUTH_ALL, EndpointConstant.LOGIN)
                .and()
                .authorizeRequests()
                .antMatchers(EndpointConstant.OAUTH_ALL).authenticated()
                .and()
                .formLogin()
                .loginPage(EndpointConstant.LOGIN)
                .loginProcessingUrl(EndpointConstant.LOGIN)
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
                .and().csrf().disable()
                .httpBasic().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder);
    }
}
