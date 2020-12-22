package com.oscar.migration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "SYS_USER")
public class SysUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Column(name = "SALT", length = 40)
    private String salt;

    @Column(name = "EMAIL", length = 100)
    private String email;

    @Column(name = "MOBILE", length = 40)
    private String mobile;

    @Column(name = "STATUS")
    private Long status;

    /**非数据库字段*/
    @Transient
    private List<Long> userRoles = new ArrayList<>();

}

