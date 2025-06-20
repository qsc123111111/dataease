package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasourceGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 数据源文件夹(DatasourceGroup)表数据库访问层
 */
public interface DatasourceGroupMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DatasourceGroup queryById(String id);

    /**
     * 查询指定行数据
     *
     * @param datasourceGroup 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<DatasourceGroup> queryAllByLimit(DatasourceGroup datasourceGroup, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param datasourceGroup 查询条件
     * @return 总行数
     */
    long count(DatasourceGroup datasourceGroup);

    /**
     * 新增数据
     *
     * @param datasourceGroup 实例对象
     * @return 影响行数
     */
    int insert(DatasourceGroup datasourceGroup);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatasourceGroup> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DatasourceGroup> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatasourceGroup> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DatasourceGroup> entities);

    /**
     * 修改数据
     *
     * @param datasourceGroup 实例对象
     * @return 影响行数
     */
    int update(DatasourceGroup datasourceGroup);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @param createBy 用户
     * @return 影响行数
     */
    int deleteById(@Param("id") String id,@Param("createBy") String createBy);

    int queryByName(@Param("name") String name,@Param("createBy") String userId);

    /**
     * 更新数据 条件为id和userid
     * @param datasourceGroup
     * @return
     */
    int updateById(DatasourceGroup datasourceGroup);

    List<DatasourceGroup> listAll(@Param("createBy") String createBy);
}

