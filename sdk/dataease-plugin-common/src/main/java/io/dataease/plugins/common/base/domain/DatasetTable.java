package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DatasetTable implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String desp;
    @ApiModelProperty("数据集的周期，1主题对象（只进行了关联） 2主题模型（进行了关联并进行了新建字段）")
    private Integer period;
    @ApiModelProperty("场景ID")
    private String sceneId;
    @ApiModelProperty("数据源ID")
    private String dataSourceId;
    @ApiModelProperty("类型")
    private String type;
    @ApiModelProperty("模式")
    private Integer mode;
    @ApiModelProperty("信息")
    private String info;
    @ApiModelProperty("创建者")
    private String createBy;
    @ApiModelProperty("创建时间")
    private Long createTime;
    @ApiModelProperty("定时任务实例")
    private String qrtzInstance;
    @ApiModelProperty("同步状态")
    private String syncStatus;
    @ApiModelProperty("上次更新时间")
    private Long lastUpdateTime;

    private String sqlVariableDetails;

    private static final long serialVersionUID = 1L;
}