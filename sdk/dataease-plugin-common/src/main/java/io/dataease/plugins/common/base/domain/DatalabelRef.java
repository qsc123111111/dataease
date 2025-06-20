package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

/**
 * (DatalabelRef)实体类
 *
 * @author makejava
 * @since 2023-12-06 17:56:04
 */
public class DatalabelRef implements Serializable {
    private static final long serialVersionUID = -91773096266467426L;
    
    private Integer id;
    /**
     * 主题模型id
     */
    private String datamodelId;
    /**
     * 字段id
     */
    private String datasetFieldId;
    /**
     * 自定义标签id
     */
    private Integer datalabelId;
    /**
     * 数据集id（模型对象id）
     */
    private String datasetName;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatamodelId() {
        return datamodelId;
    }

    public void setDatamodelId(String datamodelId) {
        this.datamodelId = datamodelId;
    }

    public String getDatasetFieldId() {
        return datasetFieldId;
    }

    public void setDatasetFieldId(String datasetFieldId) {
        this.datasetFieldId = datasetFieldId;
    }

    public Integer getDatalabelId() {
        return datalabelId;
    }

    public void setDatalabelId(Integer datalabelId) {
        this.datalabelId = datalabelId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

}

