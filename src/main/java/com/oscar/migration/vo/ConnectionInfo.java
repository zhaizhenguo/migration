package com.oscar.migration.vo;

import lombok.Data;

/**
 * @author zzg
 * @description: 连接信息
 * @date 2021/1/12 13:30
 */
@Data
public class ConnectionInfo {

    /**连接类型 0：源端  1：目标端*/
    private int connectionType;
    /**数据库类型*/
    private String dataSource;
    /**驱动类型*/
    private String driver;
    /**数据库*/
    private String dataBase;
    /**数据库ip*/
    private String server;
    /**数据库端口*/
    private String port;
    /**用户名*/
    private String userName;
    /**密码*/
    private String password;

}
