package com.study.sprintbootwithsqldemo.model.vo;

import lombok.Data;

import java.util.List;

//自动写getter和setter
@Data
public class ListVo<T> {
    private int total;
    private List<T> list;
    private int pageNum;
    private int pageSize;
    private boolean hasMore;

    private ListVo(int total, List<T> list, int pageNum, int pageSize, boolean hasMore) {
        this.total = total;
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.hasMore = hasMore;
    }

    public static <F> ListVo<F> getListVo(int total, List<F> list, int pageNum, int pageSize, boolean hasMore) {
        return new ListVo<>(total, list, pageNum, pageSize, hasMore);
    }

}

 

 