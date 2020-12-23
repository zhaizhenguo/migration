package com.oscar.migration.service.impl;

import com.oscar.migration.entity.SysUser;
import com.oscar.migration.entity.SysUserRole;
import com.oscar.migration.service.SysUserService;
import com.oscar.migration.vo.Result;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zzg
 * @description: 测试文件
 * @date 2020/12/21 13:26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class SysUserServiceImplTest {

    @Autowired
    SysUserService userService;

    @Test
    void save() {
        SysUser sysUser = new SysUser();
        sysUser.setCreateBy("admin");
        sysUser.setCreateTime(new Date());
        sysUser.setDelFlag(0L);
        sysUser.setEmail("361522251@qq.com");
        sysUser.setMobile("1236547895");
        sysUser.setPassword("123456");
        sysUser.setName("jsz");
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        sysUser.setUserRoles(list);
        Result res = userService.save(sysUser);
        System.out.println(res);
        Assert.assertEquals(res.getCode(),0);
    }
    @Test
    void update() {
        SysUser sysUser = new SysUser();
        sysUser.setId(2L);
        sysUser.setCreateBy("admin");
        sysUser.setCreateTime(new Date());
        sysUser.setDelFlag(0L);
        sysUser.setLaseUpdateBy("admin");
        sysUser.setEmail("361522251@qq.com");
        sysUser.setMobile("1236547895");
        sysUser.setPassword("123456");
        sysUser.setName("Test");
        Result res = userService.update(sysUser);
        Assert.assertEquals(res.getCode(),0);
    }

    @Test
    void delete() {
        SysUser sysUser = new SysUser();
        sysUser.setId(7L);
        sysUser.setLaseUpdateBy("admin");
        Result res = userService.delete(sysUser);
        Assert.assertEquals(res.getCode(),0);
    }

    @Test
    void findById() {
        System.out.println(userService.findById(4L));
    }


    @Test
    void findRolesByUserId() {
        Result userRoles = userService.findRolesByUserId(3L);
        System.out.println(userRoles);
        Assert.assertEquals(0,userRoles.getCode());
    }
}