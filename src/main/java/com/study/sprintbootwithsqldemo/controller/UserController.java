package com.study.sprintbootwithsqldemo.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.study.sprintbootwithsqldemo.model.dto.LoginDto;
import com.study.sprintbootwithsqldemo.model.dto.ModifyPasswordDto;
import com.study.sprintbootwithsqldemo.model.dto.RegisterDto;
import com.study.sprintbootwithsqldemo.model.dto.UserIdDto;
import com.study.sprintbootwithsqldemo.model.vo.BaseVo;
import com.study.sprintbootwithsqldemo.model.vo.UserVo;
import com.study.sprintbootwithsqldemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;


//所有用户相关的接口都写在这
@RestController
//所有接口都有一个user前缀
@RequestMapping("/user")
public class UserController {
    @Autowired
    DefaultKaptcha defaultKaptcha;
    @Autowired
    UserService userService;

    // 注册
    @PostMapping("/register")
    public BaseVo<Void> registerUser(@Valid @RequestBody RegisterDto form) {
        BaseVo<Void> result = userService.registerFun(form);
        return result;
    }

    // 登录
    @PostMapping("/login")
    public BaseVo<Optional<UserVo>> login(@Valid @RequestBody LoginDto form, HttpSession session) {
        BaseVo<Optional<UserVo>> result = userService.loginFun(form, session);
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

    @GetMapping("/getCode")
    public String getCode(HttpSession session) throws IOException {
        //存session
        // session.setAttribute("code", "abcde");

        String text = defaultKaptcha.createText();
        session.setAttribute("code", text);

        //image->字符数组
        BufferedImage image = defaultKaptcha.createImage(text);
        ByteArrayOutputStream boutput = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", boutput);
        byte[] imageByteArr = boutput.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageByteArr);

        return "data:image/jpeg;base64," + base64Image;
    }
}
