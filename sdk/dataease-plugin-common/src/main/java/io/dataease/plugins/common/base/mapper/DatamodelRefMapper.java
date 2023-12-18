package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatamodelRef;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DatamodelRef)表数据库访问层
 */
public interface DatamodelRefMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DatamodelRef selectByPrimaryKey(Integer id);

    /**
     * 统计总行数
     *
     * @param datamodelRef 查询条件
     * @return 总行数
     */
    long count(DatamodelRef datamodelRef);

    /**
     * 新增数据
     *
     * @param datamodelRef 实例对象
     * @return 影响行数
     */
    int insert(DatamodelRef datamodelRef);


    /**
     * 新增数据
     *
     * @param datamodelRef 实例对象
     * @return 影响行数
     */
    int insertSelective(DatamodelRef datamodelRef);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatamodelRef> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DatamodelRef> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatamodelRef> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DatamodelRef> entities);

    /**
     * 修改数据
     *
     * @param datamodelRef 实例对象
     * @return 影响行数
     */
    int update(DatamodelRef datamodelRef);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DatamodelRef> selectByModeId(String modelId);
}

