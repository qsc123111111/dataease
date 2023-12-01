package io.dataease.controller.datalabel.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DatalabelRequest {
    /**
     * 主键id
     */
    @ApiModelProperty("标签id")
    private Integer id;
    /**
     * 标签名称
     */
    @ApiModelProperty("标签名称")
    private String name;
    /**
     * 标签描述
     */
    @ApiModelProperty("标签描述")
    private String desc;
    /**
     * 标签表达式
     */
    @ApiModelProperty("标签表达式")
    private String exp;
    /**
     * 字段类型：1文本 2数值
     */
    @ApiModelProperty("字段类型：1文本 2数值")
    private Integer fieldType;

    /**
     * 数据类型：1维度 2指标
     */
    @ApiModelProperty("数据类型：1维度 2指标")
    private Integer dataType;

}
