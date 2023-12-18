package io.dataease.service.datamodel;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.enums.DatamodelEnum;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.controller.dataobject.enums.ObjectPeriodEnum;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.union.UnionDTO;
import io.dataease.dto.dataset.union.UnionItemDTO;
import io.dataease.dto.dataset.union.UnionParamDTO;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.util.RegexUtil;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.provider.ProviderFactory;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DatamodelService {
    @Resource
    private CommonThreadPool commonThreadPool;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetGroupService dataSetGroupService;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private DatamodelRefMapper datamodelRefMapper;
    @Resource
    private DatalabelRefMapper datalabelRefMapper;
    @Resource
    private DatamodelMapper datamodelMapper;
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;
    @Value("${retry.createUnion}")
    private String retry;

    //     @Transactional(rollbackFor = Exception.class)
    public ResultHolder save(DatamodelRequest datamodelRequest) throws Exception {
        if (datamodelRequest.getName() == null) {
            return ResultHolder.error("主题模型名称不能为空");
        }
        //---->>创建文件夹
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setPid(datamodelRequest.getSceneId());
        datasetGroup.setName(datamodelRequest.getName());
        datasetGroup.setDesc(datamodelRequest.getDesc());
        if (datamodelRequest.getDataName() == null) {
            datasetGroup.setDataName(datamodelRequest.getName());
        } else {
            datasetGroup.setDataName(datamodelRequest.getDataName());
        }
        datasetGroup.setDataDesc(datamodelRequest.getDataDesc());
        datasetGroup.setLevel(datamodelRequest.getLevel());
        datasetGroup.setType("group");
        datasetGroup.setDirType(DatamodelEnum.MODEL_DIR.getValue());
        DataSetGroupDTO result = dataSetGroupService.save(datasetGroup);
        //==========================创建新的数据集==========================
        //主题对象 都是多表关联  只需要类型是union的
        //以下基础为之关联两张表   查询此主题对象是由 哪两个数据集生成的  然后去重新生成两个数据集
        DatasetTable datasetTable = dataSetTableService.queryData(datamodelRequest.getTableId());
        String info = datasetTable.getInfo();
        DataTableInfoDTO dataTableInfoDTO = JSON.parseObject(info, DataTableInfoDTO.class);
        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // String firstDataSourceId = union.get(0).getCurrentDs().getDataSourceId();
        String firstDatasetId = union.get(0).getCurrentDs().getId();
        List<String> firstFields = union.get(0).getCurrentDsField();
        StringBuffer firstFieldsString = new StringBuffer();
        for (String field : firstFields) {
            firstFieldsString.append("\"").append(field).append("\"").append(",");
        }
        String firstFiledsName = "\"" + datasetTableFieldMapper.selectConcatNameByIds(firstFieldsString.toString().substring(0, firstFieldsString.length() - 1), firstDatasetId) + "\"";
        System.out.println("firstFiledsName = " + firstFiledsName);
        // String secondDataSourceId = union.get(0).getChildrenDs().get(0).getCurrentDs().getDataSourceId();
        String secendDatasetId = union.get(0).getChildrenDs().get(0).getCurrentDs().getId();
        List<String> secendFields = union.get(0).getChildrenDs().get(0).getCurrentDsField();
        //关联关系
        UnionParamDTO outUnionToParent = union.get(0).getUnionToParent();
        UnionParamDTO unionToParent = union.get(0).getChildrenDs().get(0).getUnionToParent();
        StringBuffer secendFieldsString = new StringBuffer();
        for (String field : secendFields) {
            secendFieldsString.append("\"").append(field).append("\"").append(",");
        }
        String secendFiledsName = "\"" + datasetTableFieldMapper.selectConcatNameByIds(secendFieldsString.toString().substring(0, secendFieldsString.length() - 1), secendDatasetId) + "\"";
        System.out.println("secendFiledsName = " + secendFiledsName);
        //获取这两个数据集创建的原始信息
        DatasetTable firstDatasetTable = dataSetTableService.get(firstDatasetId);
        DatasetTable secondDatasetTable = dataSetTableService.get(secendDatasetId);
        //获取创建的sql
        String firstSql = getSql(firstDatasetTable) + " where";
        String secondSql = getSql(secondDatasetTable) + " where";
        //dataSetTable添加period字段 1：对象主题 2:主题模型
        if (datasetTable.getType().equalsIgnoreCase("union")) {
            datasetTable.setInfo(null);
            datasetTable.setPeriod(ObjectPeriodEnum.MODEL.getValue());
            //更新添加标签
            HashMap<String, List<DatasetTableField>> map = datamodelRequest.getMap();
            //遍历map map为下方的所需要创建的数据表
            for (Map.Entry<String, List<DatasetTableField>> entry : map.entrySet()) {
                List<DatamodelRef> datamodelRefs = new ArrayList<>();
                String key = entry.getKey();
                List<DatasetTableField> value = entry.getValue();
                //拼接新的sql
                String firstSqlTemp = firstSql;
                String secendSqlTemp = secondSql;
                for (DatasetTableField field : value) {
                    String originName = field.getOriginName();
                    List<String> fieldIds = RegexUtil.extractBracketContents(originName);
                    String exp = RegexUtil.extractBracketsAndCommas(originName);
                    String fieldId = fieldIds.get(0);
                    //查询这个fieldId来源于哪个数据集
                    DatasetTableField tableField = dataSetTableFieldsService.selectTableByPrimaryKey(fieldId);

                    if (tableField.getTableId().equals(firstDatasetId)) {
                        firstSqlTemp = firstSqlTemp + " " + tableField.getOriginName() + exp + " and ";
                    } else if (tableField.getTableId().equals(secendDatasetId)) {
                        secendSqlTemp = secendSqlTemp + " " + tableField.getOriginName() + exp + " and ";
                    }
                }
                //去掉末尾最后4个字符
                if (firstSqlTemp.endsWith("and ")) {
                    firstSqlTemp = firstSqlTemp.substring(0, firstSqlTemp.length() - 4);
                }
                if (secendSqlTemp.endsWith("and ")) {
                    secendSqlTemp = secendSqlTemp.substring(0, secendSqlTemp.length() - 4);
                }
                if (firstSqlTemp.endsWith("where")) {
                    firstSqlTemp = firstSqlTemp.substring(0, firstSqlTemp.length() - 6);
                }
                DataSetTableRequest firstCreate = createDatasetTable(firstDatasetTable, firstSqlTemp, datamodelRefs, result.getId());
                firstCreate.setDataRaw(null);
                if (secendSqlTemp.endsWith("where")) {
                    secendSqlTemp = secendSqlTemp.substring(0, secendSqlTemp.length() - 6);
                }
                DataSetTableRequest secendCreate = createDatasetTable(secondDatasetTable, secendSqlTemp, datamodelRefs, result.getId());
                secendCreate.setDataRaw(null);
                //重新组建关联数据集
                UnionDTO unionDTO = new UnionDTO();
                //重构第一个数据集信息
                firstCreate.setType(datasourceMapper.selectByPrimaryKey(firstCreate.getDataSourceId()).getType());
                secendCreate.setType(datasourceMapper.selectByPrimaryKey(secendCreate.getDataSourceId()).getType());
                unionDTO.setCurrentDs(firstCreate);
                //重构第一个数据集的字段
                //查出旧的字段 替换成新的字段
                List<String> firstFieldsList = datasetTableFieldMapper.selectIdByName(firstFiledsName, firstCreate.getId());
                List<String> secendFieldsList = datasetTableFieldMapper.selectIdByName(secendFiledsName, secendCreate.getId());
                unionDTO.setCurrentDsField(firstFieldsList);
                UnionDTO childUnion = new UnionDTO();
                childUnion.setCurrentDs(secendCreate);
                childUnion.setCurrentDsField(secendFieldsList);
                //查询旧的关联关系 替换成新的关联关系
                UnionParamDTO unionToParentTemp = unionToParent;
                List<UnionItemDTO> unionFields = unionToParentTemp.getUnionFields();
                //目前针对一个关联关系
                for (UnionItemDTO unionItemDTO : unionFields) {
                    DatasetTableField parentField = unionItemDTO.getParentField();
                    setNewUnion(firstDatasetId, secendDatasetId, firstCreate, secendCreate, parentField);

                    DatasetTableField currentField = unionItemDTO.getCurrentField();
                    setNewUnion(firstDatasetId, secendDatasetId, firstCreate, secendCreate, currentField);
                    unionItemDTO.setParentField(parentField);
                    unionItemDTO.setCurrentField(currentField);
                }
                childUnion.setUnionToParent(unionToParentTemp);
                childUnion.setChildrenDs(new ArrayList<>());
                childUnion.setAllChildCount(0);
                ArrayList<UnionDTO> dtos = new ArrayList<>();
                dtos.add(childUnion);
                unionDTO.setChildrenDs(dtos);
                unionDTO.setUnionToParent(outUnionToParent);
                unionDTO.setAllChildCount(1);
                //创建主题对象
                DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
                dataSetTableRequest.setName(key);
                dataSetTableRequest.setSceneId(result.getId());
                dataSetTableRequest.setType("union");
                dataSetTableRequest.setMode(1);
                DataTableInfoDTO dto = new DataTableInfoDTO();
                List<UnionDTO> unionList = new ArrayList<>();
                unionList.add(unionDTO);
                dto.setUnion(unionList);
                dataSetTableRequest.setInfo(JSON.toJSONString(dto));
                dataSetTableRequest.setDataSourceId(datasetTable.getDataSourceId());
                String jsonString = JSON.toJSONString(dataSetTableRequest);
                System.out.println("jsonString = " + jsonString);
                //更新模型与表{数据集}的关系
                datamodelRefMapper.insertBatch(datamodelRefs);
                // TODO 查询新建的数据集的完成状态是否是已经同步到doris
                Integer total = Integer.valueOf(retry);
                while ( 0 < total ) {
                    System.out.println("count = " + total);
                    DatasetTable firstTable = dataSetTableService.get(firstCreate.getId());
                    DatasetTable secendTable = dataSetTableService.get(secendCreate.getId());
                    if (firstTable.getSyncStatus().equals("Completed") && secendTable.getSyncStatus().equals("Completed")) {
                        try {
                            String createModelJson = JSON.toJSONString(dataSetTableRequest);
                            System.out.println("createModelJson = " + createModelJson);
                            dataSetTableService.save(dataSetTableRequest);
                        } catch (Exception e) {
                            LogUtil.debug("创建主题模型异常 " + e.getMessage());
                            throw new RuntimeException("创建主题模型异常 " + e.getMessage());
                        }
                        //添加标签
                        for (DatasetTableField datasetTableField : value) {
                            datasetTableField.setTableId(dataSetTableRequest.getId());
                            // 替换originName字段id
                            String originName = datasetTableField.getOriginName();
                            List<String> filedIds = RegexUtil.extractBracketContents(originName);
                            String extractedContent = "";
                            if (filedIds.size() > 0) {
                                // 获取原来绑定的字段id 查找新生成的数据集的字段  更换成新的字段id
                                extractedContent = filedIds.get(0);
                                DatasetTableField extraField = dataSetTableFieldsService.get(extractedContent);
                                if (extraField != null) {
                                    DatasetTableField fieldNew = dataSetTableFieldsService.selectByNameAndTableId(extraField.getName(), extraField.getColumnIndex(), dataSetTableRequest.getId());
                                    if (fieldNew == null) {
                                        System.out.println("!111111111111");
                                    }
                                    originName = originName.replaceAll(extractedContent, fieldNew.getId());
                                    datasetTableField.setOriginName(originName);
                                    dataSetTableFieldsService.save(datasetTableField);
                                    //添加到字段引用表
                                    DatalabelRef datalabelRef = new DatalabelRef();
                                    datalabelRef.setDatamodelId(result.getId());
                                    datalabelRef.setDatalabelId(datasetTableField.getLabelId());
                                    datalabelRefMapper.insert(datalabelRef);
                                }
                            }
                        }
                        break;
                    } else {
                        if (total == 1){
                            throw new RuntimeException("创建主题模型异常,数据集同步超时");
                        }
                        Thread.sleep(1000);
                        total--;
                    }
                }
            }
        }
        //将信息写入datamodel
        Datamodel datamodel = new Datamodel();
        datamodel.setDatasetGroupId(result.getId());
        datamodel.setMapRaw(JSON.toJSONString(datamodelRequest.getMap()));
        datamodel.setDataobjectId(datamodelRequest.getTableId());
        datamodelMapper.insert(datamodel);
        return ResultHolder.successMsg("添加主题模型成功");
    }

    private void setNewUnion(String firstDatasetId, String secendDatasetId, DataSetTableRequest firstCreate, DataSetTableRequest secendCreate, DatasetTableField parentField) {
        DatasetTableField field = datasetTableFieldMapper.selectByPrimaryKeyHasFrom(parentField.getId());
        String tableId = field.getTableId();
        if ((tableId.equals(firstDatasetId))) {
            parentField.setId(datasetTableFieldMapper.selectIdByNameAndTableId(field.getName(), firstCreate.getId()));
        } else if (tableId.equals(secendDatasetId)) {
            parentField.setId(datasetTableFieldMapper.selectIdByNameAndTableId(field.getName(), secendCreate.getId()));
        }
    }

    private DataSetTableRequest createDatasetTable(DatasetTable firstDatasetTable, String firstSqlTemp, List<DatamodelRef> datamodelRefs, String id) throws Exception {
        //组装创建数据集信息
        DatasetTable createTable = new DatasetTable();
        BeanUtils.copyBean(createTable, firstDatasetTable);
        createTable.setId(null);
        createTable.setName(firstDatasetTable.getName() + UUID.randomUUID());
        String sqlInfo = createTable.getInfo();
        DataTableInfoDTO dt = new Gson().fromJson(sqlInfo, DataTableInfoDTO.class);
        dt.setSql(Base64.getEncoder().encodeToString(firstSqlTemp.getBytes()));
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        createTable.setInfo(gson.toJson(dt));
        //创建数据集
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, createTable);
        dataSetTableRequest.setSyncStatus(null);
        dataSetTableRequest.setSyncType("sync_now");
        dataSetTableRequest.setQrtzInstance(null);
        dataSetTableRequest.setLastUpdateTime(null);
        dataSetTableService.save(dataSetTableRequest);
        DatamodelRef datamodelRef = new DatamodelRef();
        datamodelRef.setModelId(id);
        datamodelRef.setTableId(dataSetTableRequest.getId());
        datamodelRefs.add(datamodelRef);
        return dataSetTableRequest;
    }

    private static String getSql(DatasetTable firstDataSetTableRequest) {
        String sqlInfo = firstDataSetTableRequest.getInfo();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        DataTableInfoDTO dto = gson.fromJson(sqlInfo, DataTableInfoDTO.class);
        String sql = dto.getSql();
        return new String(java.util.Base64.getDecoder().decode(sql));
    }


    public static void getDataSourceIdsNew(List<UnionDTO> union, List<String> dataSourceIds) {
        for (UnionDTO data : union) {
            if (data.getCurrentDs() != null && data.getCurrentDs().getDataSourceId() != null) {
                dataSourceIds.add(data.getCurrentDs().getDataSourceId());
            }
            if (data.getChildrenDs().size() == 0) {
                getDataSourceIdsNew(data.getChildrenDs(), dataSourceIds);
            }
        }
    }
}
