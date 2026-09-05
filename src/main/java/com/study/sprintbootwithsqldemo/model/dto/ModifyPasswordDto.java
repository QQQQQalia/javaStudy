package com.study.sprintbootwithsqldemo.model.dto;

import lombok.Data;

@Data
public class ModifyPasswordDto {
    private String id;
    private String oldPassword;
    private String newPassword;
}
