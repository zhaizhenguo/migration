package com.oscar.migration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "SYS_ROLE")
public class SysRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "REMARK", length = 2000)
    private String remark;

    /**非数据库字段*/
    @Transient
    private List<Long> roleMenus = new ArrayList<>();

}

