package io.dataease.plugins.common.request;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class PluginParam {
    @ApiModelProperty("ids")
    private List<Long> ids;
    @ApiModelProperty("id")
    private Long id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("上架状态")
    private Integer showFlag;
    @ApiModelProperty("类型")
    private Integer pluginType;

}
