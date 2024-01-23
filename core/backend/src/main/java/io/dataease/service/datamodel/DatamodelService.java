package io.dataease.service.datamodel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.enums.DatamodelEnum;
import io.dataease.controller.datamodel.enums.DatamodelStatusEnum;
import io.dataease.controller.datamodel.enums.DatamodelUpDownEnum;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.controller.dataobject.enums.ObjectPeriodEnum;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.datamodel.DatamodelChartDTO;
import io.dataease.dto.datamodel.DatamodelLabelRefDTO;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.union.UnionDTO;
import io.dataease.dto.dataset.union.UnionItemDTO;
import io.dataease.dto.dataset.union.UnionParamDTO;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.plugins.common.dto.chart.ChartCustomFilterItemDTO;
import io.dataease.plugins.common.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.plugins.common.entity.FilterItem;
import io.dataease.plugins.common.util.RegexUtil;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
@Slf4j
@Service
public class DatamodelService {
    @Resource
    private TermTableMapper termTableMapper;
    @Resource
    private DatamodelMapper datamodelMapper;
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
    private DatalabelMapper datalabelMapper;
    @Resource
    private DatalabelGroupMapper datalabelGroupMapper;
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
        datasetGroup.setStatus(DatamodelStatusEnum.DOING.getValue());
        datasetGroup.setCreateTime(datamodelRequest.getCreateTime());
        DataSetGroupDTO result = dataSetGroupService.save(datasetGroup);
//        createModel(datamodelRequest, result);
        //开启线程异步执行
        Thread t = new Thread(()->{
            try {
                createModel(datamodelRequest, result);
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
                System.err.println(e.getMessage());
                log.error(e.getMessage());
                DatasetGroup errorDatasetGroup = new DatasetGroup();
                errorDatasetGroup.setId(result.getId());
                errorDatasetGroup.setStatus(DatamodelStatusEnum.ERROR.getValue());
                dataSetGroupService.update(errorDatasetGroup);
            }
        });
        t.start();
        return ResultHolder.successMsg("添加主题模型成功");
    }

    private void createModel(DatamodelRequest datamodelRequest, DataSetGroupDTO result) throws Exception {
        String mapRaw = JSON.toJSONString(datamodelRequest.getMap());
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
            firstFieldsString.append("'").append(field).append("'").append(",");
        }
        String firstFiledsName = "'" + getFieldsName(firstFieldsString, firstDatasetId) + "'";
        // String secondDataSourceId = union.get(0).getChildrenDs().get(0).getCurrentDs().getDataSourceId();
        String secendDatasetId = union.get(0).getChildrenDs().get(0).getCurrentDs().getId();
        List<String> secendFields = union.get(0).getChildrenDs().get(0).getCurrentDsField();
        //关联关系
        UnionParamDTO outUnionToParent = union.get(0).getUnionToParent();
        UnionParamDTO unionToParent = union.get(0).getChildrenDs().get(0).getUnionToParent();
        StringBuffer secendFieldsString = new StringBuffer();
        for (String field : secendFields) {
            secendFieldsString.append("'").append(field).append("'").append(",");
        }
        String secendFiledsName = "'" + getFieldsName(secendFieldsString, secendDatasetId) + "'";
        //获取这两个数据集创建的原始信息
        DatasetTable firstDatasetTable = dataSetTableService.get(firstDatasetId);
        DatasetTable secondDatasetTable = dataSetTableService.get(secendDatasetId);
        //获取创建的sql
        String firstSql = getSql(firstDatasetTable);
        String secondSql = getSql(secondDatasetTable);
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
                    String fieldId = fieldIds.get(0);
                    //查询这个fieldId来源于哪个数据集
                    DatasetTableField tableField = dataSetTableFieldsService.selectTableByPrimaryKey(fieldId);
                    String exp = RegexUtil.extractBracketsAndCommasReplace(originName,fieldId,tableField.getOriginName());
                    // if (exp.contains("(")){
                    //     exp = exp + ")";
                    // }
                    if ( firstSqlTemp!=null && tableField.getTableId().equals(firstDatasetId)) {
                        firstSqlTemp = firstSqlTemp + " " + exp + " and ";
                    } else if ( secendSqlTemp!=null && tableField.getTableId().equals(secendDatasetId)) {
                        secendSqlTemp = secendSqlTemp + " " + exp + " and ";
                    }
                }
                //去掉末尾最后4个字符
                if (firstSqlTemp!=null && firstSqlTemp.endsWith("and ")) {
                    firstSqlTemp = firstSqlTemp.substring(0, firstSqlTemp.length() - 4);
                }
                if (secendSqlTemp!=null && secendSqlTemp.endsWith("and ")) {
                    secendSqlTemp = secendSqlTemp.substring(0, secendSqlTemp.length() - 4);
                }
                if (firstSqlTemp!=null && firstSqlTemp.endsWith("where")) {
                    firstSqlTemp = firstSqlTemp.substring(0, firstSqlTemp.length() - 6);
                }
                DataSetTableRequest firstCreate = createDatasetTable(firstDatasetTable, firstSqlTemp, datamodelRefs, result.getId());
                firstCreate.setDataRaw(null);
                if (secendSqlTemp!=null && secendSqlTemp.endsWith("where")) {
                    secendSqlTemp = secendSqlTemp.substring(0, secendSqlTemp.length() - 6);
                }
                DataSetTableRequest secendCreate = createDatasetTable(secondDatasetTable, secendSqlTemp, datamodelRefs, result.getId());
                secendCreate.setDataRaw(null);
                //重新组建关联数据集
                UnionDTO unionDTO = new UnionDTO();
                //重构第一个数据集信息
