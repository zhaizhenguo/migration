package com.oscar.migration.vo;

import lombok.Data;

@Data
public class LoginBean {
    private String userName;
    private String password;
    private String captcha;
}
