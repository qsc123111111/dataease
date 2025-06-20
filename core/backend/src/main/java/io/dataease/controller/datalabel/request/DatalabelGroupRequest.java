package io.dataease.controller.datalabel.request;

import io.dataease.plugins.common.base.domain.BackData;
import io.dataease.plugins.common.base.domain.DatalabelGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatalabelGroupRequest {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 标签分组名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String desc;


    private Long createTime;


    private Long updateTime;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 逻辑删除0正常 1删除
     */
    private Boolean isDelete;

    /**
     * 是否能用
     */
    private Boolean isEnable;

    /**
     * 标签
     */
    List<DatalabelRequest> labels;

    /**
     * 回传信息
     */
    List<BackData> expression;
}
