package com.oscar.migration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "SYS_MENU")
public class SysMenu extends BaseEntity{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "PARENT_ID")
    private Long parentId;

    @Column(name = "INDEX" , length = 500)
    private String index;

    @Column(name = "TYPE" )
    private String type;

    @Column(name = "ICON" , length = 100)
    private String icon;

    @Column(name = "ORDER_NUM")
    private Long orderNum;

    /**非数据库字段*/
    @Transient
    private String parentName;

    /**非数据库字段*/
    @Transient
    private List<SysMenu> children;
}

