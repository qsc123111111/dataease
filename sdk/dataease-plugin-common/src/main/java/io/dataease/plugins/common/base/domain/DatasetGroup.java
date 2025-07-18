package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetGroup implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("模型描述")
    private String desc;
    @ApiModelProperty("主题数据名称")
    private String dataName;
    @ApiModelProperty("主题数据描述")
    private String dataDesc;
    @ApiModelProperty("父ID")
    private String pid;
    @ApiModelProperty("级别")
    private Integer level;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("文件夹类型")
    private Integer dirType;
    @ApiModelProperty("对象名称")
    private String objectName;
    @ApiModelProperty("引用的标签")
    private String labelRef;
    @ApiModelProperty("模型状态")
    private Integer status;
    @ApiModelProperty("模型上下线状态")
    private Integer upDown;

    private static final long serialVersionUID = 1L;
}