package io.dataease.controller.datamodel.request;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class DatamodelRequest {
    //模型名称
    String name;
    //模型描述
    String desc;
    //主体数据名称
    String dataName;
    //主体数据描述
    String dataDesc;
    //主题分类id
    String sceneId;
    //主题对象
    String tableId;
    //标签数组
    HashMap<String,List<DatasetTableField>> map;
}
