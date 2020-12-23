package com.oscar.migration.service.impl;

import com.oscar.migration.entity.SysRole;
import com.oscar.migration.service.SysRoleService;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;
import com.oscar.migration.vo.Result;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzg
 * @description: 测试文件
 * @date 2020/12/21 13:26
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class SysRoleServiceImplTest {

    @Autowired
    SysRoleService sysRoleService;

    @Test
    void findRoleMenus() {
    }

    @Test
    void save() {
        SysRole role = new SysRole();
        role.setName("管理员4");
        role.setCreateBy("admin");
        role.setRemark("试试呀！");
        List<Long> list = new ArrayList<>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        role.setRoleMenus(list);
        Result res = sysRoleService.save(role);
        System.out.println(res);
        Assert.assertEquals(0, res.getCode());
    }

    @Test
    void delete() {
        SysRole role = new SysRole();
        role.setId(9L);
        Result res = sysRoleService.delete(role);
        System.out.println(res);
        Assert.assertEquals(0, res.getCode());
    }

    @Test
    void delete1() {
    }

    @Test
    void update() {
        SysRole role = new SysRole();
        role.setId(10L);
        role.setName("管理员4修改后2");
        role.setCreateBy("admin");
        role.setRemark("试试呀！");
        role.setLaseUpdateBy("管理员");
        List<Long> list = new ArrayList<>();
        list.add(1L);
        role.setRoleMenus(list);
        Result res = sysRoleService.update(role);
        System.out.println(res);
        Assert.assertEquals(0, res.getCode());
    }


    @Test
    void findById() {
    }

    @Test
    void findPage() {
        PageRequest pr = new PageRequest();
        PageResult resPage = sysRoleService.findPage(pr);
        System.out.println(resPage);
    }

    @Test
    void findPageByName() {
    }

    @Test
    void findPageCommon() {
    }
}