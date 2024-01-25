package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.TermTable;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (TermTable)表数据库访问层
 * @since 2024-01-18 20:09:24
 */
public interface TermTableMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TermTable selectByPrimaryKey(Integer id);

    /**
     * 统计总行数
     *
     * @param termTable 查询条件
     * @return 总行数
     */
    long count(TermTable termTable);

    /**
     * 新增数据
     *
     * @param termTable 实例对象
     * @return 影响行数
     */
    int insert(TermTable termTable);


    /**
     * 新增数据
     *
     * @param termTable 实例对象
     * @return 影响行数
     */
    int insertSelective(TermTable termTable);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TermTable> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<TermTable> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TermTable> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<TermTable> entities);

    /**
     * 修改数据
     *
     * @param termTable 实例对象
     * @return 影响行数
     */
    int update(TermTable termTable);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<String> findTerms(String id);

    TermTable findByModelAndExcel(@Param("modelId") String modelId,@Param("tableId") String tableId);

    int deleteByModelId(String modelId);

    int deleteByModelIds(List<String> ids);

    List<TermTable> selectByModel(String tableId);
}

