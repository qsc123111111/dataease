package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetRef;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DatasetRef)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-20 10:50:26
 */
public interface DatasetRefMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DatasetRef selectByPrimaryKey(Integer id);

    /**
     * 统计总行数
     *
     * @param datasetRef 查询条件
     * @return 总行数
     */
    long count(DatasetRef datasetRef);

    /**
     * 新增数据
     *
     * @param datasetRef 实例对象
     * @return 影响行数
     */
    int insert(DatasetRef datasetRef);


    /**
     * 新增数据
     *
     * @param datasetRef 实例对象
     * @return 影响行数
     */
    int insertSelective(DatasetRef datasetRef);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatasetRef> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DatasetRef> entities);
    int updateCountsBySourceIds(@Param("sourceIds") List<String> sourceIds);
    int reduceCountsBySourceIds(@Param("sourceIds") List<String> sourceIds);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatasetRef> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DatasetRef> entities);

    /**
     * 修改数据
     *
     * @param datasetRef 实例对象
     * @return 影响行数
     */
    int update(DatasetRef datasetRef);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    // List<DatasetRef> selectByDatasetId(String datasetId);
    DatasetRef selectByDatasetId(String datasetId);
}

