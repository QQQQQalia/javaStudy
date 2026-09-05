package com.study.sprintbootwithsqldemo.service;

import com.study.sprintbootwithsqldemo.model.dto.ModifyPasswordDto;
import com.study.sprintbootwithsqldemo.model.dto.RegisterDto;
import com.study.sprintbootwithsqldemo.model.dto.UserIdDto;
import com.study.sprintbootwithsqldemo.model.entity.User;
import com.study.sprintbootwithsqldemo.model.vo.BaseVo;
import com.study.sprintbootwithsqldemo.model.vo.UserVo;
import com.study.sprintbootwithsqldemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
//加上这个注解，说明把这个类交个Spring容器管着

public class UserService {

    @Autowired
    UserRepository userRepository;

    public BaseVo<Void> registerFun(RegisterDto form) {
        Optional<User> Optuser = userRepository.searchUserByUsername(form.getUsername());
        if (Optuser.isPresent()) {
            return BaseVo.fail(null, "用户已注册");
        } else {
            boolean result = userRepository.insertUser(form);
            if (result) {
                return BaseVo.success(null);
            } else {
                return BaseVo.fail(null, "注册失败");
            }
        }
    }

    public BaseVo<Optional<UserVo>> loginFun(RegisterDto form) {
        Optional<User> Optuser = userRepository.searchUserByUsername(form.getUsername());
        if (Optuser.isEmpty()) {
            return BaseVo.fail(null, "登录失败,用户不存在");
        } else {
            User user = Optuser.get();
            if (user.getPassword().equals(form.getPassword())) {
                UserVo userVo = new UserVo();
                userVo.setUsername(user.getUsername());
                userVo.setId(user.getId());
                return BaseVo.success(Optional.of(userVo));
            } else {
                return BaseVo.fail(null, "登录失败,密码错误");
            }
        }
    }

    public BaseVo<Void> modifyUserPasswordFun(ModifyPasswordDto value) {
        Optional<User> Optuser = userRepository.searchUserById(value.getId());
        if (Optuser.isEmpty()) {
            return BaseVo.fail(null, "修改失败,用户不存在");
        } else {
            User user = Optuser.get();
            if (user.getPassword().equals(value.getOldPassword())) {
                boolean result = userRepository.updateUserPassword(value.getId(), value.getNewPassword());
                if (result) {
                    return BaseVo.success(null);
                } else {
                    return BaseVo.fail(null, "修改失败");
                }
            } else {
                return BaseVo.fail(null, "修改失败,旧密码不正确");
            }
        }
    }

    public BaseVo<Void> deleteUserFun(UserIdDto value) {
        Optional<User> Optuser = userRepository.searchUserById(value.getId());
        if (Optuser.isEmpty()) {
            return BaseVo.fail(null, "删除失败,用户不存在");
        } else {
            boolean result = userRepository.deleteUser(value.getId());
            if (result) {
                return BaseVo.success(null);
            } else {
                return BaseVo.fail(null, "删除失败");
            }
        }
    }
}

