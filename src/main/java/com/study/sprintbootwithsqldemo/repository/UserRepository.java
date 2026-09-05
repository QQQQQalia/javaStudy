package com.study.sprintbootwithsqldemo.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.sprintbootwithsqldemo.model.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserRepository extends BaseMapper<User> {
    // mybatis 写法
    //    @Select("select * from user where username = #{username}")
    //    public Optional<User> searchUserByUsername(String username);
    //
    //    @Insert("insert into user(username, password) values (#{username},#{password})")
    //    public boolean insertUser(RegisterDto form);
    //
    //    @Select("select * from user where id = #{id}")
    //    public Optional<User> searchUserById(String id);
    //
    //    @Update("update user set password = #{newPassword} where id = #{id}")
    //    public boolean updateUserPassword(String id, String newPassword);
    //
    //    @Delete("delete from user where id = #{id}")
    //    public boolean deleteUser(String id);
}




