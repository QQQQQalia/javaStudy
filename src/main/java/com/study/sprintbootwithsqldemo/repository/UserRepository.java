package com.study.sprintbootwithsqldemo.repository;

import com.study.sprintbootwithsqldemo.model.dto.RegisterDto;
import com.study.sprintbootwithsqldemo.model.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;


@Mapper
public interface UserRepository {
    @Select("select * from user where username = #{username}")
    public Optional<User> searchUserByUsername(String username);

    @Insert("insert into user(username, password) values (#{username},#{password})")
    public int insertUser(RegisterDto form);
}




