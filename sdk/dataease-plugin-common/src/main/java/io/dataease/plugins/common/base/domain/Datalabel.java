package io.dataease.plugins.common.base.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * (Datalabel)实体类
 *
 * @author makejava
 * @since 2023-12-01 16:58:45
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Datalabel implements Serializable {
    private static final long serialVersionUID = 122046527831935108L;
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签描述
     */
    private String desc;
    /**
     * 标签表达式
     */
    private String exp;
    
    private Long createTime;
    
    private Long updateTime;
    /**
     * 创建人ID
     */
    private String createBy;
    /**
     * 涉及到的模型数量
     */
    private Integer involve;
    /**
     * 逻辑删除0正常 1删除
     */
    private Boolean isDelete;

    /**
     * 字段类型：1文本 2数值
     */
    private Integer fieldType;

    /**
     * 数据类型：1维度 2指标
     */
    private Integer dataType;

    public Integer getFieldType() {
        return fieldType;
    }

    public void setFieldType(Integer fieldType) {
        this.fieldType = fieldType;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
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

    public Integer getInvolve() {
        return involve;
    }

    public void setInvolve(Integer involve) {
        this.involve = involve;
    }

    public Boolean getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Boolean isDelete) {
        this.isDelete = isDelete;
    }

    public Datalabel(Boolean first) {
        if (first){
            this.createTime = System.currentTimeMillis();
            this.updateTime = System.currentTimeMillis();
        }
    }
}

