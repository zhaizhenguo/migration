package com.oscar.migration.service;

import com.alibaba.fastjson.JSON;
import com.oscar.anywhere.UniverLanguage;
import com.oscar.entity.*;
import com.oscar.exception.ColumnTransformException;
import com.oscar.migration.entity.UserResource;
import com.oscar.migration.vo.ConnectionInfo;
import com.oscar.migration.vo.MigrationConfigInfo;
import com.oscar.migration.vo.Result;
import com.oscar.ui.common.controller.PageController;
import com.oscar.ui.navigation.page.MigrationExecPage;
import com.oscar.util.MigrationConstants;
import com.oscar.util.PreviewUtil;
import com.oscar.util.typeMap.Xdb2OscarMapUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author zzg
 * @description: 数据迁移业务层
 * @date 2021/1/18 11:02
 */
@Service
@Slf4j
public class DataMigrationService {

    /**
     * @description: 测试连接
     * @author zzg
     * @date: 2021/1/18 11:14
     * @param: [userResource, pageController, connectionInfo]
     * @return: com.oscar.migration.vo.Result
     */
    public synchronized Result testConnection(UserResource userResource, PageController pageController,
                                              ConnectionInfo connectionInfo) {
        if (userResource == null || pageController == null || connectionInfo == null) {
            return Result.error("参数错误");
        }
        try {
            //判断此用户是否已经创建了工程
            if (!userResource.isCreateProject()) {
                userResource.setCreateProject(true);
                pageController.createProject();
            }
        } catch (Exception e) {
            log.error("创建项目失败", e);
            return Result.error(e.getMessage());
        }
        boolean isConnectionSuccess;
        try {
            //测试连接  源端端
            if (connectionInfo.getConnectionType() == 0) {
                isConnectionSuccess = pageController.createSourceConnection(connectionInfo.getDataSource(), connectionInfo.getDriver(),
                        connectionInfo.getServer(), connectionInfo.getPort(), connectionInfo.getDataBase(), connectionInfo.getUserName(),
                        connectionInfo.getPassword(), null, false, new SslInfo(), "sid");
            }
            //目标端
            else {
                isConnectionSuccess = pageController.createDestConnection(connectionInfo.getDataSource(), connectionInfo.getDriver(),
                        connectionInfo.getServer(), connectionInfo.getPort(), connectionInfo.getDataBase(), connectionInfo.getUserName(),
                        connectionInfo.getPassword(), null, false, new SslInfo(), "sid");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        if (!isConnectionSuccess) {
            return Result.error();
        } else {
            //当源端采集器不为空时
            if (pageController.getSrcAcquire() != null) {
                pageController.getSrcAcquire().setDestDbType(connectionInfo.getDataSource());
            }
        }
        return Result.ok();
    }

    /**
     * @description: 保存配置
     * @author zzg
     * @date: 2021/1/18 11:19
     * @param: [pageController, configInfo]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveConfig(PageController pageController, ConfigInfo configInfo) {
        if (pageController == null || configInfo == null) {
            return Result.error("参数错误");
        }
        MigrationConstants.UpperControl = configInfo.isUpper();
        pageController.setProjectConfigInfo(new ProjectConfigInfo(configInfo));
        return Result.ok();
    }

    /**
     * @description: 获取字段类型映射
     * @author zzg
     * @date: 2021/1/18 11:25
     * @param: [pageController]
     * @return: com.oscar.migration.vo.Result
     */
    public Result getTypeMapping(PageController pageController) {
        if (pageController == null) {
            return Result.error("参数错误");
        }
        // 源端类型
        String tempDbType = pageController.getProject().getConvertionData().getSourceConnInfo().getType();
        String tempDetailDbType = pageController.getProject().getConvertionData().getSourceConnInfo().getDetailType();
        if (tempDbType.equals(MigrationConstants.ORACLE_Type) && tempDetailDbType != null) {
            tempDbType = tempDetailDbType;
        }
        // 目标端类型
        String targetDbType = pageController.getProject().getConvertionData().getTargetConnInfo().getType();
        // 获取xml类型
        String sDbType = Xdb2OscarMapUtil.getMapTypeRoot(tempDbType, targetDbType);
        try {
            List<SourceTypesInfo> sourceTypesInfos = Xdb2OscarMapUtil.loadTempXml(sDbType, null);
            //重组列内容
            Xdb2OscarMapUtil.srcTypes2List(sourceTypesInfos, sDbType, null);
            System.out.println("获取字段类型映射JSON.toJSONString(sourceTypesInfos)==="+JSON.toJSONString(sourceTypesInfos));
            return Result.ok(sourceTypesInfos);
        } catch (Exception ex) {
            log.error("Exception", ex);
            return Result.error();
        }
    }

    /**
     * @description: 将table中的类型映射信息存入Project类中的对应Map对象
     * @author zzg
     * @date: 2021/1/18 13:30
     * @param: [sourceTypesInfos, pageController]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveTypeMapping(List<SourceTypesInfo> sourceTypesInfos, PageController pageController) {
        if (sourceTypesInfos == null || pageController == null) {
            return Result.error("参数错误");
        }
        //源端类型
        String sourceType = pageController.getProject().getConvertionData().getSourceConnInfo().getType();
        Map<String, SourceTypesInfo> typeMapSet = new HashMap<>(sourceTypesInfos.size());
        Iterator<SourceTypesInfo> iterator = sourceTypesInfos.iterator();
        /**将table中的类型映射信息存入Project类中的对应Map对象
         * 无重复情况已源类型名称作为key，有重复的情况用ID作为key
         */
        if (MigrationConstants.ORACLE_Type.equals(sourceType)) {
            while (iterator.hasNext()) {
                SourceTypesInfo typeInfo = iterator.next();
                if ("NUMBER".equals(typeInfo.getSname()) || "FLOAT".equals(typeInfo.getSname())
                        || "NVARCHAR2".equals(typeInfo.getSname())) {
                    typeMapSet.put(typeInfo.getId(), typeInfo);
                } else {
                    typeMapSet.put(typeInfo.getSname(), typeInfo);
                }
                //标记选中的目的列类型状态
                updateSeletedStatus(typeInfo);
            }
        } else if (MigrationConstants.SQLSERVER_Type.equals(sourceType)) {
            while (iterator.hasNext()) {
                SourceTypesInfo stype = iterator.next();
                if ("DECIMAL".equals(stype.getSname())
                        || "NUMERIC".equals(stype.getSname())
                        || "FLOAT".equals(stype.getSname())
                        || "VARCHAR".equals(stype.getSname())
                        || "NVARCHAR".equals(stype.getSname())
                        || "VARBINARY".equals(stype.getSname())) {
                    typeMapSet.put(stype.getId(), stype);
                } else {
                    typeMapSet.put(stype.getSname(), stype);
                }
                updateSeletedStatus(stype);
            }
        } else if (MigrationConstants.MYSQL_Type.equals(sourceType)) {
            while (iterator.hasNext()) {
                SourceTypesInfo stype = iterator.next();
                if ("DECIMAL".equals(stype.getSname())
                        || "VARCHAR".equals(stype.getSname())
                        || "VARBINARY".equals(stype.getSname())
                        || "BLOB".equals(stype.getSname())
                        || "TEXT".equals(stype.getSname())) {
                    typeMapSet.put(stype.getId(), stype);
                } else {
                    typeMapSet.put(stype.getSname(), stype);
                }
                updateSeletedStatus(stype);
            }
        } else if (MigrationConstants.DB2_Type.equals(sourceType)) {
            while (iterator.hasNext()) {
                SourceTypesInfo stype = iterator.next();
                if ("DECIMAL".equals(stype.getSname())
                        || "VARCHAR".equals(stype.getSname())
                        || "VARGRAPHIC".equals(stype.getSname())) {
                    typeMapSet.put(stype.getId(), stype);
                } else {
                    typeMapSet.put(stype.getSname(), stype);
                }
                updateSeletedStatus(stype);
            }
        } else if (MigrationConstants.POSTGRESQL_Type.equals(sourceType)) {
            while (iterator.hasNext()) {
                SourceTypesInfo stype = iterator.next();
                if ("DECIMAL".equals(stype.getSname())
                        || "NUMERIC".equals(stype.getSname())
                        || "CHARACTER VARYING".equals(stype.getSname())
                        || "VARCHAR".equals(stype.getSname())
                        || "TEXT".equals(stype.getSname())
                        || "TIMESTAMP WITHOUT TIME ZONE".equals(stype.getSname())
                        || "TIMESTAMP WITH TIME ZONE".equals(stype.getSname())
                        || "TIME WITHOUT TIME ZONE".equals(stype.getSname())
                        || "TIME WITH TIME ZONE".equals(stype.getSname())) {
                    typeMapSet.put(stype.getId(), stype);
                } else {
                    typeMapSet.put(stype.getSname(), stype);
                }
                updateSeletedStatus(stype);
            }
        } else {
            if (MigrationConstants.ORACLE_Type.equals(sourceType)) {
                while (iterator.hasNext()) {
                    SourceTypesInfo stype = iterator.next();
                    if ("NUMERIC".equals(stype.getSname()) || (stype.getSname().indexOf("TIMESTAMP") != -1)
                            || "BPCHAR".equals(stype.getSname()) || "CHAR".equals(stype.getSname())
                            || "VARCHAR".equals(stype.getSname())) {
                        typeMapSet.put(stype.getId(), stype);
                    } else {
                        typeMapSet.put(stype.getSname(), stype);
                    }
                    updateSeletedStatus(stype);
                }
            } else {
                while (iterator.hasNext()) {
                    SourceTypesInfo typeInfo = iterator.next();
                    typeMapSet.put(typeInfo.getSname(), typeInfo);
                    updateSeletedStatus(typeInfo);
                }
            }
        }
        pageController.getProject().setStypes(typeMapSet);
        return Result.ok();
    }

    /**
     * @description: 获取所有模式信息
     * @author zzg
     * @date: 2021/1/18 14:27
     * @param: [pageController]
     * @return: com.oscar.migration.vo.Result
     */
    public Result getAllPattern(PageController pageController) {
        if (pageController == null) {
            return Result.error("参数错误");
        }
        Map<String, Object> resMap = new HashMap<>(2);
        //源端模式信息
        List<SchemaInfo> schemaInfos = pageController.getSrcAcquire().fetchSchemaInfos();
        pageController.setSchemaInfos(schemaInfos);
        //目的端模式信息
        List<SchemaInfo> targetSchemaInfos = pageController.getDestAcquire().fetchSchemaInfos();
        pageController.setDestSchemaInfos(targetSchemaInfos);
        //源端name集合
        List<String> schemaNameList = new ArrayList(schemaInfos.size());
        //源端id name 映射关系
        Map<String, String> sourceSchemaIdNameMap = new HashMap<>(schemaInfos.size());
        for (SchemaInfo schemainfo : schemaInfos) {
            schemaNameList.add(schemainfo.getName());
            sourceSchemaIdNameMap.put(schemainfo.getOid(), schemainfo.getName());
        }
        //目的端id name 映射关系
        Map<String, String> targetSchemaIdNameMap = new HashMap<>(targetSchemaInfos.size());
        for (SchemaInfo schemainfo : targetSchemaInfos) {
            targetSchemaIdNameMap.put(schemainfo.getOid(), schemainfo.getName());
        }
        resMap.put("sourcePattern", schemaInfos);
        resMap.put("sourcePatternName", schemaNameList);
        resMap.put("sourceSchemaIdNameMap", sourceSchemaIdNameMap);
        resMap.put("targetSchemaIdNameMap", targetSchemaIdNameMap);
        resMap.put("targetPattern", targetSchemaInfos);
        System.out.println("获取所有模式信息JSON.toJSONString(resMap)==="+JSON.toJSONString(resMap));
        return Result.ok(resMap);
    }

    /**
     * @description: 获取源库和目的库表空间信息列表
     * @author zzg
     * @date: 2021/1/20 17:20
     * @param: [pageController]
     * @return: com.oscar.migration.vo.Result
     */
    public Result getTableSpace(PageController pageController) {
        String sourceType = pageController.getCurrentSourceConnInfo().getType();
        String targetType = pageController.getCurrentTargetConnInfo().getType();
        List<TableSpace> srcTableSpaces = null;
        List<TableSpace> destTableSpaces = null;
        try {
            if (sourceType.equals(MigrationConstants.SHENTONG_Type)
                    || sourceType.equals(MigrationConstants.KSTORE_Type)
                    || sourceType.equals(MigrationConstants.ORACLE_Type)
                    || sourceType.equals(MigrationConstants.POSTGRESQL_Type)) {
                srcTableSpaces = pageController.getSrcAcquire().fetchTableSpace();
                pageController.setSrcTableSpaces(srcTableSpaces);
            }
            if (targetType.equals(MigrationConstants.SHENTONG_Type)
                    || targetType.equals(MigrationConstants.KSTORE_Type)
                    || targetType.equals(MigrationConstants.ORACLE_Type)) {
                destTableSpaces = pageController.getDestAcquire().fetchTableSpace();
                pageController.setDestTableSpaces(destTableSpaces);
            }
        } catch (Exception e) {
            log.error("获取源库和目的库表空间信息列表异常", e);
            return Result.error();
        }

        Map<String, Object> resMap = new HashMap<>(2);
        resMap.put("sourceTablespace", srcTableSpaces);
        resMap.put("targetTablespace", destTableSpaces);

        System.out.println("获取源库和目的库表空间信息列表JSON.toJSONString(resMap)==="+JSON.toJSONString(resMap));

        return Result.ok(resMap);
    }

    /**
     * @description: 根据模式名称获取模式下所有信息
     * @author zzg
     * @date: 2021/1/18 16:13
     * @param: [pageController, patternName]
     * @return: com.oscar.migration.vo.Result
     */
    public Result getPatternDataById(PageController pageController, String id) {
        if (pageController == null || StringUtils.isBlank(id)) {
            return Result.error("参数错误");
        }
        String srcDBType = pageController.getProject().getConvertionData().getSourceConnInfo().getType();
        SchemaInfo schemaInfo = getPatternObjectById(pageController, id);
        if (schemaInfo == null) {
            return Result.error("参数错误");
        }
        if (srcDBType.equals(MigrationConstants.SHENTONG_Type) || srcDBType.equals(MigrationConstants.KSTORE_Type)) {
            List<TableSpace> tableSpaces = pageController.getSrcTableSpaces();
            pageController.setSrcTableSpaces(tableSpaces);
            //函数
            List<FunctionInfo> functions = pageController.fetchFunctionInfo(schemaInfo);
            schemaInfo.setFunctionInfos(functions);
            //存储过程
            List<Procedure> procedures = pageController.fetchProcedures(schemaInfo);
            schemaInfo.setProcedures(procedures);
            //包
            List<PackageInfo> packageInfos = pageController.fetchPackages(schemaInfo);
            schemaInfo.setPackages(packageInfos);
            //同义词
            List<SynonymInfo> synonymInfos = pageController.fetchSynonymInfo(schemaInfo);
            schemaInfo.setSynonyms(synonymInfos);
            //物化视图
            List<MaterializedViewInfo> viewInfos = pageController.fetchMaterializedViews(schemaInfo);
            schemaInfo.setMaterViewsInfo(viewInfos);
            //Type类型
            List<TypeInfo> typeInfos = pageController.fetchTypes(schemaInfo);
            schemaInfo.setTypeInfos(typeInfos);
        } else if (srcDBType.equals(MigrationConstants.ORACLE_Type)) {
            List<TableSpace> tableSpaces = pageController.getSrcTableSpaces();
            pageController.setSrcTableSpaces(tableSpaces);
            // TODO 模式下怎么没有表空间

            //同义词
            List<SynonymInfo> synonymInfos = pageController.fetchSynonymInfo(schemaInfo);
            schemaInfo.setSynonyms(synonymInfos);
            //物化视图
            List<MaterializedViewInfo> viewInfos = pageController.fetchMaterializedViews(schemaInfo);
            schemaInfo.setMaterViewsInfo(viewInfos);

            //oracle触发器可以建在多种数据库对象中
            if (pageController.getProjectConfigInfo().isMigrateOraclePl()) {
                //存储过程
                List<Procedure> procedures = pageController.fetchProcedures(schemaInfo);
                schemaInfo.setProcedures(procedures);
                //包
                List<PackageInfo> packageInfos = pageController.fetchPackages(schemaInfo);
                schemaInfo.setPackages(packageInfos);
                //触发器
                List<Trigger> triggers = pageController.fetchTriggers(schemaInfo);
                schemaInfo.setTriggers(triggers);
                //函数
                List<FunctionInfo> functions = pageController.fetchFunctionInfo(schemaInfo);
                schemaInfo.setFunctionInfos(functions);
                //Type类型
                List<TypeInfo> typeInfos = pageController.fetchTypes(schemaInfo);
                schemaInfo.setTypeInfos(typeInfos);
            }
        } else if (srcDBType.equals(MigrationConstants.KB_Type) || srcDBType.equals(MigrationConstants.KB8_Type)) {
            //同义词
            List<SynonymInfo> synonymInfos = pageController.fetchSynonymInfo(schemaInfo);
            schemaInfo.setSynonyms(synonymInfos);
        }
        //单独把dm拿出来增加物化视图
        else if (srcDBType.equals(MigrationConstants.DM_Type)) {
            //同义词
            List<SynonymInfo> synonymInfos = pageController.fetchSynonymInfo(schemaInfo);
            schemaInfo.setSynonyms(synonymInfos);
            // 物化视图
            List<MaterializedViewInfo> viewInfos = pageController.fetchMaterializedViews(schemaInfo);
            schemaInfo.setMaterViewsInfo(viewInfos);
        } else if (srcDBType.equals(MigrationConstants.POSTGRESQL_Type)) {
            // 表空间
            List<TableSpace> tableSpaces = pageController.getSrcTableSpaces();
            // TODO 模式下怎么没有表空间
        }

        if (!srcDBType.equals(MigrationConstants.SQLSERVER_Type)) {
            //序列
            List<Sequence> sequences = pageController.fetchSequence(schemaInfo);
            schemaInfo.setSequences(sequences);
        }
        //表
        List<TableInfo> tableInfos = pageController.fetchTables(schemaInfo);
        schemaInfo.setTableInfos(tableInfos);

        if (!srcDBType.equals(MigrationConstants.General_Type)) {
            //视图
            List<ViewInfo> viewInfos = pageController.fetchViews(schemaInfo);
            schemaInfo.setViewInfos(viewInfos);
        }

        List<TableSpace> srcTableSpaces = pageController.getSrcTableSpaces();

        Map<String, Object> resMap = new HashMap<>(2);
        List<String> tableNameList = null;
        if (tableInfos != null && tableInfos.size() > 0) {
            tableNameList = new ArrayList<>(tableInfos.size());
            for (TableInfo tableInfo : tableInfos) {
                tableNameList.add(tableInfo.getSourceName());
            }
        }
        resMap.put("tablespace", srcTableSpaces);
        resMap.put("tableNameList", tableNameList);
        String s = JSON.toJSONString(schemaInfo);
        System.out.println("s=========" + s);
        resMap.put("schemaInfo", schemaInfo);

        return Result.ok(resMap);
    }

    /**
     * @description: 获取表下的所有信息
     * @author zzg
     * @date: 2021/1/21 10:32
     * @param: [pageController, patternName, tableName]
     * @return: com.oscar.migration.vo.Result
     */
    public Result getAllTableData(PageController pageController, String patternName, String tableName) {
        if (pageController == null || StringUtils.isBlank(patternName) || StringUtils.isBlank(tableName)) {
            return Result.error("参数错误");
        }
        TableInfo tableInfo = loadTableData(pageController, patternName, tableName);
        return Result.ok(tableInfo);
    }

    /**
     * @description: 批量获取表下面的属性
     * @author zzg
     * @date: 2021/1/26 17:25
     * @param: [pageController, tables]
     * @return: com.oscar.migration.vo.Result
     */
    public Result batchGetAllTableData(PageController pageController, List<TableInfo> tables) {
        if (pageController == null || tables == null) {
            return Result.error("参数错误");
        }
        List<TableInfo> resList = new ArrayList<>(tables.size());
        for (TableInfo tableInfo : tables) {
            TableInfo resTableInfo = loadTableData(pageController, tableInfo.getTargetSchema(), tableInfo.getSourceName());
            resList.add(resTableInfo);
        }
        return Result.ok(resList);
    }

    /**
     * @description: 保存迁移参数
     * @author zzg
     * @date: 2021/1/21 12:59
     * @param: [pageController, migrationConfigInfo]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveMigrationInfo(PageController pageController, MigrationConfigInfo migrationConfigInfo) {
        if (migrationConfigInfo == null) {
            return Result.error("参数错误");
        }
        //保存强制迁移
        if (migrationConfigInfo.getIsOpenConstraintMigrate()) {
            pageController.setTargetSchema(migrationConfigInfo.getCheckTargetSchemaName());
        }
        //保存迁移范围
        pageController.setMigType(migrationConfigInfo.getCheckMigrationCircle());
        return Result.ok();
    }

    /**
     * @description: 保存模式信息
     * @author zzg
     * @date: 2021/1/26 13:14
     * @param: [pageController, schemaInfos]
     * @return: com.oscar.migration.vo.Result
     */
    public Result saveSchemaInfo(PageController pageController, List<SchemaInfo> schemaInfos) {
        if (pageController == null || schemaInfos == null) {
            return Result.error("参数错误");
        }
        //将模式信息赋给下级
        updateSchemaData(schemaInfos);
        List<SchemaInfo> schemaInfoList = pageController.getSchemaInfos();
        pageController.setSchemaInfos(schemaInfos);
        return Result.ok();
    }

    /**
     * @description: 执行迁移
     * @author zzg
     * @date: 2021/1/26 17:51
     * @param: [pageController]
     * @return: com.oscar.migration.vo.Result
     */
    public Result startMigration(PageController pageController) {
        /**加载列信息*/
        uploadLineData(pageController);
        /**加载其他信息*/
        uploadOtherData(pageController);

        CountDownLatch latch = new CountDownLatch(1);
        Long startTime = System.currentTimeMillis();
        PreviewUtil previewUtil = new PreviewUtil(pageController.getProject(), pageController
                .getCurrentSourceConnInfo().getType(), pageController);
        pageController.setPreviewUtil(previewUtil);
        /**执行迁移*/
        new MigrationExecPage(pageController, latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long endTime = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String time = decimalFormat.format(((double) endTime - (double) startTime) / 1000);
        log.info("迁移用时： " + time + "s");
        return Result.ok();
    }

    /**
     * @description: 通过模式id获取模式对象
     * @author zzg
     * @date: 2021/1/18 15:20
     * @param: [pageController, id]
     * @return: com.oscar.entity.SchemaInfo
     */
    private SchemaInfo getPatternObjectById(PageController pageController, String id) {
        Iterator<SchemaInfo> iterator = pageController.getSchemaInfos().iterator();
        SchemaInfo resSchemaInfo = null;
        while (iterator.hasNext()) {
            SchemaInfo schemaInfo = iterator.next();
            if (schemaInfo.getOid().equals(id)) {
                resSchemaInfo = schemaInfo;
                break;
            }
        }
        return resSchemaInfo;
    }

    /**
     * @description: 通过模式名称获取模式对象
     * @author zzg
     * @date: 2021/1/18 15:20
     * @param: [pageController, name]
     * @return: com.oscar.entity.SchemaInfo
     */
    private SchemaInfo getPatternObjectByName(PageController pageController, String name) {
        Iterator<SchemaInfo> iterator = pageController.getSchemaInfos().iterator();
        SchemaInfo resSchemaInfo = null;
        while (iterator.hasNext()) {
            SchemaInfo schemaInfo = iterator.next();
            if (schemaInfo.getName().equals(name)) {
                resSchemaInfo = schemaInfo;
                break;
            }
        }
        return resSchemaInfo;
    }

    /**
     * @description: 通过模式名 表名 获取表对象
     * @author zzg
     * @date: 2021/1/21 13:12
     * @param: [pageController, patternName, tableName]
     * @return: com.oscar.entity.TableInfo
     */
    private TableInfo getTableObjectByName(PageController pageController, String patternName, String tableName) {
        Iterator<SchemaInfo> iterator = pageController.getSchemaInfos().iterator();
        SchemaInfo resSchemaInfo = null;
        while (iterator.hasNext()) {
            SchemaInfo schemaInfo = iterator.next();
            if (schemaInfo.getName().equals(patternName)) {
                resSchemaInfo = schemaInfo;
                break;
            }
        }
        if (resSchemaInfo == null) {
            return null;
        }
        TableInfo resTableInfo = null;

        for (TableInfo tableInfo : resSchemaInfo.getTableInfos()) {
            if (tableInfo.getSourceName().equals(tableName)) {
                resTableInfo = tableInfo;
            }
        }
        return resTableInfo;
    }


    /**
     * @description: 增加自定义列
     * @author zzg
     * @date: 2021/1/18 13:03
     * @param: [sName, tName, typeMapSet]
     * @return: void
     */
    private void addLine(String sName, String tName, Map<String, SourceTypesInfo> typeMapSet) {
        SourceTypesInfo stype = new SourceTypesInfo();
        stype.setSname(sName.toUpperCase());
        stype.setId(sName.toUpperCase());
        List<TargetTypesInfo> accTypes = new ArrayList<TargetTypesInfo>();
        TargetTypesInfo targetTypesInfo = new TargetTypesInfo();
        targetTypesInfo.setTtypeName(tName.toUpperCase());
        targetTypesInfo.setId("T" + sName.toUpperCase() + "2" + tName.toUpperCase() + "PS");
        targetTypesInfo.setpValue("");
        targetTypesInfo.setsValue("");
        targetTypesInfo.setmValue("");
        accTypes.add(targetTypesInfo);
        stype.setAccTypes(accTypes);
        typeMapSet.put(stype.getId(), stype);
    }

    /**
     * @description: 更新目标列选中状态
     * @author zzg
     * @date: 2021/1/18 13:05
     * @param: [typeInfo]
     * @return: void
     */
    private void updateSeletedStatus(SourceTypesInfo typeInfo) {
        for (TargetTypesInfo subType : typeInfo.getAccTypes()) {
            subType.setSelected(false);
            if (typeInfo.getTargetFieldType().equals(subType.getTtypeName())) {
                subType.setSelected(true);
            }
        }
    }

    /**
     * @description: 加载表中的资源
     * @author zzg
     * @date: 2021/1/26 17:36
     * @param: [pageController, patternName, tableName]
     * @return: com.oscar.entity.TableInfo
     */
    private TableInfo loadTableData(PageController pageController, String patternName, String tableName) {
        // 源端类型
        String sourceType = pageController.getProject().getConvertionData().getSourceConnInfo().getType();
        // 目标端类型
        String targetType = pageController.getProject().getConvertionData().getTargetConnInfo().getType();
        //获取表对象
        TableInfo tableInfo = getTableObjectByName(pageController, patternName, tableName);
        //加载列基础信息
        pageController.fetchColumns(tableInfo);
        //重组列信息
        pageController.getConvertionData().setTypeMappingChanged(false);
        loadTableColumn(sourceType, targetType, tableInfo, pageController.getProject().getStypes());
        List list = new ArrayList<TableInfo>(1);
        list.add(tableInfo);
        //加载表中其他信息
        fetchUnloadObjects(list, pageController);
        return tableInfo;
    }

    /**
     * @description: 加载表中其他信息
     * @author zzg
     * @date: 2021/1/21 14:20
     * @param: [selTableInfos, pageController]
     * @return: void
     */
    public void fetchUnloadObjects(List<TableInfo> selTableInfos, PageController pageController) {
        List<CheckInfo> checks;
        List<ForeignKey> foreignKeys;
        List<UniqueInfo> uniqueInfos;
        List<IndexInfo> indexes;
        List<Trigger> triggers;
        List<FullIndexInfo> fullIndexInfos;

        String srcDBType = pageController.getCurrentSourceConnInfo().getType();
        for (TableInfo tableInfo : selTableInfos) {
            if (!tableInfo.isCommon()) {
                continue;
            }
            if (!tableInfo.isHasLoadCheck()) {
                if (!tableInfo.isHasLoadCheck()) {
                    checks = pageController.fetchChecks(tableInfo);
                    if (checks != null) {
                        for (CheckInfo checkInfo : checks) {
                            checkInfo.setChecked(true);
                        }
                    }
                    tableInfo.setCheckInfos(checks);
                    tableInfo.setHasLoadCheck(true);
                }
            }
            if (!tableInfo.isHasLoadForeignKey()) {
                if (!tableInfo.isHasLoadForeignKey()) {
                    foreignKeys = pageController.fetchForeignKeys(tableInfo);
                    if (foreignKeys != null) {
                        for (ForeignKey foreignKey : foreignKeys) {
                            foreignKey.setChecked(true);
                        }
                    }
                    tableInfo.setForeignKeys(foreignKeys);
                    tableInfo.setHasLoadForeignKey(true);
                }
            }
            if (!tableInfo.isHasLoadIndex()) {
                if (!tableInfo.isHasLoadIndex()) {
                    indexes = pageController.fetchIndexes(tableInfo);
                    if (indexes != null) {
                        for (IndexInfo indexInfo : indexes) {
                            indexInfo.setChecked(true);
                        }
                    }
                    tableInfo.setIndexInfos(indexes);
                    tableInfo.setHasLoadIndex(true);
                }
            }
            if (!tableInfo.isHasLoadTrigger() && srcDBType.equals(MigrationConstants.SHENTONG_Type)) {
                if (!tableInfo.isHasLoadTrigger()) {
                    triggers = pageController.fetchTriggers(tableInfo);
                    if (triggers != null) {
                        for (Trigger trigger : triggers) {
                            trigger.setChecked(true);
                        }
                    }
                    tableInfo.setTriggers(triggers);
                    tableInfo.setHasLoadTrigger(true);
                }

            }
            if (!tableInfo.isHasLoadUnique()) {
                if (!tableInfo.isHasLoadUnique()) {
                    uniqueInfos = pageController.fetchUniqueInfos(tableInfo);
                    if (uniqueInfos != null) {
                        for (UniqueInfo uniqueInfo : uniqueInfos) {
                            uniqueInfo.setChecked(true);
                        }
                    }
                    tableInfo.setUniqueInfos(uniqueInfos);
                    tableInfo.setHasLoadUnique(true);
                }

            }
            if (!tableInfo.hasLoadFullIndex() && srcDBType.equals(MigrationConstants.SHENTONG_Type)) {
                if (!tableInfo.hasLoadFullIndex()) {
                    fullIndexInfos = pageController.fetchFullIndexInfos(tableInfo);
                    if (fullIndexInfos != null) {
                        for (FullIndexInfo fullIndexInfo : fullIndexInfos) {
                            fullIndexInfo.setChecked(true);
                        }
                        tableInfo.setFullIndexInfos(fullIndexInfos);
                        tableInfo.setHasLoadFullIndex(true);
                    }
                }
            }
        }
    }

    /**
     * @description: 将模式信息赋给下级
     * @author zzg
     * @date: 2021/1/26 14:43
     * @param: [schemaInfos]
     * @return: void
     */
    private void updateSchemaData(List<SchemaInfo> schemaInfos) {
        for (SchemaInfo schemaInfo : schemaInfos) {
            //表
            List<TableInfo> tableInfos = schemaInfo.getTableInfos();
            if (tableInfos != null) {
                for (TableInfo tableInfo : tableInfos) {
                    tableInfo.setSchemaInfo(schemaInfo);
                    //更新表信息
                    updateTableData(schemaInfo, tableInfo);
                }
            }
            //视图
            List<ViewInfo> viewInfos = schemaInfo.getViewInfos();
            if (viewInfos != null) {
                for (ViewInfo viewInfo : viewInfos) {
                    viewInfo.setSchemaInfo(schemaInfo);
                }
            }
            //序列
            List<Sequence> sequences = schemaInfo.getSequences();
            if (sequences != null) {
                for (Sequence sequence : sequences) {
                    sequence.setSchemaInfo(schemaInfo);
                }
            }
            //存储过程
            List<Procedure> procedures = schemaInfo.getProcedures();
            if (procedures != null) {
                for (Procedure procedure : procedures) {
                    procedure.setSchemaInfo(schemaInfo);
                }
            }
            //同义词
            List<SynonymInfo> synonyms = schemaInfo.getSynonyms();
            if (synonyms != null) {
                for (SynonymInfo synonymInfo : synonyms) {
                    synonymInfo.setSchemaInfo(schemaInfo);
                }
            }
            //包
            List<PackageInfo> packages = schemaInfo.getPackages();
            if (packages != null) {
                for (PackageInfo packageInfo : packages) {
                    packageInfo.setSchemaInfo(schemaInfo);
                }
            }
            //方法
            List<FunctionInfo> functionInfos = schemaInfo.getFunctionInfos();
            if (functionInfos != null) {
                for (FunctionInfo functionInfo : functionInfos) {
                    functionInfo.setSchemaInfo(schemaInfo);
                }
            }
            //方法
            List<TypeInfo> typeInfos = schemaInfo.getTypeInfos();
            if (typeInfos != null) {
                for (TypeInfo typeInfo : typeInfos) {
                    typeInfo.setSchemaInfo(schemaInfo);
                }
            }
        }
    }

    /**
     * @description: 更新表信息
     * @author zzg
     * @date: 2021/1/26 15:17
     * @param: [schemaInfo, tableInfo]
     * @return: void
     */
    private void updateTableData(SchemaInfo schemaInfo, TableInfo tableInfo) {
        //触发器
        List<Trigger> triggers = tableInfo.getTriggers();
        if (triggers != null) {
            for (Trigger trigger : triggers) {
                trigger.setSchemaInfo(schemaInfo);
                trigger.setTableInfo(tableInfo);
            }
        }
        //主键约束
        PrimaryKey primarykey = tableInfo.getPrimarykey();
        if (primarykey != null) {
            primarykey.setTableInfo(tableInfo);
        }
        //约束
        List<CheckInfo> checkInfos = tableInfo.getCheckInfos();
        if (checkInfos != null) {
            for (CheckInfo checkInfo : checkInfos) {
                checkInfo.setTableInfo(tableInfo);
            }
        }
        //外键约束
        List<ForeignKey> foreignKeys = tableInfo.getForeignKeys();
        if (foreignKeys != null) {
            for (ForeignKey foreignKey : foreignKeys) {
                foreignKey.setTableInfo(tableInfo);
            }
        }
        //唯一约束
        List<UniqueInfo> uniqueInfos = tableInfo.getUniqueInfos();
        if (uniqueInfos != null) {
            for (UniqueInfo uniqueInfo : uniqueInfos) {
                uniqueInfo.setTableInfo(tableInfo);
            }
        }
        //索引
        List<IndexInfo> indexInfos = tableInfo.getIndexInfos();
        if (indexInfos != null) {
            for (IndexInfo indexInfo : indexInfos) {
                indexInfo.setTableInfo(tableInfo);
            }
        }
        //全文索引
        List<FullIndexInfo> fullIndexInfos = tableInfo.getFullIndexInfos();
        if (fullIndexInfos != null) {
            for (FullIndexInfo fullIndexInfo : fullIndexInfos) {
                fullIndexInfo.setTableInfo(tableInfo);
            }
        }
    }

    /**
     * @description: 把没有加载过的表信息加载到tableInfo中
     * @author zzg
     * @date: 2021/1/26 17:49
     * @param: [selTableInfos]
     * @return: void
     */
    public void fetchAllUnloadObjects(PageController pageController, List<TableInfo> selTableInfos) {
        List<CheckInfo> checks;
        List<ForeignKey> foreignKeys;
        List<UniqueInfo> uniqueInfos;
        List<IndexInfo> indexes;
        List<Trigger> triggers;
        List<FullIndexInfo> fullIndexInfos;

        String srcDBType = pageController.getCurrentSourceConnInfo().getType();
        for (TableInfo tableInfo : selTableInfos) {
            if (!tableInfo.isCommon()) {
                continue;
            }
            if (!tableInfo.isHasLoadCheck()) {
                if (!tableInfo.isHasLoadCheck()) {
                    checks = pageController.fetchChecks(tableInfo);
                    if (checks != null) {
                        for (CheckInfo checkInfo : checks) {
                            checkInfo.setChecked(true);
                        }
                    }
                    tableInfo.setCheckInfos(checks);
                    tableInfo.setHasLoadCheck(true);
                }
            }
            if (!tableInfo.isHasLoadForeignKey()) {
                if (!tableInfo.isHasLoadForeignKey()) {
                    foreignKeys = pageController.fetchForeignKeys(tableInfo);
                    if (foreignKeys != null) {
                        for (ForeignKey foreignKey : foreignKeys) {
                            foreignKey.setChecked(true);
                        }
                    }
                    tableInfo.setForeignKeys(foreignKeys);
                    tableInfo.setHasLoadForeignKey(true);
                }
            }
            if (!tableInfo.isHasLoadIndex()) {
                if (!tableInfo.isHasLoadIndex()) {
                    indexes = pageController.fetchIndexes(tableInfo);
                    if (indexes != null) {
                        for (IndexInfo indexInfo : indexes) {
                            indexInfo.setChecked(true);
                        }
                    }
                    tableInfo.setIndexInfos(indexes);
                    tableInfo.setHasLoadIndex(true);
                }
            }
            if (!tableInfo.isHasLoadTrigger() && srcDBType.equals(MigrationConstants.SHENTONG_Type)) {
                if (!tableInfo.isHasLoadTrigger()) {
                    triggers = pageController.fetchTriggers(tableInfo);
                    if (triggers != null) {
                        for (Trigger trigger : triggers) {
                            trigger.setChecked(true);
                        }
                    }
                    tableInfo.setTriggers(triggers);
                    tableInfo.setHasLoadTrigger(true);
                }

            }
            if (!tableInfo.isHasLoadUnique()) {
                if (!tableInfo.isHasLoadUnique()) {
                    uniqueInfos = pageController.fetchUniqueInfos(tableInfo);
                    if (uniqueInfos != null) {
                        for (UniqueInfo uniqueInfo : uniqueInfos) {
                            uniqueInfo.setChecked(true);
                        }
                    }
                    tableInfo.setUniqueInfos(uniqueInfos);
                    tableInfo.setHasLoadUnique(true);
                }

            }
            if (!tableInfo.hasLoadFullIndex() && srcDBType.equals(MigrationConstants.SHENTONG_Type)) {
                if (!tableInfo.hasLoadFullIndex()) {
                    fullIndexInfos = pageController.fetchFullIndexInfos(tableInfo);
                    if (fullIndexInfos != null) {
                        for (FullIndexInfo fullIndexInfo : fullIndexInfos) {
                            fullIndexInfo.setChecked(true);
                        }
                        tableInfo.setFullIndexInfos(fullIndexInfos);
                        tableInfo.setHasLoadFullIndex(true);
                    }
                }
            }
        }
    }

    /**
     * @description: 保存表分区信息
     * @author zzg
     * @date: 2021/1/26 17:50
     * @param: [selTableInfos]
     * @return: void
     */
    public void fetchPartitionInfos(PageController pageController, List<TableInfo> selTableInfos) {
        for (TableInfo tableInfo : selTableInfos) {
            if (!tableInfo.isHasLoadPartitionInfo()) {
                pageController.fetchPartitionInfo(tableInfo);
                tableInfo.setHasLoadPartitionInfo(true);
            }
        }
    }

    /**
     * @description: 获取表注释 列注释
     * @author zzg
     * @date: 2021/1/26 17:47
     * @param: [pageController, selTableInfos]
     * @return: void
     */
    private void fetchComments(PageController pageController, List<TableInfo> selTableInfos) {
        if (selTableInfos == null || !pageController.getProjectConfigInfo().isMigComment()) {
            return;
        }
        for (TableInfo tableInfo : selTableInfos) {
            pageController.fetchComments(tableInfo);
        }
    }

    /**
     * @description: 获取视图注释
     * @author zzg
     * @date: 2021/1/26 17:47
     * @param: [pageController, viewInfos]
     * @return: void
     */
    private void fetchViewComments(PageController pageController, List<ViewInfo> viewInfos) {
        if (viewInfos == null || !pageController.getProjectConfigInfo().isMigComment()) {
            return;
        }
        for (ViewInfo viewInfo : viewInfos) {
            pageController.fetchViewComments(viewInfo);
        }

    }
    /**
     * @description: 加载所选表列信息
     * @author zzg
     * @date: 2021/1/27 14:10
     * @param: [pageController]
     * @return: void
     */
    private void uploadLineData(PageController pageController) {
        //检查是否符合列映射规范  此时处理的都是已选择的列
        String msg = pageController.fetchAllColumns();
        if (msg.length() > 0) {
            //报错之前，移除不支持迁移的表
            for (SchemaInfo schemainfo : pageController.getSchemaInfos()) {
                List<TableInfo> tables = schemainfo.getTableInfos();
                if (tables != null && tables.size() > 0) {
                    Iterator<TableInfo> tableIterator = tables.iterator();
                    while (tableIterator.hasNext()) {
                        TableInfo tableInfo = tableIterator.next();
                        if (tableInfo.isMappingError()) {
                            tableIterator.remove();
                        }
                    }
                }
            }
        }
        //重置列映射状态 false，表示未做过修改
        pageController.getConvertionData().setTypeMappingChanged(false);
        //查找自增列
        pageController.fetchAutoIncrementCol();
    }

    /**
     * @description: 加载其他信息
     * @author zzg
     * @date: 2021/1/27 14:11
     * @param: [pageController]
     * @return: void
     */
    private void uploadOtherData(PageController pageController) {
        List<TableInfo> selTableInfos = pageController.getSelTables();
        fetchAllUnloadObjects(pageController, selTableInfos);
        fetchPartitionInfos(pageController, selTableInfos);
        //保存注释信息
        fetchComments(pageController, selTableInfos);
        fetchViewComments(pageController, pageController.getSelViews());
        try {
            //修改项目状态为未完成状态
            pageController.modifiedPrjState(MigrationConstants.MigrationState.UnFinished.toString());
        } catch (Exception e) {
            log.error("修改项目状态异常", e);
        }
    }


    // TODO  从老代码copy过来的 后续删掉
    public void loadTableColumn(String sDBName, String tDBName, TableInfo tableInfo,
                                Map<String, SourceTypesInfo> typeMap) {
        synchronized (this) {
            List<ColumnInfo> colList = tableInfo.getColumns();
            TargetTypesInfo ttype = null;
            ColumnInfo col = null;
            List<TargetTypesInfo> ttypeList = null;
            for (int i = 0; i < colList.size(); i++) {
                col = colList.get(i);
                //begin add by fqy to fix td14399:跨平台迁移工具迁移含特殊类型的视图和表无反应
                SourceTypesInfo sourceTypesInfo = typeMap.get(getMapKeyFromColumn(sDBName, tDBName, col));
                if (sourceTypesInfo != null) {
                    try {
                        SourceTypesInfo sourceTypesInfoClone = sourceTypesInfo.clone();
                        List<TargetTypesInfo> list = new ArrayList<TargetTypesInfo>();
                        if (col.isAutoIncrement()) {
                            if (MigrationConstants.MYSQL_Type.equals(sDBName) && MigrationConstants.IS_AUTOINCREMENT) {
                                for (TargetTypesInfo targetTypesInfo : sourceTypesInfoClone.getAccTypes()) {
                                    TargetTypesInfo targetTypesInfoClone = targetTypesInfo.clone();
                                    if (col.getSourceType().equalsIgnoreCase("TINYINT UNSIGNED")) {
                                        if (targetTypesInfoClone.getTtypeName().equalsIgnoreCase("SMALLINT")) {
                                            targetTypesInfoClone.setSelected(true);
                                        } else {
                                            targetTypesInfoClone.setSelected(false);
                                        }
                                    } else if (col.getSourceType().equalsIgnoreCase("SMALLINT UNSIGNED") ||
                                            col.getSourceType().equalsIgnoreCase("MEDIUMINT") ||
                                            col.getSourceType().equalsIgnoreCase("MEDIUMINT UNSIGNED")) {
                                        if (targetTypesInfoClone.getTtypeName().equalsIgnoreCase("INT")) {
                                            targetTypesInfoClone.setSelected(true);
                                        } else {
                                            targetTypesInfoClone.setSelected(false);
                                        }
                                    } else if (col.getSourceType().equalsIgnoreCase("INT UNSIGNED") ||
                                            col.getSourceType().equalsIgnoreCase("BIGINT UNSIGNED")) {
                                        if (targetTypesInfoClone.getTtypeName().equalsIgnoreCase("BIGINT")) {
                                            targetTypesInfoClone.setSelected(true);
                                        } else {
                                            targetTypesInfoClone.setSelected(false);
                                        }
                                    }
                                    list.add(targetTypesInfoClone);
                                }
                            } else {
                                for (TargetTypesInfo targetTypesInfo : sourceTypesInfoClone.getAccTypes()) {
                                    TargetTypesInfo targetTypesInfoClone = targetTypesInfo.clone();
                                    if (targetTypesInfoClone.getTtypeName().equalsIgnoreCase("serial")) {
                                        targetTypesInfoClone.setSelected(true);
                                    } else {
                                        targetTypesInfoClone.setSelected(false);
                                    }
                                    list.add(targetTypesInfoClone);
                                }
                            }
                            sourceTypesInfoClone.setAccTypes(list);
                        }
                        col.setStype(sourceTypesInfo);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //fix#107419 提示添加上列对应的类型 by.cw 2020-06-10
                    throw new ColumnTransformException(tableInfo.getTargetSchema() + "." + tableInfo.getTargetName()
                            + UniverLanguage.getString("ui.OtherObjectSelPage.label.table") + col.getSourceName() + "(" + col.getSourceType() + ")");
                }
                //end

                // 如果是INTERVAL YEAR/DAY类型需要特殊处理
                if ((!MigrationConstants.SQLSERVER_Type.equals(sDBName))
                        && (!MigrationConstants.SQLSERVER_Type.equals(tDBName))
                        && (col.getSourceType().toUpperCase().contains("INTERVAL") || col
                        .getSourceType().toUpperCase().contains("TIMESTAMP"))) {
                    TargetTypesInfo targetType = col.getStype().getAccTypes()
                            .get(0);
                    if (col.getSourceType().toUpperCase().contains("STIMESTAMPP<=6SL")) {
                        targetType.setTtypeName("TIMESTAMP(" + col.getScale() + ")");
                    } else if (targetType.getTtypeName().equals("DATE")) {
                        col.setLength(0);
                    } else {
                        StringBuilder newType = new StringBuilder();
                        String[] reu = null;
                        for (int t = 0; t < col.getSourceType().length(); t++) {
                            char c = col.getSourceType().charAt(t);
                            if (c > '6' && c < ('6' + 4)) {
                                if (col.getSourceType().toUpperCase().contains("YEAR")) {
                                    newType.append(c);
                                } else {
                                    newType.append('6');
                                }
                            } else {
                                newType.append(c);
                            }
                        }
                        String timeStr = newType.toString();
                        if (timeStr.contains("LOCAL")) {
                            reu = timeStr.split("LOCAL ");
                            targetType.setTtypeName(reu[0] + reu[1]);
                        } else {
                            targetType.setTtypeName(timeStr);
                        }
                        col.setLength(0);
                        //					col.setPrecision(0);
                        //					col.setPlaces("0");
                    }
                }
                // 获得目的列集合
                ttypeList = col.getStype().getAccTypes();
                if (1 == ttypeList.size()) {
                    ttype = ttypeList.get(0);
                    ttype.setSelected(true);
                } else {
                    for (int k = 0; k < ttypeList.size(); k++) {
                        if (ttypeList.get(k).isSelected()) {
                            ttype = ttypeList.get(k);
                        }
                    }
                }
                //bool/boolean到char，修改长度为8
                if (sDBName.equals(MigrationConstants.SHENTONG_Type) && tDBName.equals(MigrationConstants.ORACLE_Type)
                        && (ttype.getId().equals("TBOOL2CHARPS") || ttype.getId().equals("TBOOLEAN2CHARPS"))) {
                    col.setLength(8);
                }
                if (sDBName.equals(MigrationConstants.SHENTONG_Type) && tDBName.equals(MigrationConstants.SQLSERVER_Type)
                        && (ttype.getId().equals("SS2BOOL2CHAR") || ttype.getId().equals("SS2BOOLEAN2CHAR"))) {
                    col.setLength(8);
                }
                // 设置目的列名称
                col.setTargetName(col.getSourceName());
                // 设置目的列类型
                col.setTargetType(ttype.getTtypeName());
                // 设置目的列长度
                col.setLengthTarget(targetLenthCalculation(col.getLength(),
                        ttype.getmValue()));
                // 设置目的列精度
                col.setPrecisionTarget(targetPrecisionCalculation(col
                        .getPrecision(), ttype.getpValue(), col.getScale()));
                // 设置目的列小数位
                col.setScaleTarget(targetPlacesCalculation(col.getScale(),
                        ttype.getsValue(), col.getPrecision()));
                // 设置是否有大对象LOB标识位
                if ((!tableInfo.isHasLob())
                        && col.getTargetType().toUpperCase().contains("LOB")) {
                    tableInfo.setHasLob(true);
                }
            }
        }
    }

    public static String targetPlacesCalculation(String places, String tsValue, int sPrecision) {
        if (null != tsValue && !"".equals(tsValue)) {
            try {
                return String.valueOf(Integer.parseInt(tsValue));
            } catch (NumberFormatException nfEx) {
                //非数字的表达式处理
                if ("S".equals(tsValue)) {
                    return places;
                } else {
                    int reu = (int) (2 * (Math.ceil((sPrecision * 0.30103) + 1)));
                    if (39 < reu) {
                        return String.valueOf(39);
                    } else {
                        return String.valueOf(reu);
                    }
                }
            }
        } else {
            return places;
        }
    }

    public static int targetPrecisionCalculation(int precision, String pValue, String sValue) {
        //判断目的列P值类型
        if (null != pValue && !"".equals(pValue)) {
            try {
                return Integer.parseInt(pValue);
            } catch (NumberFormatException nfEx) {
                //非数字的表达式处理
                if ("P".equals(pValue)) {
                    int scale = Integer.parseInt(sValue);
                    if (precision < scale) {
                        return precision + scale;
                    }
                    return precision;
                } else if ("P+S".equals(pValue)) {
                    return (precision + Integer.parseInt(sValue));
                } else if ("max(P,S)".equals(pValue)) {
                    if (precision >= Integer.parseInt(sValue)) {
                        return precision;
                    } else {
                        return Integer.parseInt(sValue);
                    }
                } else if ("P+|S|".equals(pValue)) {
                    return precision + Math.abs(Integer.parseInt(sValue));
                } else {
                    int reu = (int) (4 * Math.ceil((precision * 0.30103) + 1));
                    if (78 < reu) {
                        return 78;
                    } else {
                        return reu;
                    }
                }
            }
        } else {
            return precision;
        }
    }

    public static int targetLenthCalculation(int length, String mValue) {
        if (null != mValue && !"".equals(mValue)) {
            try {
                return (int) (length * Float.parseFloat(mValue));
            } catch (NumberFormatException nfEx) {
                //非数字的表达式处理
                if (mValue.startsWith("L+")) {
                    return (length + Integer.parseInt(mValue.substring(2, mValue.length())));
                } else if (mValue.startsWith("L=")) {
                    return Integer.parseInt(mValue.substring(2, mValue.length()));
                } else {
                    return length;
                }
            }
        } else {
            return length;
        }
    }

    public static String getMapKeyFromColumn(String sDBName, String tDBName, ColumnInfo col) {
        if (MigrationConstants.ORACLE_Type.equals(sDBName) || MigrationConstants.ORACLE8I_Type.equals(sDBName)) {
            if ("NUMBER".equals(col.getSourceType().toUpperCase())
                    || "FLOAT".equals(col.getSourceType().toUpperCase())
                    || "NVARCHAR2".equals(col.getSourceType().toUpperCase())) {
                return getMapKeyFromPSLOracle(col);
            } else if (col.getSourceType().toUpperCase().startsWith("TIMESTAMP")) {
                return "TIMESTAMP";
            } else if (col.getSourceType().toUpperCase().startsWith("INTERVAL")) {
                return "INTERVAL";
            } else {
                return col.getSourceType().toUpperCase();
            }

        } else if (MigrationConstants.SQLSERVER_Type.equals(sDBName)) {
            if ("DECIMAL".equals(col.getSourceType().toUpperCase())
                    || "NUMERIC".equals(col.getSourceType().toUpperCase())
                    || "FLOAT".equals(col.getSourceType().toUpperCase())
                    || "VARCHAR".equals(col.getSourceType().toUpperCase())
                    || "NVARCHAR".equals(col.getSourceType().toUpperCase())
                    || "VARBINARY".equals(col.getSourceType().toUpperCase())) {
                return getMapKeyFromPSLSQLServer(col);
            } else {
                return col.getSourceType().toUpperCase();
            }
        } else if (MigrationConstants.MYSQL_Type.equals(sDBName)) {
            if ("DECIMAL".equals(col.getSourceType().toUpperCase())
                    || "VARCHAR".equals(col.getSourceType().toUpperCase())
                    || "VARBINARY".equals(col.getSourceType().toUpperCase())
                    || "BLOB".equals(col.getSourceType().toUpperCase())
                    || "TEXT".equals(col.getSourceType().toUpperCase())) {
                return getMapKeyFromPSLMySQL(col);
            } else {
                return col.getSourceType().toUpperCase();
            }

        } else if (MigrationConstants.DB2_Type.equals(sDBName)) {
            if ("DECIMAL".equals(col.getSourceType().toUpperCase())
                    || "VARCHAR".equals(col.getSourceType().toUpperCase())
                    || "VARGRAPHIC".equals(col.getSourceType().toUpperCase())) {
                return getMapKeyFromPSLDB2(col);
            } else {
                return col.getSourceType().toUpperCase();
            }
        } else if (MigrationConstants.FILE_TYPE.equals(sDBName)) {
            return col.getSourceType().toUpperCase();
            // fixed redmine#105981 源端支持PostgreSQL数据库 by：cw 2020-01-04
        } else if (MigrationConstants.POSTGRESQL_Type.equals(sDBName)) {
            if ("DECIMAL".equals(col.getSourceType().toUpperCase())
                    || "NUMERIC".equals(col.getSourceType().toUpperCase())
                    || "CHARACTER VARYING".equals(col.getSourceType().toUpperCase())
                    || "VARCHAR".equals(col.getSourceType().toUpperCase())
                    || "TEXT".equals(col.getSourceType().toUpperCase())
                    || "TIMESTAMP WITHOUT TIME ZONE".equals(col.getSourceType().toUpperCase())
                    || "TIMESTAMP WITH TIME ZONE".equals(col.getSourceType().toUpperCase())
                    || "TIME WITHOUT TIME ZONE".equals(col.getSourceType().toUpperCase())
                    || "TIME WITH TIME ZONE".equals(col.getSourceType().toUpperCase())) {
                return getMapKeyFromPSLPostgreSQL(col);
            } else {
                return col.getSourceType().toUpperCase();
            }
            // end redmine#105981
        } else {
            //神通数据库特殊FLOAT类型处理
            if (col.getSourceType().startsWith("HPFLOAT") || col.getSourceType().startsWith("LPFLOAT")) {
                col.setSourceType("FLOAT");
            }
            //TIMETZ
            if (col.getSourceType().startsWith("TIMETZ")) {
                col.setSourceType("TIME WITH TIME ZONE");
            }

            //TIMESTAMPTZ
            if (col.getSourceType().startsWith("TIMESTAMPTZ")) {
                col.setSourceType("TIMESTAMP WITH TIME ZONE");
            }
            //INTERVAL
            if (col.getSourceType().startsWith("INTERVAL")) {
                return "INTERVAL";
            }
            if (MigrationConstants.ORACLE_Type.equals(tDBName)) {
                if (col.getSourceType().startsWith("TIMESTAMP")) {
                    if (col.getSourceType().contains("TIMESTAMP WITH")) {
                        return "STIMESTAMPTZPSL";
                    } else {
                        if (col.getScale() != null && !("-1").equals(col.getScale())) {
                            return "STIMESTAMPP<=6SL";
                        } else {
                            return "STIMESTAMPPSL";
                        }
                    }
                } else {
                    return getMapKeyFromPSLKSTORE2ORACLE(col).toUpperCase();
                }
            } else {
                return col.getSourceType().toUpperCase();
            }
        }
    }

    public static String getMapKeyFromPSLPostgreSQL(ColumnInfo col) {
        // 拼接模板ID字符串
        StringBuffer sb = new StringBuffer("S").append(col.getSourceType().toUpperCase());
        int p = col.getPrecision();
        int l = col.getLength();
        String s = col.getScale();
        String sourceType = col.getSourceType().toUpperCase();
        // P值内容拼接
        if (-1 < p) {
            if ("NUMERIC".equals(sourceType) || "DECIMAL".equals(sourceType)) {
                if (p <= 2 && "0".equals(s)) {
                    sb.append("P<=2S=0L");
                } else if (p > 2 && p <= 4 && "0".equals(s)) {
                    sb.append("2<P<=4S=0L");
                } else if (p > 4 && p <= 9 && "0".equals(s)) {
                    sb.append("4<P<=9S=0L");
                } else if (p > 9 && p <= 18 && "0".equals(s)) {
                    sb.append("9<P<=18S=0L");
                } else if (p <= 15 && !"0".equals(s)) {
                    sb.append("P<=15S!=0L");
                } else if (p > 15 && !"0".equals(s)) {
                    sb.append("P>15S!=0L");
                } else {
                    sb.append("PSL");
                }
            }
        }

        if (-1 < l) {
            if ("CHARACTER VARYING".equals(sourceType) || "VARCHAR".equals(sourceType)
                    || "TEXT".equals(sourceType)) {
                if (0 <= l && l <= 8000) {
                    sb.append("PS0<=L<=8000");
                } else {
                    sb.append("PSL>8000");
                }
            }
        }

        if ("TIMESTAMP WITHOUT TIME ZONE".equals(sourceType)
                || "TIMESTAMP WITH TIME ZONE".equals(sourceType)
                || "TIME WITHOUT TIME ZONE".equals(sourceType)
                || "TIME WITH TIME ZONE".equals(sourceType)) {
            sb.append("PSL");
        }
        return sb.toString();
    }

    public static String getMapKeyFromPSLKSTORE2ORACLE(ColumnInfo col) {
        //拼接模板ID字符串
        int p = col.getPrecision();
        String scale = col.getScale();
        int scaleIntValue = -1;
        if (scale != null && !"".equals(scale)) {
            try {
                scaleIntValue = Integer.valueOf(scale);
            } catch (Exception e) {
            }
        }
        int length = col.getLength();
        String sourceType = col.getSourceType().toUpperCase();
        StringBuffer sb = new StringBuffer("S").append(sourceType);
        //P值内容拼接
        if (-1 < p) {
            if ("NUMERIC".equals(sourceType)) {
                if (p <= 38) {
                    sb.append("P<=38SL");
                } else if (38 < p && p < 260) {
                    sb.append("38<P<260SL");
                } else if (p == 260 && scaleIntValue == 130) {
                    sb.append("P=260S=130L");
                } else {
                    sb.append("260<PSL");
                }
                return sb.toString();
            }
        }
        if ("BPCHAR".equals(sourceType) || "CHAR".equals(sourceType)) {
            if (0 <= length && length <= 2000) {
                sb.append("PS0<=L<=2000");
            } else if (p <= 4000) {
                sb.append("PS2000<L<=4000");
            } else {
                sb.append("PSL>4000");
            }
            return sb.toString();
        }
        if ("VARCHAR".equals(sourceType)) {
            if (0 <= length && length <= 4000) {
                sb.append("PS0<=L<=4000");
            } else {
                sb.append("PSL>4000");
            }
            return sb.toString();
        }
        return sourceType;
    }

    public static String getMapKeyFromPSLSQLServer(ColumnInfo col) {
        //拼接模板ID字符串
        StringBuffer sb = new StringBuffer("S").append(col.getSourceType().toUpperCase());
        int p = col.getPrecision();
        int len = col.getLength();
        String s = col.getScale();
        //P值内容拼接
        if (-1 < p) {
            if ("FLOAT".equals(col.getSourceType().toUpperCase())) {
                if (p >= 1 && p <= 24) {
                    sb.append("1<=P<=24SL");
                } else {
                    sb.append("25<=P<=53SL");
                }
            } else {//DECIMAL 和 NUMERIC类型
                if (p <= 2 && "0".equals(s)) {
                    sb.append("P<=2S=0L");
                } else if (p > 2 && p <= 4 && "0".equals(s)) {
                    sb.append("2<P<=4S=0L");
                } else if (p > 4 && p <= 9 && "0".equals(s)) {
                    sb.append("4<P<=9S=0L");
                } else if (p > 9 && p <= 18 && "0".equals(s)) {
                    sb.append("9<P<=18S=0L");
                } else if (p <= 15 && (null != s && Integer.valueOf(s) != 0)) {
                    sb.append("P<=15S!=0L");
                } else {
                    sb.append("P=PS=SL");
                }
            }
        }

        if ("VARCHAR".equals(col.getSourceType().toUpperCase())
                || "NVARCHAR".equals(col.getSourceType().toUpperCase())
                || "VARBINARY".equals(col.getSourceType().toUpperCase())) {
            if (-1 == len) {
                sb.append("PSL=-1");
            } else {
                sb.append("PSL");
            }
        }
        return sb.toString();
    }

    public static String getMapKeyFromPSLMySQL(ColumnInfo col) {
        //拼接模板ID字符串
        StringBuffer sb = new StringBuffer("S").append(col.getSourceType().toUpperCase());
        int p = col.getPrecision();
        int l = col.getLength();
        String s = col.getScale();
        String sourceType = col.getSourceType().toUpperCase();
        //P值内容拼接
        if (-1 < p) {
            if ("DECIMAL".equals(sourceType)) {
                if (p <= 2 && "0".equals(s)) {
                    sb.append("P<=2S=0L");
                } else if (p > 2 && p <= 4 && "0".equals(s)) {
                    sb.append("2<P<=4S=0L");
                } else if (p > 4 && p <= 9 && "0".equals(s)) {
                    sb.append("4<P<=9S=0L");
                } else if (p > 9 && p <= 18 && "0".equals(s)) {
                    sb.append("9<P<=18S=0L");
                } else if (p <= 15 && !"0".equals(s)) {
                    sb.append("P<=15S!=0L");
                } else {
                    sb.append("PSL");
                }
            }
        }
        if (-1 < l) {
            if ("VARBINARY".equals(sourceType)
                    || "BLOB".equals(sourceType) || "TEXT".equals(sourceType)) {
                if (0 <= l && l <= 8000) {
                    sb.append("PS0<=L<=8000");
                } else {
                    sb.append("PS8001<=L<=65535");
                }
            } else if ("VARCHAR".equals(sourceType)) {
                if (0 <= l && l <= 4000) {
                    sb.append("PS0<=L<=4000");
                } else if (4000 < l && l <= 8000) {
                    sb.append("PS4001<=L<=8000");
                } else {
                    sb.append("PS8001<=L<=65535");
                }
            }
        }
        return sb.toString();
    }

    public static String getMapKeyFromPSLDB2(ColumnInfo col) {
        //拼接模板ID字符串
        StringBuffer sb = new StringBuffer("S").append(col.getSourceType().toUpperCase());
        int p = col.getPrecision();
        int l = col.getLength();
        String s = col.getScale();
        //P值内容拼接
        if (-1 < p) {
            if ("DECIMAL".equals(col.getSourceType().toUpperCase())) {
                if (p <= 2 && "0".equals(s)) {
                    sb.append("P<=2S=0L");
                } else if (p > 2 && p <= 4 && "0".equals(s)) {
                    sb.append("2<P<=4S=0L");
                } else if (p > 4 && p <= 18 && "0".equals(s)) {
                    sb.append("4<P<=18S=0L");
                } else if (p <= 15 && (null != s && Integer.valueOf(s) != 0)) {
                    sb.append("P<=15S!=0L");
                } else {
                    sb.append("PSL");
                }
            }
        }
        if (-1 < l) {
            if ("VARCHAR".equals(col.getSourceType().toUpperCase())) {
                if (0 <= l && l <= 8000) {
                    sb.append("PS0<=L<=8000");
                } else {
                    sb.append("PS8001<=L<=32672");
                }
            } else if ("VARGRAPHIC".equals(col.getSourceType().toUpperCase())) {
                if (0 <= l && l <= 4000) {
                    sb.append("PS0<=L<=4000");
                } else {
                    sb.append("PS4001<=L<=16350");
                }
            }
        }
        return sb.toString();
    }

    public static String getMapKeyFromPSLOracle(ColumnInfo col) {
        //拼接模板ID字符串
        StringBuffer sb = new StringBuffer("S").append(col.getSourceType().toUpperCase());
        int p = col.getPrecision();
        int l = col.getLength();
        String s = col.getScale();
        //P值内容拼接
        if (-1 < p) {
            if ("NUMBER".equals(col.getSourceType().toUpperCase())) {
                if (0 < p && p < 5 && "0".equals(s)) {
                    sb.append("P<5S=0L");
                } else if (p == 5 && "0".equals(s)) {
                    sb.append("P=5S=0L");
                } else if (5 < p && p < 10 && "0".equals(s)) {
                    sb.append("5<P<10S=0L");
                } else if (p == 10 && "0".equals(s)) {
                    sb.append("P=10S=0L");
                } else if (10 < p && p < 20 && "0".equals(s)) {
                    sb.append("10<P<20S=0L");
                } else if (p == 20 && "0".equals(s)) {
                    sb.append("P=20S=0L");
                } else if (20 < p && p < 38 && "0".equals(s)) {
                    sb.append("20<P<38S=0L");
                } else if (p == 38 && "0".equals(s)) {
                    sb.append("P=38S=0L");
                } else if (p == 126 && "0".equals(s)) {
                    sb.append("P=*S=0L");
                } else if (p == 126 && (null != s && Integer.valueOf(s) > 0)) {
                    sb.append("P=*S>0L");
                } else if (p == 126 && (null != s && Integer.valueOf(s) < 0)) {
                    sb.append("P=*S<0L");
                } else if (p == 126 && "0".equals(s)) {
                    sb.append("38<P<=126S=0L");
                } else if (p <= 15 && (null != s && Integer.valueOf(s) > 0)) {
                    sb.append("P<=15S>0L");
                } else if (p <= 15 && (null != s && Integer.valueOf(s) < 0)) {
                    sb.append("P<=15S<0L");
                } else if (p > 15 && p <= 38 && (null != s && Integer.valueOf(s) > 0)) {
                    sb.append("15<P<=38S>0L");
                } else if (p > 15 && p <= 38 && (null != s && Integer.valueOf(s) < 0)) {
                    sb.append("15<P<=38S<0L");
                } else if (p == 0 && "0".equals(s)) {
                    sb.append("P=0S=0L");
                } else if (p > 38 && "-127".equals(s)) {
                    sb.append("38<P<=126S=-127L");
                }
            } else if ("FLOAT".equals(col.getSourceType().toUpperCase())) {
                if (p <= 15) {
                    sb.append("P<=15SL");
                } else if (16 <= p && p <= 53) {
                    sb.append("16<=P<=53SL");
                } else {
                    sb.append("53<P<127SL");
                }
            }
        }

        if (-1 < l) {
            if (("NVARCHAR2".equals(col.getSourceType().toUpperCase()))) {
                if (0 <= l && l <= 3999) {
                    sb.append("PS0<=2L<=3999");
                } else {
                    sb.append("PS2L>=4000");
                }
            }
        }

        return sb.toString();
    }


}
