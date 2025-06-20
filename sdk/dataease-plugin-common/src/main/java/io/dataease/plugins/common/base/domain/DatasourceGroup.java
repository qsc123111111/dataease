package io.dataease.plugins.common.base.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.UUID;

public class DatasourceGroup implements Serializable {
    private static final long serialVersionUID = -48731604812070541L;
    /**
     * ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;
    /**
     * 名称
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    /**
     * 创建人ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createBy;
    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long createTime;
    /**
     * 创建时间
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long updateTime;

    /**
     * 分组描述
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String desc;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 如果init为true就是初始化id 时间  用于插入新数据
     * @param init
     */
    public DatasourceGroup(Boolean init) {
        if (init){
            this.id = UUID.randomUUID().toString();
            this.createTime = System.currentTimeMillis();
            this.updateTime = System.currentTimeMillis();
        }
    }

    public DatasourceGroup() {
    }

    public DatasourceGroup(String id, String name, String createBy, Long createTime, Long updateTime, String desc) {
        this.id = id;
        this.name = name;
        this.createBy = createBy;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.desc = desc;
    }
}

