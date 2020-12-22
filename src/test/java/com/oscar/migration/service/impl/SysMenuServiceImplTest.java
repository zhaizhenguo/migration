package com.oscar.migration.service.impl;

import com.oscar.migration.service.SysMenuService;
import com.oscar.migration.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
class SysMenuServiceImplTest {


    @Autowired
    SysMenuServiceImpl sysMenuService;

    @Test
    void findTree() {
        System.out.println(sysMenuService.findTree("zzg"));
    }

    @Test
    void findMenuByUser() {
        System.out.println(sysMenuService.findMenuByUser("admin"));
    }
}