package com.study.sprintbootwithsqldemo.repository;

import com.study.sprintbootwithsqldemo.model.dto.RegisterDto;
import com.study.sprintbootwithsqldemo.model.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;


@Mapper
public interface UserRepository {
    @Select("select * from user where username = #{username}")
    public Optional<User> searchUserByUsername(String username);

    @Insert("insert into user(username, password) values (#{username},#{password})")
    public boolean insertUser(RegisterDto form);

    @Select("select * from user where id = #{id}")
    public Optional<User> searchUserById(String id);

    @Update("update user set password = #{newPassword} where id = #{id}")
    public boolean updateUserPassword(String id, String newPassword);

    @Delete("delete from user where id = #{id}")
    public boolean deleteUser(String id);
}




