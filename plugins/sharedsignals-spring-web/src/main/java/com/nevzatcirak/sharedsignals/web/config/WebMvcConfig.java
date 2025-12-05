package com.nevzatcirak.sharedsignals.web.config;

import com.nevzatcirak.sharedsignals.web.interceptor.ActivityTrackingInterceptor;
import com.nevzatcirak.sharedsignals.web.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    private final ActivityTrackingInterceptor activityTrackingInterceptor;


    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor, ActivityTrackingInterceptor activityTrackingInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        this.activityTrackingInterceptor = activityTrackingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                    "/ssf/**"
                );

        registry.addInterceptor(activityTrackingInterceptor)
                .addPathPatterns("/ssf/**")
                .excludePathPatterns("/.well-known/**");
    }
}
