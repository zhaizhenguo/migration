package com.oscar.migration.vo;

/**
 * @author zzg
 * @description: TODO
 * @date 2021/1/20 9:44
 */
//@Data
//@ToString
public class TableInfoVo {
//    @JSONField(serialize=false)
    private SchemaInfoVo schemaInfoVo;
    private String tableName;

    public SchemaInfoVo getSchemaInfoVo() {
        return schemaInfoVo;
    }

//    @JSONField(serialize=false)
    public void setSchemaInfoVo(SchemaInfoVo schemaInfoVo) {
        this.schemaInfoVo = schemaInfoVo;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
