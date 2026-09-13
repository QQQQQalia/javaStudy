package com.study.sprintbootwithsqldemo.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 登录专用的入参：比注册多一个验证码
@Data
public class LoginDto {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;
}
