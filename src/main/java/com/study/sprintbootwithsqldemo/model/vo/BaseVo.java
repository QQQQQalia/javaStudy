package com.study.sprintbootwithsqldemo.model.vo;

import lombok.Data;
//自动写getter和setter
@Data
public class BaseVo<T> {
    private boolean success;
    private String msg;
    private T data;
    private BaseVo (boolean success,String msg,T data){
        this.success=success;
        this.msg=msg;
        this.data=data;
    }
    public static <F> BaseVo<F> success(F data){
        BaseVo<F> baseVo= new BaseVo<>(true,"成功",data);
        return baseVo;
    }
    public static <F> BaseVo<F> fail(F data,String msg){
        BaseVo<F> baseVo= new BaseVo<>(false,msg,data);
        return baseVo;
    }    
}

 

 