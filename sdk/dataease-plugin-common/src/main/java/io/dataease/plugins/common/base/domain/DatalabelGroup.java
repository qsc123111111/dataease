package io.dataease.plugins.common.base.domain;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * (DatalabelGroup)实体类
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatalabelGroup implements Serializable {
    private static final long serialVersionUID = -10126704885187057L;
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




    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }
    public DatalabelGroup(Boolean first) {
        if (first){
            this.createTime = System.currentTimeMillis();
            this.updateTime = System.currentTimeMillis();
        }
    }

}

