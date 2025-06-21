package io.dataease.plugins.common.base.domain;
import java.io.Serializable;
import lombok.Data;


/**
 * (DatasetRef)实体类
 */

@Data
public class DatasetRef implements Serializable {
    private static final long serialVersionUID = 547240033595110753L;
            
    private Integer id;

    /**
     * 数据集id
     */        
    private String datasetId;

    /**
     * 数据源id
     */        
    private String datasourceId;

    /**
     * 引用次数
     */
    private Integer refCount;

    public Integer getRefCount() {
        return refCount;
    }

    public void setRefCount(Integer refCount) {
        this.refCount = refCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public DatasetRef(String datasetId, String datasourceId) {
        this.datasetId = datasetId;
        this.datasourceId = datasourceId;
    }
}

