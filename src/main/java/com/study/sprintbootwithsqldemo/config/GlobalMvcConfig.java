package com.study.sprintbootwithsqldemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalMvcConfig implements WebMvcConfigurer {
   @Autowired
   AuthInterceptor authInterceptor;
   @Override
    public void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(authInterceptor).excludePathPatterns(
          "/user/login",
          "/user/register",
          "/user/getCode",
          // 不放行的话，接口抛异常转发到 /error 时会被拦成 401，真正的报错就看不见了
          "/error"
         //  "/user/testBase64",
         //  "/user/decodeBase64"
         );
    }
   @Override
   public void addCorsMappings(CorsRegistry registry){
      //cors设置一定要设置允许携带cookies
     //allowHeader就不能为*
     //allowOrigins也不能为*
        registry.addMapping("/**")
        .allowCredentials(true)
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("*");
   }
     
} 
