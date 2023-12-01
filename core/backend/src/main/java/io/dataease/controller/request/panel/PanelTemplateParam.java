package io.dataease.controller.request.panel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PanelTemplateParam implements Serializable {
    @ApiModelProperty("ids")
    private List<String> ids;
    @ApiModelProperty("名称")
    private Integer showFlag;
}
