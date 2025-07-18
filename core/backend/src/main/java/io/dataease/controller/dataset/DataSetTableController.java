package io.dataease.controller.dataset;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.dataease.auth.annotation.DeLog;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.dataset.DataSetExportRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.RelationDTO;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.authModel.modelCacheEnum;
import io.dataease.dto.dataset.DataSetTableDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.ExcelFileData;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DatamodelMapper;
import io.dataease.plugins.common.base.mapper.DatasetRefMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.datasource.OracleConstants;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.datasource.DatasourceService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/20 8:29 下午
 */
@Api(tags = "数据集：数据集表")
@ApiSupport(order = 50)
@RestController
@RequestMapping("dataset/table")
public class DataSetTableController {
    @Resource
    private DatamodelMapper datamodelMapper;
    @Resource
    private DatasetRefMapper datasetRefMapper;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private VAuthModelService vAuthModelService;
    @ApiOperation("二开 查询当前数据集详细信息")
    @GetMapping("/getInfo/{id}")
    public DatasourceDTO getInfo(@PathVariable String id) throws Exception {
        return dataSetTableService.getInfo(id);
    }

    @ApiOperation("二开 获取数据源同步状态")
    @GetMapping("/check/{id}")
    public ResultHolder check(@PathVariable String id) throws Exception {
        return dataSetTableService.check(id);
    }

    @ApiOperation("二开 查询当前用户分组的数据源")
    @GetMapping("/listByGroup")
    public List<DatasetTable> getDatasourceListByGroup(@RequestParam String groupId,@RequestParam(required = false) String keyWord) throws Exception {
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        dataSetTableRequest.setGroupId(groupId);
        dataSetTableRequest.setKeyWord(keyWord);
        return dataSetTableService.listByGroup(dataSetTableRequest);
    }

