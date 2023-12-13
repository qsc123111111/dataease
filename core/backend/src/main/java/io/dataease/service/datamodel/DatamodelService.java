package io.dataease.service.datamodel;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datamodel.enums.DatamodelEnum;
import io.dataease.controller.datamodel.request.DatamodelRequest;
import io.dataease.controller.dataobject.enums.ObjectPeriodEnum;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.dto.authModel.VAuthModelDTO;
import io.dataease.dto.dataset.DataSetGroupDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.union.UnionDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DatalabelRefMapper;
import io.dataease.plugins.common.base.mapper.DatamodelMapper;
import io.dataease.plugins.common.base.mapper.DatasetTableMapper;
import io.dataease.plugins.common.util.RegexUtil;
import io.dataease.service.authModel.VAuthModelService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DatamodelService {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetGroupService dataSetGroupService;

    @Resource
    private DataSetTableService dataSetTableService;

    @Resource
    private DatamodelMapper datamodelMapper;
    @Resource
    private DatalabelRefMapper datalabelRefMapper;

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
        //通过tableId查询原始信息
        DatasetTable datasetTable = dataSetTableService.queryDataRaw(datamodelRequest.getTableId());
        //获取创建时候的关联信息
        String dataRaw = datasetTable.getDataRaw();
        DataSetTableRequest dataSetTableRequest = JSON.parseObject(dataRaw, DataSetTableRequest.class);
        //创建新的数据集 将id置为null
        dataSetTableRequest.setId(null);
        //dataSetTable添加period字段 1：对象主题 2:主题模型
        if (dataSetTableRequest.getType().equalsIgnoreCase("union")) {
            dataSetTableRequest.setPeriod(ObjectPeriodEnum.MODEL.getValue());
            //更新添加标签
            HashMap<String, List<DatasetTableField>> map = datamodelRequest.getMap();
            //遍历map map为下方的所需要创建的数据表
            for (Map.Entry<String, List<DatasetTableField>> entry : map.entrySet()) {
                //将数据集的key作为新的数据集的名称
                String name = entry.getKey();
                List<DatasetTableField> datasetTableFields = entry.getValue();
                dataSetTableRequest.setName(name);
                dataSetTableRequest.setSceneId(result.getId());
                DatasetTable save = dataSetTableService.save(dataSetTableRequest);
                //拼装sql语句
                //添加标签
                for (DatasetTableField datasetTableField : datasetTableFields) {
//                    datasetTableField.
                }
                //获取该主题对象的使用的数据集
                String info = dataSetTableRequest.getInfo();
                DataTableInfoDTO dataTableInfoDTO = JSON.parseObject(info, DataTableInfoDTO.class);
                List<UnionDTO> union = dataTableInfoDTO.getUnion();
                //获取左边的数据集的原始创建的数据
                union.forEach(unionDTO -> {
                    DatasetTable currentDs = unionDTO.getCurrentDs();
                    //数据集id
                    String id = currentDs.getId();
                    //查询此数据集的原始创建参数
                    DatasetTable createData = dataSetTableService.getDataRaw(id);
                    String dataDataRaw = createData.getDataRaw();
                    DataSetTableRequest oldDataSetTableRequest = JSON.parseObject(dataDataRaw, DataSetTableRequest.class);
                    //重命名 加上随机后缀
                    oldDataSetTableRequest.setSceneId("temp");
                    oldDataSetTableRequest.setName(currentDs.getName() + UUID.randomUUID());
                    //重组sql筛选语句
                    String sqlInfo = oldDataSetTableRequest.getInfo();
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    DataTableInfoDTO dto = gson.fromJson(sqlInfo, DataTableInfoDTO.class);
                    String sql = dto.getSql();
                    // TODO 修改sql
                    dto.setSql(sql);
                    String newInfo = gson.toJson(dto);
//                    createData.setGroupId();
                    oldDataSetTableRequest.setInfo(newInfo);
                    //新建数据集
                    DatasetTable saveNew = null;
                    try {
                        saveNew = dataSetTableService.save(oldDataSetTableRequest);
                    } catch (Exception e) {
                        DataEaseException.throwException(e);
                    }
                    //获取新建数据集的字段
                    List<DatasetTableField> fieldsByTableId = dataSetTableFieldsService.getFieldsByTableId(saveNew.getId());
                    //来自数据源
                    String dataSourceId = currentDs.getDataSourceId();
                    //获取原始数据集创建的语句
                });





                //添加标签
                for (DatasetTableField datasetTableField : datasetTableFields) {
                    datasetTableField.setTableId(save.getId());
                    //替换originName字段id
                    String originName = datasetTableField.getOriginName();
                    List<String> filedIds = RegexUtil.extractBracketContents(originName);
                    String extractedContent="";
                    if (filedIds.size()>0){
                        //获取原来绑定的字段id 查找新生成的数据集的字段  更换成新的字段id
                        extractedContent = filedIds.get(0);
                        DatasetTableField extraField = dataSetTableFieldsService.get(extractedContent);
                        if (extraField != null) {
                            DatasetTableField fieldNew = dataSetTableFieldsService.selectByNameAndTableId(extraField.getName(),extraField.getColumnIndex(), save.getId());
                            originName = originName.replaceAll(extractedContent,fieldNew.getId());
                            datasetTableField.setOriginName(originName);
                            //更新数据集的标签(添加新的自定义标签)
                            dataSetTableFieldsService.save(datasetTableField);
                            //将标签引用写入 datalabel_ref
                            DatalabelRef datalabelRef = new DatalabelRef();
                            datalabelRef.setDatamodelId(result.getId());
                            datalabelRef.setDatasetFieldId(fieldNew.getId());
                            datalabelRef.setDatalabelId(datasetTableField.getLabelId());
                            datalabelRefMapper.insert(datalabelRef);
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
        } else {
            return ResultHolder.error("类型不是union");
        }
    }
}
