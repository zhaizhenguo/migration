package com.oscar.migration.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Table(name = "SYS_ROLE_MENU")
public class SysRoleMenu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ROLE_ID")
    private Long RoleId;

    @Column(name = "MENU_ID")
    private Long menuId;

}