//                firstCreate.setType(datasourceMapper.selectByPrimaryKey(firstCreate.getDataSourceId()).getType());
//                secendCreate.setType(datasourceMapper.selectByPrimaryKey(secendCreate.getDataSourceId()).getType());
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
                UnionParamDTO unionToParentTemp = new UnionParamDTO();
                unionToParentTemp.setUnionType(unionToParent.getUnionType());
                //将原始属性copy新对象
                ArrayList<UnionItemDTO> unionItemDTOSTemp = new ArrayList<>();
                List<UnionItemDTO> fieldsRaw = unionToParent.getUnionFields();
                for (UnionItemDTO unionItemDTO : fieldsRaw) {
                    DatasetTableField currentField = unionItemDTO.getCurrentField();
                    DatasetTableField currentFieldTemp = new DatasetTableField();
                    BeanUtils.copyBean(currentFieldTemp,currentField);
                    DatasetTableField parentField = unionItemDTO.getParentField();
                    DatasetTableField parentFieldTemp = new DatasetTableField();
                    BeanUtils.copyBean(parentFieldTemp,parentField);
                    UnionItemDTO unionItemDTOTemp = new UnionItemDTO();
                    unionItemDTOTemp.setCurrentField(currentFieldTemp);
                    unionItemDTOTemp.setParentField(parentFieldTemp);
                    unionItemDTOSTemp.add(unionItemDTOTemp);
                }
                unionToParentTemp.setUnionFields(unionItemDTOSTemp);
                List<UnionItemDTO> unionFields = unionToParentTemp.getUnionFields();
                //目前针对一个关联关系
                for (UnionItemDTO unionItemDTO : unionFields) {
                    DatasetTableField parentFieldTemp = unionItemDTO.getParentField();
                    DatasetTableField parentField = new DatasetTableField();
                    BeanUtils.copyBean(parentField,parentFieldTemp);
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
                //更新模型与表{数据集}的关系
                datamodelRefMapper.insertBatch(datamodelRefs);
                Integer total = Integer.valueOf(retry);
                while (true) {
                    log.debug("count---->" + total);
                    DatasetTable firstTable = dataSetTableService.get(firstCreate.getId());
                    DatasetTable secendTable = dataSetTableService.get(secendCreate.getId());
                    if ("error".equalsIgnoreCase(firstTable.getSyncStatus()) || "error".equalsIgnoreCase(secendTable.getSyncStatus())){
                        throw new RuntimeException("创建主题模型异常,数据集同步异常");
                    }
                    if (firstTable.getSyncStatus().equals("Completed") && secendTable.getSyncStatus().equals("Completed")) {
                        try {
                            dataSetTableService.save(dataSetTableRequest);
                        } catch (Exception e) {
                            LogUtil.debug("创建主题模型异常 " + e.getMessage());
                            throw new RuntimeException("创建主题模型异常 " + e.getMessage());
                        }
                        //添加标签
                        for (DatasetTableField datasetTableField : value) {
                            //判断是sql数据集 还是excel数据集
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
                                    originName = originName.replaceAll(extractedContent, fieldNew.getId());
                                    datasetTableField.setOriginName(originName);
                                    dataSetTableFieldsService.save(datasetTableField);
                                    //查询from来源
                                    DatasetTableField fromFiled = dataSetTableFieldsService.get(extraField.getFromField());
                                    //校验类型 是sql还是excel
                                    String tableType = dataSetTableService.getType(fromFiled.getTableId());
                                    if ("excel".equalsIgnoreCase(tableType)) {
                                        //截取逗号之前的文本
                                        originName = originName.substring(0, originName.indexOf(","));
                                        ArrayList<FilterItem> term = RegexUtil.getTerm(originName);
                                        List<ChartCustomFilterItemDTO> filter = new ArrayList<>();
                                        ChartCustomFilterItemDTO chartCustomFilterItemDTO = new ChartCustomFilterItemDTO();
                                        for (FilterItem filterItem : term) {
                                            chartCustomFilterItemDTO = new ChartCustomFilterItemDTO();
                                            BeanUtils.copyBean(chartCustomFilterItemDTO, filterItem);
                                            chartCustomFilterItemDTO.setFieldId(fromFiled.getDataeaseName());
                                            filter.add(chartCustomFilterItemDTO);
                                        }
                                        //查询此表是否有 有的话更新 没有的话插入
                                        TermTable termTableCheck = termTableMapper.findByModelAndExcel(dataSetTableRequest.getId(),fromFiled.getTableId());
                                        if (termTableCheck == null){
                                            //TODO 0118 Excel过滤
                                            List<ChartFieldCustomFilterDTO> filterTest = new ArrayList<>();
                                            ChartFieldCustomFilterDTO chartFieldCustomFilterDTO = new ChartFieldCustomFilterDTO();
                                            DatasetTableField field = datasetTableFieldMapper.selectByPrimaryKey(extractedContent);
                                            chartFieldCustomFilterDTO.setField(field);

                                            chartFieldCustomFilterDTO.setFilter(filter);
                                            filterTest.add(chartFieldCustomFilterDTO);
                                            //储存信息
                                            TermTable termTable = new TermTable();
                                            termTable.setModelId(dataSetTableRequest.getId());
                                            termTable.setExcelId(fromFiled.getTableId());
                                            termTable.setTerms(JSON.toJSONString(filterTest));
                                            termTableMapper.insert(termTable);
                                        } else {
                                            String terms = termTableCheck.getTerms();
                                            List<ChartFieldCustomFilterDTO> list = JSON.parseObject(terms, new TypeReference<List<ChartFieldCustomFilterDTO>>(){});
                                            boolean flag = true;
                                            for (int i = 0; i < list.size(); i++) {
                                                ChartFieldCustomFilterDTO chartFieldCustomFilterDTO = list.get(i);
                                                if (chartFieldCustomFilterDTO.getField().getId().equalsIgnoreCase(extractedContent)){
                                                    flag = false;
                                                    List<ChartCustomFilterItemDTO> lastFilters = chartFieldCustomFilterDTO.getFilter();
                                                    List<ChartCustomFilterItemDTO> mergedList = new ArrayList<>(lastFilters);
                                                    mergedList.addAll(filter);
                                                    chartFieldCustomFilterDTO.setFilter(mergedList);
                                                    termTableCheck.setTerms(JSON.toJSONString(list));
                                                    termTableMapper.update(termTableCheck);
                                                    break;
                                                }
                                            }
                                            if (flag){
                                                //说明是新字段
                                                ChartFieldCustomFilterDTO chartFieldCustomFilterDTO = new ChartFieldCustomFilterDTO();
                                                DatasetTableField field = datasetTableFieldMapper.selectByPrimaryKey(extractedContent);
                                                chartFieldCustomFilterDTO.setField(field);
                                                chartFieldCustomFilterDTO.setFilter(filter);
                                                list.add(chartFieldCustomFilterDTO);
                                                termTableCheck.setTerms(JSON.toJSONString(list));
                                                termTableMapper.update(termTableCheck);
                                            }

                                        }
                                    }
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
                        Thread.sleep(2000);
                    }
                }
            }
        }
        //将信息写入datamodel
        Datamodel datamodel = new Datamodel();
        datamodel.setDatasetGroupId(result.getId());
        datamodel.setMapRaw(mapRaw);
        datamodel.setDataobjectId(datamodelRequest.getTableId());
        datamodelMapper.insert(datamodel);
        //修改模型为完成状态
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setId(result.getId());
        datasetGroup.setStatus(DatamodelStatusEnum.Done.getValue());
        dataSetGroupService.update(datasetGroup);
    }

    private String getFieldsName(StringBuffer fieldsString,String datasetId) {
        String result = datasetTableFieldMapper.selectConcatNameByIds(fieldsString.toString().substring(0, fieldsString.length() - 1), datasetId);
        result = result.replaceAll("\"","'");
        return result;
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
        //创建数据集
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        if (firstSqlTemp==null){
            BeanUtils.copyBean(dataSetTableRequest, firstDatasetTable);
            dataSetTableRequest.setSyncStatus(null);
            dataSetTableRequest.setSyncType("sync_now");
            dataSetTableRequest.setQrtzInstance(null);
            dataSetTableRequest.setLastUpdateTime(null);
            dataSetTableRequest.setGroupId(null);
            return dataSetTableRequest;
        } else {
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
            BeanUtils.copyBean(dataSetTableRequest, createTable);
            dataSetTableRequest.setSyncStatus(null);
            dataSetTableRequest.setSyncType("sync_now");
            dataSetTableRequest.setQrtzInstance(null);
            dataSetTableRequest.setLastUpdateTime(null);
            dataSetTableRequest.setGroupId(null);
            dataSetTableService.save(dataSetTableRequest);
            DatamodelRef datamodelRef = new DatamodelRef();
            datamodelRef.setModelId(id);
            datamodelRef.setTableId(dataSetTableRequest.getId());
            datamodelRefs.add(datamodelRef);
            return dataSetTableRequest;
        }
    }

    private static String getSql(DatasetTable firstDataSetTableRequest) {
        if(!"excel".equalsIgnoreCase(firstDataSetTableRequest.getType())){
            String sqlInfo = firstDataSetTableRequest.getInfo();
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            DataTableInfoDTO dto = gson.fromJson(sqlInfo, DataTableInfoDTO.class);
            String sql = dto.getSql();
            return new String(Base64.getDecoder().decode(sql))+ " where";
        } else {
            return null;
        }
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

    public DatamodelRequest getInfo(String id) {
        //查询模型、模型描述  数据名、数据描述
        DatasetGroup datasetGroup = dataSetGroupService.selectById(id);
        DatamodelRequest result = new DatamodelRequest();
        BeanUtils.copyBean(result,datasetGroup);
        result.setSceneId(datasetGroup.getPid());
        //查询与标签关联信息
        Datamodel datamodel = datamodelMapper.selectByModelId(id);
        String mapRaw = datamodel.getMapRaw();
        HashMap map = JSON.parseObject(mapRaw, HashMap.class);
        result.setMap(map);
        //查询关联的主体对象
        result.setTableId(datamodel.getDataobjectId());
        return result;
    }

    public DatamodelChartDTO getModelChart(String id) {
        TreeSet<String> labels = new TreeSet<>();
        HashMap<String,List<DatamodelLabelRefDTO>> datas = new HashMap<>();
        //查询原始信息
        Datamodel datamodel = datamodelMapper.selectByModelId(id);
        String mapRaw = datamodel.getMapRaw();
        HashMap<String,List<JSONObject>> map = JSON.parseObject(mapRaw, HashMap.class);
        for (String s : map.keySet()) {
            List<DatamodelLabelRefDTO> data = new ArrayList<>();
            List<JSONObject> datasetTableFields = map.get(s);
            //获取标签对应关系
            for (JSONObject datasetTableFieldJo : datasetTableFields) {
                DatasetTableField datasetTableField = datasetTableFieldJo.toJavaObject(DatasetTableField.class);
                labels.add(datasetTableField.getName());
                DatamodelLabelRefDTO refDTO = new DatamodelLabelRefDTO();
                //原始标签
                List<String> ids = RegexUtil.extractBracketContents(datasetTableField.getOriginName());
                if (ids.size()>0){
                    DatasetTableField field = datasetTableFieldMapper.selectByPrimaryKey(ids.get(0));
                    refDTO.setDatasetLabel(field.getName());
                }
                //引用标签
                Integer labelId = datasetTableField.getLabelId();
                Datalabel datalabel = datalabelMapper.queryById(labelId);
                refDTO.setLabelRef(datalabel.getName());
                data.add(refDTO);
            }
            datas.put(s,data);
        }
        DatamodelChartDTO datamodelChartDTO = new DatamodelChartDTO();
        datamodelChartDTO.setRules(datas);
        datamodelChartDTO.setLabels(labels);
        return datamodelChartDTO;
    }

    public Boolean upDown(String id, Integer tag) {
        switch (tag){
            case 1:
                //上架 1
                return changeUpDown(id, DatamodelUpDownEnum.UP.getValue());
            case 0:
                //下架 0
                return changeUpDown(id, DatamodelUpDownEnum.DOWN.getValue());
            default:
                throw new RuntimeException("参数异常");
        }
    }
    private Boolean changeUpDown(String id, Integer upDown){
        DatasetGroup datasetGroup = new DatasetGroup();
        datasetGroup.setId(id);
        datasetGroup.setUpDown(upDown);
        int update = dataSetGroupService.update(datasetGroup);
        return update>0?true:false;
    }
}
