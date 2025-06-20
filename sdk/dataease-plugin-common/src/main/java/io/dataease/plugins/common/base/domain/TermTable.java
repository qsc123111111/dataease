package io.dataease.plugins.common.base.domain;
import java.io.Serializable;
import lombok.Data;


/**
 * (TermTable)实体类
 * @since 2024-01-18 20:09:26
 */


@Data

public class TermTable implements Serializable {
    private static final long serialVersionUID = 640277102804436952L;
    /**
     * 主键id
     */        
    private Integer id;

    /**
     * 模型id
     */        
    private String modelId;

    /**
     * excel数据集id
     */        
    private String excelId;

    /**
     * 条件json
     */        
    private String terms;



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

    public String getExcelId() {
        return excelId;
    }

    public void setExcelId(String excelId) {
        this.excelId = excelId;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

}

