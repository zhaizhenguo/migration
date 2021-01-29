package com.oscar.migration.controller;

import com.oscar.migration.vo.LoginBean;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zzg
 * @description:
 * @date 2020/12/23 13:58
 */

@RunWith(SpringRunner.class)
@SpringBootTest
class SysLoginControllerTest {

    @Autowired
    SysLoginController sysLoginController;

    @Test
    void captcha() {
    }

    @Test
    void login() {
        LoginBean login = new LoginBean();
        login.setUserName("jsz");
        login.setPassword("123456");
    }
}