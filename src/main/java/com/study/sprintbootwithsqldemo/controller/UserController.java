package com.study.sprintbootwithsqldemo.controller;

import com.study.sprintbootwithsqldemo.model.dto.ModifyPasswordDto;
import com.study.sprintbootwithsqldemo.model.dto.RegisterDto;
import com.study.sprintbootwithsqldemo.model.dto.UserIdDto;
import com.study.sprintbootwithsqldemo.model.vo.BaseVo;
import com.study.sprintbootwithsqldemo.model.vo.UserVo;
import com.study.sprintbootwithsqldemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


//所有用户相关的接口都写在这
@RestController
//所有接口都有一个user前缀
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    // 注册
    @PostMapping("/register")
    public BaseVo<Void> registerUser(@RequestBody RegisterDto form) {
        BaseVo<Void> result = userService.registerFun(form);
        return result;
    }

    // 登录
    @PostMapping("/login")
    public BaseVo<Optional<UserVo>> login(@RequestBody RegisterDto form) {
        BaseVo<Optional<UserVo>> result = userService.loginFun(form);
        return result;
    }

    // 修改密码
    @PostMapping("/modifyPassword")
    public BaseVo<Void> modifyUserPassword(@RequestBody ModifyPasswordDto value) {
        BaseVo<Void> result = userService.modifyUserPasswordFun(value);
        return result;
    }

    // 删除用户
    @PostMapping("/deleteUser")
    public BaseVo<Void> deleteUser(@RequestBody UserIdDto userId) {
        BaseVo<Void> result = userService.deleteUserFun(userId);
        return result;
    }
}