    @ApiOperation("二开 提交数据源 和 表 形成 数据集")
    @PostMapping("/add")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
            sourcetype = SysLogConstants.SOURCE_TYPE.DATASOURCE,
            positionIndex = 0, positionKey = "type",
            value = "id"
    )
    public List<VAuthModelDTO> addDatasource(@RequestBody DatasourceDTO datasource) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        String name = datasource.getName();
        datasource.setName(name + UUID.randomUUID());
        //添加数据源
        Datasource added = datasourceService.addDatasource(datasource);
        datasource.setName(name);
        return vAuthModelService.queryAuthModelByIds("dataset", Collections.singletonList(dataSetTableService.saveAndRef(added,datasource).getId()));
    }

    @ApiOperation("excel上传")
    @PostMapping("/addExcel")
    public List<VAuthModelDTO> addExcel(@RequestPart @RequestParam("file") MultipartFile file, @RequestParam("groupId") String groupId,@RequestParam String name,@RequestParam String desc) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        return vAuthModelService.queryAuthModelByIds("dataset", dataSetTableService.saveExcelData(file, groupId,name,desc));
    }

    @ApiOperation("二开 提交excel数据")
    @PostMapping("/excelSubmit")
    public List<VAuthModelDTO> excelSubmit(@RequestBody DataSetTableRequest datasetTable) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        datasetTable.setSceneId(null);
        datasetTable.setType("excel");
        List<String> ids = dataSetTableService.saveExcelChangeName(datasetTable).stream().map(DatasetTable::getId).collect(Collectors.toList());
        return vAuthModelService.queryAuthModelByIds("dataset", ids);
    }

    @ApiOperation("二开 修改 数据集")
    @PostMapping("/updateDataset")
    public List<VAuthModelDTO> updateDataset(@RequestBody DatasourceDTO datasource) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        if (!"excel".equalsIgnoreCase(datasource.getType())){
            //判断是否被引用
            DatasetRef datasetRef = datasetRefMapper.selectByDatasetId(datasource.getTableId());
            if (datasetRef != null) {
                //判断被引用的次数 如果大于1 不允许删除  =1说明只有自己在用
                if (datasetRef.getRefCount() > 1) {
                    throw new RuntimeException("当前数据集有别的主题对象正在使用,不能进行编辑");
                }
            }
            //修改数据源
            DatasourceDTO datasourceChange = new DatasourceDTO();
            datasourceChange.setConfigurationEncryption(datasource.isConfigurationEncryption());
            datasourceChange.setName(datasource.getName());
            datasourceChange.setDesc(datasource.getDesc());
            datasourceChange.setConfiguration(datasource.getConfiguration());
            datasourceChange.setCreateTime(null);
            datasourceChange.setType(datasource.getType());
            datasourceChange.setUpdateTime(System.currentTimeMillis());
            if (StringUtils.isNotEmpty(datasource.getId())) {
                datasourceChange.setId(datasource.getId());
            }
            datasourceService.preCheckDs(datasourceChange);
            datasourceService.updateDatasource(datasource.getId(), datasourceChange);
            //修改数据集
            DataSetTableRequest datasetTable = new DataSetTableRequest();
            datasetTable.setName(datasource.getName());
            datasetTable.setGroupId(datasource.getGroupId());
            datasetTable.setDesc(datasource.getDesc());
            datasetTable.setDataSourceId(datasource.getId());
            datasetTable.setType(DatasetType.SQL.getType());
            datasetTable.setSyncType("sync_now");
            datasetTable.setMode(1);
            datasetTable.setTableId(datasource.getTableId());
            datasetTable.setSqlVariableDetails("[]");
            datasetTable.setId(datasource.getTableId());
            DataTableInfoDTO dto = new DataTableInfoDTO();
            String sql;
            switch (datasource.getType().toLowerCase()) {
                case "dm":
                    //获取模式
                    String configuration = new String(Base64.getDecoder().decode(datasource.getConfiguration()));
//                Datasource ds = datasourceService.get(datasource.getId());
//                String configuration = ds.getConfiguration();
//                String configuration = datasource.getConfiguration();
                    JdbcConfiguration jcf = new Gson().fromJson(configuration, JdbcConfiguration.class);
                    sql = "select * from \"" + jcf.getSchema() + "\"." + String.format(OracleConstants.FROM_VALUE, datasource.getTableName());
                    break;
                case "kingbase":
                    JdbcConfiguration jdbcConfiguration = new Gson().fromJson(new String(Base64.getDecoder().decode(datasource.getConfiguration())), JdbcConfiguration.class);
                    sql = "select * from SCHEMA.TABLE"
                        .replace("SCHEMA", jdbcConfiguration.getSchema())
                        .replace("TABLE", String.format(OracleConstants.FROM_VALUE, datasource.getTableName()));
                    break;
                case "es":
                    sql = "select * from \"" + datasource.getTableName() + "\"";
                    break;
                default:
                    sql = "select * from " + datasource.getTableName();
            }
//            if ("dm".equalsIgnoreCase(datasource.getType())){
//                //获取模式
//                String configuration = new String(Base64.getDecoder().decode(datasource.getConfiguration()));
////                Datasource ds = datasourceService.get(datasource.getId());
////                String configuration = ds.getConfiguration();
////                String configuration = datasource.getConfiguration();
//                JdbcConfiguration jcf = new Gson().fromJson(configuration, JdbcConfiguration.class);
//                sql = "select * from \"" + jcf.getSchema() + "\"." + String.format(OracleConstants.FROM_VALUE, datasource.getTableName());
//            } else if ("es".equalsIgnoreCase(datasource.getType())) {
//                sql = "select * from \"" + datasource.getTableName() + "\"";
//            } else {
//                sql = "select * from " + datasource.getTableName();
//            }
            dto.setSql(Base64.getEncoder().encodeToString(sql.getBytes()));
            dto.setBase64Encryption(true);
            //防止gson将等于号进行转码
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            String info = gson.toJson(dto);
            datasetTable.setInfo(info);
            Thread.sleep(400);
            DatasetTable save = dataSetTableService.save(datasetTable);
//            Datasource added = datasourceService.addDatasource(datasource);
            //删除原来的数据集和数据源
//            dataSetTableService.deleteDataset(datasource.getTableId());
            //添加数据源
            return vAuthModelService.queryAuthModelByIds("dataset", Collections.singletonList(save.getId()));
        } else {
            //excel只能编辑名称和描述
            return vAuthModelService.queryAuthModelByIds("dataset", Collections.singletonList(dataSetTableService.updateDataset(datasource.getName(),datasource.getDesc(),datasource.getTableId()).getId()));
        }
    }

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id"),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @ApiOperation("批量保存")
    @PostMapping("batchAdd")
    public List<VAuthModelDTO> batchAdd(@RequestBody List<DataSetTableRequest> datasetTable) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        List<String> ids = dataSetTableService.batchInsert(datasetTable).stream().map(DatasetTable::getId).collect(Collectors.toList());
        return vAuthModelService.queryAuthModelByIds("dataset", ids);
    }

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    @ApiOperation("更新")
    @PostMapping("update")
    public List<VAuthModelDTO> save(@RequestBody DataSetTableRequest datasetTable) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        if (datasetTable.getType().equalsIgnoreCase("excel")) {
            List<String> ids = dataSetTableService.saveExcel(datasetTable).stream().map(DatasetTable::getId).collect(Collectors.toList());
            return vAuthModelService.queryAuthModelByIds("dataset", ids);
        } else {
            return vAuthModelService.queryAuthModelByIds("dataset", Collections.singletonList(dataSetTableService.save(datasetTable).getId()));
        }
    }




    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE),
            @DePermission(type = DePermissionType.DATASET, value = "sceneId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    }, logical = Logical.AND)
    @ApiOperation("修改")
    @PostMapping("alter")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
            sourcetype = SysLogConstants.SOURCE_TYPE.DATASET,
            value = "id",
            positionIndex = 0,
            positionKey = "sceneId"
    )
    public void alter(@RequestBody DataSetTableRequest request) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        dataSetTableService.alter(request);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@ApiParam(name = "id", value = "数据集ID", required = true) @PathVariable String id) throws Exception {
        //检测该主体对象是否有别的主题模型 使用
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        int count = datamodelMapper.selectByObjectId(id);
        if (count>0){
            throw new Exception("该主体对象已被使用,无法删除,如需删除 请先删除引用的主题模型");
        }
        dataSetTableService.delete(id);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("二开 删除数据集")
    @PostMapping("deleteDataset/{id}")
    public void deleteDataset(@ApiParam(name = "id", value = "数据集ID", required = true) @PathVariable String id) throws Exception {
        // 检查数据集是否被其他对象引用
        DatasetRef datasetRef = datasetRefMapper.selectByDatasetId(id);
        if (datasetRef != null && datasetRef.getRefCount() > 1) {
            throw new RuntimeException("当前数据集有别的主题对象正在使用，不能删除");
        }

        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        dataSetTableService.deleteDataset(id);
    }


    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "sceneId")
    @ApiOperation("查询")
    @PostMapping("list")
    public List<DataSetTableDTO> list(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.list(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "sceneId")
    @ApiOperation("查询组")
    @PostMapping("listAndGroup")
    public List<DataSetTableDTO> listAndGroup(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.listAndGroup(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE)
    @ApiOperation("详细信息")
    @PostMapping("get/{id}")
    public DatasetTable get(@ApiParam(name = "id", value = "数据集ID", required = true) @PathVariable String id) {
        return dataSetTableService.get(id);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE)
    @ApiOperation("带权限查询")
    @PostMapping("getWithPermission/{id}")
    public DataSetTableDTO getWithPermission(@PathVariable String id) {
        return dataSetTableService.getWithPermission(id, null);
    }

    @ApiOperation("查询原始字段")
    @PostMapping("getFields")
    public List<TableField> getFields(@RequestBody DatasetTable datasetTable) throws Exception {
        return dataSetTableService.getFields(datasetTable);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "id")
    @ApiOperation("查询生成字段")
    @PostMapping("getFieldsFromDE")
    public Map<String, List<DatasetTableField>> getFieldsFromDE(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getFieldsFromDE(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "id")
    @ApiOperation("查询预览数据")
    @PostMapping("getPreviewData/{page}/{pageSize}")
    public Map<String, Object> getPreviewData(@RequestBody DataSetTableRequest dataSetTableRequest, @PathVariable Integer page, @PathVariable Integer pageSize) throws Exception {
        return dataSetTableService.getPreviewData(dataSetTableRequest, page, pageSize, null, null);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "id")
    @ApiOperation("查询预览数据")
    @PostMapping("/getCount")
    public ResultHolder getCount(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getCount(dataSetTableRequest, 1, 1, null, null);
    }

    @ApiOperation("db数据库表预览数据")
    @PostMapping("dbPreview")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public Map<String, Object> getDBPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getDBPreview(dataSetTableRequest);
    }

    @ApiOperation("根据sql查询预览数据")
    @PostMapping("sqlPreview")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public ResultHolder getSQLPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getSQLPreview(dataSetTableRequest, true);
    }

    @ApiOperation("根据sql查询预览数据")
    @PostMapping("sqlLog/{goPage}/{pageSize}")
    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, value = "id", level = ResourceAuthLevel.DATASET_LEVEL_USE),
            @DePermission(type = DePermissionType.DATASOURCE, value = "dataSourceId", level = ResourceAuthLevel.DATASOURCE_LEVEL_USE)
    }, logical = Logical.AND)
    public Pager<List<DatasetSqlLog>> getSQLLog(@RequestBody DataSetTableRequest dataSetTableRequest, @PathVariable int goPage, @PathVariable int pageSize) throws Exception {
        Page<DatasetSqlLog> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, dataSetTableService.getSQLLog(dataSetTableRequest));
    }

    @ApiOperation("预览自定义数据数据")
    @PostMapping("customPreview")
    public Map<String, Object> customPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getCustomPreview(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_USE, value = "tableId")
    @ApiOperation("查询增量配置")
    @PostMapping("incrementalConfig")
    public DatasetTableIncrementalConfig incrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        return dataSetTableService.incrementalConfig(datasetTableIncrementalConfig);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE, value = "tableId")
    @ApiOperation("保存增量配置")
    @PostMapping("save/incrementalConfig")
    public void saveIncrementalConfig(@RequestBody DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        dataSetTableService.saveIncrementalConfig(datasetTableIncrementalConfig);
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("数据集详细信息")
    @PostMapping("datasetDetail/{id}")
    public DataSetDetail datasetDetail(@PathVariable String id) {
        return dataSetTableService.getDatasetDetail(id);
    }

    @ApiOperation("excel上传")
    @PostMapping("excel/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "tableId", value = "数据表ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "editType", value = "编辑类型", required = true, dataType = "Integer")
    })
    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("tableId") String tableId, @RequestParam("editType") Integer editType) throws Exception {
        CacheUtils.remove(modelCacheEnum.modeltree.getValue(), AuthUtils.getUser().getUserId());
        return dataSetTableService.excelSaveAndParse(file, tableId, editType);
    }

    @ApiOperation("excel下载")
    @GetMapping("/excel/download/{datasetId}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String datasetId) {
        return dataSetTableService.downloadExcel(datasetId);
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("检测doris")
    @PostMapping("checkDorisTableIsExists/{id}")
    public Boolean checkDorisTableIsExists(@PathVariable String id) throws Exception {
        return dataSetTableService.checkEngineTableIsExists(id);
    }

    @ApiOperation("搜索")
    @PostMapping("search")
    public List<DataSetTableDTO> search(@RequestBody DataSetTableRequest dataSetTableRequest) {
        return dataSetTableService.search(dataSetTableRequest);
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("数据集同步表结构")
    @PostMapping("syncField/{id}")
    public DatasetTable syncDatasetTableField(@PathVariable String id) throws Exception {
        return dataSetTableService.syncDatasetTableField(id);
    }

    @DePermission(type = DePermissionType.DATASET, value = "id")
    @ApiOperation("关联数据集预览数据")
    @PostMapping("unionPreview")
    public Map<String, Object> unionPreview(@RequestBody DataSetTableRequest dataSetTableRequest) throws Exception {
        return dataSetTableService.getUnionPreview(dataSetTableRequest);
    }

    @ApiOperation("根据仪表板视图ID查询数据集变量")
    @PostMapping("/paramsWithIds/{type}")
    List<SqlVariableDetails> paramsWithIds(@PathVariable String type, @RequestBody List<String> viewIds) {
        return dataSetTableService.paramsWithIds(type, viewIds);
    }

    @ApiOperation("数据集的SQL变量")
    @PostMapping("/params/{id}/{type}")
    List<SqlVariableDetails> paramsWithIds(@PathVariable String type, @PathVariable String id) {
        return dataSetTableService.datasetParams(type, id);
    }

    @ApiOperation("根据数据集文件夹ID查询数据集名称")
    @PostMapping("/getDatasetNameFromGroup/{sceneId}")
    public List<String> getDatasetNameFromGroup(@PathVariable String sceneId) {
        return dataSetTableService.getDatasetNameFromGroup(sceneId);
    }

    @ApiOperation("数据集导出")
    @PostMapping("/exportDataset")
    @I18n
    public void exportDataset(@RequestBody DataSetExportRequest request, HttpServletResponse response) throws Exception {
        dataSetTableService.exportDataset(request, response);
    }
}
