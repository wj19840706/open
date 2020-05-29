package com.feida.ams.framework.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class SessionConfig implements WebMvcConfigurer {

    @Bean
    public LoginInterceptor getLoginInterceptor(){
        return new LoginInterceptor();
    }

    @Bean
    public AuthorityInterceptor getAuthorityInterceptor(){
        return new AuthorityInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new CrosInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(getLoginInterceptor())
                //拦截路径
                .addPathPatterns("/**")
                //排除拦截
                .excludePathPatterns("/pc/user/login")
                .excludePathPatterns("/pc/user/logout")
                .excludePathPatterns("/pc/file/download")
                .excludePathPatterns("/pc/file/upload");

        registry.addInterceptor(getAuthorityInterceptor())
                //拦截路径
                .addPathPatterns("/**")
                //排除拦截
                .excludePathPatterns("/pc/user/login")
                .excludePathPatterns("/pc/user/logout")
                .excludePathPatterns("/pc/file/download")
                .excludePathPatterns("/pc/file/upload");

    }

}
