package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.Datamodel;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * (Datamodel)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-06 18:20:42
 */
public interface DatamodelMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    Datamodel queryById(Integer id);

    /**
     * 查询指定行数据
     *
     * @param datamodel 查询条件
     * @param pageable         分页对象
     * @return 对象列表
     */
    List<Datamodel> queryAllByLimit(Datamodel datamodel, @Param("pageable") Pageable pageable);

    /**
     * 统计总行数
     *
     * @param datamodel 查询条件
     * @return 总行数
     */
    long count(Datamodel datamodel);

    /**
     * 新增数据
     *
     * @param datamodel 实例对象
     * @return 影响行数
     */
    int insert(Datamodel datamodel);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<Datamodel> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Datamodel> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<Datamodel> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<Datamodel> entities);

    /**
     * 修改数据
     *
     * @param datamodel 实例对象
     * @return 影响行数
     */
    int update(Datamodel datamodel);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    Datamodel selectByModelId(String id);

    Integer deleteByModelId(String id);
}

