package com.oscar.migration.vo;

import com.oscar.migration.constants.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 翟振国
 * @description: 结果返回类
 * @date 2020/11/27 18:05
 */
@Data
public class Result implements Serializable {

    private int code = 0;
    private String msg;
    private Object data;


    public static Result ok(String msg) {
        Result r = new Result();
        r.setMsg(msg);
        return r;
    }

    public static Result ok(Object data) {
        Result r = new Result();
        r.setData(data);
        return r;
    }

    public static Result ok() {
        return new Result();
    }

    public static Result error() {
        return error(ResponseCode.ERROR.code, ResponseCode.ERROR.msg);
    }

    public static Result error(String msg) {
        return error(ResponseCode.ERROR.code, msg);
    }

    public static Result error(int code, String msg) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }


}

