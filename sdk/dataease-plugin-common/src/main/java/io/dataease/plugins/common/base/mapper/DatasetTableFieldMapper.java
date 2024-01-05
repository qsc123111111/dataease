package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.DatasetTableFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableFieldMapper {
    long countByExample(DatasetTableFieldExample example);

    int deleteByExample(DatasetTableFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableField record);
    int insertNoAutoKey(DatasetTableField record);

    int insertSelective(DatasetTableField record);

    List<DatasetTableField> selectByExample(DatasetTableFieldExample example);

    DatasetTableField selectByPrimaryKey(String id);
    DatasetTableField selectByPrimaryKeyHasFrom(String id);

    int updateByExampleSelective(@Param("record") DatasetTableField record, @Param("example") DatasetTableFieldExample example);

    int updateByExample(@Param("record") DatasetTableField record, @Param("example") DatasetTableFieldExample example);

    int updateByPrimaryKeySelective(DatasetTableField record);

    int updateByPrimaryKey(DatasetTableField record);
    int updateFrom(DatasetTableField record);

    DatasetTableField selectByNameAndTableId(@Param("name") String name,@Param("columnIndex") Integer columnIndex,@Param("tableId") String tableId);
    DatasetTableField selectByTableIdAndDataeaseName(@Param("tableId") String tableId,@Param("dataeaseName") String dataeaseName);

    String selectConcatNameByIds(@Param("ids") String ids,@Param("tableId") String tableId);

    List<String> selectIdByName(@Param("names") String names,@Param("tableId") String tableId);

    String selectIdByNameAndTableId(@Param("name") String name,@Param("tableId") String tableId);
}