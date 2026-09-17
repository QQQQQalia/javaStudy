package com.study.sprintbootwithsqldemo.model.dto;

import lombok.Data;

@Data
public class UserListDto {
    private String username;
    private String id;
    private String startTime;
    private String endTime;
    private int pageNum;
    private int pageSize;
}
