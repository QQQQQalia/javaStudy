package com.study.sprintbootwithsqldemo.utils;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TimeUtil {
    public String getNowTime(){
        return new Date().toString();
    }
}
