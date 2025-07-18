package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelTemplate;
import io.dataease.plugins.common.base.domain.PanelTemplateExample;
import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelTemplateMapper {
    long countByExample(PanelTemplateExample example);

    int deleteByExample(PanelTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelTemplateWithBLOBs record);

    int insertSelective(PanelTemplateWithBLOBs record);

    List<PanelTemplateWithBLOBs> selectByExampleWithBLOBs(PanelTemplateExample example);

    List<PanelTemplate> selectByExample(PanelTemplateExample example);

    PanelTemplateWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelTemplateWithBLOBs record, @Param("example") PanelTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelTemplateWithBLOBs record, @Param("example") PanelTemplateExample example);

    int updateByExample(@Param("record") PanelTemplate record, @Param("example") PanelTemplateExample example);

    int updateByPrimaryKeySelective(PanelTemplateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PanelTemplateWithBLOBs record);

    int updateByPrimaryKey(PanelTemplate record);


    List<PanelTemplate> pageList(@Param("limit")String limit,@Param("name")String name);
    int pageCount(@Param("name")String name);
    int updateStatus(@Param("id")String id,@Param("showFlag")int showFlag);

    List<PanelTemplate> batchListById(@Param("ids") List<String> ids);

    String findIdByType(@Param("templateType")String templateType);

    Integer updateNameAndFlagById(@Param("id") String id,@Param("name") String name,@Param("templateType") String templateType);
}