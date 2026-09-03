package com.study.sprintbootwithsqldemo.model.entity;
import lombok.Data;


@Data
public class User {
    private String id;
    private String username;
    private String password;
    private String create_time;
    private String update_time;
}
