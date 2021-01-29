package com.oscar.migration.vo;

import com.oscar.entity.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zzg
 * @description: TODO
 * @date 2021/1/19 13:38
 */
@Data
public class SchemaInfoVo {
    private String name;
    private String oid;
    /**
     * 通过SQL查询迁移构造的表信息列表
     */
    private List<TableInfoVo> sqlTableInfos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public List<TableInfoVo> getSqlTableInfos() {
        return sqlTableInfos;
    }

    public void setSqlTableInfos(List<TableInfoVo> sqlTableInfos) {
        this.sqlTableInfos = sqlTableInfos;
    }

}
