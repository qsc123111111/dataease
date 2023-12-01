package io.dataease.controller.request.panel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PanelTemplatePageRequest  implements Serializable {
    @ApiModelProperty("页码")
    private Integer page;
    @ApiModelProperty("分页量")
    private Integer pageSize;
    @ApiModelProperty("名称")
    private String name;
}
