package io.dataease.plugins.common.base.domain;

import java.io.Serializable;

/**
 * (Datamodel)实体类
 *
 * @author makejava
 * @since 2023-12-06 18:20:43
 */
public class Datamodel implements Serializable {
    private static final long serialVersionUID = -49223429940732693L;
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 主题模型id
     */
    private String datasetGroupId;
    /**
     * map原始字段
     */
    private String mapRaw;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatasetGroupId() {
        return datasetGroupId;
    }

    public void setDatasetGroupId(String datasetGroupId) {
        this.datasetGroupId = datasetGroupId;
    }

    public String getMapRaw() {
        return mapRaw;
    }

    public void setMapRaw(String mapRaw) {
        this.mapRaw = mapRaw;
    }

}

