package com.oscar.migration.constants;

/**
 * @author zzg
 * @description: 状态结果集
 * @date 2020/12/18 15:49
 */
public enum ResponseCode {

    /**
     * @description: 请求成功
     */
    SUCCESS(0, "SUCCESS"),
    /**
     * @description: 错误,服务器出BUG
     */
    ERROR(999, "未知异常，请联系管理员"),
    /**
     * @description: 参数错误
     */
    PARAM_ERROR(-7, "参数错误")
   ;
    public final int code;
    public final String msg;

    /**
     */
    ResponseCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
