package com.ic.filemanage.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result <T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    private Result() {}

    public static <T> Result<T> success1(){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        return result;
 }
    public static <T> Result<T> success2(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message){
        Result<T> result = new Result<>();
        result.setCode(100);
        result.setMsg("操作失败");
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }


}
