package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.TableDataOrder;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (TableDataOrder)表数据库访问层
 */
public interface TableDataOrderMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TableDataOrder selectByPrimaryKey(Integer id);
    TableDataOrder selectByDatasetId(String datasetId);

    /**
     * 统计总行数
     *
     * @param tableDataOrder 查询条件
     * @return 总行数
     */
    long count(TableDataOrder tableDataOrder);

    /**
     * 新增数据
     *
     * @param tableDataOrder 实例对象
     * @return 影响行数
     */
    int insert(TableDataOrder tableDataOrder);


    /**
     * 新增数据
     *
     * @param tableDataOrder 实例对象
     * @return 影响行数
     */
    int insertSelective(TableDataOrder tableDataOrder);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<TableDataOrder> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<TableDataOrder> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<TableDataOrder> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<TableDataOrder> entities);

    /**
     * 修改数据
     *
     * @param tableDataOrder 实例对象
     * @return 影响行数
     */
    int update(TableDataOrder tableDataOrder);
    int updateByDatasetId(TableDataOrder tableDataOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);
    int deleteByDatasetId(String id);

}

