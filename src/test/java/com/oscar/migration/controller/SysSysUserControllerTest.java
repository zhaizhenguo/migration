package com.oscar.migration.controller;

import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class SysSysUserControllerTest {

    @Autowired
    SysUserService userService;

    @Test
    void findPage() {
        System.out.println(userService.findPage(new PageRequest()));
    }
}