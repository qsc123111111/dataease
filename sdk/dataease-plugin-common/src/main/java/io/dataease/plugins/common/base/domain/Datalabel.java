package io.dataease.plugins.common.base.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * (Datalabel)实体类
 *
 * @author makejava
 * @since 2023-12-01 16:58:45
 */
@Data
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
    @ApiModelProperty("前端回显字段")
    private String expression;
    @ApiModelProperty("标签分组")
    private String groupId;
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

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

