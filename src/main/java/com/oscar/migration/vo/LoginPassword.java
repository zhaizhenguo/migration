package com.oscar.migration.vo;

import lombok.Data;

/**
 * @author zzg
 * @description: 修改密码bean
 * @date 2020/12/28 15:16
 */
@Data
public class LoginPassword {
    Long id;
    String oldPassword;
    String newPassword;
}
