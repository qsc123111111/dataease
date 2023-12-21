package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatalabelGroup;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * (DatalabelGroup)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-12 11:20:22
 */
public interface DatalabelGroupMapper {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DatalabelGroup selectByPrimaryKey(Integer id);

    /**
     * 统计总行数
     *
     * @param datalabelGroup 查询条件
     * @return 总行数
     */
    long count(DatalabelGroup datalabelGroup);

    /**
     * 新增数据
     *
     * @param datalabelGroup 实例对象
     * @return 影响行数
     */
    int insert(DatalabelGroup datalabelGroup);


    /**
     * 新增数据
     *
     * @param datalabelGroup 实例对象
     * @return 影响行数
     */
    int insertSelective(DatalabelGroup datalabelGroup);

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatalabelGroup> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<DatalabelGroup> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<DatalabelGroup> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<DatalabelGroup> entities);

    /**
     * 修改数据
     *
     * @param datalabelGroup 实例对象
     * @return 影响行数
     */
    int update(DatalabelGroup datalabelGroup);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    DatalabelGroup queryByName(@Param("name") String name,@Param("userId") String userId);
    DatalabelGroup queryByNameLimit(@Param("name") String name,@Param("userId") String userId,@Param("id") Integer id);

    long simpleCount(@Param("keyWord") String keyWord, @Param("userId") Long userId,@Param("time") Long time,@Param("plusOneTime") Long plusOneTime);

    List<DatalabelGroup> queryPageAllByLimit(@Param("pageNo") Integer pageNo, @Param("pageSize") Integer pageSize, @Param("keyWord") String keyWord, @Param("userId") Long userId, @Param("time") Long time, @Param("plusOneTime") Long plusOneTime,@Param("sort") String sort);

    List<DatalabelGroup> queryIdAndNameAll(@Param("keyWord") String keyWord,@Param("userId") String userId);

    Integer deleteBatch(@Param("idsText") String idsText,@Param("createBy") String createBy);

    DatalabelGroup queryById(@Param("id") Integer id,@Param("createBy") String createBy);
}

