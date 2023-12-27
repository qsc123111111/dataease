package io.dataease.controller.datamodel.request;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@Data
public class DatamodelRequest implements Serializable {
    //模型id
    private String id;
    //模型层级
    private Integer level;
    //模型名称
    private String name;
    //模型描述
    private String desc;
    //主题数据名称
    private String dataName;
    //主题数据描述
    private String dataDesc;
    //主题分类id
    private String sceneId;
    //主题对象
    private String tableId;
    //创建时间
    private Long createTime;
    //标签数组
    private HashMap<String,List<DatasetTableField>> map;
}
