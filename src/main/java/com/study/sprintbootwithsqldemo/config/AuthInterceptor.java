package com.study.sprintbootwithsqldemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.study.sprintbootwithsqldemo.utils.JwtUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    JwtUtils jwtUtils;
    @Override
    public boolean preHandle(
        HttpServletRequest request, 
        HttpServletResponse response, 
        Object handler)throws Exception {
        // 浏览器跨域调用受保护接口前会先发 OPTIONS 预检请求，
        // 预检请求不会带 auth 头，这里必须放行，否则请求在浏览器里直接就失败了
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        String token= request.getHeader("auth");
        if(token==null||token.equals("")){
            response.setStatus(401);
            return false;
        }
        if(jwtUtils.verifyToken(token)){
            return true;
        }else{
            response.setStatus(401);
            return false;
        }
    }
}
