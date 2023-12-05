package io.dataease.controller.datalabel.request;

import io.dataease.plugins.common.base.domain.BackData;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @ApiModelProperty("前端回显字段")
    private List<BackData> expression;

}
