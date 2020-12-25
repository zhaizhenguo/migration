package com.oscar.migration.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author zzg
 * @description: 登录认证信息
 * @date 2020/12/24 11:00
 */
@Data
public class LoginAuthInfo {
    private Long userId;
    private String userName;
    private String userRole;
    private String createTime;
    private String token;
}
