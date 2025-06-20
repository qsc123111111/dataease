package io.dataease.plugins.common.base.domain;
import java.io.Serializable;
import lombok.Data;


/**
 * (TableDataOrder)实体类
 */


@Data

public class TableDataOrder implements Serializable {
    private static final long serialVersionUID = 296885772035721138L;
    /**
     * 主键id
     */        
    private Integer id;

    /**
     * 数据集id
     */        
    private String datasetId;

    /**
     * 排序
     */        
    private String orderText;



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

    public String getOrderText() {
        return orderText;
    }

    public void setOrderText(String orderText) {
        this.orderText = orderText;
    }

}

