package org.cc.enterpriseagent.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private T data;
    private String message;
    private Integer code;

    //成功无数据
    public static <T> Result<T> success(){
        return new Result<T>(null,"success",200);
    };

    //成功有数据
    public static <T> Result<T> success(T data){
        return new Result<T>(data,"success",200);
    }

    //错误信息
    public static <T> Result<T> error(Integer code,String message){
        return new Result<>(null,message,code);
    }

    //内部服务器错误
    public static <T> Result<T> error(String message){
        return new Result<>(null,message,500);
    }

}
