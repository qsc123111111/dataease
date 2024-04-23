package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.plugins.common.base.domain.DatasetGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetGroupMapper {
    long countByExample(DatasetGroupExample example);

    int deleteByExample(DatasetGroupExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetGroup record);

    int insertSelective(DatasetGroup record);

    List<DatasetGroup> selectByExample(DatasetGroupExample example);

    DatasetGroup selectByPrimaryKey(String id);
    DatasetGroup selectById(String id);

    int updateByExampleSelective(@Param("record") DatasetGroup record, @Param("example") DatasetGroupExample example);

    int updateByExample(@Param("record") DatasetGroup record, @Param("example") DatasetGroupExample example);

    int updateByPrimaryKeySelective(DatasetGroup record);

    int updateByPrimaryKey(DatasetGroup record);

    Integer getDirTypeById(String id);

    List<DatasetGroup> page(@Param("createBy") String createBy, @Param("id") String id, @Param("pageNo") Integer pageNo,
                            @Param("pageSize") Integer pageSize, @Param("keyWord") String keyWord,
                            @Param("order") String order,
                            @Param("time") Long time,@Param("plusOneTime") Long plusOneTime);

    Long count(@Param("createBy") String createBy, @Param("id") String id,@Param("pageNo") Integer pageNo,
               @Param("pageSize") Integer pageSize,
               @Param("keyWord") String keyWord,@Param("order") String order,
               @Param("time") Long time,@Param("plusOneTime") Long plusOneTime);

    List<DatasetGroup> PageData(@Param("limit")String limit,
                                @Param("pid")String pid,
                                @Param("keyword")String keyword,
                                @Param("sort")Integer sort,
                                @Param("create_time")Long create_time,
                                @Param("end_time")Long end_time);

    Long PageDataCount(@Param("pid")String pid,
                       @Param("keyword")String keyword,
                       @Param("create_time")Long create_time,
                       @Param("end_time")Long end_time);

    int updateModel(@Param("id")String id,
                    @Param("name")String name,
                    @Param("desc")String desc);

}