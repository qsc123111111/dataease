package io.dataease.dto;

import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-05-18
 * Description:
 */
@Data
public class DatasourceDTO extends Datasource {

    @ApiModelProperty("权限")
    private String privileges;
    private List<ApiDefinition> apiConfiguration;
    private String apiConfigurationStr;
    private String typeDesc;
    private DatasourceCalculationMode calculationMode;
    private boolean isConfigurationEncryption = false;
    private String tableId;
    private String fileName;

    @Override
    public String toString() {
        return "DatasourceDTO{" +
                "privileges='" + privileges + '\'' +
                ", apiConfiguration=" + apiConfiguration +
                ", apiConfigurationStr='" + apiConfigurationStr + '\'' +
                ", typeDesc='" + typeDesc + '\'' +
                ", calculationMode=" + calculationMode +
                ", isConfigurationEncryption=" + isConfigurationEncryption +
                ", tableId='" + tableId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", desc='" + getDesc() + '\'' +
                ", type='" + getType() + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", createBy='" + getCreateBy() + '\'' +
                ", status='" + getStatus() + '\'' +
                ", configuration='" + getConfiguration() + '\'' +
                ", groupId='" + getGroupId() + '\'' +
                ", tableName='" + getTableName() + '\'' +
                '}';
    }


}
