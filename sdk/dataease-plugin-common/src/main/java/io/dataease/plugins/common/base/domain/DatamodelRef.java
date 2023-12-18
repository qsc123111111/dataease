package io.dataease.plugins.common.base.domain;
import java.io.Serializable;
import lombok.Data;

@Data
public class DatamodelRef implements Serializable {
    private static final long serialVersionUID = -23965594983305201L;
    /**
     * 主键id
     */        
    private Integer id;

    /**
     * 主题模型id
     */        
    private String modelId;

    /**
     * 主题模型生成的表
     */        
    private String tableId;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

}

