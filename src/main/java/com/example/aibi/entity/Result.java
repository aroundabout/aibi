package com.example.aibi.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class Result<T> {
    private int code;
    private String msg;
    private T data;
    @Tolerate
    public Result() {}
}
