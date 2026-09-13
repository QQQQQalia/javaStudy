package com.study.sprintbootwithsqldemo.config;

import com.study.sprintbootwithsqldemo.model.vo.BaseVo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 把参数校验失败转成项目自己的响应结构，前端直接读 msg 就能显示提示
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseVo<Void> handleValidation(MethodArgumentNotValidException err) {
        String msg = err.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数不合法");
        return BaseVo.fail(null, msg);
    }
}
