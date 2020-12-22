package com.oscar.migration.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author zzg
 * @description: 基础实体类
 * @date 2020/12/22 11:12
 */
@Data
@MappedSuperclass
public class BaseEntity {
    @Column(name = "CREATE_BY", length = 100)
    private String createBy;

    @Column(name = "CREATE_TIME" )
    private Date createTime;

    @Column(name = "LAST_UPDATE_BY", length = 40)
    private String laseUpdateBy;

    @Column(name = "LAST_UPDATE_TIME" )
    private Date laseUpdateTime;

    @Column(name = "DEL_FLAG")
    private Long delFlag = 0L;
}
