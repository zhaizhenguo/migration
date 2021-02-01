package com.oscar.migration.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.oscar.entity.*;
import com.oscar.exception.ColumnTransformException;
import com.oscar.migration.vo.SchemaInfoVo;
import com.oscar.migration.vo.TableInfoVo;
import com.oscar.reverse.project.ProjectManager;
import com.oscar.ui.common.controller.PageController;
import com.oscar.ui.navigation.page.DataObjectSelPage;
import com.oscar.ui.navigation.page.MigrationExecPage;
import com.oscar.ui.navigation.page.OtherObjectSelPage;
import com.oscar.util.MigrationConstants;
import com.oscar.util.PreviewUtil;
import com.oscar.util.typeMap.Xdb2OscarMapUtil;
import com.ximpleware.*;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


/**
 * @author zzg
 * @description: ceshi
 * @date 2021/1/13 17:17
 */
@RunWith(SpringRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataMigrationControllerTest {

    private static final PageController pageController = new PageController();
    private static final ProjectManager projectManager = new ProjectManager();

    /**
     * @description: 创建项目
     */
    @Test
    @Order(1)
    public void test01createProject() {
        try {

            String path = projectManager.getPrjDefinedPath();
            String projectName = "web_" + System.currentTimeMillis();
            //获取最终项目路径
            path = ProjectManager.getPath(projectName, path);
            //创建项目对象
            pageController.createProject(null, null);
            System.out.println("创建项目成功,路径：" + null);
        } catch (Exception e) {
            System.out.println("创建项目失败");
            e.printStackTrace();
        }
    }

    /**
     * @description: 测试源端连接是否正常
     */
    @Test
    @Order(2)
    public void test02createSourceConnection() {
        try {
            boolean sourceConnection = pageController.createSourceConnection("ShenTong", "ShenTong JDBC Driver", "localhost", "2003"
                    , "OSRDB", "sysdba", "szoscar55", null, false, new SslInfo(), "sid");
            String res = sourceConnection ? "成功" : "失败";
            System.out.println("源端连接测试" + res);
            Assert.assertTrue(sourceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 测试目标端连接是否正常
     */
    @Test
    @Order(3)
    public void test03createDestConnection() {
        try {
            boolean sourceConnection = pageController.createDestConnection("ShenTong", "ShenTong JDBC Driver", "localhost", "2004"
                    , "ZG", "sysdba", "szoscar55", null, false, new SslInfo(), "sid");
            String res = sourceConnection ? "成功" : "失败";
            System.out.println("目标端连接测试" + res);
            Assert.assertTrue(sourceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: 保存配置信息
     */
    @Test
    @Order(4)
    public void test04saveCfg() {
        ProjectConfigInfo projectConfigInfo = pageController.getProjectConfigInfo();
        System.out.println("参数配置保存成功：" + projectConfigInfo);
    }

    /**
     * @description: 设置列映射信息
     */
    @Test
    @Order(5)
    public void test05saveTypeMapping() {
        try {
            FileInputStream inputStream = new FileInputStream("E:/typeMapSet.txt");
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            Map<String, SourceTypesInfo> typeMapSet = (Map<String, SourceTypesInfo>) objInputStream.readObject();
            objInputStream.close();

            System.out.println("类型映射配置保存成功：" + typeMapSet);
            pageController.getProject().setStypes(typeMapSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * @description: 设置模式信息
     */
    @Test
    @Order(6)
    public void test06savePatternData() throws Exception {
        DataObjectSelPage selPage = new DataObjectSelPage(pageController);
        //获取源端、目的端模式 =>模式接口
        selPage.fetchSchemaInfos();
        selPage.fetchTableSpaces();
        //获取模式下的所有数据项=>模式详情接口
        List<SchemaInfo> schemaInfos = pageController.getSchemaInfos();
        SchemaInfo schemaInfo = schemaInfos.get(5);
        String s = JSON.toJSONString(schemaInfo);
        System.out.println("s==="+s);
        List<TableInfo> tableInfos = pageController.fetchTables(schemaInfo);
        List<ViewInfo> viewInfos = pageController.fetchViews(schemaInfo);
        //保存强制迁移至目的模式名 TODO 页面信息
        pageController.setTargetSchema(null);
        //获取已选择的所有数据项

        Iterator<TableInfo> iterator = tableInfos.iterator();
        while (iterator.hasNext()){
            TableInfo info = iterator.next();
            if (!info.getSourceName().equals("DEPT")){
                System.out.println(info.getSourceName()+"  已删除");
                iterator.remove();
            }
        }
        schemaInfo.setTableInfos(tableInfos);
//        schemaInfo.setViewInfos(viewInfos);
        //保存迁移范围
        pageController.setMigType(0);
        //断点续传 TODO
        System.out.println("模式信息保存成功");
    }

    /**
     * @description: 设置列信息
     */
    @Test
    @Order(7)
    public void test07saveColumnData() {
        //获取表下面的所有列
        TableInfo tableInfoTemp = pageController.getSchemaInfos().get(5).getTableInfos().get(0);
        pageController.fetchColumns(tableInfoTemp);
        //获取目的列类型集合
        Object[][] colDataItems = null;
        try {
            colDataItems = Xdb2OscarMapUtil
                    .getTypeMapFromTableInfo(pageController.getCurrentSourceConnInfo().getType(),
                            pageController.getCurrentTargetConnInfo().getType(), tableInfoTemp, pageController.getProject());
        } catch (ColumnTransformException e) {
            System.out.println(e.getMessage());
            tableInfoTemp.setMappingError(true);
        } catch (Exception ex) {
            tableInfoTemp.setMappingError(true);
        }
        //获取已选择的列 TODO 页面处理
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
        System.out.println("列信息保存成功");
    }

    /**
     * @description: 保存其它对象信息索引、约束、外键等
     */
    @Test
    @Order(8)
    public void test08saveOtherData() throws NavException, ParseException, IOException, TranscodeException, XPathEvalException, ModifyException, XPathParseException {
        OtherObjectSelPage otherInfo = new OtherObjectSelPage(pageController);
        List<TableInfo> selTableInfos = pageController.getSelTables();
        fetchAllUnloadObjects(selTableInfos);
        fetchPartitionInfos(selTableInfos);
        otherInfo.fetchProcedures();
        //保存注释信息 修改工程状态
        fetchComments(selTableInfos);
        fetchViewComments(pageController.getSelViews());
        //修改项目状态为未完成状态
        pageController.modifiedPrjState(MigrationConstants.MigrationState.UnFinished.toString());
        System.out.println("保存其它对象信息保存成功");
    }

    /**
     * 执行迁移
     */
    @Test
    @Order(9)
    public void test09StartMigration() {
        CountDownLatch latch = new CountDownLatch(2);
        Long startTime = System.currentTimeMillis();
        PreviewUtil previewUtil = new PreviewUtil(pageController.getProject(), pageController
                .getCurrentSourceConnInfo().getType(), pageController);
        pageController.setPreviewUtil(previewUtil);
        MigrationExecPage execPage = new MigrationExecPage(pageController, latch);

        Long endTime = System.currentTimeMillis();
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        String time = decimalFormat.format(((double) endTime - (double) startTime) / 1000);
        System.out.println("迁移用时： " + time + "s");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取表注释 列注释
     */
    private void fetchComments(List<TableInfo> selTableInfos) {
        if (selTableInfos == null || !pageController.getProjectConfigInfo().isMigComment()) {
            return;
        }
        for (TableInfo tableInfo : selTableInfos) {
            pageController.fetchComments(tableInfo);
        }
    }

    /**
     * 获取视图注释
     */
    private void fetchViewComments(List<ViewInfo> viewInfos) {
        if (viewInfos == null || !pageController.getProjectConfigInfo().isMigComment()) {
            return;
        }
        for (ViewInfo viewInfo : viewInfos) {
            pageController.fetchViewComments(viewInfo);
        }

    }

    /**
     * 保存表分区信息
     */
    public void fetchPartitionInfos(List<TableInfo> selTableInfos) {
        for (TableInfo tableInfo : selTableInfos) {
            if (!tableInfo.isHasLoadPartitionInfo()) {
                pageController.fetchPartitionInfo(tableInfo);
                tableInfo.setHasLoadPartitionInfo(true);
            }
        }
    }

    /**
     * 把没有加载过的表信息加载到tableInfo中
     */
    public void fetchAllUnloadObjects(List<TableInfo> selTableInfos) {
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
}