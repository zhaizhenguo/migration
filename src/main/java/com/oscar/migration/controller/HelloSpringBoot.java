package com.oscar.migration.controller;

import com.oscar.migration.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 翟振国
 * @description: 测试使用
 * @date 2020/11/27 10:03
 */
@RestController
public class HelloSpringBoot {

    @RequestMapping(path = "/hello")
    public Result HelloSpring() {
        System.out.println("hello spring boot");
        return new Result();
    }

}
