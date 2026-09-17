package com.study.sprintbootwithsqldemo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.sprintbootwithsqldemo.model.dto.*;
import com.study.sprintbootwithsqldemo.model.entity.User;
import com.study.sprintbootwithsqldemo.model.vo.BaseVo;
import com.study.sprintbootwithsqldemo.model.vo.ListVo;
import com.study.sprintbootwithsqldemo.model.vo.UserVo;
import com.study.sprintbootwithsqldemo.repository.UserRepository;
import com.study.sprintbootwithsqldemo.utils.JwtUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//加上这个注解，说明把这个类交个Spring容器管着

public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtils jwtUtils;

    public BaseVo<Void> registerFun(RegisterDto form) {
//        Optional<User> Optuser = userRepository.searchUserByUsername(form.getUsername());
//        if (Optuser.isPresent()) {
//            return BaseVo.fail(null, "用户已注册");
//        } else {
//            boolean result = userRepository.insertUser(form);
//            if (result) {
//                return BaseVo.success(null);
//            } else {
//                return BaseVo.fail(null, "注册失败");
//            }
//        }
        String username = form.getUsername();
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
        queryWrapper.eq("username", username);
        if (userRepository.selectOne(queryWrapper) == null) {
            User user = new User();
            user.setUsername(form.getUsername());
            user.setPassword(form.getPassword());
            int result = userRepository.insert(user);
            if (result == 1) {
                return BaseVo.success(null);
            } else {
                return BaseVo.fail(null, "注册失败");
            }
        } else {
            return BaseVo.fail(null, "用户名已经存在");
        }
    }

    public BaseVo<Optional<UserVo>> loginFun(LoginDto form, HttpSession session) {
//        Optional<User> Optuser = userRepository.searchUserByUsername(form.getUsername());
//        if (Optuser.isEmpty()) {
//            return BaseVo.fail(null, "登录失败,用户不存在");
//        } else {
//            User user = Optuser.get();
//            if (user.getPassword().equals(form.getPassword())) {
//                UserVo userVo = new UserVo();
//                userVo.setUsername(user.getUsername());
//                userVo.setId(user.getId());
//                return BaseVo.success(Optional.of(userVo));
//            } else {
//                return BaseVo.fail(null, "登录失败,密码错误");
//            }
//        }
        String code = (String) session.getAttribute("code");
        if (form.getCode().equals(code)) {
            String username = form.getUsername();
            String password = form.getPassword();
            QueryWrapper<User> queryWrapper = new QueryWrapper<User>();
            queryWrapper.eq("username", username);
            User user = userRepository.selectOne(queryWrapper);
            // 用户不存在和密码错误返回同一句提示，避免别人用登录接口试探某个用户名是否注册过
            if (user == null || !user.getPassword().equals(password)) {
                return BaseVo.fail(null, "登录失败,用户名或密码错误");
            }
            UserVo userVo = new UserVo();
            userVo.setUsername(user.getUsername());
            userVo.setId(user.getId());
            userVo.setToken(jwtUtils.getToken(user.getId()));
            return BaseVo.success(Optional.of(userVo));
        } else {
            return BaseVo.fail(null, "验证码错误");

        }
    }

    public BaseVo<Void> modifyUserPasswordFun(ModifyPasswordDto value) {
//        Optional<User> Optuser = userRepository.searchUserById(value.getId());
//        if (Optuser.isEmpty()) {
//            return BaseVo.fail(null, "修改失败,用户不存在");
//        } else {
//            User user = Optuser.get();
//            if (user.getPassword().equals(value.getOldPassword())) {
//                boolean result = userRepository.updateUserPassword(value.getId(), value.getNewPassword());
//                if (result) {
//                    return BaseVo.success(null);
//                } else {
//                    return BaseVo.fail(null, "修改失败");
//                }
//            } else {
//                return BaseVo.fail(null, "修改失败,旧密码不正确");
//            }
//        }
        String id = value.getId();
        User user = userRepository.selectById(id);
        if (user == null) {
            return BaseVo.fail(null, "修改失败,用户不存在");
        } else {
            if (user.getPassword().equals(value.getOldPassword())) {
                user.setPassword(value.getNewPassword());
                userRepository.updateById(user);
                return BaseVo.success(null);
            } else {
                return BaseVo.fail(null, "修改失败,旧密码不正确");
            }
        }
    }

    public BaseVo<Void> deleteUserFun(UserIdDto value) {
//        Optional<User> Optuser = userRepository.searchUserById(value.getId());
//        if (Optuser.isEmpty()) {
//            return BaseVo.fail(null, "删除失败,用户不存在");
//        } else {
//            boolean result = userRepository.deleteUser(value.getId());
//            if (result) {
//                return BaseVo.success(null);
//            } else {
//                return BaseVo.fail(null, "删除失败");
//            }
//        }
//    }
        String id = value.getId();
        User user = userRepository.selectById(id);
        if (user == null) {
            return BaseVo.fail(null, "删除失败,用户不存在");
        } else {
            int result = userRepository.deleteById(id);
            if (result == 1) {
                return BaseVo.success(null);
            } else {
                return BaseVo.fail(null, "删除失败");
            }
        }
    }

    // 分页查询
    public BaseVo<ListVo<User>> getListFun(UserListDto form) {
        int pageNum = form.getPageNum();
        int pageSize = form.getPageSize();
        int offset = (pageNum - 1) * pageSize;
        List<User> userList = userRepository.getUserList(form, offset);
        int totalNum = userRepository.getCountUser();
        boolean hasMore = pageNum * pageSize < totalNum;
       return BaseVo.success(ListVo.getListVo(totalNum, userList, pageNum, pageSize, hasMore));
    }
}

