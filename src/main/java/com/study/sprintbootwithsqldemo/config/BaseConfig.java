package com.study.sprintbootwithsqldemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

 
@Configuration
 
public class BaseConfig {
    @Bean
    public String getText(){
        return "hello";
    }
    @Bean
    public String getText2(){
        return "hello2";
    }
    @Bean
    public Dog dog(){
        return new Dog();
    }
}
