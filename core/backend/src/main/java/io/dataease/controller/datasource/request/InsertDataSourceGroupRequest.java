package io.dataease.controller.datasource.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsertDataSourceGroupRequest {
    /**
     * 分组id
     */
    private String id;
    /**
     * 名称
     */
    private String name;

    /**
     * 分组描述
     */
    private String desc;
}
