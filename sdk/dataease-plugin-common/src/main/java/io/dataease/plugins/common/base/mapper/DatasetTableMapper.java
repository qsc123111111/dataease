package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableMapper {
    long countByExample(DatasetTableExample example);

    int deleteByExample(DatasetTableExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTable record);

    int insertSelective(DatasetTable record);

    List<DatasetTable> selectByExample(DatasetTableExample example);

    DatasetTable selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTable record, @Param("example") DatasetTableExample example);

    int updateByExample(@Param("record") DatasetTable record, @Param("example") DatasetTableExample example);

    int updateByPrimaryKeySelective(DatasetTable record);

    int updateByPrimaryKey(DatasetTable record);

    List<DatasetTable> page(@Param("pageNo") Integer pageNo,@Param("pageSize") Integer pageSize,@Param("keyWord") String keyWord);

    List<DatasetTable> select(DatasetTable datasetTable);
    List<DatasetTable> selectHasType(DatasetTable datasetTable);

    DatasetTable queryDataRaw(@Param("tableId") String tableId,@Param("createBy") String createBy);

    List<DatasetTable> queryObjectAll(String keyWord);

    DatasetTable queryData(@Param("tableId") String tableId,@Param("createBy") String createBy);

}