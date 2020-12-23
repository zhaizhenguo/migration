package com.oscar.migration.service.impl;

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
        System.out.println(sysMenuService.findTree(4L));
    }

    @Test
    void findMenuByUser() {
        System.out.println(sysMenuService.findMenuByUserId(4L));
    }
}