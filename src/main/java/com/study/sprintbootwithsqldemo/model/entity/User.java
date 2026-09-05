package com.study.sprintbootwithsqldemo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;


@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private String id;
    private String username;
    private String password;
    @TableField(fill = FieldFill.DEFAULT)
    private String create_time;
    @TableField(fill = FieldFill.DEFAULT)
    private String update_time;
}
