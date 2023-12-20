package com.model;
import java.io.Serializable;
import lombok.Data;


/**
 * (DatasetRef)实体类
 *
 * @author makejava
 * @since 2023-12-20 10:50:27
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

}

